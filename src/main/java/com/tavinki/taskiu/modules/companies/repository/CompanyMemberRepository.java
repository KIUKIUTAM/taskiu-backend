package com.tavinki.taskiu.modules.companies.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tavinki.taskiu.common.enums.role.CompanyRole;
import com.tavinki.taskiu.modules.companies.entity.CompanyMember;

@Repository
public interface CompanyMemberRepository extends JpaRepository<CompanyMember, String> {

    // ─── Basic Queries ───────────────────────────────────────

    // Get all members of a company
    List<CompanyMember> findByCompanyId(String companyId);

    // Get all companies a user has joined
    List<CompanyMember> findByUserId(String userId);

    // Get a specific user's membership in a company (check if already a member)
    Optional<CompanyMember> findByCompanyIdAndUserId(String companyId, String userId);

    // Check if a user is already a member
    boolean existsByCompanyIdAndUserId(String companyId, String userId);

    // ─── Role-Related ────────────────────────────────────────

    // Get all members with a specific role in a company
    List<CompanyMember> findByCompanyIdAndRole(String companyId, CompanyRole role);

    // Get a user's role in a specific company
    @Query("SELECT cm.role FROM CompanyMember cm WHERE cm.company.id = :companyId AND cm.user.id = :userId")
    Optional<CompanyRole> findRoleByCompanyIdAndUserId(
        @Param("companyId") String companyId,
        @Param("userId") String userId
    );

    // ─── With User / Company Data (Avoid N+1) ────────────────

    // Get all members of a company with User data eagerly fetched
    @Query("SELECT cm FROM CompanyMember cm JOIN FETCH cm.user WHERE cm.company.id = :companyId")
    List<CompanyMember> findByCompanyIdWithUser(@Param("companyId") String companyId);

    // Get all companies a user has joined with Company data eagerly fetched
    @Query("SELECT cm FROM CompanyMember cm JOIN FETCH cm.company WHERE cm.user.id = :userId")
    List<CompanyMember> findByUserIdWithCompany(@Param("userId") String userId);

    // ─── Delete ──────────────────────────────────────────────

    // Remove a specific member from a company
    void deleteByCompanyIdAndUserId(String companyId, String userId);

    // Remove all members from a company (used when dissolving a company)
    void deleteByCompanyId(String companyId);

    // ─── Statistics ──────────────────────────────────────────

    // Count total members in a company
    long countByCompanyId(String companyId);

    // Count members with a specific role in a company (e.g. ensure at least one OWNER)
    long countByCompanyIdAndRole(String companyId, CompanyRole role);
}
