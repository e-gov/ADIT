package ee.adit.integrationtests;

import dvk.api.ml.PojoMessage;
import ee.adit.dao.dvk.DvkDAO;
import ee.adit.service.DocumentService;
import org.hibernate.Hibernate;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Clob;

public class UtilsService {
    public static PojoMessage prepareMessageBeforeInsert(DvkDAO dvkDAO, String containerFile) {
        PojoMessage dvkMessage = new PojoMessage();

        dvkMessage.setDhlMessageId(1);
        dvkMessage.setIsIncoming(true);
        dvkMessage.setTitle("TestDocument1");
        dvkMessage.setSenderOrgCode("10885324");
        dvkMessage.setSenderOrgName("IceFire OÜ");
        dvkMessage.setSenderPersonCode("39105200028");
        dvkMessage.setSenderName("Igor Mishurov");
        dvkMessage.setRecipientOrgCode("10885324");
        dvkMessage.setRecipientOrgName("IceFire OÜ");
        dvkMessage.setRecipientPersonCode("12345678901");
        dvkMessage.setRecipientName("Hendrik Pärna");
        dvkMessage.setSendingStatusId(DocumentService.DVK_STATUS_WAITING);
        dvkMessage.setUnitId(0);
        dvkMessage.setLocalItemId(null);
        dvkMessage.setStatusUpdateNeeded((long) 0);
        dvkMessage.setDhlFolderName("/");
        String container = "";
        try {
            container = readSQLToString(containerFile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Clob clob = Hibernate.createClob(container, dvkDAO.getSessionFactory().openSession());
        dvkMessage.setData(clob);
        return dvkMessage;
    }

    public static String readSQLToString(String filePath) throws Exception {
        StringBuilder fileData = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(filePath), "UTF-8"));
        char[] buf = new char[1024];
        int numRead = 0;

        while((numRead=reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();

        return fileData.toString();
    }

}
