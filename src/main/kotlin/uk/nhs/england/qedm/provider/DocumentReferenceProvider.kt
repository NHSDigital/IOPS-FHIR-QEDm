package uk.nhs.england.qedm.provider

import ca.uhn.fhir.rest.annotation.*
import ca.uhn.fhir.rest.param.DateRangeParam
import ca.uhn.fhir.rest.param.StringParam
import ca.uhn.fhir.rest.param.TokenOrListParam
import ca.uhn.fhir.rest.param.TokenParam
import ca.uhn.fhir.rest.server.IResourceProvider
import org.hl7.fhir.r4.model.*
import org.springframework.stereotype.Component
import uk.nhs.england.qedm.awsProvider.AWSPatient
import uk.nhs.england.qedm.configuration.FHIRServerProperties
import uk.nhs.england.qedm.interceptor.CognitoAuthInterceptor
import jakarta.servlet.http.HttpServletRequest

@Component
class DocumentReferenceProvider(var cognitoAuthInterceptor: CognitoAuthInterceptor,
                                val  fhirServerProperties: FHIRServerProperties,
    val awsPatient: AWSPatient
)  {


    @Read(type=DocumentReference::class)
    fun read(httpRequest : HttpServletRequest, @IdParam internalId: IdType): DocumentReference? {
        val resource: Resource? = cognitoAuthInterceptor.readFromUrl(httpRequest.pathInfo, null,"DocumentReference")
        return if (resource is DocumentReference) fixUrl(resource) else null
    }

    fun fixUrl(documentReference: DocumentReference) : DocumentReference {
        if (documentReference.hasContent() ) {
            for (content in documentReference.content) {
                if (content.hasAttachment() && content.attachment.hasUrl()) {
                    if (content.attachment.url.startsWith("http://localhost:")) {
                        var urls = content.attachment.url.split("Binary")
                        if (urls.size>1) content.attachment.url = fhirServerProperties.server.baseUrl + "/FHIR/R4/Binary"+ urls[1]
                    }
                }
            }
        }
        return documentReference
    }
    @Search(type=DocumentReference::class)
    fun search(
        httpRequest : HttpServletRequest,
        @OptionalParam(name = DocumentReference.SP_PATIENT) patient : TokenParam?,
        @OptionalParam(name = "patient:identifier") nhsNumber : TokenParam?,
        @OptionalParam(name = DocumentReference.SP_DATE) date : DateRangeParam?,
        @OptionalParam(name = DocumentReference.SP_TYPE) type : TokenOrListParam?,
        @OptionalParam(name = "custodian:identifier") typeIdentifier : TokenOrListParam?,
        @OptionalParam(name = DocumentReference.SP_IDENTIFIER)  identifier :TokenParam?,
        @OptionalParam(name = DocumentReference.SP_RES_ID)  resid : StringParam?,
        @OptionalParam(name = "_getpages")  pages : StringParam?,
        @OptionalParam(name = "_count")  count : StringParam?

    ): Bundle? {
        val queryString = awsPatient.processQueryString(httpRequest.queryString,nhsNumber)

        val resource: Resource? = cognitoAuthInterceptor.readFromUrl(httpRequest.pathInfo, queryString,"DocumentReference")
        if (resource != null && resource is Bundle) {
            for (entry in resource.entry) {
                if (entry.hasResource() && entry.resource is DocumentReference) {
                    entry.resource = fixUrl(entry.resource as DocumentReference)
                }
            }
            return resource
        }

        return null
    }
}
