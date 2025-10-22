package cr.una.pai.security

import cr.una.pai.domain.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.Key
import java.time.Instant
import java.util.Date

@Service
class JwtService(
    private val properties: JwtProperties
) {

    private val signingKey: Key by lazy {
        val secretBytes = try {
            Decoders.BASE64.decode(properties.secret)
        } catch (_: IllegalArgumentException) {
            properties.secret.toByteArray(StandardCharsets.UTF_8)
        }
        Keys.hmacShaKeyFor(secretBytes)
    }

    fun generateAccessToken(user: User, primaryRole: String?): String =
        generateToken(user = user, role = primaryRole, tokenType = properties.accessTokenType, ttlSeconds = properties.accessTokenTtl.seconds)

    fun generateRefreshToken(user: User): String =
        generateToken(user = user, role = null, tokenType = properties.refreshTokenType, ttlSeconds = properties.refreshTokenTtl.seconds)

    private fun generateToken(user: User, role: String?, tokenType: String, ttlSeconds: Long): String {
        val userId = requireNotNull(user.id) { "User must have an id to issue tokens" }
        val now = Instant.now()
        val builder = Jwts.builder()
            .signWith(signingKey, SignatureAlgorithm.HS512)
            .setIssuer(properties.issuer)
            .setSubject(userId.toString())
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plusSeconds(ttlSeconds)))
            .claim("email", user.email)
            .claim("typ", tokenType)

        if (!role.isNullOrBlank()) {
            builder.claim("role", role)
        }

        return builder.compact()
    }

    fun parseAndValidate(token: String): Claims =
        Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .requireIssuer(properties.issuer)
            .build()
            .parseClaimsJws(token)
            .body

    fun accessTokenType(): String = properties.accessTokenType

    fun refreshTokenType(): String = properties.refreshTokenType

    fun refreshTokenTtl(): Long = properties.refreshTokenTtl.seconds
}
