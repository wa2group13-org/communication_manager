package it.polito.wa2.g13.communication_manager.dtos

enum class Priority {
    Low,
    Medium,
    High,
}

data class CreateMessageDTO(
    val sender: String,
    val channel: String,
    val priority: Priority,
    val subject: String?,
    val body: String?,
)