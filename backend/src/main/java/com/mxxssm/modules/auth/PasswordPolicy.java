package com.mxxssm.modules.auth;

import com.mxxssm.common.api.ErrorCode;
import com.mxxssm.common.exception.BusinessException;
import com.mxxssm.config.AppProperties;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class PasswordPolicy {

    private static final Pattern LETTER = Pattern.compile("[A-Za-z]");
    private static final Pattern DIGIT = Pattern.compile("\\d");

    private final AppProperties properties;

    public PasswordPolicy(AppProperties properties) {
        this.properties = properties;
    }

    public void validate(String rawPassword) {
        int min = properties.getPassword().getMinLength();
        int max = properties.getPassword().getMaxLength();
        if (rawPassword == null || rawPassword.length() < min || rawPassword.length() > max
                || !LETTER.matcher(rawPassword).find() || !DIGIT.matcher(rawPassword).find()) {
            throw new BusinessException(ErrorCode.PASSWORD_POLICY, min, max);
        }
    }
}
