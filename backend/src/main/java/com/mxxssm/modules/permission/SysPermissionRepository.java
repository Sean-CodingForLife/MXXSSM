package com.mxxssm.modules.permission;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SysPermissionRepository
        extends JpaRepository<SysPermission, Long>, JpaSpecificationExecutor<SysPermission> {

    Optional<SysPermission> findByCode(String code);

    boolean existsByCode(String code);

    List<SysPermission> findAllByOrderByIdAsc();

    List<SysPermission> findByStatusOrderByIdAsc(String status);

    @Query("select count(distinct r) from SysRole r join r.permissions p where p.id = :permissionId")
    long countRoleAssignments(@Param("permissionId") Long permissionId);
}
