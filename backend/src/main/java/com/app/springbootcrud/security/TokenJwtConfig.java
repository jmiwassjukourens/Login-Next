package com.app.springbootcrud.security;

import javax.crypto.SecretKey;

public class TokenJwtConfig {
    
    // This will be set by JwtSecretKeyConfig bean
    public static SecretKey SECRET_KEY;
    
    public static final String PREFIX_TOKEN = "Bearer ";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String CONTENT_TYPE = "application/json";
}
