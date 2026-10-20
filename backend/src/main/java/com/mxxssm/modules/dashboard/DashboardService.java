package com.mxxssm.modules.dashboard;

import com.mxxssm.common.domain.Statuses;
import com.mxxssm.modules.log.LogService;
import com.mxxssm.modules.menu.SysMenuRepository;
import com.mxxssm.modules.permission.SysPermissionRepository;
import com.mxxssm.modules.role.SysRoleRepository;
import com.mxxssm.modules.user.SysUserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final SysPermissionRepository permissionRepository;
    private final SysMenuRepository menuRepository;
    private final LogService logService;

    public DashboardService(SysUserRepository userRepository, SysRoleRepository roleRepository,
            SysPermissionRepository permissionRepository, SysMenuRepository menuRepository,
            LogService logService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.menuRepository = menuRepository;
        this.logService = logService;
    }

    @Transactional(readOnly = true)
    public DashboardSummary summary() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return new DashboardSummary(
                userRepository.count(),
                userRepository.countByStatus(Statuses.ACTIVE),
                roleRepository.count(),
                permissionRepository.count(),
                menuRepository.count(),
                logService.countLoginsSince(startOfDay),
                logService.countFailedLoginsSince(startOfDay),
                logService.countOperationsSince(startOfDay),
                logService.recentLogins(),
                LocalDateTime.now());
    }
}
