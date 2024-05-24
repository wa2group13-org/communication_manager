package it.polito.wa2.g13.communication_manager.routes

import it.polito.wa2.g13.communication_manager.configurations.CrmConfigProperties
import it.polito.wa2.g13.communication_manager.dtos.CreateMessageDTO
import it.polito.wa2.g13.communication_manager.dtos.Priority
import org.apache.camel.EndpointInject
import org.apache.camel.Exchange
import org.apache.camel.LoggingLevel
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.google.mail.GoogleMailEndpoint
import org.apache.hc.client5.http.HttpHostConnectException
import org.springframework.stereotype.Component

@Component
class GmailRoute(
    private val crmConfig: CrmConfigProperties
) : RouteBuilder() {
    @EndpointInject("google-mail:messages/get")
    lateinit var ep: GoogleMailEndpoint

    override fun configure() {
        onException(HttpHostConnectException::class.java)
            .maximumRedeliveries(0)
            .useOriginalMessage()
            .log(LoggingLevel.ERROR, "Cannot send email from \${headers.CamelGoogleMailStreamFrom} to CRM service. Cause: \${exception.message}")
            .markRollbackOnly()

        from("google-mail-stream:0?markAsRead=true&scopes=https://mail.google.com/")
            .process {
                val id = it.getIn().getHeader("CamelGoogleMailId").toString()
                val message = ep.client.users().messages().get("me", id).execute()

                val subject = message
                    .payload
                    .headers
                    .find { header -> header.name.equals("subject", ignoreCase = true) }
                    ?.get("value")
                    ?.toString()
                    ?: ""

                val from = message
                    .payload
                    .headers
                    .find { header -> header.name.equals("from", ignoreCase = true) }
                    ?.get("value")
                    ?.toString()
                    ?: ""

                it.getIn().body = CreateMessageDTO(
                    sender = from,
                    channel = "email",
                    priority = Priority.Low,
                    subject = subject,
                    body = message.snippet,
                )
            }
            .log(LoggingLevel.INFO, "Sending email from \${body.sender} to CRM service.")
            .marshal().json()
            .setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http.HttpMethods.POST))
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .to("${crmConfig.url}:${crmConfig.port}/API/messages")
    }
}