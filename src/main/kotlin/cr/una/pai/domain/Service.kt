package cr.una.pai.domain

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AppUserDetailsService(
    @Autowired private val userRepository: UserRepository
) : UserDetailsService {

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByEmail(username)
            .orElseThrow { UsernameNotFoundException("Usuario no encontrado: $username") }

        val authorities = getAuthoritiesFromUser(user)

        return org.springframework.security.core.userdetails.User(
            user.email,
            user.password,
            true,
            true,
            true,
            true,
            authorities
        )
    }

    private fun getAuthoritiesFromUser(user: User): Collection<GrantedAuthority> {
        return user.userRoles.flatMap { userRole ->
            val role = userRole.role
            val authorities = mutableListOf<GrantedAuthority>()
            if (role != null) {
                authorities.add(SimpleGrantedAuthority(role.name))
                authorities.addAll(
                    role.rolePrivileges.mapNotNull { it.privilege?.let { p -> SimpleGrantedAuthority(p.name) } }
                )
            }
            authorities
        }.toSet()
    }
}
