package com.mxxssm.modules.auth;

import com.mxxssm.common.api.ErrorCode;
import com.mxxssm.common.audit.AuditLog;
import com.mxxssm.common.exception.BusinessException;
import com.mxxssm.common.security.CustomUserDetails;
import com.mxxssm.common.security.SecurityUtils;
import com.mxxssm.common.web.ClientIp;
import com.mxxssm.config.AppProperties;
import com.mxxssm.modules.config.ConfigService;
import com.mxxssm.modules.log.LogService;
import com.mxxssm.modules.menu.MenuResponse;
import com.mxxssm.modules.menu.MenuService;
import com.mxxssm.modules.permission.SysPermission;
import com.mxxssm.modules.publicapi.PublicSettingsResponse;
import com.mxxssm.modules.role.SysRole;
import com.mxxssm.modules.role.SysRoleRepository;
import com.mxxssm.modules.user.SysUser;
import com.mxxssm.modules.user.SysUserRepository;
import com.mxxssm.modules.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final MenuService menuService;
    private final ConfigService configService;
    private final CaptchaService captchaService;
    private final PasswordPolicy passwordPolicy;
    private final PasswordEncoder passwordEncoder;
    private final LogService logService;
    private final UserService userService;
    private final AppProperties appProperties;

    public AuthService(AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            SysUserRepository userRepository,
            SysRoleRepository roleRepository,
            MenuService menuService,
            ConfigService configService,
            CaptchaService captchaService,
            PasswordPolicy passwordPolicy,
            PasswordEncoder passwordEncoder,
            LogService logService,
            UserService userService,
            AppProperties appProperties) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.menuService = menuService;
        this.configService = configService;
        this.captchaService = captchaService;
        this.passwordPolicy = passwordPolicy;
        this.passwordEncoder = passwordEncoder;
        this.logService = logService;
        this.userService = userService;
        this.appProperties = appProperties;
    }

    public CaptchaResponse captcha(HttpSession session) {
        if (!captchaEnabled()) {
            return new CaptchaResponse(null, false, 0);
        }
        return captchaService.generate(session);
    }

    public CurrentUserResponse login(LoginRequest request, HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        HttpSession session = httpRequest.getSession(true);
        if (captchaEnabled() && !captchaService.verify(session, request.captcha())) {
            safeLogLogin(request.username(), "FAIL", httpRequest, "Captcha verification failed.");
            throw new BusinessException(ErrorCode.CAPTCHA_INVALID);
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username().trim(), request.password()));
        } catch (DisabledException exception) {
            safeLogLogin(request.username(), "FAIL", httpRequest, "Account is disabled.");
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        } catch (AuthenticationException exception) {
            if (exception.getCause() instanceof DataAccessException) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, exception);
            }
            safeLogLogin(request.username(), "FAIL", httpRequest, "Invalid credentials.");
            throw new BusinessException(ErrorCode.BAD_CREDENTIALS);
        }

        httpRequest.changeSessionId();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        try {
            userService.updateLastLogin(principal.getId());
        } catch (RuntimeException exception) {
            log.warn("Unable to update last login timestamp", exception);
        }
        safeLogLogin(principal.getUsername(), "SUCCESS", httpRequest, "Login succeeded.");
        return currentUser();
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse currentUser() {
        CustomUserDetails principal = SecurityUtils.requireUser();
        SysUser user = userRepository.findWithAuthoritiesById(principal.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Set<String> roles = new TreeSet<>();
        Set<String> permissions = new TreeSet<>();
        for (SysRole role : user.getRoles()) {
            if (!"ACTIVE".equals(role.getStatus())) {
                continue;
            }
            roles.add(role.getCode());
            for (SysPermission permission : role.getPermissions()) {
                if ("ACTIVE".equals(permission.getStatus())) {
                    permissions.add(permission.getCode());
                }
            }
        }
        List<MenuResponse> menus = menuService.forPermissions(permissions);
        return new CurrentUserResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getEmail(),
                user.getPhone(),
                user.getLastLoginAt(),
                roles,
                permissions,
                menus);
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (!registrationEnabled()) {
            throw new BusinessException(ErrorCode.REGISTRATION_DISABLED);
        }
        String username = request.username().trim();
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS, username);
        }
        passwordPolicy.validate(request.password());
        SysRole userRole = roleRepository.findByCode(SysRole.USER)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setDisplayName(request.displayName().trim());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEmail(blankToNull(request.email()));
        user.setPhone(blankToNull(request.phone()));
        user.setStatus("ACTIVE");
        user.getRoles().add(userRole);
        userRepository.save(user);
    }

    @Transactional
    @AuditLog(action = "auth.profile.update")
    public CurrentUserResponse updateProfile(ProfileUpdateRequest request) {
        SysUser user = currentUserEntity();
        user.setDisplayName(request.displayName().trim());
        user.setEmail(blankToNull(request.email()));
        user.setPhone(blankToNull(request.phone()));
        userRepository.save(user);
        return currentUser();
    }

    @Transactional
    @AuditLog(action = "auth.password.update")
    public void changePassword(PasswordChangeRequest request) {
        SysUser user = currentUserEntity();
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }
        passwordPolicy.validate(request.newPassword());
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    public PublicSettingsResponse publicSettings() {
        return new PublicSettingsResponse(
                configService.getString("site.name", "MXXSSM"),
                captchaEnabled(),
                registrationEnabled(),
                configService.getString("ui.default-locale", "zh-CN"),
                "2.0.0");
    }

    public boolean captchaEnabled() {
        return appProperties.getCaptcha().isEnabled()
                && configService.getBoolean("security.captcha.enabled", true);
    }

    public boolean registrationEnabled() {
        return appProperties.getRegistration().isEnabled()
                && configService.getBoolean("security.registration.enabled", true);
    }

    private SysUser currentUserEntity() {
        return userRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void safeLogLogin(String username, String status, HttpServletRequest request, String message) {
        try {
            logService.recordLogin(username, status, ClientIp.resolve(request), ClientIp.userAgent(request), message);
        } catch (RuntimeException exception) {
            log.warn("Unable to write login audit log", exception);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
