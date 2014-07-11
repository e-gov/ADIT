package ee.adit.integrationtests.Parameters;

import ee.adit.integrationtests.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Liza Leo
 * Date: 7.07.14
 * Time: 12:38
 */

public class ReceiveFromDvkTestParameter {

    final static String RECEIVE_FROM_DVK_TEST_CONTAINERS_FOLDER = "to_ADIT/";

    public String xmlContainerFileName;
    public List<ContainerFile> containerFiles;
    public List<ContainerFile> filesInDdoc;
    public List<ContainerSignature> signaturesInDdoc;

    public ReceiveFromDvkTestParameter(String containerFileName, List<ContainerFile> filesInDocument,
                                       List<ContainerFile> filesInDdocContainer, List<ContainerSignature> signatures){
        xmlContainerFileName = containerFileName;
        containerFiles = filesInDocument;
        filesInDdoc = filesInDdocContainer;
        signaturesInDdoc = signatures;
    }

    public String getPathToXmlContainer() throws Exception{
        String containersPath = ConfigurationConstants.CONTAINERS_PATH + RECEIVE_FROM_DVK_TEST_CONTAINERS_FOLDER;
        return ReceiveFromDvkTestParameter.class.getResource(containersPath + xmlContainerFileName).getPath();
    }

    public List<ContainerFile> getFileByGuid(String guid){
        List<ContainerFile> fileList = new ArrayList<ContainerFile>();
        for (ContainerFile file : containerFiles){
            if (Utils.compareStringsIgnoreCase(file.getGuid(), guid)){
                fileList.add(file);
            }
        }
        return fileList;
    }

    public List<ContainerFile> getFileByName(String name){
        List<ContainerFile> fileList = new ArrayList<ContainerFile>();
        for (ContainerFile file : containerFiles){
            if (Utils.compareStringsIgnoreCase(file.getName(), name)){
                fileList.add(file);
            }
        }
        return fileList;
    }

    public String getXmlContainerFileName() {
        return xmlContainerFileName;
    }

    public List<ContainerFile> getContainerFiles() {
        return containerFiles;
    }

    public List<ContainerFile> getFilesInDdoc() {
        return filesInDdoc;
    }

    public List<ContainerSignature> getSignaturesInDdoc() {
        return signaturesInDdoc;
    }
}
