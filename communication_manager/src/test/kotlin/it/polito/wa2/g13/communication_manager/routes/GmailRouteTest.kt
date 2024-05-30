package it.polito.wa2.g13.communication_manager.routes

import com.google.api.services.gmail.model.Message
import it.polito.wa2.g13.communication_manager.IntegrationTest
import it.polito.wa2.g13.communication_manager.configurations.CrmConfigProperties
import it.polito.wa2.g13.communication_manager.dtos.CreateMessageDTO
import it.polito.wa2.g13.communication_manager.dtos.CrmSendMessageResponse
import it.polito.wa2.g13.communication_manager.dtos.Priority
import it.polito.wa2.g13.communication_manager.util.randomAttachments
import it.polito.wa2.g13.communication_manager.util.randomMessage
import org.apache.camel.*
import org.apache.camel.builder.AdviceWith
import org.apache.camel.builder.ExchangeBuilder
import org.apache.camel.component.google.mail.stream.GoogleMailStreamConstants
import org.apache.camel.component.mock.MockEndpoint
import org.apache.camel.test.spring.junit5.CamelSpringBootTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.*
import org.springframework.test.annotation.DirtiesContext
import org.springframework.web.client.RestTemplate

@SpringBootTest
@CamelSpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GmailRouteTest : IntegrationTest() {

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

    @Autowired
    private lateinit var context: CamelContext

    @Test
    fun `test container connection`() {
        val resCrm = postMessage(
            CreateMessageDTO(
                priority = Priority.Low,
                channel = "email",
                body = "sium",
                sender = "io",
                subject = "boh",
            )
        )

        assertEquals(HttpStatusCode.valueOf(201), resCrm.statusCode)
    }

    private fun Message.toDto() = CrmSendMessageResponse(
        id = 0,
        body = this.snippet,
        sender = this
            .payload
            .headers
            .find { header -> header.name.equals("from", ignoreCase = true) }
            ?.value
            ?.toString()!!,
        channel = "email",
        priority = "",
        subject = this
            .payload
            .headers
            .find { header -> header.name.equals("subject", ignoreCase = true) }
            ?.get("value")
            ?.toString()!!,
        status = "",
        date = "",
    )

    @Produce("direct:google-mail-stream")
    private lateinit var stream: ProducerTemplate

    @EndpointInject("mock:checkResult")
    private lateinit var check: MockEndpoint


    @Test
    fun `an email sent through google-mail-stream should be inserted in the crm service`() {
        val message = randomMessage()
        // Replace the route `from` with a source sent later
        // After the `direct:sendMessage` use a mock route to check the response
        // received from the Crm service
        AdviceWith.adviceWith(context, GmailRoute.MAIN_ROUTE_ID) {
            it.replaceFromWith("direct:google-mail-stream")
            it.weaveByToUri(GmailRoute.Direct.GET_GMAIL_MESSAGE).replace().transform(it.constant(message))
            it.weaveByToUri(GmailRoute.Direct.SEND_MESSAGE_TO_CRM).after().to("mock:checkResult")
        }

        val exchange = ExchangeBuilder.anExchange(context)
            .withHeader(GoogleMailStreamConstants.MAIL_ID, "id")
            .build()

        stream.send(exchange)

        val result = check.exchanges.firstOrNull()?.getIn()?.getBody(CrmSendMessageResponse::class.java)

        Assertions.assertThat(result)
            .usingRecursiveComparison()
            .ignoringFields("date", "status", "priority", "id")
            .isEqualTo(message.toDto())
    }

    @Test
    fun `an email with attachments sent through google-mail-stream should be inserted in the crm and document_store service`() {
        val message = randomMessage()
        val attachments = randomAttachments(5)

        // Replace the route `from` with a source sent later
        // After the `direct:sendMessage` use a mock route to check the response
        // received from the Crm service
        AdviceWith.adviceWith(context, GmailRoute.MAIN_ROUTE_ID) {
            it.replaceFromWith("direct:google-mail-stream")
            it.weaveByToUri(GmailRoute.Direct.GET_GMAIL_MESSAGE).replace().transform(it.constant(message))
            it.weaveByToUri(GmailRoute.Direct.GET_ATTACHMENTS).replace()
                .process { exchange -> exchange.setVariable("attachments", attachments) }
            it.weaveByToUri(GmailRoute.Direct.SEND_MESSAGE_TO_CRM).after().to("mock:checkResult")
            it.weaveByToUri(GmailRoute.Direct.SEND_ATTACHMENT_TO_DOCUMENT_STORE).after().to("mock:checkResult")
        }

        val exchange = ExchangeBuilder.anExchange(context)
            .withHeader(GoogleMailStreamConstants.MAIL_ID, "id")
            .build()

        stream.send(exchange)

        val messageResponse = check.exchanges.getOrNull(0)?.getIn()?.getBody(CrmSendMessageResponse::class.java)

        Assertions.assertThat(messageResponse)
            .usingRecursiveComparison()
            .ignoringFields("date", "status", "priority", "id")
            .isEqualTo(message.toDto())

        for ((index, _) in attachments.withIndex()) {
            val attachmentResponse =
                check.exchanges.getOrNull(index + 1)?.getIn()?.getHeader(Exchange.HTTP_RESPONSE_CODE)

            assertEquals(201, attachmentResponse)
        }
    }
}