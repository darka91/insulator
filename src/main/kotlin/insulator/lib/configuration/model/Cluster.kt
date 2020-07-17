package insulator.lib.configuration.model

import java.util.*

data class Cluster(
        val guid: UUID,
        val name: String,
        val endpoint: String,

        val useSSL: Boolean = false,
        val sslConfiguration: SslConfiguration? = null,

        val useSasl: Boolean = false,
        val saslConfiguration: SaslConfiguration? = null,

        val schemaRegistryConfig: SchemaRegistryConfiguration? = null
)

data class SslConfiguration(
        val sslTruststoreLocation: String? = null,
        val sslTruststorePassword: String? = null,
        val sslKeystoreLocation: String? = null,
        val sslKeyStorePassword: String? = null
)

data class SaslConfiguration(
        val saslUsername: String? = null,
        val saslPassword: String? = null
)

data class SchemaRegistryConfiguration(
        val url: String? = null,
        val username: String? = null,
        val password: String? = null
)