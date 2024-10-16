package it.polito.wa2.g13.communication_manager.controllers

import it.polito.wa2.g13.communication_manager.dtos.CreateEmailDTO
import org.apache.camel.CamelContext
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.ExchangeBuilder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/API/emails")
class EmailController(
    val camelContext: CamelContext,
    val producerTemplate: ProducerTemplate
) {
    @PostMapping("")
    fun sendEmail(@RequestBody message: CreateEmailDTO): Any {
        val exchange = ExchangeBuilder.anExchange(camelContext).withBody(message).build()
        producerTemplate.send("direct:sendMail", exchange)
        return exchange.getIn().body
    }
}