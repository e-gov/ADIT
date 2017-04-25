--
-- Definition for function log_document (OID = 24583) : 
--
CREATE OR REPLACE FUNCTION aditlog.log_document (
  document_new adit.document,
  document_old adit.document,
  operation text
)
RETURNS void
AS 
$body$
DECLARE

    usr       varchar(20);
    pkey_col  varchar(50);
    tablename varchar(50) := 'DOCUMENT';
    primary_key_v bigint := document_old.id;
    test_date timestamp := aditlog.get_test_date();
  
BEGIN
  
    IF(coalesce(primary_key_v::text, '') = '') THEN
      primary_key_v := document_new.id;
    END IF;
  
    -- Current user
    SELECT USER INTO usr ;
  
    -- id changed
    IF(coalesce(document_new.id, 0) != coalesce(document_old.id, 0)) THEN
    
      INSERT INTO adit.adit_log(
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
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- guid changed
    IF(coalesce(document_new.guid, '') != coalesce(document_old.guid, '')) THEN
    
      INSERT INTO adit.adit_log(
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
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- title changed
    IF(coalesce(document_new.title, '') != coalesce(document_old.title, '')) THEN
    
      INSERT INTO adit.adit_log(
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
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- type changed
    IF(coalesce(document_new.type, '') != coalesce(document_old.type, '')) THEN
    
      INSERT INTO adit.adit_log(
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
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- creator_code changed
    IF(coalesce(document_new.creator_code, '') != coalesce(document_old.creator_code, '')) THEN
    
      INSERT INTO adit.adit_log(
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
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- creator_name changed
    IF(coalesce(document_new.creator_name, '') != coalesce(document_old.creator_name, '')) THEN
    
      INSERT INTO adit.adit_log(
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
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- creator_user_code changed
    IF(coalesce(document_new.creator_user_code, '') != coalesce(document_old.creator_user_code, '')) THEN
    
      INSERT INTO adit.adit_log(
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
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- creator_user_name changed
    IF(coalesce(document_new.creator_user_name, '') != coalesce(document_old.creator_user_name, '')) THEN
    
      INSERT INTO adit.adit_log(
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
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- creation_date changed
    IF(coalesce(document_new.creation_date, test_date) != coalesce(document_old.creation_date, test_date)) THEN
    
      INSERT INTO adit.adit_log(
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
        document_old.creation_date::character varying,
        document_new.creation_date::character varying,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- remote_application changed
    IF(coalesce(document_new.remote_application, '') != coalesce(document_old.remote_application, '')) THEN
    
      INSERT INTO adit.adit_log(
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
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- last_modified_date changed
    IF(coalesce(document_new.last_modified_date, test_date) != coalesce(document_old.last_modified_date, test_date)) THEN
    
      INSERT INTO adit.adit_log(
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
        document_old.last_modified_date::character varying,
        document_new.last_modified_date::character varying,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- document_dvk_status_id changed
    IF(coalesce(document_new.document_dvk_status_id, 0) != coalesce(document_old.document_dvk_status_id, 0)) THEN
    
      INSERT INTO adit.adit_log(
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
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- dvk_id changed
    IF(coalesce(document_new.dvk_id, 0) != coalesce(document_old.dvk_id, 0)) THEN
    
      INSERT INTO adit.adit_log(
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
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- document_wf_status_id changed
    IF(coalesce(document_new.document_wf_status_id, 0) != coalesce(document_old.document_wf_status_id, 0)) THEN
    
      INSERT INTO adit.adit_log(
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
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- parent_id changed
    IF(coalesce(document_new.parent_id, 0) != coalesce(document_old.parent_id, 0)) THEN
    
      INSERT INTO adit.adit_log(
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
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- locked changed
    IF(coalesce(document_new.locked, 0) != coalesce(document_old.locked, 0)) THEN
    
      INSERT INTO adit.adit_log(
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
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- locking_date changed
    IF(coalesce(document_new.locking_date, test_date) != coalesce(document_old.locking_date, test_date)) THEN
    
      INSERT INTO adit.adit_log(
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
        document_old.locking_date::character varying,
        document_new.locking_date::character varying,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- signable changed
    IF(coalesce(document_new.signable, 0) != coalesce(document_old.signable, 0)) THEN
    
      INSERT INTO adit.adit_log(
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
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- deflated changed
    IF(coalesce(document_new.deflated, 0) != coalesce(document_old.deflated, 0)) THEN
    
      INSERT INTO adit.adit_log(
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
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- deflate_date changed
    IF(coalesce(document_new.deflate_date, test_date) != coalesce(document_old.deflate_date, test_date)) THEN
    
      INSERT INTO adit.adit_log(
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
        document_old.deflate_date::character varying,
        document_new.deflate_date::character varying,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- deleted changed
    IF(coalesce(document_new.deleted, 0) != coalesce(document_old.deleted, 0)) THEN
    
      INSERT INTO adit.adit_log(
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
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- dhx_receipt_id changed
    IF(coalesce(document_new.dhx_receipt_id, '') != coalesce(document_old.dhx_receipt_id, '')) THEN
    
      INSERT INTO adit.adit_log(
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
        'dhx_receipt_id',
        document_old.dhx_receipt_id,
        document_new.dhx_receipt_id,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
     -- dhx_consignment_id changed
    IF(coalesce(document_new.dhx_consignment_id, '') != coalesce(document_old.dhx_consignment_id, '')) THEN
    
      INSERT INTO adit.adit_log(
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
        'dhx_consignment_id',
        document_old.dhx_consignment_id,
        document_new.dhx_consignment_id,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
END;
$body$
LANGUAGE plpgsql
SECURITY DEFINER;
--
-- Definition for function log_document_sharing (OID = 24587) : 
--
CREATE OR REPLACE FUNCTION aditlog.log_document_sharing (
  document_sharing_new adit.document_sharing,
  document_sharing_old adit.document_sharing,
  operation text
)
RETURNS void
AS 
$body$
DECLARE

    usr       varchar(20);
    pkey_col  varchar(50);
    tablename varchar(50) := 'DOCUMENT_SHARING';
    primary_key_v adit.document_sharing.id%TYPE := document_sharing_old.id;
    test_date timestamp := aditlog.get_test_date();
  
BEGIN
  
    IF(coalesce(primary_key_v::text, '') = '') THEN
      primary_key_v := document_sharing_new.id;
    END IF;
  
    -- Current user
    SELECT USER INTO usr ;
  
    -- id changed
    IF(coalesce(document_sharing_new.id, 0) != coalesce(document_sharing_old.id, 0)) THEN
    
      INSERT INTO adit.adit_log(
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
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- document_id changed
    IF(coalesce(document_sharing_new.document_id, 0) != coalesce(document_sharing_old.document_id, 0)) THEN
    
      INSERT INTO adit.adit_log(
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
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- user_code changed
    IF(coalesce(document_sharing_new.user_code, '') != coalesce(document_sharing_old.user_code, '')) THEN
    
      INSERT INTO adit.adit_log(
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
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- user_name changed
    IF(coalesce(document_sharing_new.user_name, '') != coalesce(document_sharing_old.user_name, '')) THEN
    
      INSERT INTO adit.adit_log(
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
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- sharing_type changed
    IF(coalesce(document_sharing_new.sharing_type, '') != coalesce(document_sharing_old.sharing_type, '')) THEN
    
      INSERT INTO adit.adit_log(
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
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- task_description changed
    IF(coalesce(document_sharing_new.task_description, '') != coalesce(document_sharing_old.task_description, '')) THEN
    
      INSERT INTO adit.adit_log(
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
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- creation_date changed
    IF(coalesce(document_sharing_new.creation_date, test_date) != coalesce(document_sharing_old.creation_date, test_date)) THEN
    
      INSERT INTO adit.adit_log(
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
        document_sharing_old.creation_date::character varying,
        document_sharing_new.creation_date::character varying,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- dvk_status_id changed
    IF(coalesce(document_sharing_new.dvk_status_id, 0) != coalesce(document_sharing_old.dvk_status_id, 0)) THEN
    
      INSERT INTO adit.adit_log(
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
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- wf_status_id changed
    IF(coalesce(document_sharing_new.wf_status_id, 0) != coalesce(document_sharing_old.wf_status_id, 0)) THEN
    
      INSERT INTO adit.adit_log(
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
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- first_access_date changed
    IF(coalesce(document_sharing_new.first_access_date, test_date) != coalesce(document_sharing_old.first_access_date, test_date)) THEN
    
      INSERT INTO adit.adit_log(
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
        document_sharing_old.first_access_date::character varying,
        document_sharing_new.first_access_date::character varying,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
   -- dvk_id changed
    IF(coalesce(document_sharing_new.dvk_id, 0) != coalesce(document_sharing_old.dvk_id, 0)) THEN 
      INSERT INTO adit.adit_log(
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
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    -- dhx_receipt_id changed
    IF(coalesce(document_sharing_new.dhx_receipt_id, '') != coalesce(document_sharing_old.dhx_receipt_id, '')) THEN 
      INSERT INTO adit.adit_log(
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
        'dhx_receipt_id',
        document_sharing_old.dhx_receipt_id,
        document_sharing_new.dhx_receipt_id,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
        -- dhx_receipt_id changed
    IF(coalesce(document_sharing_new.dhx_consignment_id, '') != coalesce(document_sharing_old.dhx_consignment_id, '')) THEN 
      INSERT INTO adit.adit_log(
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
        'dhx_consignment_id',
        document_sharing_old.dhx_consignment_id,
        document_sharing_new.dhx_consignment_id,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
        -- dhx_fault changed
    IF(coalesce(document_sharing_new.dhx_fault, '') != coalesce(document_sharing_old.dhx_fault, '')) THEN 
      INSERT INTO adit.adit_log(
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
        'dhx_fault',
        document_sharing_old.dhx_fault,
        document_sharing_new.dhx_fault,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- first_access_date changed
    IF(coalesce(document_sharing_new.dhx_received_date, test_date) != coalesce(document_sharing_old.dhx_received_date, test_date)) THEN
    
      INSERT INTO adit.adit_log(
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
        'dhx_received_date',
        document_sharing_old.dhx_received_date::character varying,
        document_sharing_new.dhx_received_date::character varying,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
     -- first_access_date changed
    IF(coalesce(document_sharing_new.dhx_sent_date, test_date) != coalesce(document_sharing_old.dhx_sent_date, test_date)) THEN
    
      INSERT INTO adit.adit_log(
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
        'dhx_sent_date',
        document_sharing_old.dhx_sent_date::character varying,
        document_sharing_new.dhx_sent_date::character varying,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
END;
$body$
LANGUAGE plpgsql
SECURITY DEFINER;