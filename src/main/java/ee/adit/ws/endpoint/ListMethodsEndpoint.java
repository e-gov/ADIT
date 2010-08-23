package ee.adit.ws.endpoint;

import ee.adit.pojo.ListMethodsResponse;

public class ListMethodsEndpoint extends AbstractAditBaseEndpoint {
	
	@Override
	protected Object invokeInternal(Object requestObject, int version) throws Exception {
		return getResponse();
	}
	
	@Override
	protected Object getResultForGenericException(Exception ex) {
		return getResponse();
	}
	
	private ListMethodsResponse getResponse() {
		ListMethodsResponse result = new ListMethodsResponse();
		
		result.addItem("ametlikud-dokumendid.confirmSignature.v1");
		result.addItem("ametlikud-dokumendid.deflateDocument.v1");
		result.addItem("ametlikud-dokumendid.deleteDocument.v1");
		result.addItem("ametlikud-dokumendid.deleteDocumentFile.v1");
		result.addItem("ametlikud-dokumendid.getDocument.v1");
		result.addItem("ametlikud-dokumendid.getDocumentFile.v1");
		result.addItem("ametlikud-dokumendid.getDocumentHistory.v1");
		result.addItem("ametlikud-dokumendid.getDocumentList.v1");
		result.addItem("ametlikud-dokumendid.getJoined.v1");
		result.addItem("ametlikud-dokumendid.getNotifications.v1");
		result.addItem("ametlikud-dokumendid.getUserInfo.v1");
		result.addItem("ametlikud-dokumendid.join.v1");
		result.addItem("ametlikud-dokumendid.markDocumentViewed.v1");
		result.addItem("ametlikud-dokumendid.modifyStatus.v1");
		result.addItem("ametlikud-dokumendid.prepareSignature.v1");
		result.addItem("ametlikud-dokumendid.saveDocument.v1");
		result.addItem("ametlikud-dokumendid.saveDocumentFile.v1");
		result.addItem("ametlikud-dokumendid.sendDocument.v1");
		result.addItem("ametlikud-dokumendid.setNotifications.v1");
		result.addItem("ametlikud-dokumendid.shareDocument.v1");
		result.addItem("ametlikud-dokumendid.unJoin.v1");
		result.addItem("ametlikud-dokumendid.unShare.v1");
		result.addItem("ametlikud-dokumendid.listMethods");
		
		return result;
	}
}
