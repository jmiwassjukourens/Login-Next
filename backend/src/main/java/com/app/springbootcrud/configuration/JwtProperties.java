package com.app.springbootcrud.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    private long expirationMs;
    private Cookie cookie = new Cookie();

    public static class Cookie {
        private String name;
        private boolean httpOnly;
        private boolean secure;
        private String sameSite;
        private String path;
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public boolean isHttpOnly() {
            return httpOnly;
        }
        public void setHttpOnly(boolean httpOnly) {
            this.httpOnly = httpOnly;
        }
        public boolean isSecure() {
            return secure;
        }
        public void setSecure(boolean secure) {
            this.secure = secure;
        }
        public String getSameSite() {
            return sameSite;
        }
        public void setSameSite(String sameSite) {
            this.sameSite = sameSite;
        }
        public String getPath() {
            return path;
        }
        public void setPath(String path) {
            this.path = path;
        }

        
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }

    public Cookie getCookie() {
        return cookie;
    }

    public void setCookie(Cookie cookie) {
        this.cookie = cookie;
    }

    
}
