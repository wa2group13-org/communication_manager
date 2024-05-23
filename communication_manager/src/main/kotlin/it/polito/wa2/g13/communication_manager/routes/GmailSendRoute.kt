package it.polito.wa2.g13.communication_manager.routes

import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.services.gmail.model.Message
import it.polito.wa2.g13.communication_manager.dtos.CreateEmailDTO
import jakarta.mail.Session
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import org.apache.camel.EndpointInject
import org.apache.camel.LoggingLevel
import org.apache.camel.RuntimeCamelException
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.google.mail.GoogleMailEndpoint
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Component
class GmailSendRoute : RouteBuilder() {
    @EndpointInject("google-mail:messages/send")
    lateinit var ep: GoogleMailEndpoint

    @OptIn(ExperimentalEncodingApi::class)
    override fun configure() {
        onException(GoogleJsonResponseException::class.java)
            .log(LoggingLevel.ERROR, "Failed to send message to \${body.recipient}")
            .handled(false)

        from("direct:sendMail")
            .doTry()
            .process {
                val message = it.getIn().getBody(CreateEmailDTO::class.java)
                val sender = "it.polito.wa2.g13@polito.it"

                val mimeMessage = MimeMessage(Session.getDefaultInstance(Properties())).apply {
                    setFrom(InternetAddress(sender))
                    addRecipient(
                        jakarta.mail.Message.RecipientType.TO, InternetAddress(
                            message.recipient
                        )
                    )
                    subject = message.subject
                    setText(message.body)
                }
                val userId = "me"

                val stream = ByteArrayOutputStream()
                mimeMessage.writeTo(stream)

                val b64Message = Message().apply {
                    this.raw = Base64.UrlSafe.encode(stream.toByteArray())
                }

                it.getIn().apply {
                    setHeader("CamelGoogleMail.userId", userId)
                    setHeader("CamelGoogleMail.content", b64Message)
                }

            }
            .log(LoggingLevel.INFO, "Sending email to \${body.recipient}")
            .to("google-mail:messages/send?scopes=https://mail.google.com/")
            .doCatch(RuntimeCamelException::class.java)
            .log(LoggingLevel.ERROR, "Cannot send email to \${body.recipient}")
            .end()
    }
}