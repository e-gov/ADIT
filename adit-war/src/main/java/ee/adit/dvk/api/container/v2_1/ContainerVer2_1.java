package ee.adit.dvk.api.container.v2_1;

import java.io.FileReader;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.exolab.castor.xml.Marshaller;

import ee.adit.dvk.api.container.Container;
import ee.adit.dvk.api.ml.Util;

/**
 * @author Hendrik Pärna
 * @since 27.01.14
 */
public class ContainerVer2_1 extends Container {
    private static Logger logger = LogManager.getLogger(ContainerVer2_1.class);

    private Transport transport;
    private DecMetadata decMetadata;
    private Initiator initiator;
    private RecordCreator recordCreator;
    private RecordSenderToDec recordSenderToDec;
    private List<Recipient> recipient;
    private RecordMetadata recordMetadata;
    private Access access;
    private List<SignatureMetadata> signatureMetadata;
    private List<File> file;
    private String recordTypeSpecificMetadata;

    @Override
    public String getContent() {
        StringWriter sw = new StringWriter();

        try {
            Marshaller marshaller = createMarshaller(sw);
            marshaller.marshal(this);

            return sw.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                sw.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Version getInternalVersion() {
        return Version.Ver2_1;
    }

    /**
     * Get the corresponding {@link Recipient} from container.
     * OrgCode and PersonalId codes must match.
     *
     * @param decRecipient {@link DecRecipient}
     * @return recipient
     */
    public Recipient getRecipient(DecRecipient decRecipient) {
        Recipient result = null;

        if (recipient != null) {
            for (Recipient rec : recipient) {
                String orgCode = rec.getOrganisation() != null ? rec.getOrganisation().getOrganisationCode() : null;
                if (rec.getPerson() != null
                        && isDecRecipientRelatedWithRecipient(
                        decRecipient,
                        orgCode,
                        rec.getPerson().getPersonalIdCode())) {
                    result = rec;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Is this {@link DecRecipient} related with {@link Recipient}?
     *
     * @param decRecipient            {@link DecRecipient}
     * @param recipientOrgCode        orgCode
     * @param recipientPersonalIdCode personalIdCode
     * @return true if related otherwise false
     */
    public boolean isDecRecipientRelatedWithRecipient(DecRecipient decRecipient,
                                                      String recipientOrgCode, String recipientPersonalIdCode) {
        boolean result = false;

        if (decRecipient.getOrganisationCode().equalsIgnoreCase("adit")) {
            if ((recipientOrgCode == null || recipientOrgCode.equalsIgnoreCase("adit"))
                    && recipientPersonalIdCode.equalsIgnoreCase(decRecipient.getPersonalIdCode())
                    || recipientPersonalIdCode.equalsIgnoreCase("EE" + decRecipient.getPersonalIdCode())) {
               result = true;
            }
        } else {
            if (recipientOrgCode != null && recipientPersonalIdCode != null) {
                if ((recipientOrgCode.equalsIgnoreCase(decRecipient.getOrganisationCode())
                        || recipientOrgCode.equalsIgnoreCase("EE" + decRecipient.getOrganisationCode()))
                        && (recipientPersonalIdCode.equalsIgnoreCase(decRecipient.getPersonalIdCode())
                        || recipientPersonalIdCode.equalsIgnoreCase("EE" + decRecipient.getPersonalIdCode()))) {
                    result = true;
                }
            }
        }

        return result;
    }

    /**
     * Parse 2.1 container from xml.
     *
     * @param xml String
     * @return 2.1 container object representation.
     */
    public static ContainerVer2_1 parse(String xml) {
        if (Util.isEmpty(xml)) {
            return null;
        }

        StringReader in = new StringReader(xml);

        try {
            return (ContainerVer2_1) Container.marshal(in, Version.Ver2_1);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            in.close();
        }
    }

    /**
     * Parse 2.1 container from file.
     *
     * @param fileName filename
     * @return {@link ContainerVer2_1}
     * @throws IOException
     */
    public static ContainerVer2_1 parseFile(String fileName) {
        if (fileName == null || (fileName != null && fileName.trim().equals(""))) {
            logger.error("Cannot parse DVK container: empty filename.");
            throw new RuntimeException("Cannot parse DVK container: empty filename.");
        }

        ContainerVer2_1 result = null;

        try {

            FileReader fileReader = new FileReader(fileName);

            try {
                result = (ContainerVer2_1) Container.marshal(fileReader, Version.Ver2_1);
            } finally {
                fileReader.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public static ContainerVer2_1 parse(Reader reader) {
        if (reader == null) {
            logger.error("Cannot parse DVK Container: reader not initialized");
            throw new RuntimeException("Cannot parse DVK Container: reader not initialized");
        }

        try {
            return (ContainerVer2_1) Container.marshal(reader, Version.Ver2_1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public Transport getTransport() {
        return transport;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
    }

    public DecMetadata getDecMetadata() {
        return decMetadata;
    }

    public void setDecMetadata(DecMetadata decMetadata) {
        this.decMetadata = decMetadata;
    }

    public Initiator getInitiator() {
        return initiator;
    }

    public void setInitiator(Initiator initiator) {
        this.initiator = initiator;
    }

    public RecordCreator getRecordCreator() {
        return recordCreator;
    }

    public void setRecordCreator(RecordCreator recordCreator) {
        this.recordCreator = recordCreator;
    }

    public RecordSenderToDec getRecordSenderToDec() {
        return recordSenderToDec;
    }

    public void setRecordSenderToDec(RecordSenderToDec recordSenderToDec) {
        this.recordSenderToDec = recordSenderToDec;
    }

    public List<Recipient> getRecipient() {
        return recipient;
    }

    public void setRecipient(List<Recipient> recipient) {
        this.recipient = recipient;
    }

    public RecordMetadata getRecordMetadata() {
        return recordMetadata;
    }

    public void setRecordMetadata(RecordMetadata recordMetadata) {
        this.recordMetadata = recordMetadata;
    }

    public Access getAccess() {
        return access;
    }

    public void setAccess(Access access) {
        this.access = access;
    }

    public List<SignatureMetadata> getSignatureMetadata() {
        return signatureMetadata;
    }

    public void setSignatureMetadata(List<SignatureMetadata> signatureMetadata) {
        this.signatureMetadata = signatureMetadata;
    }

    public List<File> getFile() {
        return file;
    }

    public String getRecordTypeSpecificMetadata() {
        return recordTypeSpecificMetadata;
    }

    public void setRecordTypeSpecificMetadata(String recordTypeSpecificMetadata) {
        this.recordTypeSpecificMetadata = recordTypeSpecificMetadata;
    }

    public void setFile(List<File> file) {
        this.file = file;
    }
}
