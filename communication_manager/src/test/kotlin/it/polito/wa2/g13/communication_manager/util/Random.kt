package it.polito.wa2.g13.communication_manager.util

import com.google.api.services.gmail.model.Message
import com.google.api.services.gmail.model.MessagePart
import com.google.api.services.gmail.model.MessagePartBody
import com.google.api.services.gmail.model.MessagePartHeader
import it.polito.wa2.g13.communication_manager.routes.Attachment
import org.springframework.util.MimeTypeUtils
import java.util.*

fun randomMessage(): Message = Message().apply {
    this.payload = MessagePart().apply {
        this.headers = listOf(
            MessagePartHeader().apply {
                this.name = "Subject"
                this.value = UUID.randomUUID().toString()
            },
            MessagePartHeader().apply {
                this.name = "From"
                this.value = UUID.randomUUID().toString()
            }
        )
    }

    this.snippet = UUID.randomUUID().toString()
}

fun randomAttachment(): Attachment = Attachment(
    part = MessagePart().apply {
        this.filename = UUID.randomUUID().toString()
        this.mimeType = MimeTypeUtils.ALL_VALUE
    },
    attachment = MessagePartBody().apply {
        this.data = Base64.getUrlEncoder().encodeToString(UUID.randomUUID().toString().toByteArray())
    },
)

fun randomAttachments(n: Int): List<Attachment> = generateSequence { randomAttachment() }
    .take(n)
    .toList()
