# Communication Manager

- Repository: [GitHub](https://github.com/polito-WAII-2024/lab4-g13)

# Environmental variables

- `CRM_URL`: Url of the [CRM Service][crm]
- `CRM_PORT`: Port of the [CRM Service][crm]
- `DOCUMENT_STORE_URL`: Url the of the [Document Store Service][ds]
- `DOCUMENT_STORE_PORT`: Port of the [Document Store Service][ds]
- `APPLICATION_NAME`: Name of the Google application to connect
- `CLIENT_ID`: Client id given by the [Google API for Gmail][gmail-api]
- `CLIENT_SECRET`: Client secret given by the [Google API for Gmail][gmail-api]
- `REFRESH_TOKEN`: Refresh token for the application
- `SPRING_PROFILES_ACTIVE`: Spring comma-separated profiles, the only two profiles available are `dev` and `prod`, by default none is selected




[crm]: https://hub.docker.com/r/wa2group13/crm
[ds]: https://console.cloud.google.com/apis/library/gmail.googleapis.com
[gmail-api]: https://console.cloud.google.com/apis/library/gmail.googleapis.com