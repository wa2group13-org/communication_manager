package it.polito.wa2.g13.communication_manager.routes

import it.polito.wa2.g13.communication_manager.IntegrationTest
import it.polito.wa2.g13.communication_manager.configurations.CrmConfigProperties
import it.polito.wa2.g13.communication_manager.dtos.CreateMessageDTO
import it.polito.wa2.g13.communication_manager.dtos.CrmSendMessageResponse
import it.polito.wa2.g13.communication_manager.dtos.Priority
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.web.client.RestTemplate

@SpringBootTest
class GmailRouteTest : IntegrationTest() {
    companion object {
        @DynamicPropertySource
        @JvmStatic
        fun crmProperties(
            registry: DynamicPropertyRegistry
        ) {
            registry.add("crm.port") { crm.getMappedPort(8080) }
        }

        @DynamicPropertySource
        @JvmStatic
        fun documentStoreProperties(
            registry: DynamicPropertyRegistry
        ) {
            registry.add("document_store.port") { documentStore.getMappedPort(8080) }
        }
    }

    @Autowired
    private lateinit var crmConfig: CrmConfigProperties

    private fun postMessage(message: CreateMessageDTO): ResponseEntity<CrmSendMessageResponse> {
        val restTemplate = RestTemplate()
        val headers = HttpHeaders().apply {
            this.add("Content-Type", "application/json")
            this.add("Accept", "application/json")
        }

        val entity = HttpEntity(message, headers)

        return restTemplate.exchange(
            "${crmConfig.url}:${crmConfig.port}/API/messages",
            HttpMethod.POST,
            entity,
            CrmSendMessageResponse::class.java
        )
    }

    @Test
    fun `test connection`() {
        println(crm.getLogs())
        println(
            postMessage(
                CreateMessageDTO(
                    priority = Priority.Low,
                    channel = "email",
                    body = "sium",
                    sender = "io",
                    subject = "boh",
                )
            )
        )
    }
}