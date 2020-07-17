package insulator.views.configurations

import insulator.viewmodel.configurations.ClusterViewModel
import tornadofx.*

class ClusterView : Fragment("Add cluster") {
    private val viewModel: ClusterViewModel by inject()

    override val root = form {
        fieldset {
            field("Cluster name") { textfield(viewModel.nameProperty).required() }
            field("Endpoint (url:port)") { textfield(viewModel.endpointProperty).required() }
            fieldset {
                disableWhen(viewModel.useSaslProperty)
                field("Use SSL (Aiven)") { checkbox(property = viewModel.useSSLProperty) }
                field("SSL Truststore Location") { textfield(viewModel.sslTruststoreLocationProperty).requiredWhen(viewModel.useSSLProperty) }
                field("SSL Truststore Password") { textfield(viewModel.sslTruststorePasswordProperty).requiredWhen(viewModel.useSSLProperty) }
                field("SSL Keystore Location") { textfield(viewModel.sslKeystoreLocationProperty).requiredWhen(viewModel.useSSLProperty) }
                field("SSL KeyStore Password") { textfield(viewModel.sslKeyStorePasswordProperty).requiredWhen(viewModel.useSSLProperty) }
            }
            fieldset {
                disableWhen(viewModel.useSSLProperty)
                field("Use SASL (ConfluenceCloud)") { checkbox(property = viewModel.useSaslProperty) }
                field("Username") { textfield(viewModel.saslUsernameProperty).requiredWhen(viewModel.useSaslProperty) }
                field("Password") { textfield(viewModel.saslPasswordProperty).requiredWhen(viewModel.useSaslProperty) }
            }
            buttonbar {
                button("Test connection") { isDisable = true }
                button("Save") {
                    enableWhen(viewModel.valid)
                    action {
                        viewModel.commit()
                        viewModel.save()
                        viewModel.rollback()
                        close()
                    }
                }
            }
        }
    }

}