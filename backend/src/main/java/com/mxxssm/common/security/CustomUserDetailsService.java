package com.mxxssm.common.security;

import com.mxxssm.modules.permission.SysPermission;
import com.mxxssm.modules.role.SysRole;
import com.mxxssm.modules.user.SysUser;
import com.mxxssm.modules.user.SysUserRepository;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final SysUserRepository userRepository;

    public CustomUserDetailsService(SysUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        SysUser user = userRepository.findWithAuthoritiesByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Set<String> roles = new LinkedHashSet<>();
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        for (SysRole role : user.getRoles()) {
            roles.add(role.getCode());
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
            for (SysPermission permission : role.getPermissions()) {
                if ("ACTIVE".equals(permission.getStatus())) {
                    authorities.add(new SimpleGrantedAuthority(permission.getCode()));
                }
            }
        }

        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getDisplayName(),
                user.getStatus(),
                roles,
                authorities);
    }
}
