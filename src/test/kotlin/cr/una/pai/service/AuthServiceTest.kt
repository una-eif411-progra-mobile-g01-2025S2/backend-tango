package cr.una.pai.service

import cr.una.pai.domain.RefreshToken
import cr.una.pai.domain.RefreshTokenRepository
import cr.una.pai.domain.Role
import cr.una.pai.domain.User
import cr.una.pai.domain.UserRepository
import cr.una.pai.domain.UserRole
import cr.una.pai.dto.LoginRequest
import cr.una.pai.security.JwtService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import java.util.Optional
import java.util.UUID

class AuthServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var refreshTokenRepository: RefreshTokenRepository
    private lateinit var jwtService: JwtService
    private lateinit var authenticationManager: AuthenticationManager
    private lateinit var authService: AuthService

    private val userId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        userRepository = mock()
        refreshTokenRepository = mock()
        jwtService = mock()
        authenticationManager = mock()
        authService = AuthService(userRepository, refreshTokenRepository, jwtService, authenticationManager)
    }

    @Test
    fun `login authenticates credentials and returns tokens`() {
        val request = LoginRequest(" User@pai.local ", "password123")
        val role = Role(name = "USER")
        val userRole = UserRole().apply { this.role = role }
        val user = User(
            id = userId,
            email = "user@pai.local",
            password = "stored",
            fullName = "Usuario PAI"
        ).apply {
            userRoles = mutableSetOf(userRole)
        }

        val authentication: Authentication = UsernamePasswordAuthenticationToken("user@pai.local", "password123")

        whenever(authenticationManager.authenticate(any())).thenReturn(authentication)
        whenever(userRepository.findByEmail("user@pai.local")).thenReturn(Optional.of(user))
        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))
        whenever(refreshTokenRepository.findAllByUser_IdAndRevokedFalse(userId)).thenReturn(emptyList())
        whenever(jwtService.generateAccessToken(user, "USER")).thenReturn("access-token")
        whenever(jwtService.generateRefreshToken(user)).thenReturn("refresh-token")
        whenever(jwtService.accessTokenTtl()).thenReturn(900L)
        whenever(jwtService.refreshTokenTtl()).thenReturn(604800L)
        whenever(refreshTokenRepository.save(any())).thenAnswer { it.arguments[0] as RefreshToken }

        val response = authService.login(request)

        val captor = argumentCaptor<UsernamePasswordAuthenticationToken>()
        verify(authenticationManager).authenticate(captor.capture())
        assertEquals("user@pai.local", captor.firstValue.principal)
        assertEquals("password123", captor.firstValue.credentials)

        assertEquals("access-token", response.accessToken)
        assertEquals("refresh-token", response.refreshToken)
        assertEquals(900L, response.accessTokenExpiresIn)
        assertEquals(604800L, response.refreshTokenExpiresIn)
    }

    @Test
    fun `login throws InvalidCredentialsException when authentication fails`() {
        val request = LoginRequest("user@pai.local", "wrong")

        doThrow(BadCredentialsException("bad"))
            .whenever(authenticationManager)
            .authenticate(any())

        assertThrows(AuthService.InvalidCredentialsException::class.java) {
            authService.login(request)
        }

        verify(userRepository, never()).findByEmail(any())
        verify(refreshTokenRepository, never()).findAllByUser_IdAndRevokedFalse(any())
    }
}
