package com.mxxssm.modules.log;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SysOperationLogRepository
        extends JpaRepository<SysOperationLog, Long>, JpaSpecificationExecutor<SysOperationLog> {

    long countByCreatedAtAfter(LocalDateTime from);
}
