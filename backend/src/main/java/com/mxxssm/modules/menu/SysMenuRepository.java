package com.mxxssm.modules.menu;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SysMenuRepository extends JpaRepository<SysMenu, Long>, JpaSpecificationExecutor<SysMenu> {

    Optional<SysMenu> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByPermissionCode(String permissionCode);

    long countByPermissionCode(String permissionCode);

    List<SysMenu> findAllByOrderBySortOrderAscIdAsc();

    List<SysMenu> findByStatusOrderBySortOrderAscIdAsc(String status);

    List<SysMenu> findByPermissionCodeInAndStatusOrderBySortOrderAscIdAsc(Collection<String> permissionCodes,
            String status);
}
