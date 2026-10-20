package com.mxxssm.modules.role;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SysRoleRepository extends JpaRepository<SysRole, Long>, JpaSpecificationExecutor<SysRole> {

    Optional<SysRole> findByCode(String code);

    boolean existsByCode(String code);

    @EntityGraph(attributePaths = "permissions")
    @Query("select r from SysRole r where r.id = :id")
    Optional<SysRole> findWithPermissionsById(@Param("id") Long id);

    @EntityGraph(attributePaths = "permissions")
    @Query("select r from SysRole r where r.code = :code")
    Optional<SysRole> findWithPermissionsByCode(@Param("code") String code);

    List<SysRole> findAllByOrderByIdAsc();

    List<SysRole> findByStatusOrderByIdAsc(String status);

    @Query("select count(distinct u) from SysUser u join u.roles r where r.id = :roleId")
    long countUsersByRoleId(@Param("roleId") Long roleId);

    @Query("select count(distinct u) from SysUser u join u.roles r where r.id = :roleId and u.status = 'ACTIVE'")
    long countActiveUsersByRoleId(@Param("roleId") Long roleId);
}
