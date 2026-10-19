package com.greenlink.core.domain.admin.port;

import com.greenlink.core.domain.admin.model.Admin;

import java.util.List;
import java.util.Optional;

public interface AdminRepository {
    Optional<Admin> findById(Long id);
    Optional<Admin> findByLoginId(String loginId);
    boolean existsByLoginId(String loginId);
    Admin save(Admin admin);
    List<Admin> findAllVendors();
    List<Admin> findAllByCompanyId(Long companyId);
}
