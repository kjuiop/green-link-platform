package com.greenlink.core.application.point.service;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.point.dto.CompanyPointResult;
import com.greenlink.core.application.point.dto.PointTransactionResult;
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
import com.greenlink.core.domain.point.model.PointTransaction;
import com.greenlink.core.domain.point.port.CompanyPointRepository;
import com.greenlink.core.domain.point.port.PointTransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PointService implements
        ChargePointUseCase,
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

    @Override
    public List<CompanyPointResult> getAdminPointList() {
        List<Company> companies = companyRepository.findAllByTypeIn(
                List.of(CompanyType.ORDERING, CompanyType.VENDOR, CompanyType.BOTH));
        List<Long> companyIds = companies.stream().map(Company::getId).toList();
        Map<Long, Long> balanceMap = companyPointRepository.findAllByCompanyIdIn(companyIds).stream()
                .collect(Collectors.toMap(CompanyPoint::getCompanyId, CompanyPoint::getBalance));
        return companies.stream()
                .map(c -> new CompanyPointResult(
                        c.getId(), c.getCompanyName(), c.getType().name(),
                        balanceMap.getOrDefault(c.getId(), 0L)))
                .toList();
    }

    @Override
    public CompanyPointResult getMyPoint(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalStateException("Company not found: " + companyId));
        long balance = companyPointRepository.findByCompanyId(companyId)
                .map(CompanyPoint::getBalance).orElse(0L);
        return new CompanyPointResult(company.getId(), company.getCompanyName(), company.getType().name(), balance);
    }

    @Override
    public PageResult<PointTransactionResult> getTransactions(Long companyId, Pageable pageable) {
        Page<PointTransaction> page = pointTransactionRepository.findByCompanyId(companyId, pageable);
        Map<Long, String> loginIdMap = adminRepository.findAllByCompanyId(companyId).stream()
                .collect(Collectors.toMap(Admin::getId, Admin::getLoginId, (a, b) -> a));
        return PageResult.of(page.map(t -> new PointTransactionResult(
                t.getId(),
                t.getType().name(),
                t.getAmount(),
                t.getBalanceAfter(),
                t.getMemo(),
                t.getMemberId() != null ? loginIdMap.get(t.getMemberId()) : null,
                t.getCreatedAt()
        )));
    }
}
