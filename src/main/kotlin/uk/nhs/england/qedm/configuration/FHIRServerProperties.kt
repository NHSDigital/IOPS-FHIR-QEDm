package uk.nhs.england.qedm.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "fhir")
data class FHIRServerProperties(
    var server: uk.nhs.england.qedm.configuration.FHIRServerProperties.Server
) {
    data class Server(
        var baseUrl: String,
        var name: String,
        var version: String
    )
}
