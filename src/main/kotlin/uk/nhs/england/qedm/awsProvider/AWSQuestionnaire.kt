package uk.nhs.england.qedm.awsProvider

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.api.MethodOutcome
import ca.uhn.fhir.rest.client.api.IGenericClient
import ca.uhn.fhir.rest.param.UriParam
import org.hl7.fhir.instance.model.api.IBaseBundle
import org.hl7.fhir.r4.model.*
import org.hl7.fhir.r4.model.Questionnaire
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

import java.nio.charset.StandardCharsets
import java.util.*

@Component
class AWSQuestionnaire (val awsClient: IGenericClient,
               //sqs: AmazonSQS?,
                        @Qualifier("R4") val ctx: FhirContext) {


    private val log = LoggerFactory.getLogger("FHIRAudit")

    fun search(uriParam: UriParam?): List<Questionnaire>? {
        var awsBundle: Bundle? = null
        val list = mutableListOf<Questionnaire>()
        var response: MethodOutcome? = null
       // if (uriParam == null || uriParam.value == null) throw UnprocessableEntityException("url parameter is mandatory")
        var retry = 3
        while (retry > 0) {
            try {
                if (uriParam == null || uriParam.value == null) {
                    awsBundle = awsClient!!.search<IBaseBundle>().forResource(Questionnaire::class.java)
                        .returnBundle(Bundle::class.java)
                        .execute()
                } else {
                    val decodeUrl = java.net.URLDecoder.decode(uriParam.value, StandardCharsets.UTF_8.name())
                    awsBundle = awsClient!!.search<IBaseBundle>().forResource(Questionnaire::class.java)
                        .where(
                            Questionnaire.URL.matches().value(decodeUrl)
                        )
                        .returnBundle(Bundle::class.java)
                        .execute()
                }
                break
            } catch (ex: Exception) {
                // do nothing
                log.error(ex.message)
                retry--
                if (retry == 0) throw ex
            }
        }
        if (awsBundle != null) {
            if (awsBundle.hasEntry() ) {
                for (entry in awsBundle.entry) {
                    if (entry.hasResource() && entry.resource is Questionnaire) {
                        list.add(entry.resource as Questionnaire)
                    }
                }
            }
        }
        return list
    }



}
