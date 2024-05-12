package it.polito.wa2.g13.communication_manager.configurations

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.web.SecurityFilterChain

class SecurityConfig(
    val crr: ClientRegistrationRepository,
) {
    private fun oidcLogoutSessionHandler() = OidcClientInitiatedLogoutSuccessHandler(crr)

    @Bean
    fun springSecurityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        return httpSecurity
            .authorizeHttpRequests {
                it.requestMatchers("/API/*").authenticated()
            }
            .oauth2Login {}
            .logout { it.logoutSuccessHandler(oidcLogoutSessionHandler()) }
            .build()
    }
}