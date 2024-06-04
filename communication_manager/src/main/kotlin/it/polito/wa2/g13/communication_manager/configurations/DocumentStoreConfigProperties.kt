package it.polito.wa2.g13.communication_manager.configurations

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "document-store")
data class DocumentStoreConfigProperties(
    val url: String,
    val port: String,
    val path: String,
)