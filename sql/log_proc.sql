CREATE OR REPLACE PACKAGE ADITLOG AS 
  
  -- Logimise muutujad
  xtee_isikukood VARCHAR2(100);
  xtee_asutus VARCHAR2(100);
  remote_application_short_name VARCHAR2(50);

  PROCEDURE LOG_ACCESS_RESTRICTION (
    access_restriction_new IN access_restriction%ROWTYPE,
    access_restriction_old IN access_restriction%ROWTYPE,
    operation IN VARCHAR2
  );

  PROCEDURE LOG_ADIT_USER (
    adit_user_new IN adit_user%ROWTYPE,
    adit_user_old IN adit_user%ROWTYPE,
    operation IN VARCHAR2
  );
  
  PROCEDURE LOG_DOCUMENT (
    document_new IN document%ROWTYPE,
    document_old IN document%ROWTYPE,
    operation IN VARCHAR2
  );
  
  
  PROCEDURE LOG_DOCUMENT_DVK_STATUS (
    document_dvk_status_new IN document_dvk_status%ROWTYPE,
    document_dvk_status_old IN document_dvk_status%ROWTYPE,
    operation IN VARCHAR2
  );
  
  
  PROCEDURE LOG_DOCUMENT_FILE (
    document_file_new IN document_file%ROWTYPE,
    document_file_old IN document_file%ROWTYPE,
    operation IN VARCHAR2
  );
  
  
  PROCEDURE LOG_DOCUMENT_HISTORY (
    document_history_new IN document_history%ROWTYPE,
    document_history_old IN document_history%ROWTYPE,
    operation IN VARCHAR2
  );
  
  PROCEDURE LOG_DOCUMENT_HISTORY_TYPE (
    document_history_type_new IN document_history_type%ROWTYPE,
    document_history_type_old IN document_history_type%ROWTYPE,
    operation IN VARCHAR2
  );
  
  PROCEDURE LOG_DOCUMENT_SHARING (
    document_sharing_new IN document_sharing%ROWTYPE,
    document_sharing_old IN document_sharing%ROWTYPE,
    operation IN VARCHAR2
  );
  
  PROCEDURE LOG_DOCUMENT_SHARING_TYPE (
    document_sharing_type_new IN document_sharing_type%ROWTYPE,
    document_sharing_type_old IN document_sharing_type%ROWTYPE,
    operation IN VARCHAR2
  );
  
  PROCEDURE LOG_DOCUMENT_TYPE (
    document_type_new IN document_type%ROWTYPE,
    document_type_old IN document_type%ROWTYPE,
    operation IN VARCHAR2
  );
  
  PROCEDURE LOG_DOCUMENT_WF_STATUS (
    document_wf_status_new IN document_wf_status%ROWTYPE,
    document_wf_status_old IN document_wf_status%ROWTYPE,
    operation IN VARCHAR2
  );
  
  PROCEDURE LOG_NOTIFICATION (
    notification_new IN notification%ROWTYPE,
    notification_old IN notification%ROWTYPE,
    operation IN VARCHAR2
  );
  
  PROCEDURE LOG_NOTIFICATION_TYPE (
    notification_type_new IN notification_type%ROWTYPE,
    notification_type_old IN notification_type%ROWTYPE,
    operation IN VARCHAR2
  );
  
  PROCEDURE LOG_REMOTE_APPLICATION (
    remote_application_new IN remote_application%ROWTYPE,
    remote_application_old IN remote_application%ROWTYPE,
    operation IN VARCHAR2
  );
  
  PROCEDURE LOG_SIGNATURE (
    signature_new IN signature%ROWTYPE,
    signature_old IN signature%ROWTYPE,
    operation IN VARCHAR2
  );
  
  PROCEDURE LOG_USER_NOTIFICATION (
    user_notification_new IN user_notification%ROWTYPE,
    user_notification_old IN user_notification%ROWTYPE,
    operation IN VARCHAR2
  );
  
  PROCEDURE LOG_USERTYPE (
    usertype_new IN usertype%ROWTYPE,
    usertype_old IN usertype%ROWTYPE,
    operation IN VARCHAR2
  );

END ADITLOG;
/

