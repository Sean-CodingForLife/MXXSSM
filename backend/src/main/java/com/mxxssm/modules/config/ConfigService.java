package com.mxxssm.modules.config;

import com.mxxssm.common.api.ErrorCode;
import com.mxxssm.common.api.PageResult;
import com.mxxssm.common.domain.Statuses;
import com.mxxssm.common.exception.BusinessException;
import com.mxxssm.common.web.Pageables;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ConfigService {

    private final SysConfigRepository configRepository;

    public ConfigService(SysConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<ConfigResponse> page(String keyword, int page, int size) {
        Pageable pageable = Pageables.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Specification<SysConfig> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("configKey")), pattern),
                        builder.like(builder.lower(root.get("description")), pattern)));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
        return PageResult.of(configRepository.findAll(specification, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public List<ConfigResponse> all() {
        return configRepository.findAllByOrderByIdAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public String getString(String key, String fallback) {
        return configRepository.findByConfigKey(key)
                .filter(config -> Statuses.isActive(config.getStatus()))
                .map(SysConfig::getConfigValue)
                .orElse(fallback);
    }

    @Transactional(readOnly = true)
    public boolean getBoolean(String key, boolean fallback) {
        String value = getString(key, null);
        return value == null ? fallback : Boolean.parseBoolean(value);
    }

    @Transactional
    public ConfigResponse create(ConfigRequest request) {
        if (configRepository.existsByConfigKey(request.configKey())) {
            throw new BusinessException(ErrorCode.CONFIG_KEY_EXISTS, request.configKey());
        }
        SysConfig entity = new SysConfig();
        entity.setConfigKey(request.configKey().trim());
        entity.setConfigValue(request.configValue().trim());
        entity.setDescription(request.description());
        entity.setStatus(statusOrDefault(request.status()));
        return toResponse(configRepository.save(entity));
    }

    @Transactional
    public ConfigResponse update(Long id, ConfigRequest request) {
        SysConfig entity = configRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONFIG_NOT_FOUND));
        String key = request.configKey().trim();
        if (!key.equals(entity.getConfigKey())) {
            if (configRepository.existsByConfigKey(key)) {
                throw new BusinessException(ErrorCode.CONFIG_KEY_EXISTS, key);
            }
            entity.setConfigKey(key);
        }
        entity.setConfigValue(request.configValue().trim());
        entity.setDescription(request.description());
        entity.setStatus(statusOrDefault(request.status()));
        return toResponse(configRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        SysConfig entity = configRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONFIG_NOT_FOUND));
        configRepository.delete(entity);
    }

    public ConfigResponse toResponse(SysConfig entity) {
        return new ConfigResponse(
                entity.getId(),
                entity.getConfigKey(),
                entity.getConfigValue(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private String statusOrDefault(String status) {
        return Statuses.isValid(status) ? status : Statuses.ACTIVE;
    }
}
