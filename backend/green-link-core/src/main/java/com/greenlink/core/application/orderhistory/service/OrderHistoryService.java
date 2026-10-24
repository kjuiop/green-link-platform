package com.greenlink.core.application.orderhistory.service;

import com.greenlink.core.application.orderhistory.dto.OrderHistoryResult;
import com.greenlink.core.application.orderhistory.usecase.GetOrderHistoryUseCase;
import com.greenlink.core.application.orderhistory.usecase.RecordOrderHistoryUseCase;
import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.orderhistory.model.OrderHistory;
import com.greenlink.core.domain.orderhistory.model.OrderHistoryAction;
import com.greenlink.core.domain.orderhistory.port.OrderHistoryRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class OrderHistoryService implements RecordOrderHistoryUseCase, GetOrderHistoryUseCase {

    private final OrderHistoryRepository orderHistoryRepository;
    private final AdminRepository adminRepository;
    private final CompanyRepository companyRepository;

    public OrderHistoryService(OrderHistoryRepository orderHistoryRepository,
                               AdminRepository adminRepository,
                               CompanyRepository companyRepository) {
        this.orderHistoryRepository = orderHistoryRepository;
        this.adminRepository = adminRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    @Transactional
    public void record(Long orderId, OrderHistoryAction action, String detail) {
        String[] actor = resolveActor();
        orderHistoryRepository.save(OrderHistory.create(orderId, action, detail, actor[0], actor[1]));
    }

    @Override
    public List<OrderHistoryResult> getHistory(Long orderId) {
        return orderHistoryRepository.findAllByOrderId(orderId).stream()
                .map(h -> new OrderHistoryResult(
                        h.getId(), h.getOrderId(), h.getAction(), h.getDetail(),
                        h.getActorCompany(), h.getActorName(), h.getCreatedAt()))
                .toList();
    }

    private String[] resolveActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return new String[]{null, null};
        try {
            Long adminId = Long.parseLong(String.valueOf(auth.getPrincipal()));
            Admin admin = adminRepository.findById(adminId).orElse(null);
            if (admin == null) return new String[]{null, null};
            String adminName = admin.getName();
            String companyName = null;
            if (admin.getCompanyId() != null) {
                companyName = companyRepository.findById(admin.getCompanyId())
                        .map(c -> c.getCompanyName())
                        .orElse(null);
            }
            return new String[]{companyName, adminName};
        } catch (NumberFormatException e) {
            return new String[]{null, null};
        }
    }
}
