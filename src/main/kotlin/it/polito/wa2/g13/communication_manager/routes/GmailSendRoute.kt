package it.polito.wa2.g13.communication_manager.routes

import com.google.api.services.gmail.model.Message
import it.polito.wa2.g13.communication_manager.dtos.CreateEmailDTO
import jakarta.mail.Session
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import org.apache.camel.LoggingLevel
import org.apache.camel.builder.RouteBuilder
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Component
@Profile("!no-gmail")
class GmailSendRoute : RouteBuilder() {

    @OptIn(ExperimentalEncodingApi::class)
    override fun configure() {
        errorHandler(deadLetterChannel("log:dead?level=ERROR")
            .maximumRedeliveries(3)
            .redeliveryDelay(5000)
            .onRedelivery { log.warn("Redelivering email to ${it.getIn().getBody(CreateEmailDTO::class.java).recipient}") })

        from("direct:sendMail")
            .process {
                val message = it.getIn().getBody(CreateEmailDTO::class.java)
                val sender = "it.polito.wa2.g13@gmail.com"

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
    }
}