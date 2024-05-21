package it.polito.wa2.g13.communication_manager.configurations

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "crm")
data class CrmConfigProperties(
    val url: String,
    val port: String,
)