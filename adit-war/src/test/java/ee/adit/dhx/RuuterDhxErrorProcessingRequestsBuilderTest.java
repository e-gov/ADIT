package ee.adit.dhx;

import ee.adit.dao.pojo.DocumentType;
import ee.adit.dhx.api.container.v2_1.*;
import ee.adit.exception.AditCodedException;
import ee.adit.exception.AditUserInactiveException;
import ee.adit.service.dhx.DhxProcessingErrorType;
import ee.adit.service.dhx.DhxRecipientUserType;
import ee.adit.service.dhx.RuuterDhxErrorProcessingRequest;
import ee.adit.service.dhx.RuuterDhxErrorProcessingRequestsBuilder;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;

public class RuuterDhxErrorProcessingRequestsBuilderTest {

    public static final String TEST_ORGANISATION_NAME = "TestName";
    public static final String TEST_PERSON_NAME = "TestGiven";
    public static final String TEST_PERSON_SURNAME = "TestSur";
    public List<DocumentType> ADIT_DOCUMENT_TYPES = new ArrayList<DocumentType>() {
        {
            add(new DocumentType("letter", "Kiri", null));
            add(new DocumentType("invoice", "E-arve", null));
            add(new DocumentType("application", "Avaldus / Taotlus", null));
        }
    };

    @Test
    public void testBuildWithAditUserInactiveException() {
        ContainerVer2_1 containerVer2_1 = buildValidContainer();
        Exception error = new AditUserInactiveException("12345");
        RuuterDhxErrorProcessingRequestsBuilder builder = new RuuterDhxErrorProcessingRequestsBuilder(containerVer2_1, error, ADIT_DOCUMENT_TYPES);
        List<RuuterDhxErrorProcessingRequest> requests = builder.build();

        Assert.assertNotNull(requests);
        Assert.assertEquals(1, requests.size());
        Assert.assertEquals(DhxProcessingErrorType.ACTIVE_USER_NOT_FOUND, requests.get(0).getErrorCode());
        Assert.assertEquals(containerVer2_1, requests.get(0).getDocument());
        Assert.assertEquals("letter", requests.get(0).getAditDocumentType());
    }

    /**
     * If AditUserInactiveException is thrown for a different user than speicified in recipients list,
     * then error code should be UNSPECIFIED for all recipients.
     */
    @Test
    public void testBuildWithAditUserInactiveExceptionNotRecipient() {
        ContainerVer2_1 containerVer2_1 = buildValidContainer();
        //Recipient uses personalIdCode 12345
        Exception error = new AditUserInactiveException("54321");

        RuuterDhxErrorProcessingRequestsBuilder builder = new RuuterDhxErrorProcessingRequestsBuilder(containerVer2_1, error, ADIT_DOCUMENT_TYPES);
        List<RuuterDhxErrorProcessingRequest> requests = builder.build();

        Assert.assertNotNull(requests);
        Assert.assertEquals(1, requests.size());
        Assert.assertEquals(DhxProcessingErrorType.UNSPECIFIED, requests.get(0).getErrorCode());
        Assert.assertEquals(containerVer2_1, requests.get(0).getDocument());
    }

    @Test
    public void testBuildWithAditCodedException() {
        ContainerVer2_1 containerVer2_1 = buildValidContainer();
        Exception error = new AditCodedException("message");
        RuuterDhxErrorProcessingRequestsBuilder builder = new RuuterDhxErrorProcessingRequestsBuilder(containerVer2_1, error, ADIT_DOCUMENT_TYPES);
        List<RuuterDhxErrorProcessingRequest> requests = builder.build();

        Assert.assertNotNull(requests);
        Assert.assertEquals(1, requests.size());
        Assert.assertEquals(DhxProcessingErrorType.UNSPECIFIED, requests.get(0).getErrorCode());
        Assert.assertEquals(containerVer2_1, requests.get(0).getDocument());
    }

    @Test
    public void testBuildWithOrganisation() {
        ContainerVer2_1 containerVer2_1 = buildValidContainer();
        containerVer2_1.getRecipient().add(buildOrganisationRecipient("111"));

        Exception error = new AditCodedException("message");
        RuuterDhxErrorProcessingRequestsBuilder builder = new RuuterDhxErrorProcessingRequestsBuilder(containerVer2_1, error, ADIT_DOCUMENT_TYPES);
        List<RuuterDhxErrorProcessingRequest> requests = builder.build();

        Assert.assertNotNull(requests);
        Assert.assertEquals(2, requests.size());

        MatcherAssert.assertThat(requests, hasItem(allOf(
                Matchers.<RuuterDhxErrorProcessingRequest>hasProperty("recipientCode", is("111")),
                Matchers.<RuuterDhxErrorProcessingRequest>hasProperty("recipientUserType", is(DhxRecipientUserType.ORGANISATION)),
                Matchers.<RuuterDhxErrorProcessingRequest>hasProperty("recipientUserName", is(TEST_ORGANISATION_NAME)),
                Matchers.<RuuterDhxErrorProcessingRequest>hasProperty("errorCode", is(DhxProcessingErrorType.UNSPECIFIED)))
        ));

        MatcherAssert.assertThat(requests, hasItem(allOf(
                Matchers.<RuuterDhxErrorProcessingRequest>hasProperty("recipientCode", is("12345")),
                Matchers.<RuuterDhxErrorProcessingRequest>hasProperty("recipientUserType", is(DhxRecipientUserType.PERSON)),
                Matchers.<RuuterDhxErrorProcessingRequest>hasProperty("recipientUserName", is(TEST_PERSON_NAME + " " + TEST_PERSON_SURNAME)),
                Matchers.<RuuterDhxErrorProcessingRequest>hasProperty("errorCode", is(DhxProcessingErrorType.UNSPECIFIED)))
        ));
    }

    @Test
    public void testBuildWithMultipleRecipients() {
        String inactiveUserPersonalIdCode = "23456";

        ContainerVer2_1 containerVer2_1 = buildValidContainer();
        containerVer2_1.getRecipient().add(buildPersonRecipient(inactiveUserPersonalIdCode));

        Exception error = new AditUserInactiveException(inactiveUserPersonalIdCode);
        RuuterDhxErrorProcessingRequestsBuilder builder = new RuuterDhxErrorProcessingRequestsBuilder(containerVer2_1, error, ADIT_DOCUMENT_TYPES);
        List<RuuterDhxErrorProcessingRequest> requests = builder.build();

        Assert.assertNotNull(requests);
        Assert.assertEquals(2, requests.size());

        MatcherAssert.assertThat(requests, hasItem(allOf(
                Matchers.<RuuterDhxErrorProcessingRequest>hasProperty("recipientCode", is("23456")),
                Matchers.<RuuterDhxErrorProcessingRequest>hasProperty("errorCode", is(DhxProcessingErrorType.ACTIVE_USER_NOT_FOUND)))
        ));

        MatcherAssert.assertThat(requests, hasItem(allOf(
                Matchers.<RuuterDhxErrorProcessingRequest>hasProperty("recipientCode", is("12345")),
                Matchers.<RuuterDhxErrorProcessingRequest>hasProperty("errorCode", is(DhxProcessingErrorType.UNSPECIFIED)))
        ));

        Assert.assertEquals(containerVer2_1, requests.get(0).getDocument());
        Assert.assertEquals(containerVer2_1, requests.get(1).getDocument());
    }

    /**
     * No exceptions should be thrown, when getRecordMetadata is null or recordType is null
     */
    @Test
    public void testBuildWithMissingDocumentType() {
        // if all recordType is missing
        ContainerVer2_1 containerVer2_1 = buildValidContainer();
        containerVer2_1.getRecordMetadata().setRecordType(null);

        Exception error = new AditUserInactiveException("12345");
        RuuterDhxErrorProcessingRequestsBuilder builder = new RuuterDhxErrorProcessingRequestsBuilder(containerVer2_1, error, ADIT_DOCUMENT_TYPES);
        List<RuuterDhxErrorProcessingRequest> requests = builder.build();
        Assert.assertNotNull(requests);
        Assert.assertEquals(1, requests.size());
        Assert.assertNull(requests.get(0).getAditDocumentType());

        // if all recordMetadata is missing
        containerVer2_1 = buildValidContainer();
        containerVer2_1.setRecordMetadata(null);
        builder = new RuuterDhxErrorProcessingRequestsBuilder(containerVer2_1, error, ADIT_DOCUMENT_TYPES);
        requests = builder.build();
        Assert.assertNotNull(requests);
        Assert.assertEquals(1, requests.size());

        Assert.assertEquals(DhxProcessingErrorType.ACTIVE_USER_NOT_FOUND, requests.get(0).getErrorCode());
        Assert.assertEquals(containerVer2_1, requests.get(0).getDocument());
        Assert.assertNull(requests.get(0).getAditDocumentType());
    }

    @Test
    public void testBuildWithInvalidDocumentType() {
        ContainerVer2_1 containerVer2_1 = buildValidContainer();
        containerVer2_1.getRecordMetadata().setRecordType("Sõnum");
        Exception error = new AditUserInactiveException("12345");
        RuuterDhxErrorProcessingRequestsBuilder builder = new RuuterDhxErrorProcessingRequestsBuilder(containerVer2_1, error, ADIT_DOCUMENT_TYPES);
        List<RuuterDhxErrorProcessingRequest> requests = builder.build();

        Assert.assertNotNull(requests);
        Assert.assertEquals(1, requests.size());
        Assert.assertEquals(DhxProcessingErrorType.ACTIVE_USER_NOT_FOUND, requests.get(0).getErrorCode());
        Assert.assertEquals(containerVer2_1, requests.get(0).getDocument());
    }

    private ContainerVer2_1 buildValidContainer() {
        ContainerVer2_1 containerVer2_1 = new ContainerVer2_1();
        RecordMetadata recordMetadata = new RecordMetadata();
        recordMetadata.setRecordType("Kiri");
        containerVer2_1.setRecordMetadata(recordMetadata);

        Recipient recipient = buildPersonRecipient("12345");

        ArrayList<Recipient> recipients = new ArrayList<>();
        recipients.add(recipient);
        containerVer2_1.setRecipient(recipients);

        return containerVer2_1;
    }

    private Recipient buildPersonRecipient(String personalIdCode) {
        PersonType person = new PersonType();
        person.setGivenName(TEST_PERSON_NAME);
        person.setSurname(TEST_PERSON_SURNAME);
        person.setResidency("EE");
        person.setPersonalIdCode(personalIdCode);

        Recipient recipient = new Recipient();
        recipient.setPerson(person);
        return recipient;
    }

    private Recipient buildOrganisationRecipient(String code) {
        OrganisationType org = new OrganisationType();
        org.setName(TEST_ORGANISATION_NAME);
        org.setOrganisationCode(code);

        Recipient recipient = new Recipient();
        recipient.setOrganisation(org);
        return recipient;
    }
}
