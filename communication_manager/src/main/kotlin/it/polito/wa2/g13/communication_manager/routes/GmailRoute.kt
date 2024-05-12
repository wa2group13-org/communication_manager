package it.polito.wa2.g13.communication_manager.routes

import it.polito.wa2.g13.communication_manager.configurations.GmailConfigProperties
import it.polito.wa2.g13.communication_manager.data.Email
import org.apache.camel.EndpointInject
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.google.mail.GoogleMailEndpoint
import org.springframework.stereotype.Component

@Component
class GmailRoute : RouteBuilder() {
    @EndpointInject("google-mail:MESSAGES/get")
    lateinit var ep: GoogleMailEndpoint

    override fun configure() {
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

                it.getIn().body = Email(fromm = from, subject = subject, snippet = message.snippet)
            }
            .to("bean:emailRepository?method=save")
    }
}