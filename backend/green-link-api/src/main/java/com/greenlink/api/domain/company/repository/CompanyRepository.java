package com.greenlink.api.domain.company.repository;

import com.greenlink.core.domain.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByBusinessNo(String businessNo);
}
