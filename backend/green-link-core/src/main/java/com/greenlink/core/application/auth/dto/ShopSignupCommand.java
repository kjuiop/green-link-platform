package com.greenlink.core.application.auth.dto;

public record ShopSignupCommand(
    String loginId,
    String password,
    String name,
    String phone,
    String email,
    String businessNo,
    String companyName,
    String ceoName,
    String requestedRole
) {

    @Override
    public String toString() {
        return "ShopSignupCommand[loginId=" + loginId + ", businessNo=" + businessNo + ", password=****]";
    }
}
