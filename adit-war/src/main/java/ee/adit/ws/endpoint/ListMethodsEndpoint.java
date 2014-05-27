package ee.adit.ws.endpoint;

import ee.adit.pojo.ListMethodsResponse;

/**
 * Web-service endpoint for "listMethods" service. Returns the list of services
 * provided by ADIT.
 *
 * @author Marko Kurm, Microlink Eesti AS, marko.kurm@microlink.ee
 * @author Jaak Lember, Interinx, jaak@interinx.com
 *
 */
public class ListMethodsEndpoint extends AbstractAditBaseEndpoint {

    /**
     * Invocation method (entry point).
     *
     * @param requestObject
     *            request object (not used)
     * @param version
     *            query version (not used)
     * @return response object
     */
    @Override
    protected Object invokeInternal(Object requestObject, int version) throws Exception {
        return getResponse();
    }

    /**
     * Gets the response object if an exception occurs.
     * @param ex
     *     Exception that occurred
     * @return response object
     */
    @Override
    protected Object getResultForGenericException(Exception ex) {
        return getResponse();
    }

    /**
     * Constructs a response object containing all the query names that ADIT
     * publishes.
     *
     * @return "listMethods" web method tesult as {@link ListMethodsResponse}
     *         object
     */
    private ListMethodsResponse getResponse() {
        ListMethodsResponse result = new ListMethodsResponse();

        String producerName = this.getConfiguration().getXteeProducerName();

        result.addItem(producerName + ".confirmSignature.v1");
        result.addItem(producerName + ".deflateDocument.v1");
        result.addItem(producerName + ".deleteDocument.v1");
        result.addItem(producerName + ".deleteDocuments.v1");
        result.addItem(producerName + ".deleteDocumentFile.v1");
        result.addItem(producerName + ".getDocument.v1");
        result.addItem(producerName + ".getDocument.v2");
        result.addItem(producerName + ".getDocumentFile.v1");
        result.addItem(producerName + ".getDocumentHistory.v1");
        result.addItem(producerName + ".getDocumentList.v1");
        result.addItem(producerName + ".getJoined.v1");
        result.addItem(producerName + ".getNotifications.v1");
        result.addItem(producerName + ".getSendStatus.v1");
        result.addItem(producerName + ".getUserInfo.v1");
        result.addItem(producerName + ".join.v1");
        result.addItem(producerName + ".markDocumentViewed.v1");
        result.addItem(producerName + ".modifyStatus.v1");
        result.addItem(producerName + ".prepareSignature.v1");
        result.addItem(producerName + ".saveDocument.v1");
        result.addItem(producerName + ".saveDocumentFile.v1");
        result.addItem(producerName + ".sendDocument.v1");
        result.addItem(producerName + ".setNotifications.v1");
        result.addItem(producerName + ".shareDocument.v1");
        result.addItem(producerName + ".unJoin.v1");
        result.addItem(producerName + ".unShareDocument.v1");
        result.addItem(producerName + ".getUserContacts.v1");
        result.addItem(producerName + ".listMethods");

        return result;
    }
}
