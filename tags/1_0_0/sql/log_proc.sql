create or replace
PACKAGE &&ADIT_SCHEMA..ADITLOG AS 
  
  -- Logimise muutujad
  xtee_isikukood VARCHAR2(100);
  xtee_asutus VARCHAR2(100);
  remote_application_short_name VARCHAR2(50);

  TYPE result_ref_cursor IS REF CURSOR;
  date_format VARCHAR2(100) := 'dd.mm.yyyy HH24:MI:SS';

  PROCEDURE LOG_ACCESS_RESTRICTION (
    access_restriction_new IN &&ADIT_SCHEMA..access_restriction%ROWTYPE,
    access_restriction_old IN &&ADIT_SCHEMA..access_restriction%ROWTYPE,
    operation IN VARCHAR2
  );

  PROCEDURE LOG_ADIT_USER (
    adit_user_new IN &&ADIT_SCHEMA..adit_user%ROWTYPE,
    adit_user_old IN &&ADIT_SCHEMA..adit_user%ROWTYPE,
    operation IN VARCHAR2
  );
  
  PROCEDURE LOG_DOCUMENT (
    document_new IN &&ADIT_SCHEMA..document%ROWTYPE,
    document_old IN &&ADIT_SCHEMA..document%ROWTYPE,
    operation IN VARCHAR2
  );
  
  
  PROCEDURE LOG_DOCUMENT_DVK_STATUS (
    document_dvk_status_new IN &&ADIT_SCHEMA..document_dvk_status%ROWTYPE,
    document_dvk_status_old IN &&ADIT_SCHEMA..document_dvk_status%ROWTYPE,
    operation IN VARCHAR2
  );
  
  
  PROCEDURE LOG_DOCUMENT_FILE (
    document_file_new IN &&ADIT_SCHEMA..document_file%ROWTYPE,
    document_file_old IN &&ADIT_SCHEMA..document_file%ROWTYPE,
    operation IN VARCHAR2
  );
  
  
  PROCEDURE LOG_DOCUMENT_HISTORY (
    document_history_new IN &&ADIT_SCHEMA..document_history%ROWTYPE,
    document_history_old IN &&ADIT_SCHEMA..document_history%ROWTYPE,
    operation IN VARCHAR2
  );
  
  PROCEDURE LOG_DOCUMENT_HISTORY_TYPE (
    document_history_type_new IN &&ADIT_SCHEMA..document_history_type%ROWTYPE,
    document_history_type_old IN &&ADIT_SCHEMA..document_history_type%ROWTYPE,
    operation IN VARCHAR2
  );
  
  PROCEDURE LOG_DOCUMENT_SHARING (
    document_sharing_new IN &&ADIT_SCHEMA..document_sharing%ROWTYPE,
    document_sharing_old IN &&ADIT_SCHEMA..document_sharing%ROWTYPE,
    operation IN VARCHAR2
  );
  
  PROCEDURE LOG_DOCUMENT_SHARING_TYPE (
    document_sharing_type_new IN &&ADIT_SCHEMA..document_sharing_type%ROWTYPE,
    document_sharing_type_old IN &&ADIT_SCHEMA..document_sharing_type%ROWTYPE,
    operation IN VARCHAR2
  );
  
  PROCEDURE LOG_DOCUMENT_TYPE (
    document_type_new IN &&ADIT_SCHEMA..document_type%ROWTYPE,
    document_type_old IN &&ADIT_SCHEMA..document_type%ROWTYPE,
    operation IN VARCHAR2
  );
  
  PROCEDURE LOG_DOCUMENT_WF_STATUS (
    document_wf_status_new IN &&ADIT_SCHEMA..document_wf_status%ROWTYPE,
    document_wf_status_old IN &&ADIT_SCHEMA..document_wf_status%ROWTYPE,
    operation IN VARCHAR2
  );
  
  PROCEDURE LOG_NOTIFICATION (
    notification_new IN &&ADIT_SCHEMA..notification%ROWTYPE,
    notification_old IN &&ADIT_SCHEMA..notification%ROWTYPE,
    operation IN VARCHAR2
  );
  
  PROCEDURE LOG_NOTIFICATION_TYPE (
    notification_type_new IN &&ADIT_SCHEMA..notification_type%ROWTYPE,
    notification_type_old IN &&ADIT_SCHEMA..notification_type%ROWTYPE,
    operation IN VARCHAR2
  );
  
  PROCEDURE LOG_REMOTE_APPLICATION (
    remote_application_new IN &&ADIT_SCHEMA..remote_application%ROWTYPE,
    remote_application_old IN &&ADIT_SCHEMA..remote_application%ROWTYPE,
    operation IN VARCHAR2
  );
  
  PROCEDURE LOG_SIGNATURE (
    signature_new IN &&ADIT_SCHEMA..signature%ROWTYPE,
    signature_old IN &&ADIT_SCHEMA..signature%ROWTYPE,
    operation IN VARCHAR2
  );
  
  PROCEDURE LOG_USER_NOTIFICATION (
    user_notification_new IN &&ADIT_SCHEMA..user_notification%ROWTYPE,
    user_notification_old IN &&ADIT_SCHEMA..user_notification%ROWTYPE,
    operation IN VARCHAR2
  );
  
  PROCEDURE LOG_USERTYPE (
    usertype_new IN &&ADIT_SCHEMA..usertype%ROWTYPE,
    usertype_old IN &&ADIT_SCHEMA..usertype%ROWTYPE,
    operation IN VARCHAR2
  );

END ADITLOG;
/