package ee.adit.dhx;

import ee.adit.dhx.api.container.v2_1.ContainerVer2_1;
import ee.adit.dhx.api.container.v2_1.OrganisationType;
import ee.adit.dhx.api.container.v2_1.PersonType;
import ee.adit.dhx.api.container.v2_1.Recipient;
import ee.adit.exception.AditCodedException;
import ee.adit.exception.AditUserInactiveException;
import ee.adit.service.dhx.DhxProcessingErrorType;
import ee.adit.service.dhx.DhxRecipientUserType;
import ee.adit.service.dhx.RuuterDhxProcessingErrorRequest;
import ee.adit.service.dhx.RuuterDhxProcessingErrorRequestsBuilder;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;

public class RuuterDhxProcessingErrorRequestsBuilderTest {

    public static final String TEST_ORGANISATION_NAME = "TestName";
    public static final String TEST_PERSON_NAME = "TestGiven";
    public static final String TEST_PERSON_SURNAME = "TestSur";

    @Test
    public void testBuildWithAditUserInactiveException() {
        ContainerVer2_1 containerVer2_1 = buildValidContainer();
        Exception error = new AditUserInactiveException("12345");
        RuuterDhxProcessingErrorRequestsBuilder builder = new RuuterDhxProcessingErrorRequestsBuilder(containerVer2_1, error);
        List<RuuterDhxProcessingErrorRequest> requests = builder.build();

        Assert.assertNotNull(requests);
        Assert.assertEquals(1, requests.size());
        Assert.assertEquals(DhxProcessingErrorType.ACTIVE_USER_NOT_FOUND, requests.get(0).getErrorCode());
        Assert.assertEquals(containerVer2_1, requests.get(0).getContainerVer2_1());
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

        RuuterDhxProcessingErrorRequestsBuilder builder = new RuuterDhxProcessingErrorRequestsBuilder(containerVer2_1, error);
        List<RuuterDhxProcessingErrorRequest> requests = builder.build();

        Assert.assertNotNull(requests);
        Assert.assertEquals(1, requests.size());
        Assert.assertEquals(DhxProcessingErrorType.UNSPECIFIED, requests.get(0).getErrorCode());
        Assert.assertEquals(containerVer2_1, requests.get(0).getContainerVer2_1());
    }

    @Test
    public void testBuildWithAditCodedException() {
        ContainerVer2_1 containerVer2_1 = buildValidContainer();
        Exception error = new AditCodedException("message");
        RuuterDhxProcessingErrorRequestsBuilder builder = new RuuterDhxProcessingErrorRequestsBuilder(containerVer2_1, error);
        List<RuuterDhxProcessingErrorRequest> requests = builder.build();

        Assert.assertNotNull(requests);
        Assert.assertEquals(1, requests.size());
        Assert.assertEquals(DhxProcessingErrorType.UNSPECIFIED, requests.get(0).getErrorCode());
        Assert.assertEquals(containerVer2_1, requests.get(0).getContainerVer2_1());
    }

    @Test
    public void testBuildWithOrganisation() {
        ContainerVer2_1 containerVer2_1 = buildValidContainer();
        containerVer2_1.getRecipient().add(buildOrganisationRecipient("111"));

        Exception error = new AditCodedException("message");
        RuuterDhxProcessingErrorRequestsBuilder builder = new RuuterDhxProcessingErrorRequestsBuilder(containerVer2_1, error);
        List<RuuterDhxProcessingErrorRequest> requests = builder.build();

        Assert.assertNotNull(requests);
        Assert.assertEquals(2, requests.size());

        MatcherAssert.assertThat(requests, hasItem(allOf(
                Matchers.<RuuterDhxProcessingErrorRequest>hasProperty("recipientCode", is("111")),
                Matchers.<RuuterDhxProcessingErrorRequest>hasProperty("recipientUserType", is(DhxRecipientUserType.ORGANISATION)),
                Matchers.<RuuterDhxProcessingErrorRequest>hasProperty("recipientUserName", is(TEST_ORGANISATION_NAME)),
                Matchers.<RuuterDhxProcessingErrorRequest>hasProperty("errorCode", is(DhxProcessingErrorType.UNSPECIFIED)))
        ));

        MatcherAssert.assertThat(requests, hasItem(allOf(
                Matchers.<RuuterDhxProcessingErrorRequest>hasProperty("recipientCode", is("12345")),
                Matchers.<RuuterDhxProcessingErrorRequest>hasProperty("recipientUserType", is(DhxRecipientUserType.PERSON)),
                Matchers.<RuuterDhxProcessingErrorRequest>hasProperty("recipientUserName", is(TEST_PERSON_NAME + " " + TEST_PERSON_SURNAME)),
                Matchers.<RuuterDhxProcessingErrorRequest>hasProperty("errorCode", is(DhxProcessingErrorType.UNSPECIFIED)))
        ));
    }

    @Test
    public void testBuildWithMultipleRecipients() {
        String inactiveUserPersonalIdCode = "23456";

        ContainerVer2_1 containerVer2_1 = buildValidContainer();
        containerVer2_1.getRecipient().add(buildPersonRecipient(inactiveUserPersonalIdCode));

        Exception error = new AditUserInactiveException(inactiveUserPersonalIdCode);
        RuuterDhxProcessingErrorRequestsBuilder builder = new RuuterDhxProcessingErrorRequestsBuilder(containerVer2_1, error);
        List<RuuterDhxProcessingErrorRequest> requests = builder.build();

        Assert.assertNotNull(requests);
        Assert.assertEquals(2, requests.size());

        MatcherAssert.assertThat(requests, hasItem(allOf(
                Matchers.<RuuterDhxProcessingErrorRequest>hasProperty("recipientCode", is("23456")),
                Matchers.<RuuterDhxProcessingErrorRequest>hasProperty("errorCode", is(DhxProcessingErrorType.ACTIVE_USER_NOT_FOUND)))
        ));

        MatcherAssert.assertThat(requests, hasItem(allOf(
                Matchers.<RuuterDhxProcessingErrorRequest>hasProperty("recipientCode", is("12345")),
                Matchers.<RuuterDhxProcessingErrorRequest>hasProperty("errorCode", is(DhxProcessingErrorType.UNSPECIFIED)))
        ));

        Assert.assertEquals(containerVer2_1, requests.get(0).getContainerVer2_1());
        Assert.assertEquals(containerVer2_1, requests.get(1).getContainerVer2_1());
    }

    private ContainerVer2_1 buildValidContainer() {
        ContainerVer2_1 containerVer2_1 = new ContainerVer2_1();

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
