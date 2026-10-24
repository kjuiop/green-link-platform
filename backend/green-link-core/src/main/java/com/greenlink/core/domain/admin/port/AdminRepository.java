package com.greenlink.core.domain.admin.port;

import com.greenlink.core.domain.admin.model.Admin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface AdminRepository {
    Optional<Admin> findById(Long id);
    Optional<Admin> findByLoginId(String loginId);
    boolean existsByLoginId(String loginId);
    Admin save(Admin admin);
    List<Admin> findAllVendors();
    List<Admin> findAllByCompanyId(Long companyId);
    Page<Admin> findVendorsPaged(String status, Pageable pageable);
    Optional<Admin> findByPhone(String phone);
    Admin updatePassword(Long id, String encodedPassword);
}
