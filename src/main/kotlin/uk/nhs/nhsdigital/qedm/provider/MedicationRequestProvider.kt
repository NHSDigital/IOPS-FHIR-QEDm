package uk.nhs.nhsdigital.qedm.provider

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
import uk.nhs.nhsdigital.qedm.interceptor.CognitoAuthInterceptor
import javax.servlet.http.HttpServletRequest

@Component
class MedicationRequestProvider(var cognitoAuthInterceptor: CognitoAuthInterceptor) : IResourceProvider {
    override fun getResourceType(): Class<MedicationRequest> {
        return MedicationRequest::class.java
    }

    @Read
    fun read(httpRequest : HttpServletRequest, @IdParam internalId: IdType): MedicationRequest? {
        val resource: Resource? = cognitoAuthInterceptor.readFromUrl(httpRequest.pathInfo, null)
        return if (resource is MedicationRequest) resource else null
    }

    @Search
    fun search(
        httpRequest : HttpServletRequest,
        @OptionalParam(name = MedicationRequest.SP_PATIENT) patient : ReferenceParam?,
        @OptionalParam(name = MedicationRequest.SP_AUTHOREDON)  date : DateRangeParam?,
        @OptionalParam(name = MedicationRequest.SP_IDENTIFIER)  identifier :TokenParam?,
        @OptionalParam(name = MedicationRequest.SP_STATUS)  status :TokenParam?,
        @OptionalParam(name = MedicationRequest.SP_RES_ID)  resid : StringParam?
    ): List<MedicationRequest> {
        val medicationRequests = mutableListOf<MedicationRequest>()
        val resource: Resource? = cognitoAuthInterceptor.readFromUrl(httpRequest.pathInfo, httpRequest.queryString)
        if (resource != null && resource is Bundle) {
            for (entry in resource.entry) {
                if (entry.hasResource() && entry.resource is MedicationRequest) medicationRequests.add(entry.resource as MedicationRequest)
            }
        }

        return medicationRequests
    }
}
