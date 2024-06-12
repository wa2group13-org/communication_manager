package it.polito.wa2.g13.communication_manager.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration for the Apache Camel routes, these variables will be used to
 * set up the connection with the Gmail Services.
 */
@ConfigurationProperties(prefix = "gmail")
class GmailConfigProperties {
    /**
     * Application name of the Google application.
     */
    lateinit var applicationName: String

    /**
     * ClientID of the [applicationName], it can be found in the
     * [Google API page](https://console.cloud.google.com/apis/library/gmail.googleapis.com)
     */
    lateinit var clientId: String

    /**
     * ClientSecret of the [applicationName], it can be found in the
     * [Google API page](https://console.cloud.google.com/apis/library/gmail.googleapis.com)
     */
    lateinit var clientSecret: String

    /**
     * Refresh token for the email to access the Google Gmail services, it can be generated on the
     * [Google Playground page][https://developers.google.com/oauthplayground/]
     */
    lateinit var refreshToken: String
}