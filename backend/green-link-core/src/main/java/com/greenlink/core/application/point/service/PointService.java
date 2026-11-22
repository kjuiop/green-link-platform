package com.greenlink.core.application.point.service;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.point.dto.CompanyPointResult;
import com.greenlink.core.application.point.dto.PointTransactionResult;
import com.greenlink.core.application.point.usecase.ApplyOrderPointUseCase;
import com.greenlink.core.application.point.usecase.ChargePointUseCase;
import com.greenlink.core.application.point.usecase.GetAdminPointListUseCase;
import com.greenlink.core.application.point.usecase.GetMyPointUseCase;
import com.greenlink.core.application.point.usecase.GetPointTransactionsUseCase;
import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.point.model.CompanyPoint;
import com.greenlink.core.domain.point.model.OrderPointState;
import com.greenlink.core.domain.point.model.PointTransaction;
import com.greenlink.core.domain.point.port.CompanyPointRepository;
import com.greenlink.core.domain.point.port.PointTransactionRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.point.PointErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PointService implements
        ChargePointUseCase,
        ApplyOrderPointUseCase,
        GetAdminPointListUseCase,
        GetMyPointUseCase,
        GetPointTransactionsUseCase {

    private final CompanyPointRepository companyPointRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final CompanyRepository companyRepository;
    private final AdminRepository adminRepository;

    public PointService(CompanyPointRepository companyPointRepository,
                        PointTransactionRepository pointTransactionRepository,
                        CompanyRepository companyRepository,
                        AdminRepository adminRepository) {
        this.companyPointRepository = companyPointRepository;
        this.pointTransactionRepository = pointTransactionRepository;
        this.companyRepository = companyRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    @Transactional
    public void charge(Long companyId, long amount, String memo, Long adminId) {
        if (memo != null && memo.length() > 255) {
            throw new IllegalArgumentException("메모는 255자 이내로 입력해주세요.");
        }
        if (companyRepository.findById(companyId).isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 회사입니다: " + companyId);
        }
        CompanyPoint point = companyPointRepository.findByCompanyIdForUpdate(companyId)
                .orElseGet(() -> CompanyPoint.init(companyId));
        point.charge(amount);
        companyPointRepository.save(point);
        pointTransactionRepository.save(
                PointTransaction.charge(companyId, adminId, amount, point.getBalance(), memo));
    }

    private Long resolveActor(Long companyId, Long memberId) {
        if (memberId == null) return null;
        return adminRepository.findById(memberId)
                .filter(a -> a.getCompanyId() == null || a.getCompanyId().equals(companyId))
                .map(Admin::getId)
                .orElse(null);
    }

    @Override
    @Transactional
    public void holdForNewOrder(Long companyId, Long orderId, String orderNumber, long totalAmount, Long memberId) {
        Long actorId = resolveActor(companyId, memberId);
        CompanyPoint point = companyPointRepository.findByCompanyIdForUpdate(companyId)
                .orElseGet(() -> CompanyPoint.init(companyId));
        if (!point.canHold(totalAmount)) {
            throw new BusinessException(PointErrorCode.INSUFFICIENT_POINT);
        }
        point.hold(totalAmount);
        companyPointRepository.save(point);
        pointTransactionRepository.save(PointTransaction.use(
                companyId, actorId, orderId, totalAmount, point.getBalance(), "발주 예치 " + orderNumber));
    }

    @Override
    @Transactional
    public void syncOrder(Long companyId, Long orderId, String orderNumber,
                          OrderPointState state, long totalAmount, Long memberId) {
        if (!pointTransactionRepository.existsOrderTransaction(orderId)) return;
        if (pointTransactionRepository.existsSettle(orderId)) return;
        Long actorId = resolveActor(companyId, memberId);

        CompanyPoint point = companyPointRepository.findByCompanyIdForUpdate(companyId)
                .orElseGet(() -> CompanyPoint.init(companyId));
        long held = pointTransactionRepository.sumNetHeldByOrderId(orderId);

        if (state == OrderPointState.DELIVERED) {
            point.settle(held);
            companyPointRepository.save(point);
            pointTransactionRepository.save(PointTransaction.settle(
                    companyId, actorId, orderId, held, point.getBalance(), "발주 정산 " + orderNumber));
            return;
        }

        long desired = state == OrderPointState.ACTIVE ? totalAmount : 0L;
        long delta = desired - held;
        if (delta > 0) {
            if (!point.canHold(delta)) {
                throw new BusinessException(PointErrorCode.INSUFFICIENT_POINT);
            }
            point.hold(delta);
            companyPointRepository.save(point);
            pointTransactionRepository.save(PointTransaction.use(
                    companyId, actorId, orderId, delta, point.getBalance(), "발주 금액 변경 추가 예치 " + orderNumber));
        } else if (delta < 0) {
            point.release(-delta);
            companyPointRepository.save(point);
            pointTransactionRepository.save(PointTransaction.refund(
                    companyId, actorId, orderId, -delta, point.getBalance(), "발주 환불 " + orderNumber));
        }
    }

    @Override
    @Transactional
    public void restoreSettledOrder(Long companyId, Long orderId, String orderNumber, Long memberId) {
        if (pointTransactionRepository.existsRestore(orderId)) return;
        long settled = pointTransactionRepository.sumSettleByOrderId(orderId);
        if (settled <= 0) return;
        Long actorId = resolveActor(companyId, memberId);
        CompanyPoint point = companyPointRepository.findByCompanyIdForUpdate(companyId)
                .orElseGet(() -> CompanyPoint.init(companyId));
        point.restore(settled);
        companyPointRepository.save(point);
        pointTransactionRepository.save(PointTransaction.restore(
                companyId, actorId, orderId, settled, point.getBalance(), "배송완료 환불 " + orderNumber));
    }

    @Override
    public boolean isSettled(Long orderId) {
        return pointTransactionRepository.existsSettle(orderId);
    }

    @Override
    public List<CompanyPointResult> getAdminPointList() {
        List<Company> companies = companyRepository.findAllByTypeIn(
                List.of(CompanyType.ORDERING, CompanyType.VENDOR, CompanyType.BOTH));
        List<Long> companyIds = companies.stream().map(Company::getId).toList();
        Map<Long, CompanyPoint> pointMap = companyPointRepository.findAllByCompanyIdIn(companyIds).stream()
                .collect(Collectors.toMap(CompanyPoint::getCompanyId, p -> p));
        return companies.stream()
                .map(c -> {
                    CompanyPoint p = pointMap.get(c.getId());
                    return new CompanyPointResult(
                            c.getId(), c.getCompanyName(), c.getType().name(),
                            p != null ? p.getBalance() : 0L,
                            p != null ? p.getDepositBalance() : 0L);
                })
                .toList();
    }

    @Override
    public CompanyPointResult getMyPoint(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalStateException("Company not found: " + companyId));
        CompanyPoint point = companyPointRepository.findByCompanyId(companyId).orElse(null);
        return new CompanyPointResult(company.getId(), company.getCompanyName(), company.getType().name(),
                point != null ? point.getBalance() : 0L,
                point != null ? point.getDepositBalance() : 0L);
    }

    @Override
    public PageResult<PointTransactionResult> getTransactions(Long companyId, Pageable pageable) {
        Page<PointTransaction> page = pointTransactionRepository.findByCompanyId(companyId, pageable);
        Set<Long> memberIds = page.getContent().stream()
                .map(PointTransaction::getMemberId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> loginIdMap = adminRepository.findAllByIdIn(memberIds).stream()
                .collect(Collectors.toMap(Admin::getId, Admin::getLoginId, (a, b) -> a));
        return PageResult.of(page.map(t -> new PointTransactionResult(
                t.getId(),
                t.getType().name(),
                t.getAmount(),
                t.getBalanceAfter(),
                t.getMemo(),
                t.getMemberId() != null ? loginIdMap.get(t.getMemberId()) : null,
                t.getOrderId(),
                t.getCreatedAt()
        )));
    }
}
