package it.polito.wa2.g13.communication_manager.dtos

class CreateEmailDTO(
    val recipient: String,
    val subject: String,
    val body: String,
)