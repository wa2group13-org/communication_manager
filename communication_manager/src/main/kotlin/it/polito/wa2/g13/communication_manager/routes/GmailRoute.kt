package it.polito.wa2.g13.communication_manager.routes

import com.google.api.services.gmail.model.Message
import com.google.api.services.gmail.model.MessagePart
import com.google.api.services.gmail.model.MessagePartBody
import it.polito.wa2.g13.communication_manager.dtos.CreateAttachmentDTO
import it.polito.wa2.g13.communication_manager.dtos.CreateMessageDTO
import it.polito.wa2.g13.communication_manager.dtos.Priority
import it.polito.wa2.g13.communication_manager.kafka.KafkaSender
import org.apache.camel.EndpointInject
import org.apache.camel.LoggingLevel
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.google.mail.GoogleMailEndpoint
import org.apache.camel.component.google.mail.stream.GoogleMailStreamConstants
import org.springframework.stereotype.Component

data class Attachment(
    val part: MessagePart,
    val attachment: MessagePartBody,
)

@Component
class GmailRoute(
    private val kafkaSender: KafkaSender,
) : RouteBuilder() {
    companion object {
        const val MAIN_ROUTE_ID = "receiveEmailFromGmail"
    }

    /** Direct routes names */
    object Direct {
        const val GET_ATTACHMENTS = "direct:getAttachments"
        const val GET_GMAIL_MESSAGE = "direct:getGmailMessage"
        const val SEND_MESSAGE_TO_CRM = "direct:sendMessageToCrm"
        const val SEND_ATTACHMENT_TO_DOCUMENT_STORE = "direct:sendAttachmentToDocumentStore"
    }

    @EndpointInject("google-mail:messages/get")
    lateinit var messagesEp: GoogleMailEndpoint

    @EndpointInject("google-mail:attachments/get")
    lateinit var attachmentsEp: GoogleMailEndpoint

    override fun configure() {
        onException(Exception::class.java)
            .handled(true)
            .to("direct:handleException")

        from("direct:handleException")
            .log(
                LoggingLevel.ERROR,
                "Cannot send email@\${headers.${GoogleMailStreamConstants.MAIL_ID}} to Kafka. Cause: \${exception.message}"
            )

        from("google-mail-stream:0?markAsRead=true&scopes=https://mail.google.com/")
            .routeId(MAIN_ROUTE_ID)
            .log("Started processing mail with id: \${headers.${GoogleMailStreamConstants.MAIL_ID}}")
            // Set the current message read from the stream
            .to(Direct.GET_GMAIL_MESSAGE)
            .to(Direct.GET_ATTACHMENTS)
            .to(Direct.SEND_MESSAGE_TO_CRM)
            // Send all the attachments to the document_store service
            .split(variable("attachments"))
            .to(Direct.SEND_ATTACHMENT_TO_DOCUMENT_STORE)
            .end()
            .log("Processing email from \"\${variable.sender}\" terminated.")

        // Get the email from gmail
        from(Direct.GET_GMAIL_MESSAGE)
            .process {
                it.getIn().body = messagesEp.client.users().messages()
                    .get("me", it.getIn().getHeader(GoogleMailStreamConstants.MAIL_ID).toString()).execute()
            }

        // Get attachments from gmail
        from(Direct.GET_ATTACHMENTS)
            // Get the attachments of the current message and put them in a variable
            .process {
                val message = it.getIn().getBody(Message::class.java)

                val parts = message
                    .payload
                    ?.parts
                    ?.filter { part -> part?.body?.attachmentId != null }
                    ?: listOf()

                val attachments = parts
                    .map { part ->
                        Attachment(
                            part = part,
                            attachment = attachmentsEp.client.users().messages().attachments()
                                .get("me", message.id, part.body.attachmentId)
                                .execute()
                        )
                    }

                it.setVariable("attachments", attachments)
            }

        // Send message to crm
        from(Direct.SEND_MESSAGE_TO_CRM)
            .process {
                val message = it.getIn().getBody(Message::class.java)

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
                    mailId = it.getIn().getHeader(GoogleMailStreamConstants.MAIL_ID).toString(),
                )

                it.setVariable("sender", from)
            }
            .log(LoggingLevel.INFO, "Sending email from \"\${body.sender}\" to Kafka.")
            .bean(kafkaSender, kafkaSender::sendMessage.name)

        // Send attachment to document store
        from(Direct.SEND_ATTACHMENT_TO_DOCUMENT_STORE)
            .log(
                LoggingLevel.INFO,
                "Sending attachments with name \"\${body.part.filename}\" of mail \${headers.${GoogleMailStreamConstants.MAIL_ID}} to Kafka."
            )
            .setVariable("filename", simple("\${body.part.filename}"))
            .process {
                // Get the attachment from the body
                val attachment = it.getIn().getBody(Attachment::class.java)

                // Create the multipart request
                it.getIn().body = CreateAttachmentDTO(
                    bytes = attachment.attachment.decodeData(),
                    contentType = attachment.part.mimeType,
                    filename = attachment.part.filename,
                    attachmentId = attachment.part.body.attachmentId,
                    mailId = it.getIn().getHeader(GoogleMailStreamConstants.MAIL_ID).toString(),
                )
            }
            .bean(kafkaSender, kafkaSender::sendAttachment.name)
    }
}