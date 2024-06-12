package it.polito.wa2.g13.communication_manager.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@Profile("!no-security")
class SecurityConfig(
    private val environment: Environment,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .authorizeHttpRequests {
                // If api-docs is enabled allow resource access to get the api
                if (environment.acceptsProfiles(Profiles.of("api-docs"))) {
                    it.requestMatchers("/v3/api-docs/**", "swagger-ui/**", "/swagger-ui.html").permitAll()
                }

                it.anyRequest().authenticated()
            }
            .oauth2ResourceServer { it.jwt {} }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .csrf { it.disable() }
            .cors { it.disable() }
            .build()
    }
}

@Configuration
@EnableWebSecurity
@Profile("no-security")
class SecurityNoFilterConfig {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .authorizeHttpRequests {
                it.anyRequest().permitAll()
            }
            .csrf { it.disable() }
            .cors { it.disable() }
            .build()
    }
}