package cr.una.pai.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.time.Duration

@Component
@ConfigurationProperties(prefix = "app.jwt")
class JwtProperties {
    lateinit var secret: String
    lateinit var issuer: String
    lateinit var accessTokenTtl: Duration
    lateinit var refreshTokenTtl: Duration
    var accessTokenType: String = "access"
    var refreshTokenType: String = "refresh"
}
