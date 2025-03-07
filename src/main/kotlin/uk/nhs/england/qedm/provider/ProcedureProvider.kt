package uk.nhs.england.qedm.provider

import ca.uhn.fhir.rest.annotation.IdParam
import ca.uhn.fhir.rest.annotation.OptionalParam
import ca.uhn.fhir.rest.annotation.Read
import ca.uhn.fhir.rest.annotation.Search
import ca.uhn.fhir.rest.param.*
import ca.uhn.fhir.rest.server.IResourceProvider
import org.hl7.fhir.r4.model.*
import org.springframework.stereotype.Component
import uk.nhs.england.qedm.awsProvider.AWSPatient
import uk.nhs.england.qedm.interceptor.CognitoAuthInterceptor
import jakarta.servlet.http.HttpServletRequest

@Component
class ProcedureProvider(var cognitoAuthInterceptor: CognitoAuthInterceptor,
    val awsPatient: AWSPatient) {


    @Read(type=Procedure::class)
    fun read(httpRequest : HttpServletRequest, @IdParam internalId: IdType): Procedure? {
        val resource: Resource? = cognitoAuthInterceptor.readFromUrl(httpRequest.pathInfo, null,null)
        return if (resource is Procedure) resource else null
    }

    @Search(type=Procedure::class)
    fun search(
        httpRequest : HttpServletRequest,
        @OptionalParam(name = Procedure.SP_PATIENT) patient : ReferenceParam?,
        @OptionalParam(name = "patient:identifier") nhsNumber : TokenParam?,
        @OptionalParam(name = Procedure.SP_DATE)  date : DateRangeParam?,
        @OptionalParam(name = Procedure.SP_IDENTIFIER)  identifier :TokenParam?,
        @OptionalParam(name = Procedure.SP_STATUS)  status: TokenOrListParam?,
        @OptionalParam(name = Procedure.SP_RES_ID)  resid : StringParam?,
        @OptionalParam(name = "_getpages")  pages : StringParam?,
        @OptionalParam(name = "_count")  count : StringParam?
    ): Bundle? {
        val queryString = awsPatient.processQueryString(httpRequest.queryString,nhsNumber)

        val resource: Resource? = cognitoAuthInterceptor.readFromUrl(httpRequest.pathInfo, queryString,"Procedure")
        if (resource != null && resource is Bundle) {
            return resource
        }

        return null
    }
}
