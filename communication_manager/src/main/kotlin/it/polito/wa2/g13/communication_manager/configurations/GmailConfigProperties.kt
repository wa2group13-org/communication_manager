package it.polito.wa2.g13.communication_manager.configurations

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "gmail")
data class GmailConfigProperties(
    val applicationName: String,
    val clientId: String,
    val clientSecret: String,
    val refreshToken: String,
)