package it.polito.wa2.g13.communication_manager.controllers

import it.polito.wa2.g13.communication_manager.configurations.GmailConfigProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/API/emails")
class EmailController(
    val gmailConfigProperties: GmailConfigProperties
) {

    @GetMapping( "")
    fun getEmails(): Any {
        return gmailConfigProperties
    }
}