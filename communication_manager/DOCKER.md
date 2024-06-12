# Communication Manager

- Repository: [GitHub](https://github.com/polito-WAII-2024/lab4-g13)

# Environmental variables

- `APPLICATION_NAME`: Name of the Google application to connect
- `CLIENT_ID`: Client id given by the [Google API for Gmail][gmail-api]
- `CLIENT_SECRET`: Client secret given by the [Google API for Gmail][gmail-api]
- `REFRESH_TOKEN`: Refresh token for the application
- `SPRING_PROFILES_ACTIVE`: Spring comma-separated profiles. Default: `dev`, `no-security`. Values:
    - `dev`: logging
    - `prod`: production environment
    - `no-security`: disable security filter chains
    - `no-gmail`: disable Google Mail fetch
- `KAFKA_PRODUCER_BOOTSTRAP_SERVERS`: Comma-separated list of `host:port` pairs to use for establishing the initial
  connections to the Kafka cluster.
- `KAFKA_PRODUCER_MAX_REQUEST_SIZE`: How big the request can be to Kafka. Default: `10485760`
- `KAFKA_PRODUCER_MESSAGE_MAX_BYTES`: How big a single message can be sent to Kafka. Default: `10485760`
- `OPENAPI_BASE_URL`: base url of this service that will appear in the OpenAPI documentation.
  Default `http://localhost:${PORT}`
- `PORT`: server port. Default: `8080`

[crm]: https://hub.docker.com/r/wa2group13/crm

[ds]: https://console.cloud.google.com/apis/library/gmail.googleapis.com

[gmail-api]: https://console.cloud.google.com/apis/library/gmail.googleapis.com