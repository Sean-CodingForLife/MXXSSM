package com.mxxssm.modules.config;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SysConfigRepository extends JpaRepository<SysConfig, Long>, JpaSpecificationExecutor<SysConfig> {

    Optional<SysConfig> findByConfigKey(String configKey);

    boolean existsByConfigKey(String configKey);

    List<SysConfig> findAllByOrderByIdAsc();
}
