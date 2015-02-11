CREATE OR REPLACE PACKAGE BODY &&ADIT_SCHEMA..ADITLOG AS

  PROCEDURE LOG_ACCESS_RESTRICTION (
    access_restriction_new IN &&ADIT_SCHEMA..access_restriction%ROWTYPE,
    access_restriction_old IN &&ADIT_SCHEMA..access_restriction%ROWTYPE,
    operation IN VARCHAR2
  ) AS
    usr       varchar2(20);
    pkey_col  varchar2(50);
    tablename varchar2(50) := 'ACCESS_RESTRICTION';
    primary_key_v NUMBER(18,0) := access_restriction_old.id;
  BEGIN

    IF(primary_key_v IS NULL) THEN
      primary_key_v := access_restriction_new.id;
    END IF;

    -- Current user
    SELECT USER INTO usr FROM dual;

    -- id changed
    IF(NVL(access_restriction_new.id, 0) != NVL(access_restriction_old.id, 0)) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
    adit_user_new IN &&ADIT_SCHEMA..adit_user%ROWTYPE,
    adit_user_old IN &&ADIT_SCHEMA..adit_user%ROWTYPE,
    operation IN VARCHAR2
  ) AS
    usr       varchar2(20);
    pkey_col  varchar2(50);
    tablename varchar2(50) := 'ADIT_USER';
    primary_key_v adit_user.user_code%TYPE := adit_user_old.user_code;
    test_date DATE := to_date('1900.01.01', 'yyyy.mm.dd');
  BEGIN

    IF(primary_key_v IS NULL) THEN
      primary_key_v := adit_user_new.user_code;
    END IF;

    -- Current user
    SELECT USER INTO usr FROM dual;

    -- user_code changed
    IF(NVL(adit_user_new.user_code, '') != NVL(adit_user_old.user_code, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        to_char(adit_user_old.deactivation_date, date_format),
        to_char(adit_user_new.deactivation_date, date_format),
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
    document_new IN &&ADIT_SCHEMA..document%ROWTYPE,
    document_old IN &&ADIT_SCHEMA..document%ROWTYPE,
    operation IN VARCHAR2
  ) AS
    usr       varchar2(20);
    pkey_col  varchar2(50);
    tablename varchar2(50) := 'DOCUMENT';
    primary_key_v NUMBER(18,0) := document_old.id;
    test_date DATE := to_date('1900.01.01', 'yyyy.mm.dd');
  BEGIN

    IF(primary_key_v IS NULL) THEN
      primary_key_v := document_new.id;
    END IF;

    -- Current user
    SELECT USER INTO usr FROM dual;

    -- id changed
    IF(NVL(document_new.id, 0) != NVL(document_old.id, 0)) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        to_char(document_old.creation_date, date_format),
        to_char(document_new.creation_date, date_format),
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        to_char(document_old.last_modified_date, date_format),
        to_char(document_new.last_modified_date, date_format),
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        to_char(document_old.locking_date, date_format),
        to_char(document_new.locking_date, date_format),
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        to_char(document_old.deflate_date, date_format),
        to_char(document_new.deflate_date, date_format),
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

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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

  PROCEDURE LOG_DOCUMENT_DVK_STATUS (
    document_dvk_status_new IN &&ADIT_SCHEMA..document_dvk_status%ROWTYPE,
    document_dvk_status_old IN &&ADIT_SCHEMA..document_dvk_status%ROWTYPE,
    operation IN VARCHAR2
  ) AS
    usr       varchar2(20);
    pkey_col  varchar2(50);
    tablename varchar2(50) := 'DOCUMENT_DVK_STATUS';
    primary_key_v NUMBER(18,0) := document_dvk_status_old.id;
    test_date DATE := to_date('1900.01.01', 'yyyy.mm.dd');
  BEGIN

    IF(primary_key_v IS NULL) THEN
      primary_key_v := document_dvk_status_new.id;
    END IF;

    -- Current user
    SELECT USER INTO usr FROM dual;

    -- id changed
    IF(NVL(document_dvk_status_new.id, 0) != NVL(document_dvk_status_old.id, 0)) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        document_dvk_status_old.id,
        document_dvk_status_new.id,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- description changed
    IF(NVL(document_dvk_status_new.description, '') != NVL(document_dvk_status_old.description, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'description',
        document_dvk_status_old.description,
        document_dvk_status_new.description,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

  END LOG_DOCUMENT_DVK_STATUS;

  PROCEDURE LOG_DOCUMENT_FILE (
    document_file_new IN &&ADIT_SCHEMA..document_file%ROWTYPE,
    document_file_old IN &&ADIT_SCHEMA..document_file%ROWTYPE,
    operation IN VARCHAR2
  ) AS
    usr       varchar2(20);
    pkey_col  varchar2(50);
    tablename varchar2(50) := 'DOCUMENT_FILE';
    primary_key_v NUMBER(18,0) := document_file_old.id;
    test_date DATE := to_date('1900.01.01', 'yyyy.mm.dd');
  BEGIN

    IF(primary_key_v IS NULL) THEN
      primary_key_v := document_file_new.id;
    END IF;

    -- Current user
    SELECT USER INTO usr FROM dual;

    -- id changed
    IF(NVL(document_file_new.id, 0) != NVL(document_file_old.id, 0)) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        document_file_old.id,
        document_file_new.id,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- document_id changed
    IF(NVL(document_file_new.document_id, 0) != NVL(document_file_old.document_id, 0)) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'document_id',
        document_file_old.document_id,
        document_file_new.document_id,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- file_name changed
    IF(NVL(document_file_new.file_name, '') != NVL(document_file_old.file_name, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'file_name',
        document_file_old.file_name,
        document_file_new.file_name,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- content_type changed
    IF(NVL(document_file_new.content_type, '') != NVL(document_file_old.content_type, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'content_type',
        document_file_old.content_type,
        document_file_new.content_type,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- description changed
    IF(NVL(document_file_new.description, '') != NVL(document_file_old.description, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'description',
        document_file_old.description,
        document_file_new.description,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- file_size_bytes changed
    IF(NVL(document_file_new.file_size_bytes, 0) != NVL(document_file_old.file_size_bytes, 0)) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'file_size_bytes',
        document_file_old.file_size_bytes,
        document_file_new.file_size_bytes,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- deleted changed
    IF(NVL(document_file_new.deleted, 0) != NVL(document_file_old.deleted, 0)) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        document_file_old.deleted,
        document_file_new.deleted,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

  END LOG_DOCUMENT_FILE;

  PROCEDURE LOG_DOCUMENT_HISTORY (
    document_history_new IN &&ADIT_SCHEMA..document_history%ROWTYPE,
    document_history_old IN &&ADIT_SCHEMA..document_history%ROWTYPE,
    operation IN VARCHAR2
  ) AS
    usr       varchar2(20);
    pkey_col  varchar2(50);
    tablename varchar2(50) := 'DOCUMENT_HISTORY';
    primary_key_v NUMBER(18,0) := document_history_old.id;
    test_date DATE := to_date('1900.01.01', 'yyyy.mm.dd');
  BEGIN

    IF(primary_key_v IS NULL) THEN
      primary_key_v := document_history_new.id;
    END IF;

    -- Current user
    SELECT USER INTO usr FROM dual;

    -- id changed
    IF(NVL(document_history_new.id, 0) != NVL(document_history_old.id, 0)) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        document_history_old.id,
        document_history_new.id,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- document_id changed
    IF(NVL(document_history_new.document_id, 0) != NVL(document_history_old.document_id, 0)) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'document_id',
        document_history_old.document_id,
        document_history_new.document_id,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- document_history_type changed
    IF(NVL(document_history_new.document_history_type, '') != NVL(document_history_old.document_history_type, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'document_history_type',
        document_history_old.document_history_type,
        document_history_new.document_history_type,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- description changed
    IF(NVL(document_history_new.description, '') != NVL(document_history_old.description, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'description',
        document_history_old.description,
        document_history_new.description,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- event_date changed
    IF(NVL(document_history_new.event_date, test_date) != NVL(document_history_old.event_date, test_date)) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'event_date',
        to_char(document_history_old.event_date, date_format),
        to_char(document_history_new.event_date, date_format),
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- user_code changed
    IF(NVL(document_history_new.user_code, '') != NVL(document_history_old.user_code, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        document_history_old.user_code,
        document_history_new.user_code,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- user_name changed
    IF(NVL(document_history_new.user_name, '') != NVL(document_history_old.user_name, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'user_name',
        document_history_old.user_name,
        document_history_new.user_name,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- remote_application changed
    IF(NVL(document_history_new.remote_application, '') != NVL(document_history_old.remote_application, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        document_history_old.remote_application,
        document_history_new.remote_application,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- notification_status changed
    IF(NVL(document_history_new.notification_status, '') != NVL(document_history_old.notification_status, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'notification_status',
        document_history_old.notification_status,
        document_history_new.notification_status,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- xtee_notification_id changed
    IF(NVL(document_history_new.xtee_notification_id, '') != NVL(document_history_old.xtee_notification_id, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'xtee_notification_id',
        document_history_old.xtee_notification_id,
        document_history_new.xtee_notification_id,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- xtee_user_code changed
    IF(NVL(document_history_new.xtee_user_code, '') != NVL(document_history_old.xtee_user_code, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'xtee_user_code',
        document_history_old.xtee_user_code,
        document_history_new.xtee_user_code,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- xtee_user_name changed
    IF(NVL(document_history_new.xtee_user_name, '') != NVL(document_history_old.xtee_user_name, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'xtee_user_name',
        document_history_old.xtee_user_name,
        document_history_new.xtee_user_name,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

  END LOG_DOCUMENT_HISTORY;

  PROCEDURE LOG_DOCUMENT_HISTORY_TYPE (
    document_history_type_new IN &&ADIT_SCHEMA..document_history_type%ROWTYPE,
    document_history_type_old IN &&ADIT_SCHEMA..document_history_type%ROWTYPE,
    operation IN VARCHAR2
  ) AS
    usr       varchar2(20);
    pkey_col  varchar2(50);
    tablename varchar2(50) := 'DOCUMENT_HISTORY_TYPE';
    primary_key_v document_history_type_old.short_name%TYPE := document_history_type_old.short_name;
    test_date DATE := to_date('1900.01.01', 'yyyy.mm.dd');
  BEGIN

    IF(primary_key_v IS NULL) THEN
      primary_key_v := document_history_type_new.short_name;
    END IF;

    -- Current user
    SELECT USER INTO usr FROM dual;

    -- short_name changed
    IF(NVL(document_history_type_new.short_name, '') != NVL(document_history_type_old.short_name, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'short_name',
        document_history_type_old.short_name,
        document_history_type_new.short_name,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- description changed
    IF(NVL(document_history_type_new.description, '') != NVL(document_history_type_old.description, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'description',
        document_history_type_old.description,
        document_history_type_new.description,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

  END LOG_DOCUMENT_HISTORY_TYPE;

  PROCEDURE LOG_DOCUMENT_SHARING (
    document_sharing_new IN &&ADIT_SCHEMA..document_sharing%ROWTYPE,
    document_sharing_old IN &&ADIT_SCHEMA..document_sharing%ROWTYPE,
    operation IN VARCHAR2
  ) AS
    usr       varchar2(20);
    pkey_col  varchar2(50);
    tablename varchar2(50) := 'DOCUMENT_SHARING';
    primary_key_v document_sharing_old.id%TYPE := document_sharing_old.id;
    test_date DATE := to_date('1900.01.01', 'yyyy.mm.dd');
  BEGIN

    IF(primary_key_v IS NULL) THEN
      primary_key_v := document_sharing_new.id;
    END IF;

    -- Current user
    SELECT USER INTO usr FROM dual;

    -- id changed
    IF(NVL(document_sharing_new.id, 0) != NVL(document_sharing_old.id, 0)) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        document_sharing_old.id,
        document_sharing_new.id,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- document_id changed
    IF(NVL(document_sharing_new.document_id, 0) != NVL(document_sharing_old.document_id, 0)) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'document_id',
        document_sharing_old.document_id,
        document_sharing_new.document_id,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- user_code changed
    IF(NVL(document_sharing_new.user_code, '') != NVL(document_sharing_old.user_code, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        document_sharing_old.user_code,
        document_sharing_new.user_code,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- user_name changed
    IF(NVL(document_sharing_new.user_name, '') != NVL(document_sharing_old.user_name, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'user_name',
        document_sharing_old.user_name,
        document_sharing_new.user_name,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- sharing_type changed
    IF(NVL(document_sharing_new.sharing_type, '') != NVL(document_sharing_old.sharing_type, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'sharing_type',
        document_sharing_old.sharing_type,
        document_sharing_new.sharing_type,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- task_description changed
    IF(NVL(document_sharing_new.task_description, '') != NVL(document_sharing_old.task_description, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'task_description',
        document_sharing_old.task_description,
        document_sharing_new.task_description,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- creation_date changed
    IF(NVL(document_sharing_new.creation_date, test_date) != NVL(document_sharing_old.creation_date, test_date)) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        to_char(document_sharing_old.creation_date, date_format),
        to_char(document_sharing_new.creation_date, date_format),
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- dvk_status_id changed
    IF(NVL(document_sharing_new.dvk_status_id, 0) != NVL(document_sharing_old.dvk_status_id, 0)) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'dvk_status_id',
        document_sharing_old.dvk_status_id,
        document_sharing_new.dvk_status_id,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- wf_status_id changed
    IF(NVL(document_sharing_new.wf_status_id, 0) != NVL(document_sharing_old.wf_status_id, 0)) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'wf_status_id',
        document_sharing_old.wf_status_id,
        document_sharing_new.wf_status_id,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- first_access_date changed
    IF(NVL(document_sharing_new.first_access_date, test_date) != NVL(document_sharing_old.first_access_date, test_date)) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'first_access_date',
        to_char(document_sharing_old.first_access_date, date_format),
        to_char(document_sharing_new.first_access_date, date_format),
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
   -- dvk_id changed
    IF(NVL(document_sharing_new.dvk_id, 0) != NVL(document_sharing_old.dvk_id, 0)) THEN
      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        document_sharing_old.dvk_id,
        document_sharing_new.dvk_id,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;
  END LOG_DOCUMENT_SHARING;

  PROCEDURE LOG_DOCUMENT_SHARING_TYPE (
    document_sharing_type_new IN &&ADIT_SCHEMA..document_sharing_type%ROWTYPE,
    document_sharing_type_old IN &&ADIT_SCHEMA..document_sharing_type%ROWTYPE,
    operation IN VARCHAR2
  ) AS
    usr       varchar2(20);
    pkey_col  varchar2(50);
    tablename varchar2(50) := 'DOCUMENT_SHARING_TYPE';
    primary_key_v document_sharing_type_old.short_name%TYPE := document_sharing_type_old.short_name;
    test_date DATE := to_date('1900.01.01', 'yyyy.mm.dd');
  BEGIN

    IF(primary_key_v IS NULL) THEN
      primary_key_v := document_sharing_type_new.short_name;
    END IF;

    -- Current user
    SELECT USER INTO usr FROM dual;

    -- short_name changed
    IF(NVL(document_sharing_type_new.short_name, '') != NVL(document_sharing_type_old.short_name, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'short_name',
        document_sharing_type_old.short_name,
        document_sharing_type_new.short_name,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- description changed
    IF(NVL(document_sharing_type_new.description, '') != NVL(document_sharing_type_old.description, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'description',
        document_sharing_type_old.description,
        document_sharing_type_new.description,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

  END LOG_DOCUMENT_SHARING_TYPE;

  PROCEDURE LOG_DOCUMENT_TYPE (
    document_type_new IN &&ADIT_SCHEMA..document_type%ROWTYPE,
    document_type_old IN &&ADIT_SCHEMA..document_type%ROWTYPE,
    operation IN VARCHAR2
  ) AS
    usr       varchar2(20);
    pkey_col  varchar2(50);
    tablename varchar2(50) := 'DOCUMENT_TYPE';
    primary_key_v document_type_old.short_name%TYPE := document_type_old.short_name;
    test_date DATE := to_date('1900.01.01', 'yyyy.mm.dd');
  BEGIN

    IF(primary_key_v IS NULL) THEN
      primary_key_v := document_type_new.short_name;
    END IF;

    -- Current user
    SELECT USER INTO usr FROM dual;

    -- short_name changed
    IF(NVL(document_type_new.short_name, '') != NVL(document_type_old.short_name, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'short_name',
        document_type_old.short_name,
        document_type_new.short_name,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- description changed
    IF(NVL(document_type_new.description, '') != NVL(document_type_old.description, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'description',
        document_type_old.description,
        document_type_new.description,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

  END LOG_DOCUMENT_TYPE;

  PROCEDURE LOG_DOCUMENT_WF_STATUS (
    document_wf_status_new IN &&ADIT_SCHEMA..document_wf_status%ROWTYPE,
    document_wf_status_old IN &&ADIT_SCHEMA..document_wf_status%ROWTYPE,
    operation IN VARCHAR2
  ) AS
    usr       varchar2(20);
    pkey_col  varchar2(50);
    tablename varchar2(50) := 'DOCUMENT_WF_STATUS';
    primary_key_v document_wf_status_old.id%TYPE := document_wf_status_old.id;
    test_date DATE := to_date('1900.01.01', 'yyyy.mm.dd');
  BEGIN

    IF(primary_key_v IS NULL) THEN
      primary_key_v := document_wf_status_new.id;
    END IF;

    -- Current user
    SELECT USER INTO usr FROM dual;

    -- id changed
    IF(NVL(document_wf_status_new.id, 0) != NVL(document_wf_status_old.id, 0)) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        document_wf_status_old.id,
        document_wf_status_new.id,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- description changed
    IF(NVL(document_wf_status_new.description, '') != NVL(document_wf_status_old.description, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'description',
        document_wf_status_old.description,
        document_wf_status_new.description,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- name changed
    IF(NVL(document_wf_status_new.name, '') != NVL(document_wf_status_old.name, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'name',
        document_wf_status_old.name,
        document_wf_status_new.name,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

  END LOG_DOCUMENT_WF_STATUS;

  PROCEDURE LOG_NOTIFICATION (
    notification_new IN &&ADIT_SCHEMA..notification%ROWTYPE,
    notification_old IN &&ADIT_SCHEMA..notification%ROWTYPE,
    operation IN VARCHAR2
  ) AS
    usr       varchar2(20);
    pkey_col  varchar2(50);
    tablename varchar2(50) := 'NOTIFICATION';
    primary_key_v notification_old.id%TYPE := notification_old.id;
    test_date DATE := to_date('1900.01.01', 'yyyy.mm.dd');
  BEGIN

    IF(primary_key_v IS NULL) THEN
      primary_key_v := notification_new.id;
    END IF;

    -- Current user
    SELECT USER INTO usr FROM dual;

    -- id changed
    IF(NVL(notification_new.id, 0) != NVL(notification_old.id, 0)) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        notification_old.id,
        notification_new.id,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- user_code changed
    IF(NVL(notification_new.user_code, '') != NVL(notification_old.user_code, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        notification_old.user_code,
        notification_new.user_code,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- document_id changed
    IF(NVL(notification_new.document_id, 0) != NVL(notification_old.document_id, 0)) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'document_id',
        notification_old.document_id,
        notification_new.document_id,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- event_date changed
    IF(NVL(notification_new.event_date, test_date) != NVL(notification_old.event_date, test_date)) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'event_date',
        to_char(notification_old.event_date, date_format),
        to_char(notification_new.event_date, date_format),
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- notification_type changed
    IF(NVL(notification_new.notification_type, '') != NVL(notification_old.notification_type, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'notification_type',
        notification_old.notification_type,
        notification_new.notification_type,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- notification_text changed
    IF(NVL(notification_new.notification_text, '') != NVL(notification_old.notification_text, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'notification_text',
        notification_old.notification_text,
        notification_new.notification_text,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- notification_id changed
    IF(NVL(notification_new.notification_id, 0) != NVL(notification_old.notification_id, 0)) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'notification_id',
        notification_old.notification_id,
        notification_new.notification_id,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- notification_sending_date changed
    IF(NVL(notification_new.notification_sending_date, test_date) != NVL(notification_old.notification_sending_date, test_date)) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'notification_sending_date',
        to_char(notification_old.notification_sending_date, date_format),
        to_char(notification_new.notification_sending_date, date_format),
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

  END LOG_NOTIFICATION;

  PROCEDURE LOG_NOTIFICATION_TYPE (
    notification_type_new IN &&ADIT_SCHEMA..notification_type%ROWTYPE,
    notification_type_old IN &&ADIT_SCHEMA..notification_type%ROWTYPE,
    operation IN VARCHAR2
  ) AS
    usr       varchar2(20);
    pkey_col  varchar2(50);
    tablename varchar2(50) := 'NOTIFICATION_TYPE';
    primary_key_v notification_type_old.short_name%TYPE := notification_type_old.short_name;
    test_date DATE := to_date('1900.01.01', 'yyyy.mm.dd');
  BEGIN

    IF(primary_key_v IS NULL) THEN
      primary_key_v := notification_type_new.short_name;
    END IF;

    -- Current user
    SELECT USER INTO usr FROM dual;

    -- short_name changed
    IF(NVL(notification_type_new.short_name, '') != NVL(notification_type_old.short_name, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'short_name',
        notification_type_old.short_name,
        notification_type_new.short_name,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- description changed
    IF(NVL(notification_type_new.description, '') != NVL(notification_type_old.description, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'description',
        notification_type_old.description,
        notification_type_new.description,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

  END LOG_NOTIFICATION_TYPE;

  PROCEDURE LOG_REMOTE_APPLICATION (
    remote_application_new IN &&ADIT_SCHEMA..remote_application%ROWTYPE,
    remote_application_old IN &&ADIT_SCHEMA..remote_application%ROWTYPE,
    operation IN VARCHAR2
  ) AS
    usr       varchar2(20);
    pkey_col  varchar2(50);
    tablename varchar2(50) := 'REMOTE_APPLICATION';
    primary_key_v remote_application_old.short_name%TYPE := remote_application_old.short_name;
    test_date DATE := to_date('1900.01.01', 'yyyy.mm.dd');
  BEGIN

    IF(primary_key_v IS NULL) THEN
      primary_key_v := remote_application_new.short_name;
    END IF;

    -- Current user
    SELECT USER INTO usr FROM dual;

    -- short_name changed
    IF(NVL(remote_application_new.short_name, '') != NVL(remote_application_old.short_name, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'short_name',
        remote_application_old.short_name,
        remote_application_new.short_name,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- name changed
    IF(NVL(remote_application_new.name, '') != NVL(remote_application_old.name, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'name',
        remote_application_old.name,
        remote_application_new.name,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- organization_code changed
    IF(NVL(remote_application_new.organization_code, '') != NVL(remote_application_old.organization_code, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'organization_code',
        remote_application_old.organization_code,
        remote_application_new.organization_code,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- can_read changed
    IF(NVL(remote_application_new.can_read, 0) != NVL(remote_application_old.can_read, 0)) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'can_read',
        remote_application_old.can_read,
        remote_application_new.can_read,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- can_write changed
    IF(NVL(remote_application_new.can_write, 0) != NVL(remote_application_old.can_write, 0)) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'can_write',
        remote_application_old.can_write,
        remote_application_new.can_write,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

  END LOG_REMOTE_APPLICATION;

  PROCEDURE LOG_SIGNATURE (
    signature_new IN &&ADIT_SCHEMA..signature%ROWTYPE,
    signature_old IN &&ADIT_SCHEMA..signature%ROWTYPE,
    operation IN VARCHAR2
  ) AS
    usr       varchar2(20);
    pkey_col  varchar2(50);
    tablename varchar2(50) := 'SIGNATURE';
    primary_key_v signature_old.id%TYPE := signature_old.id;
    test_date DATE := to_date('1900.01.01', 'yyyy.mm.dd');
  BEGIN

    IF(primary_key_v IS NULL) THEN
      primary_key_v := signature_new.id;
    END IF;

    -- Current user
    SELECT USER INTO usr FROM dual;

    -- id changed
    IF(NVL(signature_new.id, 0) != NVL(signature_old.id, 0)) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        signature_old.id,
        signature_new.id,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- user_code changed
    IF(NVL(signature_new.user_code, '') != NVL(signature_old.user_code, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        signature_old.user_code,
        signature_new.user_code,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- document_id changed
    IF(NVL(signature_new.document_id, 0) != NVL(signature_old.document_id, 0)) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'document_id',
        signature_old.document_id,
        signature_new.document_id,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- signer_role changed
    IF(NVL(signature_new.signer_role, '') != NVL(signature_old.signer_role, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'signer_role',
        signature_old.signer_role,
        signature_new.signer_role,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- resolution changed
    IF(NVL(signature_new.resolution, '') != NVL(signature_old.resolution, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'resolution',
        signature_old.resolution,
        signature_new.resolution,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- country changed
    IF(NVL(signature_new.country, '') != NVL(signature_old.country, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'country',
        signature_old.country,
        signature_new.country,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- county changed
    IF(NVL(signature_new.county, '') != NVL(signature_old.county, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'county',
        signature_old.county,
        signature_new.county,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- city changed
    IF(NVL(signature_new.city, '') != NVL(signature_old.city, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'city',
        signature_old.city,
        signature_new.city,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- post_index changed
    IF(NVL(signature_new.post_index, '') != NVL(signature_old.post_index, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'post_index',
        signature_old.post_index,
        signature_new.post_index,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- signer_code changed
    IF(NVL(signature_new.signer_code, '') != NVL(signature_old.signer_code, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'signer_code',
        signature_old.signer_code,
        signature_new.signer_code,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- signer_name changed
    IF(NVL(signature_new.signer_name, '') != NVL(signature_old.signer_name, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'signer_name',
        signature_old.signer_name,
        signature_new.signer_name,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

  END LOG_SIGNATURE;

  PROCEDURE LOG_USER_NOTIFICATION (
    user_notification_new IN &&ADIT_SCHEMA..user_notification%ROWTYPE,
    user_notification_old IN &&ADIT_SCHEMA..user_notification%ROWTYPE,
    operation IN VARCHAR2
  ) AS
    usr       varchar2(20);
    pkey_col  varchar2(50);
    tablename varchar2(50) := 'USER_NOTIFICATION';
    primary_key_v user_notification_old.user_code%TYPE := user_notification_old.user_code;
    test_date DATE := to_date('1900.01.01', 'yyyy.mm.dd');
  BEGIN

    IF(primary_key_v IS NULL) THEN
      primary_key_v := user_notification_new.user_code;
    END IF;

    -- Current user
    SELECT USER INTO usr FROM dual;

    -- user_code changed
    IF(NVL(user_notification_new.user_code, '') != NVL(user_notification_old.user_code, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        user_notification_old.user_code,
        user_notification_new.user_code,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- notification_type changed
    IF(NVL(user_notification_new.notification_type, '') != NVL(user_notification_old.notification_type, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'notification_type',
        user_notification_old.notification_type,
        user_notification_new.notification_type,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

  END LOG_USER_NOTIFICATION;

  PROCEDURE LOG_USERTYPE (
    usertype_new IN &&ADIT_SCHEMA..usertype%ROWTYPE,
    usertype_old IN &&ADIT_SCHEMA..usertype%ROWTYPE,
    operation IN VARCHAR2
  ) AS
    usr       varchar2(20);
    pkey_col  varchar2(50);
    tablename varchar2(50) := 'USERTYPE';
    primary_key_v usertype_old.short_name%TYPE := usertype_old.short_name;
    test_date DATE := to_date('1900.01.01', 'yyyy.mm.dd');
  BEGIN

    IF(primary_key_v IS NULL) THEN
      primary_key_v := usertype_new.short_name;
    END IF;

    -- Current user
    SELECT USER INTO usr FROM dual;

    -- short_name changed
    IF(NVL(usertype_new.short_name, '') != NVL(usertype_old.short_name, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'short_name',
        usertype_old.short_name,
        usertype_new.short_name,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- description changed
    IF(NVL(usertype_new.description, '') != NVL(usertype_old.description, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'description',
        usertype_old.description,
        usertype_new.description,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- disk_quota changed
    IF(NVL(usertype_new.disk_quota, 0) != NVL(usertype_old.disk_quota, 0)) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        usertype_old.disk_quota,
        usertype_new.disk_quota,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

  END LOG_USERTYPE;

  PROCEDURE LOG_USER_CONTACT(
    user_contact_new IN &&ADIT_SCHEMA..user_contact%ROWTYPE,
    user_contact_old IN &&ADIT_SCHEMA..user_contact%ROWTYPE,
    operation IN VARCHAR2
  ) AS
    usr       varchar2(20);
    pkey_col  varchar2(50);
    tablename varchar2(50) := 'USER_CONTACT';
    primary_key_v user_contact_old.user_code%TYPE := user_contact_old.user_code;
    test_date DATE := to_date('1900.01.01', 'yyyy.mm.dd');
  BEGIN

    IF(primary_key_v IS NULL) THEN
      primary_key_v := user_contact_new.user_code;
    END IF;

    -- Current user
    SELECT USER INTO usr FROM dual;

    -- user_code changed
    IF(NVL(user_contact_new.user_code, '') != NVL(user_contact_old.user_code, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        user_contact_old.user_code,
        user_contact_new.user_code,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- contact_code changed
    IF(NVL(user_contact_new.contact_code, '') != NVL(user_contact_old.contact_code, '')) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'contact_code',
        user_contact_old.contact_code,
        user_contact_new.contact_code,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

    -- last_used_date changed
    IF(NVL(user_contact_new.last_used_date, test_date) != NVL(user_contact_old.last_used_date, test_date)) THEN

      INSERT INTO &&ADIT_SCHEMA..adit_log(
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
        'last_used_date',
        user_contact_old.last_used_date,
        user_contact_new.last_used_date,
        sysdate,
        ADITLOG.remote_application_short_name,
        ADITLOG.xtee_isikukood,
        ADITLOG.xtee_asutus,
        usr,
        primary_key_v
      );
    END IF;

  END LOG_USER_CONTACT;

END ADITLOG;
/