package uk.nhs.england.qedm.provider

import ca.uhn.fhir.rest.annotation.IdParam
import ca.uhn.fhir.rest.annotation.OptionalParam
import ca.uhn.fhir.rest.annotation.Read
import ca.uhn.fhir.rest.annotation.Search
import ca.uhn.fhir.rest.param.DateRangeParam
import ca.uhn.fhir.rest.param.ReferenceParam
import ca.uhn.fhir.rest.param.StringParam
import ca.uhn.fhir.rest.param.TokenParam
import ca.uhn.fhir.rest.server.IResourceProvider
import org.hl7.fhir.r4.model.*
import org.springframework.stereotype.Component
import uk.nhs.england.qedm.awsProvider.AWSPatient
import uk.nhs.england.qedm.interceptor.CognitoAuthInterceptor
import javax.servlet.http.HttpServletRequest

@Component
class MedicationRequestProvider(var cognitoAuthInterceptor: CognitoAuthInterceptor,
    val awsPatient: AWSPatient)  {


    @Read(type=MedicationRequest::class)
    fun read(httpRequest : HttpServletRequest, @IdParam internalId: IdType): MedicationRequest? {
        val resource: Resource? = cognitoAuthInterceptor.readFromUrl(httpRequest.pathInfo, null,"MedicationRequest")
        return if (resource is MedicationRequest) resource else null
    }

    @Search(type=MedicationRequest::class)
    fun search(
        httpRequest : HttpServletRequest,
        @OptionalParam(name = MedicationRequest.SP_PATIENT) patient : ReferenceParam?,
        @OptionalParam(name = "patient:identifier") nhsNumber : TokenParam?,
        @OptionalParam(name = MedicationRequest.SP_AUTHOREDON)  date : DateRangeParam?,
        @OptionalParam(name = MedicationRequest.SP_IDENTIFIER)  identifier :TokenParam?,
        @OptionalParam(name = MedicationRequest.SP_STATUS)  status :TokenParam?,
        @OptionalParam(name = MedicationRequest.SP_RES_ID)  resid : StringParam?,
        @OptionalParam(name = "_getpages")  pages : StringParam?,
        @OptionalParam(name = "_count")  count : StringParam?
    ): Bundle? {
        val queryString = awsPatient.processQueryString(httpRequest.queryString,nhsNumber)
        val resource: Resource? = cognitoAuthInterceptor.readFromUrl(httpRequest.pathInfo, queryString,"MedicationRequest")
        if (resource != null && resource is Bundle) {
            return resource
        }

        return null
    }
}
