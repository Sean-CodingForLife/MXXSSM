package com.mxxssm.modules.dashboard;

import com.mxxssm.modules.log.LoginLogResponse;
import java.time.LocalDateTime;
import java.util.List;

public record DashboardSummary(
        long userCount,
        long activeUserCount,
        long roleCount,
        long permissionCount,
        long menuCount,
        long todayLogins,
        long todayFailedLogins,
        long todayOperations,
        List<LoginLogResponse> recentLogins,
        LocalDateTime generatedAt) {
}
