package uk.nhs.england.qedm.provider

import ca.uhn.fhir.rest.annotation.*
import ca.uhn.fhir.rest.param.*
import org.hl7.fhir.r4.model.*
import org.springframework.stereotype.Component
import uk.nhs.england.qedm.awsProvider.AWSPatient
import uk.nhs.england.qedm.interceptor.CognitoAuthInterceptor


import jakarta.servlet.http.HttpServletRequest

@Component
class EpisodeOfCarePlainProvider(var cognitoAuthInterceptor: CognitoAuthInterceptor,
                                 var awsPatient: AWSPatient
)  {



    @Search(type=EpisodeOfCare::class)
    fun search(
        httpRequest : HttpServletRequest,
        @OptionalParam(name = EpisodeOfCare.SP_DATE) date: DateRangeParam?,
        @OptionalParam(name = EpisodeOfCare.SP_PATIENT) patient: ReferenceParam?,
        @OptionalParam(name = EpisodeOfCare.SP_STATUS) status: TokenOrListParam?,
        @OptionalParam(name = EpisodeOfCare.SP_IDENTIFIER)  identifier :TokenParam?,
        @OptionalParam(name = "patient:identifier") nhsNumber : TokenParam?,
        @OptionalParam(name = EpisodeOfCare.SP_RES_ID)  resid : StringParam?
    ): Bundle? {

        val queryString = awsPatient.processQueryString(httpRequest.queryString,nhsNumber)

        val resource: Resource? = cognitoAuthInterceptor.readFromUrl(httpRequest.pathInfo, queryString,"EpisodeOfCare")
        if (resource != null && resource is Bundle) {
            return resource
        }

        return null
    }
}
