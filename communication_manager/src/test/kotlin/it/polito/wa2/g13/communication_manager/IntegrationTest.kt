package it.polito.wa2.g13.communication_manager

import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.api.model.Ports
import org.slf4j.LoggerFactory
import org.springframework.test.annotation.DirtiesContext
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.shaded.com.trilead.ssh2.log.Logger.logger
import org.testcontainers.utility.DockerImageName
import org.testcontainers.utility.MountableFile

@Testcontainers
@DirtiesContext
abstract class IntegrationTest {
    companion object {
        const val POSTGRES_PORT = 5432
        const val POSTGRES_LOCAL_PORT = 5555
        val CRM_IMAGE = DockerImageName.parse("wa2group13/crm:0.0.2-SNAPSHOT")
        const val CRM_PORT = 8080
        val DOCUMENT_STORE_IMAGE = DockerImageName.parse("wa2group13/document_store:0.0.2-SNAPSHOT")
        const val DOCUMENT_STORE_PORT = 8080

        @JvmStatic
        val network = Network.newNetwork()!!

        @JvmStatic
        @Container
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16.2")
            .withExposedPorts(POSTGRES_PORT)
            .withEnv("POSTGRES_MULTIPLE_DB", "crm,document_store")
            .withEnv("POSTGRES_MULTIPLE_PASSWORD", "crm,document_store")
            .withCopyFileToContainer(
                MountableFile.forClasspathResource("scripts/init_database.sh"),
                "/docker-entrypoint-initdb.d/init_database.sh"
            )
            .withUsername("test")
            .withPassword("test")
            .withNetwork(network)
            .withNetworkAliases("postgres")
            .withCreateContainerCmdModifier { cmd ->
                cmd.withHostConfig(
                    HostConfig().withPortBindings(
                        PortBinding(
                            Ports.Binding.bindPort(POSTGRES_LOCAL_PORT),
                            ExposedPort(POSTGRES_PORT)
                        )
                    )
                )
            }
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
                .withExposedPorts(8080)
                .withNetwork(network)
                .dependsOn(postgres)
                .withLogConsumer(Slf4jLogConsumer(LoggerFactory.getLogger("DocumentStore")))

    }
}