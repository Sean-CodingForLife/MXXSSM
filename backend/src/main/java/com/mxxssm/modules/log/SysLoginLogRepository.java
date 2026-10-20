package com.mxxssm.modules.log;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SysLoginLogRepository
        extends JpaRepository<SysLoginLog, Long>, JpaSpecificationExecutor<SysLoginLog> {

    List<SysLoginLog> findTop5ByOrderByIdDesc();

    long countByCreatedAtAfter(LocalDateTime from);

    long countByCreatedAtAfterAndStatus(LocalDateTime from, String status);
}
