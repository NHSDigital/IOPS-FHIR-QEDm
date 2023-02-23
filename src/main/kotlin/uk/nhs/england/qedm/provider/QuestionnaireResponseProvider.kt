package uk.nhs.england.qedm.provider

import ca.uhn.fhir.rest.annotation.*
import ca.uhn.fhir.rest.param.ReferenceParam
import ca.uhn.fhir.rest.param.TokenParam
import ca.uhn.fhir.rest.server.IResourceProvider
import org.hl7.fhir.r4.model.*
import org.springframework.stereotype.Component
import uk.nhs.england.qedm.awsProvider.AWSQuestionnaireResponse
import uk.nhs.england.qedm.interceptor.CognitoAuthInterceptor
import javax.servlet.http.HttpServletRequest

@Component
class QuestionnaireResponseProvider(
    var cognitoAuthInterceptor: CognitoAuthInterceptor,
    var awsQuestionnaireResponse: AWSQuestionnaireResponse
) : IResourceProvider {
    override fun getResourceType(): Class<QuestionnaireResponse> {
        return QuestionnaireResponse::class.java
    }

    @Search
    fun search(httpRequest : HttpServletRequest,
               @OptionalParam(name = QuestionnaireResponse.SP_PATIENT) patient: ReferenceParam?,
               @OptionalParam(name = QuestionnaireResponse.SP_QUESTIONNAIRE) questionnaire : ReferenceParam?,
               @OptionalParam(name= QuestionnaireResponse.SP_STATUS) status : TokenParam?
    ): List<QuestionnaireResponse>? {
        return awsQuestionnaireResponse.seach(patient,questionnaire,status)
    }

}
