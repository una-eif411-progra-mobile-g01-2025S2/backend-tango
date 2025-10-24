package cr.una.pai.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import java.time.Duration

@ConfigurationProperties(prefix = "app.jwt")
data class JwtProperties(
    val secret: String,
    val issuer: String,
    val accessTokenTtl: Duration,
    val refreshTokenTtl: Duration,
    @DefaultValue("access") val accessTokenType: String = "access",
    @DefaultValue("refresh") val refreshTokenType: String = "refresh"
)
