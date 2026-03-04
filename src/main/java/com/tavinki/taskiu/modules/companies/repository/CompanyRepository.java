package com.tavinki.taskiu.modules.companies.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tavinki.taskiu.modules.companies.entity.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, String> {

    /**
     * Find a company by its unique public Company ID.
     */
    Optional<Company> findByCompanyId(String companyId);

    /**
     * Check if a company exists by its public Company ID.
     * Useful for validation to ensure uniqueness.
     */
    boolean existsByCompanyId(String companyId);

    /**
     * Search for companies by name (case-insensitive partial match).
     * Example: "Tech" finds "TechCorp", "InnoTech", etc.
     */
    List<Company> findByCompanyNameContainingIgnoreCase(String companyName);

    /**
     * Find a company by its name (exact match).
     */
    Optional<Company> findByCompanyName(String companyName);
}
