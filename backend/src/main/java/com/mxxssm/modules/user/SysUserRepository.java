package com.mxxssm.modules.user;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SysUserRepository extends JpaRepository<SysUser, Long>, JpaSpecificationExecutor<SysUser> {

    Optional<SysUser> findByUsername(String username);

    boolean existsByUsername(String username);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("select u from SysUser u where u.username = :username")
    Optional<SysUser> findWithAuthoritiesByUsername(@Param("username") String username);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("select u from SysUser u where u.id = :id")
    Optional<SysUser> findWithAuthoritiesById(@Param("id") Long id);

    long countByStatus(String status);

    @Query("select count(distinct u) from SysUser u join u.roles r where r.code = :roleCode and u.status = :status")
    long countByRoleCodeAndStatus(@Param("roleCode") String roleCode, @Param("status") String status);

    @Modifying
    @Query("update SysUser u set u.lastLoginAt = :lastLoginAt where u.id = :id")
    void updateLastLoginAt(@Param("id") Long id, @Param("lastLoginAt") LocalDateTime lastLoginAt);
}
