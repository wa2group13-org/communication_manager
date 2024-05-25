package it.polito.wa2.g13.communication_manager.routes

import com.google.api.services.gmail.model.MessagePart
import com.google.api.services.gmail.model.MessagePartBody
import it.polito.wa2.g13.communication_manager.configurations.CrmConfigProperties
import it.polito.wa2.g13.communication_manager.configurations.DocumentStoreConfigProperties
import it.polito.wa2.g13.communication_manager.dtos.CreateMessageDTO
import it.polito.wa2.g13.communication_manager.dtos.CrmSendMessageResponse
import it.polito.wa2.g13.communication_manager.dtos.Priority
import org.apache.camel.EndpointInject
import org.apache.camel.Exchange
import org.apache.camel.LoggingLevel
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.google.mail.GoogleMailEndpoint
import org.apache.hc.client5.http.HttpHostConnectException
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import org.apache.hc.client5.http.entity.mime.StringBody
import org.apache.hc.core5.http.ContentType
import org.springframework.stereotype.Component

data class Attachment(
    val part: MessagePart,
    val attachment: MessagePartBody,
)

@Component
class GmailRoute(
    private val crmConfig: CrmConfigProperties,
    private val documentStoreConfig: DocumentStoreConfigProperties,
) : RouteBuilder() {
    @EndpointInject("google-mail:messages/get")
    lateinit var ep: GoogleMailEndpoint

    @EndpointInject("google-mail:attachments/get")
    lateinit var attachmentsEp: GoogleMailEndpoint

    override fun configure() {
        onException(HttpHostConnectException::class.java)
            .maximumRedeliveries(0)
            .log(
                LoggingLevel.ERROR,
                "Cannot send email from \${body.sender} to CRM service. Cause: \${exception.message}"
            )
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
            .log(LoggingLevel.INFO, "Sending email from \"\${body.sender}\" to CRM service.")
            .marshal().json()
            .setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http.HttpMethods.POST))
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .to("${crmConfig.url}:${crmConfig.port}/API/messages")
            .unmarshal().json(CrmSendMessageResponse::class.java)
            // Save the `id` of the last created message into the variables
            .process { it.setVariable("messageId", it.getIn().getBody(CrmSendMessageResponse::class.java).id) }
            // Send all the attachments to the document_store service
            .split(variable("attachments"))
            .to("direct:handleAttachments")

        from("direct:handleAttachments")
            .log(
                LoggingLevel.INFO,
                "Sending attachments with name \"\${body.part.filename}\" of Message@\${variables.messageId} to document_store."
            )
            .setVariable("filename", simple("\${body.part.filename}"))
            .process {
                // Get the attachment from the body
                val attachment = it.getIn().getBody(Attachment::class.java)

                // Create the multipart request
                val multipart = MultipartEntityBuilder.create()
                    .setContentType(ContentType.MULTIPART_FORM_DATA)
                    .addBinaryBody(
                        "file",
                        attachment.attachment.decodeData(),
                        ContentType.create(attachment.part.mimeType),
                        attachment.part.filename
                    )
                    .addPart(
                        "messageId",
                        StringBody(it.getVariable("messageId").toString(), ContentType.APPLICATION_JSON)
                    )
                    .build()

                it.getIn().setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http.HttpMethods.POST))
                it.getIn().setHeader(Exchange.CONTENT_TYPE, multipart.contentType)
                it.getIn().body = multipart
            }
            .to("${documentStoreConfig.url}:${documentStoreConfig.port}/API/documents?throwExceptionOnFailure=false")
            .choice()
            .`when`(simple("\${headers.${Exchange.HTTP_RESPONSE_CODE}} >= 400 && \${headers.${Exchange.HTTP_RESPONSE_CODE}} < 500"))
            .log(
                LoggingLevel.ERROR,
                "It wasn't possible to send attachment \"\${variables.filename}\" of Message@\${variables.messageId}. Cause: \${body}"
            )
            .end()
    }
}