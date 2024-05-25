package it.polito.wa2.g13.communication_manager

import it.polito.wa2.g13.communication_manager.configurations.CrmConfigProperties
import it.polito.wa2.g13.communication_manager.configurations.DocumentStoreConfigProperties
import it.polito.wa2.g13.communication_manager.configurations.GmailConfigProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(
    GmailConfigProperties::class,
    CrmConfigProperties::class,
    DocumentStoreConfigProperties::class
)
class CommunicationManagerApplication

fun main(args: Array<String>) {
    runApplication<CommunicationManagerApplication>(*args)
}
