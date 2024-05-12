package it.polito.wa2.g13.communication_manager.repositories

import it.polito.wa2.g13.communication_manager.data.Email
import org.springframework.data.jpa.repository.JpaRepository

interface EmailRepository: JpaRepository<Email, Long>