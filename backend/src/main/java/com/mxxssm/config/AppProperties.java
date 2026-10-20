package com.mxxssm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Captcha captcha = new Captcha();
    private final Registration registration = new Registration();
    private final PasswordPolicy password = new PasswordPolicy();

    public Captcha getCaptcha() {
        return captcha;
    }

    public Registration getRegistration() {
        return registration;
    }

    public PasswordPolicy getPassword() {
        return password;
    }

    public static class Captcha {
        private boolean enabled = true;
        private int ttlSeconds = 120;
        private int width = 132;
        private int height = 44;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getTtlSeconds() {
            return ttlSeconds;
        }

        public void setTtlSeconds(int ttlSeconds) {
            this.ttlSeconds = ttlSeconds;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }

    public static class Registration {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class PasswordPolicy {
        private int minLength = 8;
        private int maxLength = 64;

        public int getMinLength() {
            return minLength;
        }

        public void setMinLength(int minLength) {
            this.minLength = minLength;
        }

        public int getMaxLength() {
            return maxLength;
        }

        public void setMaxLength(int maxLength) {
            this.maxLength = maxLength;
        }
    }
}
