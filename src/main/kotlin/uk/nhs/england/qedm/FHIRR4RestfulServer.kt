package uk.nhs.england.qedm

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.api.EncodingEnum
import ca.uhn.fhir.rest.server.RestfulServer
import com.amazonaws.services.sqs.AmazonSQS
import org.springframework.beans.factory.annotation.Qualifier
import uk.nhs.england.qedm.interceptor.AWSAuditEventLoggingInterceptor
import uk.nhs.england.qedm.interceptor.CapabilityStatementInterceptor
import uk.nhs.england.qedm.interceptor.ValidationInterceptor
import uk.nhs.england.qedm.provider.*
import java.util.*
import jakarta.servlet.annotation.WebServlet


@WebServlet("/FHIR/R4/*", loadOnStartup = 1, displayName = "FHIR Facade")
class FHIRR4RestfulServer(
    @Qualifier("R4") fhirContext: FhirContext,
    val sqs : AmazonSQS,
    val fhirServerProperties: uk.nhs.england.qedm.configuration.FHIRServerProperties,
    val messageProperties: uk.nhs.england.qedm.configuration.MessageProperties,
    val encounterProvider: EncounterProvider,
   // val episodeOfCarePlainProvider: EpisodeOfCarePlainProvider,
    val medicationDispenseProvider: MedicationDispenseProvider,
    val medicationRequestProvider: MedicationRequestProvider,
    val medicationStatementProvider: MedicationStatementProvider,

    val appointmentProvider: AppointmentProvider,
   // val slotProvider: SlotProvider,
   // val scheduleProvider: ScheduleProvider,

    val allergyIntoleranceProvider: AllergyIntoleranceProvider,
    val conditionProvider: ConditionProvider,
    val immunisationProvider: ImmunisationProvider,

    val observationSearchProvider: ObservationSearchProvider,
    val procedureProvider: ProcedureProvider,
    val diagnosticReportProvider: DiagnosticReportProvider,

    val patientSearchProvider: PatientSearchProvider,
    val documentReferenceProvider: DocumentReferenceProvider,
    val binaryProvider: BinaryProvider,
    val specimenProvider: SpecimenProvider,
    //val consentProvider: ConsentProvider,
    val questionnaireResponseProvider: QuestionnaireResponseProvider,
    val valueSetProvider: ValueSetProvider,
    val transactionProvider: TransactionProvider,
    val relatedPersonSearchProvider: RelatedPersonSearchProvider,
    val flagSearchProvider: FlagSearchProvider

) : RestfulServer(fhirContext) {

    override fun initialize() {
        super.initialize()

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        registerProvider(encounterProvider)
     //   registerProvider(episodeOfCarePlainProvider)
        registerProvider(appointmentProvider)
     //   registerProvider(slotProvider)
     //   registerProvider(scheduleProvider)

        registerProvider(medicationDispenseProvider)
        registerProvider(medicationRequestProvider)
        registerProvider(medicationStatementProvider)

        registerProvider(allergyIntoleranceProvider)
        registerProvider(conditionProvider)
        registerProvider(immunisationProvider)

        registerProvider(procedureProvider)
        registerProvider(diagnosticReportProvider)

        registerProvider(binaryProvider)
        registerProvider(documentReferenceProvider)
     //   registerProvider(specimenProvider)
     //   registerProvider(consentProvider)

        registerProvider(questionnaireResponseProvider)
        registerProvider(observationSearchProvider)
        registerProvider(patientSearchProvider)
        registerProvider(relatedPersonSearchProvider)
        registerProvider(flagSearchProvider)

        registerProvider(valueSetProvider)
        registerProvider(transactionProvider)

        val awsAuditEventLoggingInterceptor =
            AWSAuditEventLoggingInterceptor(
                this.fhirContext,
                fhirServerProperties,
                messageProperties,
                sqs
            )
        interceptorService.registerInterceptor(awsAuditEventLoggingInterceptor)
        registerInterceptor(CapabilityStatementInterceptor(fhirServerProperties))

        val validationInterceptor = ValidationInterceptor(fhirContext,messageProperties)
        interceptorService.registerInterceptor(validationInterceptor)


        isDefaultPrettyPrint = true
        defaultResponseEncoding = EncodingEnum.JSON
    }
}
