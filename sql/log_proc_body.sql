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

  PROCEDURE LOG_ADIT_USER (
    adit_user_new IN adit_user%ROWTYPE,
    adit_user_old IN adit_user%ROWTYPE,
    operation IN VARCHAR2
  ) AS
    usr       varchar2(20);
    pkey_col  varchar2(50);
    tablename varchar2(50) := 'ADIT_USER';
    primary_key_v adit_user.user_code%TYPE := adit_user_old.user_code;
    test_date DATE := sysdate;
  BEGIN
  
    -- Current user
    SELECT USER INTO usr FROM dual;
  
    -- user_code changed
    IF(NVL(adit_user_new.user_code, '') != NVL(adit_user_old.user_code, '')) THEN
    
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
        adit_user_old.user_code,
        adit_user_new.user_code,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
    
    -- full_name changed
    IF(NVL(adit_user_new.full_name, '') != NVL(adit_user_old.full_name, '')) THEN
    
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
        'full_name',
        adit_user_old.full_name,
        adit_user_new.full_name,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
    -- usertype changed
    IF(NVL(adit_user_new.usertype, '') != NVL(adit_user_old.usertype, '')) THEN
    
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
        'usertype',
        adit_user_old.usertype,
        adit_user_new.usertype,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
    -- active changed
    IF(NVL(adit_user_new.active, 0) != NVL(adit_user_old.active, 0)) THEN
    
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
        'active',
        adit_user_old.active,
        adit_user_new.active,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
    -- dvk_org_code changed
    IF(NVL(adit_user_new.dvk_org_code, '') != NVL(adit_user_old.dvk_org_code, '')) THEN
    
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
        'dvk_org_code',
        adit_user_old.dvk_org_code,
        adit_user_new.dvk_org_code,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
    -- dvk_subdivision_short_name changed
    IF(NVL(adit_user_new.dvk_subdivision_short_name, '') != NVL(adit_user_old.dvk_subdivision_short_name, '')) THEN
    
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
        'dvk_subdivision_short_name',
        adit_user_old.dvk_subdivision_short_name,
        adit_user_new.dvk_subdivision_short_name,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
    -- dvk_occupation_short_name changed
    IF(NVL(adit_user_new.dvk_occupation_short_name, '') != NVL(adit_user_old.dvk_occupation_short_name, '')) THEN
    
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
        'dvk_occupation_short_name',
        adit_user_old.dvk_occupation_short_name,
        adit_user_new.dvk_occupation_short_name,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
    -- disk_quota changed
    IF(NVL(adit_user_new.disk_quota, 0) != NVL(adit_user_old.disk_quota, 0)) THEN
    
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
        'disk_quota',
        adit_user_old.disk_quota,
        adit_user_new.disk_quota,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
    -- deactivation_date changed
    IF(NVL(adit_user_new.deactivation_date, test_date) != NVL(adit_user_old.deactivation_date, test_date)) THEN
    
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
        'deactivation_date',
        adit_user_old.deactivation_date,
        adit_user_new.deactivation_date,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
  END LOG_ADIT_USER;
  
  
  PROCEDURE LOG_DOCUMENT (
    document_new IN document%ROWTYPE,
    document_old IN document%ROWTYPE,
    operation IN VARCHAR2
  ) AS
    usr       varchar2(20);
    pkey_col  varchar2(50);
    tablename varchar2(50) := 'DOCUMENT';
    primary_key_v NUMBER(18,0) := document_old.id;
    test_date DATE := sysdate;
  BEGIN
  
    -- Current user
    SELECT USER INTO usr FROM dual;
  
    -- id changed
    IF(NVL(document_new.id, 0) != NVL(document_old.id, 0)) THEN
    
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
        document_old.id,
        document_new.id,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
    -- guid changed
    IF(NVL(document_new.guid, '') != NVL(document_old.guid, '')) THEN
    
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
        'guid',
        document_old.guid,
        document_new.guid,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
    -- title changed
    IF(NVL(document_new.title, '') != NVL(document_old.title, '')) THEN
    
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
        'title',
        document_old.title,
        document_new.title,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
    -- type changed
    IF(NVL(document_new.type, '') != NVL(document_old.type, '')) THEN
    
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
        'type',
        document_old.type,
        document_new.type,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
    -- creator_code changed
    IF(NVL(document_new.creator_code, '') != NVL(document_old.creator_code, '')) THEN
    
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
        'creator_code',
        document_old.creator_code,
        document_new.creator_code,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
    -- creator_name changed
    IF(NVL(document_new.creator_name, '') != NVL(document_old.creator_name, '')) THEN
    
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
        'creator_name',
        document_old.creator_name,
        document_new.creator_name,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
    -- creator_user_code changed
    IF(NVL(document_new.creator_user_code, '') != NVL(document_old.creator_user_code, '')) THEN
    
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
        'creator_user_code',
        document_old.creator_user_code,
        document_new.creator_user_code,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
    -- creator_user_name changed
    IF(NVL(document_new.creator_user_name, '') != NVL(document_old.creator_user_name, '')) THEN
    
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
        'creator_user_name',
        document_old.creator_user_name,
        document_new.creator_user_name,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
    -- creation_date changed
    IF(NVL(document_new.creation_date, test_date) != NVL(document_old.creation_date, test_date)) THEN
    
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
        'creation_date',
        document_old.creation_date,
        document_new.creation_date,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
    -- remote_application changed
    IF(NVL(document_new.remote_application, '') != NVL(document_old.remote_application, '')) THEN
    
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
        document_old.remote_application,
        document_new.remote_application,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
    -- last_modified_date changed
    IF(NVL(document_new.last_modified_date, test_date) != NVL(document_old.last_modified_date, test_date)) THEN
    
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
        'last_modified_date',
        document_old.last_modified_date,
        document_new.last_modified_date,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
    -- document_dvk_status_id changed
    IF(NVL(document_new.document_dvk_status_id, 0) != NVL(document_old.document_dvk_status_id, 0)) THEN
    
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
        'document_dvk_status_id',
        document_old.document_dvk_status_id,
        document_new.document_dvk_status_id,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
    -- dvk_id changed
    IF(NVL(document_new.dvk_id, 0) != NVL(document_old.dvk_id, 0)) THEN
    
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
        'dvk_id',
        document_old.dvk_id,
        document_new.dvk_id,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
    -- document_wf_status_id changed
    IF(NVL(document_new.document_wf_status_id, 0) != NVL(document_old.document_wf_status_id, 0)) THEN
    
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
        'document_wf_status_id',
        document_old.document_wf_status_id,
        document_new.document_wf_status_id,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
    -- parent_id changed
    IF(NVL(document_new.parent_id, 0) != NVL(document_old.parent_id, 0)) THEN
    
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
        'parent_id',
        document_old.parent_id,
        document_new.parent_id,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
    -- locked changed
    IF(NVL(document_new.locked, 0) != NVL(document_old.locked, 0)) THEN
    
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
        'locked',
        document_old.locked,
        document_new.locked,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
    -- locking_date changed
    IF(NVL(document_new.locking_date, test_date) != NVL(document_old.locking_date, test_date)) THEN
    
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
        'locking_date',
        document_old.locking_date,
        document_new.locking_date,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
    -- signable changed
    IF(NVL(document_new.signable, 0) != NVL(document_old.signable, 0)) THEN
    
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
        'signable',
        document_old.signable,
        document_new.signable,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
    -- deflated changed
    IF(NVL(document_new.deflated, 0) != NVL(document_old.deflated, 0)) THEN
    
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
        'deflated',
        document_old.deflated,
        document_new.deflated,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
    -- deflate_date changed
    IF(NVL(document_new.deflate_date, test_date) != NVL(document_old.deflate_date, test_date)) THEN
    
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
        'deflate_date',
        document_old.deflate_date,
        document_new.deflate_date,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
    -- deleted changed
    IF(NVL(document_new.deleted, 0) != NVL(document_old.deleted, 0)) THEN
    
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
        'deleted',
        document_old.deleted,
        document_new.deleted,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  
  END LOG_DOCUMENT;
  

END ADITLOG;
/