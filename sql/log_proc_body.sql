CREATE OR REPLACE PACKAGE BODY ADITLOG AS

  PROCEDURE LOG_ACCESS_RESTRICTION (
    access_restriction_new IN access_restriction%ROWTYPE,
    access_restriction_old IN access_restriction%ROWTYPE,
    operation IN VARCHAR2
  ) AS
    usr       varchar2(20);
    pkey_col  varchar2(50);
    tablename varchar2(50) := 'ACCESS_RESTRICTION';
    primary_key_v NUMBER(18,0) := access_restriction_old.id;
  BEGIN
  
    -- Current user
    SELECT USER INTO usr FROM dual;
  
    -- id changed
    IF(NVL(access_restriction_new.id, 0) != NVL(access_restriction_old.id, 0)) THEN
    
      INSERT INTO adit_log(
        table_name,
        column_name,
        old_value,
        new_value,
        log_date,
        remote_application_short_name,
        xtee_user_code,
        xtee_institution_code,
        db_user,
        primary_key_value
      ) VALUES (
        tablename,
        'id',
        access_restriction_old.id,
        access_restriction_new.id,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
    
    -- remote_application changed
    IF(NVL(access_restriction_new.remote_application, 0) != NVL(access_restriction_old.remote_application, 0)) THEN
    
      INSERT INTO adit_log(
        table_name,
        column_name,
        old_value,
        new_value,
        log_date,
        remote_application_short_name,
        xtee_user_code,
        xtee_institution_code,
        db_user,
        primary_key_value
      ) VALUES (
        tablename,
        'remote_application',
        access_restriction_old.remote_application,
        access_restriction_new.remote_application,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
    
    -- user_code changed
    IF(NVL(access_restriction_new.user_code, 0) != NVL(access_restriction_old.user_code, 0)) THEN
    
      INSERT INTO adit_log(
        table_name,
        column_name,
        old_value,
        new_value,
        log_date,
        remote_application_short_name,
        xtee_user_code,
        xtee_institution_code,
        db_user,
        primary_key_value
      ) VALUES (
        tablename,
        'user_code',
        access_restriction_old.user_code,
        access_restriction_new.user_code,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
    -- restriction changed
    IF(NVL(access_restriction_new.restriction, 0) != NVL(access_restriction_old.restriction, 0)) THEN
    
      INSERT INTO adit_log(
        table_name,
        column_name,
        old_value,
        new_value,
        log_date,
        remote_application_short_name,
        xtee_user_code,
        xtee_institution_code,
        db_user,
        primary_key_value
      ) VALUES (
        tablename,
        'restriction',
        access_restriction_old.restriction,
        access_restriction_new.restriction,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
  END LOG_ACCESS_RESTRICTION;

  

END ADITLOG;
/