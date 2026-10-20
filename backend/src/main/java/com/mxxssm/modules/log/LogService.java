package com.mxxssm.modules.log;

import com.mxxssm.common.api.PageResult;
import com.mxxssm.common.web.Pageables;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class LogService {

    private final SysLoginLogRepository loginLogRepository;
    private final SysOperationLogRepository operationLogRepository;

    public LogService(SysLoginLogRepository loginLogRepository, SysOperationLogRepository operationLogRepository) {
        this.loginLogRepository = loginLogRepository;
        this.operationLogRepository = operationLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLogin(String username, String status, String ip, String userAgent, String message) {
        SysLoginLog entity = new SysLoginLog();
        entity.setUsername(trim(username, 64));
        entity.setStatus(status);
        entity.setIp(trim(ip, 64));
        entity.setUserAgent(trim(userAgent, 255));
        entity.setMessage(trim(message, 255));
        loginLogRepository.save(entity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordOperation(String username, String action, String method, String path, String status,
            String message, long durationMs, String ip) {
        SysOperationLog entity = new SysOperationLog();
        entity.setUsername(trim(username, 64));
        entity.setAction(trim(action, 100));
        entity.setMethod(method);
        entity.setPath(path);
        entity.setStatus(status);
        entity.setMessage(trim(message, 255));
        entity.setDurationMs(Math.max(durationMs, 0));
        entity.setIp(trim(ip, 64));
        operationLogRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public PageResult<LoginLogResponse> pageLoginLogs(String keyword, String status, int page, int size) {
        Pageable pageable = Pageables.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Specification<SysLoginLog> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("username")), pattern),
                        builder.like(builder.lower(root.get("ip")), pattern),
                        builder.like(builder.lower(root.get("message")), pattern)));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
        return PageResult.of(loginLogRepository.findAll(specification, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResult<OperationLogResponse> pageOperationLogs(String keyword, String status, int page, int size) {
        Pageable pageable = Pageables.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Specification<SysOperationLog> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("username")), pattern),
                        builder.like(builder.lower(root.get("action")), pattern),
                        builder.like(builder.lower(root.get("path")), pattern),
                        builder.like(builder.lower(root.get("message")), pattern)));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
        return PageResult.of(operationLogRepository.findAll(specification, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public List<LoginLogResponse> recentLogins() {
        return loginLogRepository.findTop5ByOrderByIdDesc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public long countLoginsSince(java.time.LocalDateTime from) {
        return loginLogRepository.countByCreatedAtAfter(from);
    }

    @Transactional(readOnly = true)
    public long countFailedLoginsSince(java.time.LocalDateTime from) {
        return loginLogRepository.countByCreatedAtAfterAndStatus(from, "FAIL");
    }

    @Transactional(readOnly = true)
    public long countOperationsSince(java.time.LocalDateTime from) {
        return operationLogRepository.countByCreatedAtAfter(from);
    }

    public LoginLogResponse toResponse(SysLoginLog entity) {
        return new LoginLogResponse(
                entity.getId(),
                entity.getUsername(),
                entity.getStatus(),
                entity.getIp(),
                entity.getUserAgent(),
                entity.getMessage(),
                entity.getCreatedAt());
    }

    public OperationLogResponse toResponse(SysOperationLog entity) {
        return new OperationLogResponse(
                entity.getId(),
                entity.getUsername(),
                entity.getAction(),
                entity.getMethod(),
                entity.getPath(),
                entity.getStatus(),
                entity.getMessage(),
                entity.getDurationMs(),
                entity.getIp(),
                entity.getCreatedAt());
    }

    private String trim(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
