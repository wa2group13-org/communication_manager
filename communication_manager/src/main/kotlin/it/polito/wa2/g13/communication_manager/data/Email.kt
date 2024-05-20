package it.polito.wa2.g13.communication_manager.data

import jakarta.persistence.Entity

@Entity
class EmailCManager(
    var fromm: String,
    var subject: String,
    var snippet: String,
) : BaseEntity()