package com.fatou82.suivi.suivihoraireapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for JWT.
 * Binds properties with prefix `jwt` from application.properties.
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
	/** Secret key (Base64 encoded) used to sign JWTs */
	private String secret;

	/** Expiration time in milliseconds */
	private long expiration;
}
