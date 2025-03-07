package uk.nhs.england.qedm.provider

import ca.uhn.fhir.rest.annotation.*
import ca.uhn.fhir.rest.api.MethodOutcome
import ca.uhn.fhir.rest.api.server.RequestDetails
import ca.uhn.fhir.rest.param.DateRangeParam
import ca.uhn.fhir.rest.param.StringParam
import ca.uhn.fhir.rest.param.TokenParam
import ca.uhn.fhir.rest.server.IResourceProvider
import org.hl7.fhir.r4.model.*
import org.springframework.stereotype.Component
import uk.nhs.england.qedm.awsProvider.AWSPatient
import uk.nhs.england.qedm.interceptor.CognitoAuthInterceptor
import jakarta.servlet.http.HttpServletRequest

@Component
class PatientSearchProvider(var cognitoAuthInterceptor: CognitoAuthInterceptor, val awsPatient: AWSPatient) {

    @Create
    fun create(
        theRequest: HttpServletRequest,
        @ResourceParam patient: Patient,
    ): MethodOutcome? {
        return awsPatient.create(patient)
    }

    @Update
    fun update(
        theRequest: HttpServletRequest,
        @ResourceParam patient: Patient,
        @IdParam theId: IdType?,
        @ConditionalUrlParam theConditional : String?,
        theRequestDetails: RequestDetails?
    ): MethodOutcome? {

        return awsPatient.update(patient, theId)

    }
    @Read(type=Patient::class)
    fun read( httpRequest : HttpServletRequest,@IdParam internalId: IdType): Patient? {
        val resource: Resource? = cognitoAuthInterceptor.readFromUrl(httpRequest.pathInfo,  null, null)
        return if (resource is Patient) resource else null
    }

    @Search(type = Patient::class)
    fun search(
        httpRequest : HttpServletRequest,
        @OptionalParam(name = Patient.SP_ADDRESS_POSTALCODE) addressPostcode : StringParam?,
        @OptionalParam(name= Patient.SP_BIRTHDATE) birthDate : DateRangeParam?,
        @OptionalParam(name= Patient.SP_EMAIL) email : StringParam?,
        @OptionalParam(name = Patient.SP_FAMILY) familyName : StringParam?,
        @OptionalParam(name= Patient.SP_GENDER) gender : StringParam?,
        @OptionalParam(name= Patient.SP_GIVEN) givenName : StringParam?,
        @OptionalParam(name = Patient.SP_IDENTIFIER) identifier : TokenParam?,
        @OptionalParam(name= Patient.SP_NAME) name : StringParam?,
        @OptionalParam(name= Patient.SP_TELECOM) phone : StringParam?,
        @OptionalParam(name= Patient.SP_RES_ID) id : StringParam?,
        @OptionalParam(name = "_revinclude")  revinclude : StringParam?,
        @OptionalParam(name = "_getpages")  pages : StringParam?,
        @OptionalParam(name = "_count")  count : StringParam?
    ): Bundle? {
        val patients = mutableListOf<Patient>()
        val resource: Resource? = cognitoAuthInterceptor.readFromUrl(httpRequest.pathInfo, httpRequest.queryString,"Patient")
        if (resource != null && resource is Bundle) {
            return resource
        }
        return null
    }

}
