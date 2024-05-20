package it.polito.wa2.g13.communication_manager.routes

import it.polito.wa2.g13.communication_manager.dtos.CreateMessageDTO
import it.polito.wa2.g13.communication_manager.dtos.Priority
import org.apache.camel.EndpointInject
import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.google.mail.GoogleMailEndpoint
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class GmailRoute : RouteBuilder() {
    companion object {
        private val logger = LoggerFactory.getLogger(GmailRoute::class.java)
    }

    @EndpointInject("google-mail:messages/get")
    lateinit var ep: GoogleMailEndpoint

//    @EndpointInject("google-mail:attachments/get")
//    lateinit var attachmentsEp: GoogleMailEndpoint

    override fun configure() {
        from("google-mail-stream:0?markAsRead=true&scopes=https://mail.google.com/")
            .process {
                val id = it.getIn().getHeader("CamelGoogleMailId").toString()
                val message = ep.client.users().messages().get("me", id).execute()

//                val attachmentId = message
//                    .payload
//                    .parts
//                    .find { part -> part.body.attachmentId != null }
//                    ?.body
//                    ?.attachmentId
//
//                logger.info(it.getIn().headers.toString())
//                logger.info(attachmentId)
//                val attachment =
//                    attachmentsEp.client.users().messages().attachments().get("me", message.id, attachmentId).execute()
//                logger.info(attachment.toString())

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
            .log("Sending email from \${body.sender} to CRM service.")
            .marshal().json()
            .setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http.HttpMethods.POST))
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .to("http://localhost:8081/API/messages")
//            .to("bean:emailRepository?method=save")
    }
}