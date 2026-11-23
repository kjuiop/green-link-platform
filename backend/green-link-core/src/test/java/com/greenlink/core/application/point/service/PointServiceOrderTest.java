package com.greenlink.core.application.point.service;

import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.point.model.CompanyPoint;
import com.greenlink.core.domain.point.model.OrderPointState;
import com.greenlink.core.domain.point.model.PointTransaction;
import com.greenlink.core.domain.point.model.PointTransactionType;
import com.greenlink.core.domain.point.port.CompanyPointRepository;
import com.greenlink.core.domain.point.port.PointTransactionRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.point.PointErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PointServiceOrderTest {

    private static final Long COMPANY = 1L;
    private static final Long ORDER = 100L;

    private FakePointRepo pointRepo;
    private FakeTxRepo txRepo;
    private PointService service;

    @BeforeEach
    void setUp() {
        pointRepo = new FakePointRepo();
        txRepo = new FakeTxRepo();
        service = new PointService(pointRepo, txRepo,
                Mockito.mock(CompanyRepository.class), Mockito.mock(AdminRepository.class));
        pointRepo.point = CompanyPoint.of(1L, COMPANY, 100_000L, 0L);
    }

    private void sync(OrderPointState state, long total) {
        service.syncOrder(COMPANY, ORDER, "DF-1", state, total, 9L);
    }

    @Test
    void holdForNewOrder_movesTotalToDepositAndRecordsUse() {
        service.holdForNewOrder(COMPANY, ORDER, "DF-1", 40_000L, 9L);

        assertThat(pointRepo.point.getBalance()).isEqualTo(60_000L);
        assertThat(pointRepo.point.getDepositBalance()).isEqualTo(40_000L);
        assertThat(txRepo.saved).extracting(PointTransaction::getType).containsExactly(PointTransactionType.USE);
    }

    @Test
    void holdForNewOrder_whenInsufficient_throwsPointError() {
        assertThatThrownBy(() -> service.holdForNewOrder(COMPANY, ORDER, "DF-1", 100_001L, 9L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(PointErrorCode.INSUFFICIENT_POINT));
        assertThat(pointRepo.point.getBalance()).isEqualTo(100_000L);
        assertThat(txRepo.saved).isEmpty();
    }

    @Test
    void holdForNewOrder_zeroAmount_stillRecordsUseRow() {
        service.holdForNewOrder(COMPANY, ORDER, "DF-1", 0L, 9L);

        assertThat(txRepo.saved).hasSize(1);
    }

    @Test
    void syncOrder_amountIncrease_holdsDifference() {
        service.holdForNewOrder(COMPANY, ORDER, "DF-1", 40_000L, 9L);

        sync(OrderPointState.ACTIVE, 55_000L);

        assertThat(pointRepo.point.getBalance()).isEqualTo(45_000L);
        assertThat(pointRepo.point.getDepositBalance()).isEqualTo(55_000L);
    }

    @Test
    void syncOrder_amountIncrease_whenInsufficient_throws() {
        service.holdForNewOrder(COMPANY, ORDER, "DF-1", 40_000L, 9L);

        assertThatThrownBy(() -> sync(OrderPointState.ACTIVE, 200_000L))
                .isInstanceOf(BusinessException.class);
        assertThat(pointRepo.point.getDepositBalance()).isEqualTo(40_000L);
    }

    @Test
    void syncOrder_amountDecrease_refundsDifference() {
        service.holdForNewOrder(COMPANY, ORDER, "DF-1", 40_000L, 9L);

        sync(OrderPointState.ACTIVE, 30_000L);

        assertThat(pointRepo.point.getBalance()).isEqualTo(70_000L);
        assertThat(pointRepo.point.getDepositBalance()).isEqualTo(30_000L);
    }

    @Test
    void syncOrder_closed_refundsAll_andIsIdempotent() {
        service.holdForNewOrder(COMPANY, ORDER, "DF-1", 40_000L, 9L);

        sync(OrderPointState.CLOSED, 40_000L);
        sync(OrderPointState.CLOSED, 40_000L);

        assertThat(pointRepo.point.getBalance()).isEqualTo(100_000L);
        assertThat(pointRepo.point.getDepositBalance()).isZero();
        assertThat(txRepo.saved).extracting(PointTransaction::getType)
                .containsExactly(PointTransactionType.USE, PointTransactionType.REFUND);
    }

    @Test
    void syncOrder_reactivateAfterClosed_holdsAgain() {
        service.holdForNewOrder(COMPANY, ORDER, "DF-1", 40_000L, 9L);
        sync(OrderPointState.CLOSED, 40_000L);

        sync(OrderPointState.ACTIVE, 40_000L);

        assertThat(pointRepo.point.getBalance()).isEqualTo(60_000L);
        assertThat(pointRepo.point.getDepositBalance()).isEqualTo(40_000L);
    }

    @Test
    void syncOrder_delivered_settlesDeposit_andLocksOrder() {
        service.holdForNewOrder(COMPANY, ORDER, "DF-1", 40_000L, 9L);

        sync(OrderPointState.DELIVERED, 40_000L);

        assertThat(pointRepo.point.getBalance()).isEqualTo(60_000L);
        assertThat(pointRepo.point.getDepositBalance()).isZero();
        assertThat(service.isSettled(ORDER)).isTrue();

        sync(OrderPointState.CLOSED, 40_000L);
        assertThat(pointRepo.point.getBalance()).isEqualTo(60_000L);
        assertThat(pointRepo.point.getDepositBalance()).isZero();
    }

    @Test
    void restoreSettledOrder_returnsSettledAmountToBalance() {
        service.holdForNewOrder(COMPANY, ORDER, "DF-1", 40_000L, 9L);
        sync(OrderPointState.DELIVERED, 40_000L);

        service.restoreSettledOrder(COMPANY, ORDER, "DF-1", 9L);

        assertThat(pointRepo.point.getBalance()).isEqualTo(100_000L);
        assertThat(pointRepo.point.getDepositBalance()).isZero();
        assertThat(txRepo.saved).extracting(PointTransaction::getType)
                .containsExactly(PointTransactionType.USE, PointTransactionType.SETTLE, PointTransactionType.RESTORE);
    }

    @Test
    void restoreSettledOrder_isIdempotent() {
        service.holdForNewOrder(COMPANY, ORDER, "DF-1", 40_000L, 9L);
        sync(OrderPointState.DELIVERED, 40_000L);

        service.restoreSettledOrder(COMPANY, ORDER, "DF-1", 9L);
        service.restoreSettledOrder(COMPANY, ORDER, "DF-1", 9L);

        assertThat(pointRepo.point.getBalance()).isEqualTo(100_000L);
        assertThat(txRepo.saved).filteredOn(t -> t.getType() == PointTransactionType.RESTORE).hasSize(1);
    }

    @Test
    void restoreSettledOrder_withoutSettleHistory_doesNothing() {
        service.restoreSettledOrder(COMPANY, ORDER, "DF-1", 9L);

        assertThat(pointRepo.point.getBalance()).isEqualTo(100_000L);
        assertThat(txRepo.saved).isEmpty();
    }

    @Test
    void syncOrder_legacyOrderWithoutHistory_isIgnored() {
        sync(OrderPointState.ACTIVE, 40_000L);
        sync(OrderPointState.DELIVERED, 40_000L);

        assertThat(pointRepo.point.getBalance()).isEqualTo(100_000L);
        assertThat(txRepo.saved).isEmpty();
    }

    @Test
    void syncOrder_actorFromOtherCompany_isNotRecorded() {
        AdminRepository adminRepo = Mockito.mock(AdminRepository.class);
        PointService svc = new PointService(pointRepo, txRepo, Mockito.mock(CompanyRepository.class), adminRepo);
        var vendorAdmin = adminOf(9L, 2L);
        var orderingAdmin = adminOf(8L, COMPANY);
        Mockito.when(adminRepo.findById(9L)).thenReturn(Optional.of(vendorAdmin));
        Mockito.when(adminRepo.findById(8L)).thenReturn(Optional.of(orderingAdmin));
        svc.holdForNewOrder(COMPANY, ORDER, "DF-1", 40_000L, 8L);

        svc.syncOrder(COMPANY, ORDER, "DF-1", OrderPointState.CLOSED, 40_000L, 9L);

        assertThat(txRepo.saved).extracting(PointTransaction::getMemberId).containsExactly(8L, null);
    }

    @Test
    void adjustSettledOrder_increase_deductsBalanceAndRecordsAdjustUse() {
        service.holdForNewOrder(COMPANY, ORDER, "DF-1", 40_000L, 9L);
        sync(OrderPointState.DELIVERED, 40_000L);

        service.adjustSettledOrder(COMPANY, ORDER, "DF-1", 10_000L, 9L);

        assertThat(pointRepo.point.getBalance()).isEqualTo(50_000L);
        assertThat(pointRepo.point.getDepositBalance()).isZero();
        assertThat(txRepo.saved).extracting(PointTransaction::getType).last().isEqualTo(PointTransactionType.ADJUST_USE);
    }

    @Test
    void adjustSettledOrder_increase_whenInsufficient_throws() {
        service.holdForNewOrder(COMPANY, ORDER, "DF-1", 40_000L, 9L);
        sync(OrderPointState.DELIVERED, 40_000L);

        assertThatThrownBy(() -> service.adjustSettledOrder(COMPANY, ORDER, "DF-1", 60_001L, 9L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(PointErrorCode.INSUFFICIENT_POINT));
        assertThat(pointRepo.point.getBalance()).isEqualTo(60_000L);
    }

    @Test
    void adjustSettledOrder_decrease_restoresBalanceAndRecordsAdjustRestore() {
        service.holdForNewOrder(COMPANY, ORDER, "DF-1", 40_000L, 9L);
        sync(OrderPointState.DELIVERED, 40_000L);

        service.adjustSettledOrder(COMPANY, ORDER, "DF-1", -15_000L, 9L);

        assertThat(pointRepo.point.getBalance()).isEqualTo(75_000L);
        assertThat(txRepo.saved).extracting(PointTransaction::getType).last().isEqualTo(PointTransactionType.ADJUST_RESTORE);
    }

    @Test
    void adjustSettledOrder_legacyOrderWithoutHistory_isIgnored() {
        service.adjustSettledOrder(COMPANY, ORDER, "DF-1", 10_000L, 9L);

        assertThat(pointRepo.point.getBalance()).isEqualTo(100_000L);
        assertThat(txRepo.saved).isEmpty();
    }

    @Test
    void canAdjustSettledOrder_checksBalanceOnlyForIncrease() {
        service.holdForNewOrder(COMPANY, ORDER, "DF-1", 40_000L, 9L);
        sync(OrderPointState.DELIVERED, 40_000L);

        assertThat(service.canAdjustSettledOrder(COMPANY, ORDER, 60_000L)).isTrue();
        assertThat(service.canAdjustSettledOrder(COMPANY, ORDER, 60_001L)).isFalse();
        assertThat(service.canAdjustSettledOrder(COMPANY, ORDER, -999_999L)).isTrue();
    }

    @Test
    void restoreSettledOrder_afterAdjust_returnsSettleSumPlusNetAdjust() {
        service.holdForNewOrder(COMPANY, ORDER, "DF-1", 40_000L, 9L);
        sync(OrderPointState.DELIVERED, 40_000L);
        service.adjustSettledOrder(COMPANY, ORDER, "DF-1", 10_000L, 9L);

        service.restoreSettledOrder(COMPANY, ORDER, "DF-1", 9L);

        assertThat(pointRepo.point.getBalance()).isEqualTo(100_000L);
    }

    @Test
    void restoreSettledOrder_afterDecreaseAdjust_returnsReducedAmount() {
        service.holdForNewOrder(COMPANY, ORDER, "DF-1", 40_000L, 9L);
        sync(OrderPointState.DELIVERED, 40_000L);
        service.adjustSettledOrder(COMPANY, ORDER, "DF-1", -10_000L, 9L);

        service.restoreSettledOrder(COMPANY, ORDER, "DF-1", 9L);

        assertThat(pointRepo.point.getBalance()).isEqualTo(100_000L);
    }

    private static com.greenlink.core.domain.admin.model.Admin adminOf(Long id, Long companyId) {
        com.greenlink.core.domain.admin.model.Admin a = Mockito.mock(com.greenlink.core.domain.admin.model.Admin.class);
        Mockito.when(a.getId()).thenReturn(id);
        Mockito.when(a.getCompanyId()).thenReturn(companyId);
        return a;
    }

    static class FakePointRepo implements CompanyPointRepository {
        CompanyPoint point;

        public Optional<CompanyPoint> findByCompanyId(Long companyId) { return Optional.ofNullable(point); }
        public Optional<CompanyPoint> findByCompanyIdForUpdate(Long companyId) { return Optional.ofNullable(point); }
        public List<CompanyPoint> findAllByCompanyIdIn(List<Long> companyIds) { return List.of(point); }
        public CompanyPoint save(CompanyPoint p) { this.point = p; return p; }
    }

    static class FakeTxRepo implements PointTransactionRepository {
        final List<PointTransaction> saved = new ArrayList<>();

        public PointTransaction save(PointTransaction t) { saved.add(t); return t; }
        public Optional<PointTransaction> findById(Long id) { throw new UnsupportedOperationException(); }
        public Page<PointTransaction> findByCompanyId(Long companyId, Pageable pageable) { throw new UnsupportedOperationException(); }
        public boolean existsOrderTransaction(Long orderId) {
            return saved.stream().anyMatch(t -> orderId.equals(t.getOrderId()) && t.getType() != PointTransactionType.CHARGE);
        }
        public boolean existsSettle(Long orderId) {
            return saved.stream().anyMatch(t -> orderId.equals(t.getOrderId()) && t.getType() == PointTransactionType.SETTLE);
        }
        public boolean existsRestore(Long orderId) {
            return saved.stream().anyMatch(t -> orderId.equals(t.getOrderId()) && t.getType() == PointTransactionType.RESTORE);
        }
        public long sumSettleByOrderId(Long orderId) {
            return saved.stream().filter(t -> orderId.equals(t.getOrderId()) && t.getType() == PointTransactionType.SETTLE)
                    .mapToLong(PointTransaction::getAmount).sum();
        }
        public long sumNetAdjustByOrderId(Long orderId) {
            return saved.stream().filter(t -> orderId.equals(t.getOrderId())).mapToLong(t ->
                    t.getType() == PointTransactionType.ADJUST_USE ? t.getAmount()
                            : t.getType() == PointTransactionType.ADJUST_RESTORE ? -t.getAmount() : 0L).sum();
        }
        public long sumNetHeldByOrderId(Long orderId) {
            return saved.stream().filter(t -> orderId.equals(t.getOrderId())).mapToLong(t ->
                    t.getType() == PointTransactionType.USE ? t.getAmount()
                            : t.getType() == PointTransactionType.REFUND ? -t.getAmount() : 0L).sum();
        }
    }
}
