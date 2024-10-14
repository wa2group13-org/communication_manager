package it.polito.wa2.g13.communication_manager.dtos

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class CrmSendMessageResponse @JsonCreator constructor(
    @JsonProperty("id")
    val id: Long,
    @JsonProperty("sender")
    val sender: String,
    @JsonProperty("channel")
    val channel: String,
    @JsonProperty("priority")
    val priority: String,
    @JsonProperty("subject")
    val subject: String?,
    @JsonProperty("body")
    val body: String?,
    @JsonProperty("status")
    val status: String,
    @JsonProperty("date")
    val date: String,
)
