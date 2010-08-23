/* Drop Tables, Stored Procedures and Views */
DROP TABLE &&ADIT_SCHEMA..NOTIFICATION CASCADE CONSTRAINTS;
DROP SEQUENCE &&ADIT_SCHEMA..NOTIFICATION_ID_SEQ

DROP TABLE &&ADIT_SCHEMA..ACCESS_RESTRICTION CASCADE CONSTRAINTS;
DROP TRIGGER &&ADIT_SCHEMA..SET_ADIT_LOG_ID;

DROP SEQUENCE &&ADIT_SCHEMA..ADIT_LOG_ID_SEQ;

DROP TABLE &&ADIT_SCHEMA..ADIT_LOG CASCADE CONSTRAINTS;
DROP TABLE &&ADIT_SCHEMA..ADIT_USER CASCADE CONSTRAINTS;

DROP SEQUENCE &&ADIT_SCHEMA..DOCUMENT_ID_SEQ;

DROP TABLE &&ADIT_SCHEMA..DOCUMENT CASCADE CONSTRAINTS;
DROP TABLE &&ADIT_SCHEMA..DOCUMENT_DVK_STATUS CASCADE CONSTRAINTS;

DROP SEQUENCE &&ADIT_SCHEMA..DOCUMENT_FILE_ID_SEQ;

DROP TABLE &&ADIT_SCHEMA..DOCUMENT_FILE CASCADE CONSTRAINTS;

DROP SEQUENCE &&ADIT_SCHEMA..DOCUMENT_HISTORY_ID_SEQ;

DROP TABLE &&ADIT_SCHEMA..DOCUMENT_HISTORY CASCADE CONSTRAINTS;
DROP TABLE &&ADIT_SCHEMA..DOCUMENT_HISTORY_TYPE CASCADE CONSTRAINTS;

DROP SEQUENCE &&ADIT_SCHEMA..DOCUMENT_SHARING_ID_SEQ;

DROP TABLE &&ADIT_SCHEMA..DOCUMENT_SHARING CASCADE CONSTRAINTS;
DROP TABLE &&ADIT_SCHEMA..DOCUMENT_SHARING_TYPE CASCADE CONSTRAINTS;
DROP TABLE &&ADIT_SCHEMA..DOCUMENT_TYPE CASCADE CONSTRAINTS;
DROP TABLE &&ADIT_SCHEMA..DOCUMENT_WF_STATUS CASCADE CONSTRAINTS;

DROP SEQUENCE &&ADIT_SCHEMA..DOWNLOAD_REQUEST_LOG_ID_SEQ;

DROP TABLE &&ADIT_SCHEMA..DOWNLOAD_REQUEST_LOG CASCADE CONSTRAINTS;

DROP SEQUENCE &&ADIT_SCHEMA..ERROR_LOG_ID_SEQ;

DROP TABLE &&ADIT_SCHEMA..ERROR_LOG CASCADE CONSTRAINTS;
DROP SEQUENCE &&ADIT_SCHEMA..METADATA_REQUEST_LOG_ID_SEQ;

DROP TABLE &&ADIT_SCHEMA..METADATA_REQUEST_LOG CASCADE CONSTRAINTS;
DROP TABLE &&ADIT_SCHEMA..NOTIFICATION_TYPE CASCADE CONSTRAINTS;
DROP TABLE &&ADIT_SCHEMA..REMOTE_APPLICATION CASCADE CONSTRAINTS;

DROP SEQUENCE &&ADIT_SCHEMA..REQUEST_LOG_ID_SEQ;

DROP TABLE &&ADIT_SCHEMA..REQUEST_LOG CASCADE CONSTRAINTS;
DROP SEQUENCE &&ADIT_SCHEMA..SIGNATURE_ID_SEQ;

DROP TABLE &&ADIT_SCHEMA..SIGNATURE CASCADE CONSTRAINTS;
DROP TABLE &&ADIT_SCHEMA..USER_NOTIFICATION CASCADE CONSTRAINTS;
DROP TABLE &&ADIT_SCHEMA..USERTYPE CASCADE CONSTRAINTS;