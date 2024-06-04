package it.polito.wa2.g13.communication_manager

import org.junit.jupiter.api.AfterEach
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.util.StreamUtils
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import org.testcontainers.utility.MountableFile
import java.nio.charset.Charset

@Testcontainers
@DirtiesContext
@ActiveProfiles("dev", "no-security")
abstract class IntegrationTest {
    companion object {
        private const val POSTGRES_PORT = 5432
        private val CRM_IMAGE = DockerImageName.parse("wa2group13/crm:0.0.2-SNAPSHOT")
        private const val CRM_PORT = 8080
        private val DOCUMENT_STORE_IMAGE = DockerImageName.parse("wa2group13/document_store:0.0.2-SNAPSHOT")
        private const val DOCUMENT_STORE_PORT = 8080
        private val dbs = listOf("crm", "document_store")
        private val passwords = listOf("crm", "document_store")

        @JvmStatic
         val network = Network.newNetwork()!!

        @JvmStatic
        @Container
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16.2")
            .withExposedPorts(POSTGRES_PORT)
            .withEnv("POSTGRES_MULTIPLE_DB", dbs.joinToString(","))
            .withEnv("POSTGRES_MULTIPLE_PASSWORD", passwords.joinToString(","))
            .withCopyFileToContainer(
                MountableFile.forClasspathResource("scripts/init_database.sh"),
                "/docker-entrypoint-initdb.d/init_database.sh"
            )
            .withUsername("test")
            .withPassword("test")
            .withNetwork(network)
            .withNetworkAliases("postgres")
            .withLogConsumer(Slf4jLogConsumer(LoggerFactory.getLogger("Postgres")))

        @JvmStatic
        @Container
        val crm: GenericContainer<*> = GenericContainer(CRM_IMAGE)
            .withEnv("POSTGRES_URL", "postgres")
            .withEnv("POSTGRES_PORT", POSTGRES_PORT.toString())
            .withEnv("POSTGRES_DB", "crm")
            .withEnv("POSTGRES_USERNAME", "crm")
            .withEnv("POSTGRES_PASSWORD", "crm")
            .withEnv("SPRING_PROFILES_ACTIVE", "prod")
            .withExposedPorts(CRM_PORT)
            .withNetwork(network)
            .dependsOn(postgres)
            .withLogConsumer(Slf4jLogConsumer(LoggerFactory.getLogger("Crm")))

        @JvmStatic
        @Container
        val documentStore: GenericContainer<*> =
            GenericContainer(DOCUMENT_STORE_IMAGE)
                .withEnv("POSTGRES_URL", "postgres")
                .withEnv("POSTGRES_PORT", POSTGRES_PORT.toString())
                .withEnv("POSTGRES_DB", "document_store")
                .withEnv("POSTGRES_USERNAME", "document_store")
                .withEnv("POSTGRES_PASSWORD", "document_store")
                .withEnv("SPRING_PROFILES_ACTIVE", "prod")
                .withEnv("MULTIPART_MAX_FILE_SIZE", "10MB")
                .withEnv("MULTIPART_MAX_REQUEST_SIZE", "10MB")
                .withExposedPorts(DOCUMENT_STORE_PORT)
                .withNetwork(network)
                .dependsOn(postgres)
                .withLogConsumer(Slf4jLogConsumer(LoggerFactory.getLogger("DocumentStore")))

        @DynamicPropertySource
        @JvmStatic
        @Suppress("unused")
        fun crmProperties(
            registry: DynamicPropertyRegistry
        ) {
            registry.add("crm.port") { crm.getMappedPort(8080) }
        }

        @DynamicPropertySource
        @JvmStatic
        @Suppress("unused")
        fun documentStoreProperties(
            registry: DynamicPropertyRegistry
        ) {
            registry.add("document_store.port") { documentStore.getMappedPort(8080) }
        }

    }

    @AfterEach
    fun clearDb() {
        val sql =
            StreamUtils.copyToString(ClassPathResource("scripts/clean_db.sql").inputStream, Charset.defaultCharset())

        dbs.zip(passwords).forEach {
            postgres.execInContainer(
                "psql -U ${it.first} -W ${it.second} -c \"$sql\""
            )
        }
    }
}