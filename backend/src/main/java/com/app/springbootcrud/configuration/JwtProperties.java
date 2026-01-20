package com.app.springbootcrud.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    private long accessExpirationMs;

    private long refreshExpirationMs;


    private Cookie accessCookie = new Cookie();

    private Cookie refreshCookie = new Cookie();


    public long getAccessExpirationMs() {
        return accessExpirationMs;
    }

    public void setAccessExpirationMs(long accessExpirationMs) {
        this.accessExpirationMs = accessExpirationMs;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    public void setRefreshExpirationMs(long refreshExpirationMs) {
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public Cookie getAccessCookie() {
        return accessCookie;
    }

    public void setAccessCookie(Cookie accessCookie) {
        this.accessCookie = accessCookie;
    }

    public Cookie getRefreshCookie() {
        return refreshCookie;
    }

    public void setRefreshCookie(Cookie refreshCookie) {
        this.refreshCookie = refreshCookie;
    }


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


}
