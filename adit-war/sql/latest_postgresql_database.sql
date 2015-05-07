SET search_path = adit, pg_catalog;
ALTER TABLE ONLY adit.document DROP CONSTRAINT IF EXISTS parent_document_id;
DROP TRIGGER IF EXISTS tr_user_notification_log ON adit.user_notification;
DROP TRIGGER IF EXISTS tr_user_contact_log ON user_contact;
DROP TRIGGER IF EXISTS tr_usertype_log ON adit.usertype;
DROP TRIGGER IF EXISTS tr_signature_log ON adit.signature;
DROP TRIGGER IF EXISTS tr_remote_application_log ON adit.remote_application;
DROP TRIGGER IF EXISTS tr_notification_type_log ON adit.notification_type;
DROP TRIGGER IF EXISTS tr_notification_log ON adit.notification;
DROP TRIGGER IF EXISTS tr_document_dvk_status_log ON adit.document_dvk_status;
DROP TRIGGER IF EXISTS tr_document_wf_status_log ON adit.document_wf_status;
DROP TRIGGER IF EXISTS tr_document_type_log ON adit.document_type;
DROP TRIGGER IF EXISTS tr_document_sharing_type_log ON adit.document_sharing_type;
DROP TRIGGER IF EXISTS tr_document_sharing_log ON adit.document_sharing;
DROP TRIGGER IF EXISTS tr_document_log ON adit.document;
DROP TRIGGER IF EXISTS tr_document_history_type_log ON adit.document_history_type;
DROP TRIGGER IF EXISTS tr_document_history_log ON adit.document_history;
DROP TRIGGER IF EXISTS tr_document_file_log ON adit.document_file;
DROP TRIGGER IF EXISTS tr_adit_user_log ON adit.adit_user;
DROP TRIGGER IF EXISTS tr_access_restriction_log ON adit.access_restriction;
DROP TRIGGER IF EXISTS set_adit_log_id ON adit.adit_log;
DROP INDEX IF EXISTS adit.access_restriction_app_idx;
DROP INDEX IF EXISTS adit.access_restriction_user_idx;
DROP INDEX IF EXISTS adit.maintenance_job_search_idx;
DROP INDEX IF EXISTS adit.signature_user_idx;
DROP INDEX IF EXISTS adit.signature_docid_idx;
DROP INDEX IF EXISTS adit.user_notification_type_idx;
DROP INDEX IF EXISTS adit.user_notification_user_idx;
DROP INDEX IF EXISTS adit.document_history_user_idx;
DROP INDEX IF EXISTS adit.document_history_app_idx;
DROP INDEX IF EXISTS adit.document_history_docid_idx;
DROP INDEX IF EXISTS adit.document_history_search_idx;
DROP INDEX IF EXISTS adit.document_history_type_idx;
DROP INDEX IF EXISTS adit.document_file_docid_idx;
DROP INDEX IF EXISTS adit.document_parent_idx;
DROP INDEX IF EXISTS adit.document_app_idx;
DROP INDEX IF EXISTS adit.document_documenttype_idx;
DROP INDEX IF EXISTS adit.document_wfstatus_idx;
DROP INDEX IF EXISTS adit.document_dvkstatus_idx;
DROP INDEX IF EXISTS adit.document_sharing_dvkstatus_idx;
DROP INDEX IF EXISTS adit.document_sharing_user_idx;
DROP INDEX IF EXISTS adit.document_sharing_docid_idx;
DROP INDEX IF EXISTS adit.document_sharing_type_idx;
DROP INDEX IF EXISTS adit.document_sharing_wfstatus_idx;
DROP INDEX IF EXISTS adit.adit_user_active_idx;
DROP INDEX IF EXISTS adit.adit_user_usertype_idx;
SET search_path = aditlog, pg_catalog;
DROP FUNCTION IF EXISTS aditlog.get_current_setting (variable_name varchar);
SET search_path = adit, pg_catalog;
DROP FUNCTION IF EXISTS adit.trigger_fct_tr_user_notification_log ();
DROP FUNCTION IF EXISTS adit.trigger_fct_tr_user_contact_log ();
DROP FUNCTION IF EXISTS adit.trigger_fct_tr_usertype_log ();
DROP FUNCTION IF EXISTS adit.trigger_fct_tr_signature_log ();
DROP FUNCTION IF EXISTS adit.trigger_fct_tr_remote_application_log ();
DROP FUNCTION IF EXISTS adit.trigger_fct_tr_notification_type_log ();
DROP FUNCTION IF EXISTS adit.trigger_fct_tr_notification_log ();
DROP FUNCTION IF EXISTS adit.trigger_fct_tr_document_dvk_status_log ();
DROP FUNCTION IF EXISTS adit.trigger_fct_tr_document_wf_status_log ();
DROP FUNCTION IF EXISTS adit.trigger_fct_tr_document_type_log ();
DROP FUNCTION IF EXISTS adit.trigger_fct_tr_document_sharing_type_log ();
DROP FUNCTION IF EXISTS adit.trigger_fct_tr_document_sharing_log ();
DROP FUNCTION IF EXISTS adit.trigger_fct_tr_document_log ();
DROP FUNCTION IF EXISTS adit.trigger_fct_tr_document_history_type_log ();
DROP FUNCTION IF EXISTS adit.trigger_fct_tr_document_history_log ();
DROP FUNCTION IF EXISTS adit.trigger_fct_tr_document_file_log ();
DROP FUNCTION IF EXISTS adit.trigger_fct_tr_adit_user_log ();
DROP FUNCTION IF EXISTS adit.trigger_fct_tr_access_restriction_log ();
DROP FUNCTION IF EXISTS adit.trigger_fct_set_adit_log_id ();
SET search_path = adit, pg_catalog;
DROP FUNCTION IF EXISTS adit.remove_signed_file_contents (document_id bigint, file_id bigint, ddoc_start_offset bigint, ddoc_end_offset bigint);
DROP FUNCTION IF EXISTS adit.deflate_file (document_id bigint, file_id bigint, mark_deleted bigint, fail_if_signature bigint);
SET search_path = aditlog, pg_catalog;
DROP FUNCTION IF EXISTS aditlog.get_test_date ();
DROP FUNCTION IF EXISTS aditlog.log_user_contact (user_contact_new adit.user_contact, user_contact_old adit.user_contact, operation text);
DROP FUNCTION IF EXISTS aditlog.log_usertype (usertype_new adit.usertype, usertype_old adit.usertype, operation text);
DROP FUNCTION IF EXISTS aditlog.log_user_notification (user_notification_new adit.user_notification, user_notification_old adit.user_notification, operation text);
DROP FUNCTION IF EXISTS aditlog.log_signature (signature_new adit.signature, signature_old adit.signature, operation text);
DROP FUNCTION IF EXISTS aditlog.log_remote_application (remote_application_new adit.remote_application, remote_application_old adit.remote_application, operation text);
DROP FUNCTION IF EXISTS aditlog.log_notification_type (notification_type_new adit.notification_type, notification_type_old adit.notification_type, operation text);
DROP FUNCTION IF EXISTS aditlog.log_document_wf_status (document_wf_status_new adit.document_wf_status, document_wf_status_old adit.document_wf_status, operation text);
DROP FUNCTION IF EXISTS aditlog.log_notification (notification_new adit.notification, notification_old adit.notification, operation text);
DROP FUNCTION IF EXISTS aditlog.log_document_type (document_type_new adit.document_type, document_type_old adit.document_type, operation text);
DROP FUNCTION IF EXISTS aditlog.log_document_sharing_type (document_sharing_type_new adit.document_sharing_type, document_sharing_type_old adit.document_sharing_type, operation text);
SET search_path = adit, pg_catalog;
DROP SEQUENCE IF EXISTS adit.user_contact_id_seq;
DROP SEQUENCE IF EXISTS adit.signature_id_seq;
DROP SEQUENCE IF EXISTS adit.request_log_id_seq;
DROP SEQUENCE IF EXISTS adit.notification_id_seq;
DROP SEQUENCE IF EXISTS adit.metadata_request_log_id_seq;
DROP SEQUENCE IF EXISTS adit.error_log_id_seq;
DROP SEQUENCE IF EXISTS adit.download_request_log_id_seq;
DROP SEQUENCE IF EXISTS adit.document_sharing_id_seq;
DROP SEQUENCE IF EXISTS adit.document_id_seq;
DROP SEQUENCE IF EXISTS adit.document_history_id_seq;
DROP SEQUENCE IF EXISTS adit.document_file_id_seq;
DROP SEQUENCE IF EXISTS adit.adit_log_id_seq;
SET search_path = aditlog, pg_catalog;
DROP FUNCTION IF EXISTS aditlog.log_document_sharing (document_sharing_new adit.document_sharing, document_sharing_old adit.document_sharing, operation text);
DROP FUNCTION IF EXISTS aditlog.log_document_history_type (document_history_type_new adit.document_history_type, document_history_type_old adit.document_history_type, operation text);
DROP FUNCTION IF EXISTS aditlog.log_document_history (document_history_new adit.document_history, document_history_old adit.document_history, operation text);
DROP FUNCTION IF EXISTS aditlog.log_document_dvk_status (document_dvk_status_new adit.document_dvk_status, document_dvk_status_old adit.document_dvk_status, operation text);
DROP FUNCTION IF EXISTS aditlog.log_document (document_new adit.document, document_old adit.document, operation text);
DROP FUNCTION IF EXISTS aditlog.log_document_file (document_file_new adit.document_file, document_file_old adit.document_file, operation text);
DROP FUNCTION IF EXISTS aditlog.log_adit_user (adit_user_new adit.adit_user, adit_user_old adit.adit_user, operation text);
DROP FUNCTION IF EXISTS aditlog.log_access_restriction (access_restriction_new adit.access_restriction, access_restriction_old adit.access_restriction, operation text);
SET search_path = adit, pg_catalog;
DROP FUNCTION IF EXISTS adit.set_job_running_status (job_id bigint, is_running bigint);
DROP TABLE IF EXISTS adit.notification;
DROP TABLE IF EXISTS adit.error_log;
DROP TABLE IF EXISTS adit.access_restriction;
DROP TABLE IF EXISTS adit.maintenance_job;
DROP TABLE IF EXISTS adit.signature;
DROP TABLE IF EXISTS adit.document_file_type;
DROP TABLE IF EXISTS adit.user_notification;
DROP TABLE IF EXISTS adit.metadata_request_log;
DROP TABLE IF EXISTS adit.adit_log;
DROP TABLE IF EXISTS adit.request_log;
DROP TABLE IF EXISTS adit.document_history;
DROP TABLE IF EXISTS adit.document_history_type;
DROP TABLE IF EXISTS adit.notification_type;
DROP TABLE IF EXISTS adit.download_request_log;
DROP TABLE IF EXISTS adit.document_file;
DROP TABLE IF EXISTS adit.document_sharing;
DROP TABLE IF EXISTS adit.document;
DROP TABLE IF EXISTS adit.remote_application;
DROP TABLE IF EXISTS adit.document_type;
DROP TABLE IF EXISTS adit.document_dvk_status;
DROP TABLE IF EXISTS adit.document_sharing_type;
DROP TABLE IF EXISTS adit.document_wf_status;
DROP TABLE IF EXISTS adit.user_contact;
DROP TABLE IF EXISTS adit.adit_user;
DROP TABLE IF EXISTS adit.usertype;
DROP SCHEMA IF EXISTS adit;
DROP SCHEMA IF EXISTS aditlog;


CREATE SCHEMA adit AUTHORIZATION adit_admin;
CREATE SCHEMA aditlog AUTHORIZATION adit_admin;

SET check_function_bodies = false;

--
-- Structure for table adit_user (OID = 16391) : 
--
SET search_path = adit, pg_catalog;
CREATE TABLE adit.adit_user (
    user_code varchar(50) NOT NULL,
    full_name varchar(255),
    usertype varchar(50) NOT NULL,
    active smallint DEFAULT 1,
    dvk_org_code varchar(50),
    dvk_subdivision_short_name varchar(60),
    dvk_occupation_short_name varchar(50),
    disk_quota bigint,
    deactivation_date timestamp without time zone,
    disk_quota_used bigint DEFAULT 0
)
WITH (oids = false);
--
-- Structure for table user_contact (OID = 16403) : 
--
CREATE TABLE adit.user_contact (
    id bigint NOT NULL,
    user_code varchar(50) NOT NULL,
    contact_code varchar(50) NOT NULL,
    last_used_date timestamp without time zone
)
WITH (oids = false);
--
-- Structure for table document_sharing (OID = 16406) : 
--
CREATE TABLE adit.document_sharing (
    id bigint NOT NULL,
    document_id bigint NOT NULL,
    user_code varchar(50),
    user_name varchar(255),
    sharing_type varchar(50) NOT NULL,
    task_description varchar(4000),
    creation_date timestamp without time zone,
    dvk_status_id bigint,
    wf_status_id bigint,
    first_access_date timestamp without time zone,
    deleted smallint,
    dvk_folder varchar(1000),
    dvk_id bigint,
    user_email varchar(255),
    comment_text text
)
WITH (oids = false);
--
-- Structure for table document_type (OID = 16419) : 
--
CREATE TABLE adit.document_type (
    short_name varchar(50) NOT NULL,
    description varchar(4000)
)
WITH (oids = false);
--
-- Structure for table document (OID = 16427) : 
--
CREATE TABLE adit.document (
    id bigint NOT NULL,
    guid varchar(50),
    title varchar(255),
    type varchar(50) NOT NULL,
    creator_code varchar(50) NOT NULL,
    creator_name varchar(255),
    creator_user_code varchar(50),
    creator_user_name varchar(255),
    creation_date timestamp without time zone,
    remote_application varchar(50),
    last_modified_date timestamp without time zone,
    document_dvk_status_id bigint,
    dvk_id bigint,
    document_wf_status_id bigint,
    parent_id bigint,
    locked smallint DEFAULT 0,
    locking_date timestamp without time zone,
    signable smallint DEFAULT 0,
    deflated smallint DEFAULT 0,
    deflate_date timestamp without time zone,
    deleted smallint,
    invisible_to_owner smallint,
    signed smallint,
    migrated smallint,
    eform_use_id bigint,
    files_size_bytes bigint DEFAULT 0,
    sender_receiver varchar(50),
    content varchar(4000)
)
WITH (oids = false);
--
-- Structure for table document_file (OID = 16444) : 
--
CREATE TABLE adit.document_file (
    id bigint NOT NULL,
    document_id bigint NOT NULL,
    file_name varchar(255) NOT NULL,
    content_type varchar(255),
    description varchar(4000),
    file_data bytea,
    file_size_bytes bigint,
    deleted smallint DEFAULT 0,
    document_file_type_id bigint DEFAULT 1 NOT NULL,
    file_data_in_ddoc smallint,
    ddoc_datafile_id varchar(200),
    ddoc_datafile_start_offset bigint,
    ddoc_datafile_end_offset bigint,
    last_modified_date timestamp without time zone,
    guid varchar(50)
)
WITH (oids = false);
--
-- Structure for table download_request_log (OID = 16455) : 
--
CREATE TABLE adit.download_request_log (
    id bigint NOT NULL,
    document_id bigint,
    document_file_id bigint,
    request_date timestamp without time zone,
    remote_application_short_name varchar(50),
    user_code varchar(50),
    organization_code varchar(50)
)
WITH (oids = false);
--
-- Structure for table notification_type (OID = 16460) : 
--
CREATE TABLE adit.notification_type (
    short_name varchar(50) NOT NULL,
    description varchar(4000)
)
WITH (oids = false);
--
-- Structure for table document_history (OID = 16468) : 
--
CREATE TABLE adit.document_history (
    id bigint NOT NULL,
    document_id bigint NOT NULL,
    document_history_type varchar(50),
    description varchar(4000),
    event_date timestamp without time zone,
    user_code varchar(50),
    user_name varchar(255),
    remote_application varchar(50),
    notification_status varchar(50),
    xtee_notification_id varchar(50),
    xtee_user_code varchar(50),
    xtee_user_name varchar(255)
)
WITH (oids = false);
--
-- Structure for table request_log (OID = 16481) : 
--
CREATE TABLE adit.request_log (
    id bigint NOT NULL,
    request varchar(50),
    document_id bigint,
    request_date timestamp without time zone,
    remote_application_short_name varchar(50),
    user_code varchar(50),
    organization_code varchar(50),
    additional_information varchar(4000)
)
WITH (oids = false);
--
-- Structure for table adit_log (OID = 16489) : 
--
CREATE TABLE adit.adit_log (
    id bigint NOT NULL,
    table_name varchar(50),
    column_name varchar(50),
    old_value varchar(4000),
    new_value varchar(4000),
    log_date timestamp without time zone,
    primary_key_value varchar(100),
    remote_application_short_name varchar(50),
    xtee_user_code varchar(50),
    xtee_institution_code varchar(50),
    db_user varchar(50)
)
WITH (oids = false);
--
-- Structure for table metadata_request_log (OID = 16497) : 
--
CREATE TABLE adit.metadata_request_log (
    id bigint NOT NULL,
    document_id bigint NOT NULL,
    request_date timestamp without time zone,
    remote_application_short_name varchar(50),
    user_code varchar(50),
    organization_code varchar(50)
)
WITH (oids = false);
--
-- Structure for table user_notification (OID = 16502) : 
--
CREATE TABLE adit.user_notification (
    user_code varchar(50) NOT NULL,
    notification_type varchar(50) NOT NULL
)
WITH (oids = false);
--
-- Structure for table document_file_type (OID = 16509) : 
--
CREATE TABLE adit.document_file_type (
    id bigint NOT NULL,
    description varchar(100) NOT NULL
)
WITH (oids = false);
--
-- Structure for table signature (OID = 16512) : 
--
CREATE TABLE adit.signature (
    id bigint NOT NULL,
    user_code varchar(50),
    document_id bigint NOT NULL,
    signer_role varchar(200),
    resolution varchar(100),
    country varchar(100),
    county varchar(100),
    city varchar(100),
    post_index varchar(50),
    signer_code varchar(20),
    signer_name varchar(255),
    signing_date timestamp without time zone,
    user_name varchar(255)
)
WITH (oids = false);
--
-- Structure for table maintenance_job (OID = 16522) : 
--
CREATE TABLE adit.maintenance_job (
    id bigint NOT NULL,
    name varchar(100),
    is_running smallint DEFAULT 0 NOT NULL
)
WITH (oids = false);
--
-- Structure for table usertype (OID = 16529) : 
--
CREATE TABLE adit.usertype (
    short_name varchar(50) NOT NULL,
    description varchar(4000),
    disk_quota bigint
)
WITH (oids = false);
--
-- Structure for table document_dvk_status (OID = 16537) : 
--
CREATE TABLE adit.document_dvk_status (
    id bigint NOT NULL,
    description varchar(4000)
)
WITH (oids = false);
--
-- Structure for table document_history_type (OID = 16545) : 
--
CREATE TABLE adit.document_history_type (
    short_name varchar(50) NOT NULL,
    description varchar(4000)
)
WITH (oids = false);
--
-- Structure for table document_sharing_type (OID = 16553) : 
--
CREATE TABLE adit.document_sharing_type (
    short_name varchar(50) NOT NULL,
    description varchar(4000)
)
WITH (oids = false);
--
-- Structure for table access_restriction (OID = 16561) : 
--
CREATE TABLE adit.access_restriction (
    id bigint NOT NULL,
    remote_application varchar(50) NOT NULL,
    user_code varchar(50) NOT NULL,
    restriction varchar(50)
)
WITH (oids = false);
--
-- Structure for table error_log (OID = 16568) : 
--
CREATE TABLE adit.error_log (
    id bigint NOT NULL,
    document_id bigint,
    error_date timestamp without time zone,
    remote_application_short_name varchar(50),
    user_code varchar(50),
    action_name varchar(255),
    error_level varchar(50),
    error_message varchar(4000)
)
WITH (oids = false);
--
-- Structure for table notification (OID = 16576) : 
--
CREATE TABLE adit.notification (
    id bigint NOT NULL,
    user_code varchar(50),
    document_id bigint NOT NULL,
    event_date timestamp without time zone,
    notification_type varchar(50) NOT NULL,
    notification_text varchar(4000),
    notification_id bigint,
    notification_sending_date timestamp without time zone
)
WITH (oids = false);
--
-- Structure for table document_wf_status (OID = 16584) : 
--
CREATE TABLE adit.document_wf_status (
    id bigint NOT NULL,
    description varchar(4000),
    name varchar(50)
)
WITH (oids = false);
--
-- Structure for table remote_application (OID = 16592) : 
--
CREATE TABLE adit.remote_application (
    short_name varchar(50) NOT NULL,
    name varchar(50),
    organization_code varchar(50) NOT NULL,
    can_read smallint DEFAULT 0,
    can_write smallint DEFAULT 0
)
WITH (oids = false);
--
-- Definition for sequence adit_log_id_seq (OID = 24593) : 
--
CREATE SEQUENCE adit.adit_log_id_seq
    START WITH 8711977
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
--
-- Definition for sequence document_file_id_seq (OID = 24595) : 
--
CREATE SEQUENCE adit.document_file_id_seq
    START WITH 143565
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
--
-- Definition for sequence document_history_id_seq (OID = 24597) : 
--
CREATE SEQUENCE adit.document_history_id_seq
    START WITH 1331493
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
--
-- Definition for sequence document_id_seq (OID = 24599) : 
--
CREATE SEQUENCE adit.document_id_seq
    START WITH 84779
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
--
-- Definition for sequence document_sharing_id_seq (OID = 24601) : 
--
CREATE SEQUENCE adit.document_sharing_id_seq
    START WITH 77231
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
--
-- Definition for sequence download_request_log_id_seq (OID = 24603) : 
--
CREATE SEQUENCE adit.download_request_log_id_seq
    START WITH 1348477
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
--
-- Definition for sequence error_log_id_seq (OID = 24605) : 
--
CREATE SEQUENCE adit.error_log_id_seq
    START WITH 83635
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
--
-- Definition for sequence metadata_request_log_id_seq (OID = 24607) : 
--
CREATE SEQUENCE adit.metadata_request_log_id_seq
    START WITH 45665
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
--
-- Definition for sequence notification_id_seq (OID = 24609) : 
--
CREATE SEQUENCE adit.notification_id_seq
    START WITH 719
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
--
-- Definition for sequence request_log_id_seq (OID = 24611) : 
--
CREATE SEQUENCE adit.request_log_id_seq
    START WITH 4429660
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
--
-- Definition for sequence signature_id_seq (OID = 24613) : 
--
CREATE SEQUENCE adit.signature_id_seq
    START WITH 7064
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
--
-- Definition for sequence user_contact_id_seq (OID = 24615) : 
--
CREATE SEQUENCE adit.user_contact_id_seq
    START WITH 146
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
--
-- Definition for index adit_user_usertype_idx (OID = 16401) : 
--
CREATE INDEX adit_user_usertype_idx ON adit_user USING btree (usertype);
--
-- Definition for index adit_user_active_idx (OID = 16402) : 
--
CREATE INDEX adit_user_active_idx ON adit_user USING btree (active);
--
-- Definition for index document_sharing_wfstatus_idx (OID = 16414) : 
--
CREATE INDEX document_sharing_wfstatus_idx ON document_sharing USING btree (wf_status_id);
--
-- Definition for index document_sharing_type_idx (OID = 16415) : 
--
CREATE INDEX document_sharing_type_idx ON document_sharing USING btree (sharing_type);
--
-- Definition for index document_sharing_docid_idx (OID = 16416) : 
--
CREATE INDEX document_sharing_docid_idx ON document_sharing USING btree (document_id);
--
-- Definition for index document_sharing_user_idx (OID = 16417) : 
--
CREATE INDEX document_sharing_user_idx ON document_sharing USING btree (user_code);
--
-- Definition for index document_sharing_dvkstatus_idx (OID = 16418) : 
--
CREATE INDEX document_sharing_dvkstatus_idx ON document_sharing USING btree (dvk_status_id);
--
-- Definition for index document_dvkstatus_idx (OID = 16439) : 
--
CREATE INDEX document_dvkstatus_idx ON document USING btree (document_dvk_status_id);
--
-- Definition for index document_wfstatus_idx (OID = 16440) : 
--
CREATE INDEX document_wfstatus_idx ON document USING btree (document_wf_status_id);
--
-- Definition for index document_documenttype_idx (OID = 16441) : 
--
CREATE INDEX document_documenttype_idx ON document USING btree (type);
--
-- Definition for index document_app_idx (OID = 16442) : 
--
CREATE INDEX document_app_idx ON document USING btree (remote_application);
--
-- Definition for index document_parent_idx (OID = 16443) : 
--
CREATE INDEX document_parent_idx ON document USING btree (parent_id);
--
-- Definition for index document_file_docid_idx (OID = 16454) : 
--
CREATE INDEX document_file_docid_idx ON document_file USING btree (document_id);
--
-- Definition for index document_history_type_idx (OID = 16476) : 
--
CREATE INDEX document_history_type_idx ON document_history USING btree (document_history_type);
--
-- Definition for index document_history_search_idx (OID = 16477) : 
--
CREATE INDEX document_history_search_idx ON document_history USING btree (document_id, user_code, document_history_type);
--
-- Definition for index document_history_docid_idx (OID = 16478) : 
--
CREATE INDEX document_history_docid_idx ON document_history USING btree (document_id);
--
-- Definition for index document_history_app_idx (OID = 16479) : 
--
CREATE INDEX document_history_app_idx ON document_history USING btree (remote_application);
--
-- Definition for index document_history_user_idx (OID = 16480) : 
--
CREATE INDEX document_history_user_idx ON document_history USING btree (user_code);
--
-- Definition for index user_notification_user_idx (OID = 16507) : 
--
CREATE INDEX user_notification_user_idx ON user_notification USING btree (user_code);
--
-- Definition for index user_notification_type_idx (OID = 16508) : 
--
CREATE INDEX user_notification_type_idx ON user_notification USING btree (notification_type);
--
-- Definition for index signature_docid_idx (OID = 16520) : 
--
CREATE INDEX signature_docid_idx ON signature USING btree (document_id);
--
-- Definition for index signature_user_idx (OID = 16521) : 
--
CREATE INDEX signature_user_idx ON signature USING btree (user_code);
--
-- Definition for index maintenance_job_search_idx (OID = 16528) : 
--
CREATE INDEX maintenance_job_search_idx ON maintenance_job USING btree (id, is_running);
--
-- Definition for index access_restriction_user_idx (OID = 16566) : 
--
CREATE INDEX access_restriction_user_idx ON access_restriction USING btree (user_code);
--
-- Definition for index access_restriction_app_idx (OID = 16567) : 
--
CREATE INDEX access_restriction_app_idx ON access_restriction USING btree (remote_application);
--
-- Definition for index adit_user_pkey (OID = 16399) : 
--
ALTER TABLE ONLY adit_user
    ADD CONSTRAINT adit_user_pkey
    PRIMARY KEY (user_code);
--
-- Definition for index document_sharing_pkey (OID = 16412) : 
--
ALTER TABLE ONLY document_sharing
    ADD CONSTRAINT document_sharing_pkey
    PRIMARY KEY (id);
--
-- Definition for index document_type_pkey (OID = 16425) : 
--
ALTER TABLE ONLY document_type
    ADD CONSTRAINT document_type_pkey
    PRIMARY KEY (short_name);
--
-- Definition for index document_pkey (OID = 16437) : 
--
ALTER TABLE ONLY document
    ADD CONSTRAINT document_pkey
    PRIMARY KEY (id);
--
-- Definition for index document_file_pkey (OID = 16452) : 
--
ALTER TABLE ONLY document_file
    ADD CONSTRAINT document_file_pkey
    PRIMARY KEY (id);
--
-- Definition for index download_request_log_pkey (OID = 16458) : 
--
ALTER TABLE ONLY download_request_log
    ADD CONSTRAINT download_request_log_pkey
    PRIMARY KEY (id);
--
-- Definition for index notification_type_pkey (OID = 16466) : 
--
ALTER TABLE ONLY notification_type
    ADD CONSTRAINT notification_type_pkey
    PRIMARY KEY (short_name);
--
-- Definition for index document_history_pkey (OID = 16474) : 
--
ALTER TABLE ONLY document_history
    ADD CONSTRAINT document_history_pkey
    PRIMARY KEY (id);
--
-- Definition for index request_log_pkey (OID = 16487) : 
--
ALTER TABLE ONLY request_log
    ADD CONSTRAINT request_log_pkey
    PRIMARY KEY (id);
--
-- Definition for index adit_log_pkey (OID = 16495) : 
--
ALTER TABLE ONLY adit_log
    ADD CONSTRAINT adit_log_pkey
    PRIMARY KEY (id);
--
-- Definition for index metadata_request_log_pkey (OID = 16500) : 
--
ALTER TABLE ONLY metadata_request_log
    ADD CONSTRAINT metadata_request_log_pkey
    PRIMARY KEY (id);
--
-- Definition for index user_notification_pkey (OID = 16505) : 
--
ALTER TABLE ONLY user_notification
    ADD CONSTRAINT user_notification_pkey
    PRIMARY KEY (user_code, notification_type);
--
-- Definition for index signature_pkey (OID = 16518) : 
--
ALTER TABLE ONLY signature
    ADD CONSTRAINT signature_pkey
    PRIMARY KEY (id);
--
-- Definition for index maintenance_job_pkey (OID = 16526) : 
--
ALTER TABLE ONLY maintenance_job
    ADD CONSTRAINT maintenance_job_pkey
    PRIMARY KEY (id);
--
-- Definition for index usertype_pkey (OID = 16535) : 
--
ALTER TABLE ONLY usertype
    ADD CONSTRAINT usertype_pkey
    PRIMARY KEY (short_name);
--
-- Definition for index document_dvk_status_pkey (OID = 16543) : 
--
ALTER TABLE ONLY document_dvk_status
    ADD CONSTRAINT document_dvk_status_pkey
    PRIMARY KEY (id);
--
-- Definition for index document_history_type_pkey (OID = 16551) : 
--
ALTER TABLE ONLY document_history_type
    ADD CONSTRAINT document_history_type_pkey
    PRIMARY KEY (short_name);
--
-- Definition for index document_sharing_type_pkey (OID = 16559) : 
--
ALTER TABLE ONLY document_sharing_type
    ADD CONSTRAINT document_sharing_type_pkey
    PRIMARY KEY (short_name);
--
-- Definition for index access_restriction_pkey (OID = 16564) : 
--
ALTER TABLE ONLY access_restriction
    ADD CONSTRAINT access_restriction_pkey
    PRIMARY KEY (remote_application, user_code);
--
-- Definition for index error_log_pkey (OID = 16574) : 
--
ALTER TABLE ONLY error_log
    ADD CONSTRAINT error_log_pkey
    PRIMARY KEY (id);
--
-- Definition for index notification_pkey (OID = 16582) : 
--
ALTER TABLE ONLY notification
    ADD CONSTRAINT notification_pkey
    PRIMARY KEY (id);
--
-- Definition for index document_wf_status_pkey (OID = 16590) : 
--
ALTER TABLE ONLY document_wf_status
    ADD CONSTRAINT document_wf_status_pkey
    PRIMARY KEY (id);
--
-- Definition for index remote_application_pkey (OID = 16597) : 
--
ALTER TABLE ONLY remote_application
    ADD CONSTRAINT remote_application_pkey
    PRIMARY KEY (short_name);
--
-- Definition for index usertype_short_name (OID = 16599) : 
--
ALTER TABLE ONLY adit_user
    ADD CONSTRAINT usertype_short_name
    FOREIGN KEY (usertype) REFERENCES usertype(short_name);
--
-- Definition for index wf_status_id (OID = 16604) : 
--
ALTER TABLE ONLY document_sharing
    ADD CONSTRAINT wf_status_id
    FOREIGN KEY (wf_status_id) REFERENCES document_wf_status(id);
--
-- Definition for index sharing_type_short_name (OID = 16609) : 
--
ALTER TABLE ONLY document_sharing
    ADD CONSTRAINT sharing_type_short_name
    FOREIGN KEY (sharing_type) REFERENCES document_sharing_type(short_name);
--
-- Definition for index dvk_status_id (OID = 16614) : 
--
ALTER TABLE ONLY document_sharing
    ADD CONSTRAINT dvk_status_id
    FOREIGN KEY (dvk_status_id) REFERENCES document_dvk_status(id);
--
-- Definition for index sharing_document_id (OID = 16619) : 
--
ALTER TABLE ONLY document_sharing
    ADD CONSTRAINT sharing_document_id
    FOREIGN KEY (document_id) REFERENCES document(id);
--
-- Definition for index document_type_short_name (OID = 16634) : 
--
ALTER TABLE ONLY document
    ADD CONSTRAINT document_type_short_name
    FOREIGN KEY (type) REFERENCES document_type(short_name);
--
-- Definition for index document_id (OID = 16649) : 
--
ALTER TABLE ONLY document_file
    ADD CONSTRAINT document_id
    FOREIGN KEY (document_id) REFERENCES document(id);
--
-- Definition for index fk_document_hist_remote_applic (OID = 16654) : 
--
ALTER TABLE ONLY document_history
    ADD CONSTRAINT fk_document_hist_remote_applic
    FOREIGN KEY (remote_application) REFERENCES remote_application(short_name);
--
-- Definition for index fk_document_hist_document_hist (OID = 16659) : 
--
ALTER TABLE ONLY document_history
    ADD CONSTRAINT fk_document_hist_document_hist
    FOREIGN KEY (document_history_type) REFERENCES document_history_type(short_name);
--
-- Definition for index fk_document_history_document (OID = 16664) : 
--
ALTER TABLE ONLY document_history
    ADD CONSTRAINT fk_document_history_document
    FOREIGN KEY (document_id) REFERENCES document(id);
--
-- Definition for index history_user_code (OID = 16669) : 
--
ALTER TABLE ONLY document_history
    ADD CONSTRAINT history_user_code
    FOREIGN KEY (user_code) REFERENCES adit_user(user_code);
--
-- Definition for index notification_type_short_name (OID = 16674) : 
--
ALTER TABLE ONLY user_notification
    ADD CONSTRAINT notification_type_short_name
    FOREIGN KEY (notification_type) REFERENCES notification_type(short_name);
--
-- Definition for index notification_user_code (OID = 16679) : 
--
ALTER TABLE ONLY user_notification
    ADD CONSTRAINT notification_user_code
    FOREIGN KEY (user_code) REFERENCES adit_user(user_code);
--
-- Definition for index fk_signature_document (OID = 16684) : 
--
ALTER TABLE ONLY signature
    ADD CONSTRAINT fk_signature_document
    FOREIGN KEY (document_id) REFERENCES document(id);
--
-- Definition for index fk_signature_user (OID = 16689) : 
--
ALTER TABLE ONLY signature
    ADD CONSTRAINT fk_signature_user
    FOREIGN KEY (user_code) REFERENCES adit_user(user_code);
--
-- Definition for index remote_application_short_name (OID = 16694) : 
--
ALTER TABLE ONLY access_restriction
    ADD CONSTRAINT remote_application_short_name
    FOREIGN KEY (remote_application) REFERENCES remote_application(short_name);
--
-- Definition for index user_code (OID = 16699) : 
--
ALTER TABLE ONLY access_restriction
    ADD CONSTRAINT user_code
    FOREIGN KEY (user_code) REFERENCES adit_user(user_code);
--
-- Definition for index doc_remote_app_short_name (OID = 24620) : 
--
ALTER TABLE ONLY document
    ADD CONSTRAINT doc_remote_app_short_name
    FOREIGN KEY (remote_application) REFERENCES remote_application(short_name);
--
-- Definition for index document_dvk_status_id (OID = 24625) : 
--
ALTER TABLE ONLY document
    ADD CONSTRAINT document_dvk_status_id
    FOREIGN KEY (document_dvk_status_id) REFERENCES document_dvk_status(id);
--
-- Definition for index document_workflow_status_id (OID = 24630) : 
--
ALTER TABLE ONLY document
    ADD CONSTRAINT document_workflow_status_id
    FOREIGN KEY (document_wf_status_id) REFERENCES document_wf_status(id);
--
-- Definition for index parent_document_id (OID = 24635) : 
--
ALTER TABLE ONLY document
    ADD CONSTRAINT parent_document_id
    FOREIGN KEY (parent_id) REFERENCES document(id);

--
-- Comments
--
COMMENT ON TABLE adit.adit_user IS 'User account data. User account can belong to a person (identified by personal ID code) or to an organization (identified by registry code).';
COMMENT ON COLUMN adit.adit_user.user_code IS 'Users personal ID code or registry code including country prefix. For example "EE38407089945".';
COMMENT ON COLUMN adit.adit_user.full_name IS 'Users full name';
COMMENT ON COLUMN adit.adit_user.usertype IS 'Reference to user type';
COMMENT ON COLUMN adit.adit_user.active IS 'Indicates wheather or not the user is active. "1" = active, "0" = inactive.';
COMMENT ON COLUMN adit.adit_user.dvk_org_code IS 'This column contains a value only if user uses DEC to send and receive documents. Contains registry code of usesrs DEC organization.';
COMMENT ON COLUMN adit.adit_user.dvk_subdivision_short_name IS 'This column contains a value only if user uses DEC to send and receive documents. Contains short name of users DEC subdivision.';
COMMENT ON COLUMN adit.adit_user.dvk_occupation_short_name IS 'This column contains a value only if user uses DEC to send and receive documents. Contains short name of users DEC occupation.';
COMMENT ON COLUMN adit.adit_user.disk_quota IS 'User disk quota in bytes. Disk quota configured here overrides disk quota values configured on user type or application levels.';
COMMENT ON COLUMN adit.adit_user.deactivation_date IS 'Date and time when user account was deactivated';
COMMENT ON COLUMN adit.adit_user.disk_quota_used IS 'Total disk space used by current user';
COMMENT ON TABLE adit.user_contact IS 'User contact data';
COMMENT ON COLUMN adit.user_contact.id IS 'Unique identifier';
COMMENT ON COLUMN adit.user_contact.user_code IS 'Reference to the user (user_code) who owns the contact book';
COMMENT ON COLUMN adit.user_contact.contact_code IS 'Reference to the user (user_code) whos contact data is shown in the contact book';
COMMENT ON COLUMN adit.user_contact.last_used_date IS 'Date and time when given contacts has been used for sharing or sending documents';
COMMENT ON TABLE adit.document_sharing IS 'Document sharing data';
COMMENT ON COLUMN adit.document_sharing.id IS 'Unique identifier';
COMMENT ON COLUMN adit.document_sharing.document_id IS 'Reference to the document that was shared or sent';
COMMENT ON COLUMN adit.document_sharing.user_code IS 'Referente to user (user code) to whom the document was shared or sent';
COMMENT ON COLUMN adit.document_sharing.user_name IS 'Name of the user (as it was at the moment of sharing)';
COMMENT ON COLUMN adit.document_sharing.sharing_type IS 'Short name of sharing type';
COMMENT ON COLUMN adit.document_sharing.task_description IS 'Purpose of sharing (what the other user should do with this document)';
COMMENT ON COLUMN adit.document_sharing.creation_date IS 'Date and time of sharing';
COMMENT ON COLUMN adit.document_sharing.dvk_status_id IS 'DEC status ID of document. Is used when document has been sent using DEC';
COMMENT ON COLUMN adit.document_sharing.wf_status_id IS 'Workflow status ID. Is used for feedback from recipient to sender.';
COMMENT ON COLUMN adit.document_sharing.first_access_date IS 'Date and time the document was last accessed by recipient.';
COMMENT ON COLUMN adit.document_sharing.deleted IS 'Document has been deleted by the user to whom it was sent.';
COMMENT ON COLUMN adit.document_sharing.dvk_folder IS 'DVK dokumendi kausta nimi';
COMMENT ON COLUMN adit.document_sharing.user_email IS 'Recipient email if document is sent by email';
COMMENT ON TABLE adit.document_type IS 'List of possible document types';
COMMENT ON COLUMN adit.document_type.short_name IS 'Short name of document type';
COMMENT ON COLUMN adit.document_type.description IS 'Description of document type';
COMMENT ON TABLE adit.document IS 'Document data';
COMMENT ON COLUMN adit.document.id IS 'Unique identifier';
COMMENT ON COLUMN adit.document.guid IS 'Documents globally unique identifier. If document was received from DEC then GUID comes from DEC. If document is created in ADIT then GUID will be generated by ADIT.';
COMMENT ON COLUMN adit.document.title IS 'Document title';
COMMENT ON COLUMN adit.document.type IS 'Short name of document type';
COMMENT ON COLUMN adit.document.creator_code IS 'Personal ID code or registry code of document creator';
COMMENT ON COLUMN adit.document.creator_name IS 'Document creators name (as it was when document was created)';
COMMENT ON COLUMN adit.document.creator_user_code IS 'Personal ID code of document creator (if document creator is an organization)';
COMMENT ON COLUMN adit.document.creator_user_name IS 'Name of the person who created this document (if document creator is an organization)';
COMMENT ON COLUMN adit.document.creation_date IS 'Document creation date and time';
COMMENT ON COLUMN adit.document.remote_application IS 'Short name of application that was used to add this document';
COMMENT ON COLUMN adit.document.last_modified_date IS 'Date and time of last modification';
COMMENT ON COLUMN adit.document.document_dvk_status_id IS 'DEC status identifier if document is received or sent using DEC';
COMMENT ON COLUMN adit.document.dvk_id IS 'DEC identifier if document was received from DEC';
COMMENT ON COLUMN adit.document.document_wf_status_id IS 'Document workflow status ID';
COMMENT ON COLUMN adit.document.parent_id IS 'Original document ID. Is used to reference the original document if for example current document is a new version of existing document.';
COMMENT ON COLUMN adit.document.locked IS 'Indicates if this document is locked (cannot be modified). "1" = locked, "0" = not locked.';
COMMENT ON COLUMN adit.document.locking_date IS 'Date and time of locking';
COMMENT ON COLUMN adit.document.signable IS 'Indicates if this document can be signed. "1" = can be signed, "0" = cannot be signed.';
COMMENT ON COLUMN adit.document.deflated IS 'Indicates if this document is deflated (file contents removed). "1" = deflated, "0" = not deflated.';
COMMENT ON COLUMN adit.document.deflate_date IS 'Date and time of deflation';
COMMENT ON COLUMN adit.document.deleted IS 'Indicates if this document is deleted. "1" = deleted, "0" = not deleted.';
COMMENT ON COLUMN adit.document.invisible_to_owner IS 'Indicates if this document has been made invisible to its owner. Is used when document has been sent to someone else and owner wants to delete it from his/her own view.';
COMMENT ON COLUMN adit.document.signed IS 'Indicates if this document has been signed.';
COMMENT ON COLUMN adit.document.eform_use_id IS 'Evormi kasutuselevõtu ID. Täidetakse ainult kui tegemist on evormi vormiandmetega.';
COMMENT ON COLUMN adit.document.files_size_bytes IS 'Total size of files in bytes';
COMMENT ON COLUMN adit.document.sender_receiver IS 'Sender/receiver transient column';
COMMENT ON COLUMN adit.document.content IS 'Textual content of the document. Used to keep email body.';
COMMENT ON TABLE adit.document_file IS 'Document files';
COMMENT ON COLUMN adit.document_file.id IS 'Unique identifier';
COMMENT ON COLUMN adit.document_file.document_id IS 'ID of document this file belongs to';
COMMENT ON COLUMN adit.document_file.file_name IS 'File name';
COMMENT ON COLUMN adit.document_file.content_type IS 'MIME type of file';
COMMENT ON COLUMN adit.document_file.description IS 'File description';
COMMENT ON COLUMN adit.document_file.file_data IS 'File contents (binary data)';
COMMENT ON COLUMN adit.document_file.file_size_bytes IS 'File size in bytes';
COMMENT ON COLUMN adit.document_file.deleted IS 'Indicates if this file is deleted (contents removed)';
COMMENT ON COLUMN adit.document_file.document_file_type_id IS 'File type ID';
COMMENT ON COLUMN adit.document_file.file_data_in_ddoc IS 'Shows whether or not file contents should be aquired from signature container';
COMMENT ON COLUMN adit.document_file.ddoc_datafile_id IS 'ID of corresponding DataFile in signature container';
COMMENT ON COLUMN adit.document_file.ddoc_datafile_start_offset IS 'First character index of current file in corresponding signature container';
COMMENT ON COLUMN adit.document_file.ddoc_datafile_end_offset IS 'Last character index of current file in corresponding signature container';
COMMENT ON COLUMN adit.document_file.last_modified_date IS 'Date and time of last modification';
COMMENT ON COLUMN adit.document_file.guid IS 'Faili globaalselt unikaalne identifikaator';
COMMENT ON TABLE adit.download_request_log IS 'Log of file download requests. Log entries are added here if one of following requests are executed: getDocument (if file contents were requested), getDocumentFile';
COMMENT ON TABLE adit.notification_type IS 'List of possible notification types';
COMMENT ON COLUMN adit.notification_type.short_name IS 'Short name of notification type';
COMMENT ON COLUMN adit.notification_type.description IS 'Description of notification type';
COMMENT ON TABLE adit.document_history IS 'Document history. History records will be created when some action is performed on a document.';
COMMENT ON COLUMN adit.document_history.id IS 'Unique identifier';
COMMENT ON COLUMN adit.document_history.document_id IS 'ID of related document';
COMMENT ON COLUMN adit.document_history.document_history_type IS 'Short name of history type';
COMMENT ON COLUMN adit.document_history.description IS 'Detailed description of document history event';
COMMENT ON COLUMN adit.document_history.event_date IS 'Date and time of history event';
COMMENT ON COLUMN adit.document_history.user_code IS 'Personal ID code or registry code of user who performed the action';
COMMENT ON COLUMN adit.document_history.user_name IS 'Name of user who performed the action (at the moment of perforing the action).';
COMMENT ON COLUMN adit.document_history.remote_application IS 'Short name of application that was used to perform the action';
COMMENT ON COLUMN adit.document_history.notification_status IS 'Notification sending status. If notification is not sent then "SAADA", if notification is sent then "SAADETUD". If no notification has to be sent then empty (NULL).';
COMMENT ON COLUMN adit.document_history.xtee_notification_id IS 'Notification ID from notification calendar (teavituskalender) X-Road database. This column has a value only if notification was successfully sent to notification calendar.';
COMMENT ON COLUMN adit.document_history.xtee_user_code IS 'Personal ID code of person who executed the X-Road request. It is intended to identify the person who performed the action if the action was performed under organization account.';
COMMENT ON COLUMN adit.document_history.xtee_user_name IS 'Personal ID code of person who executed the X-Road request (if it waspossible to find out the name)';
COMMENT ON TABLE adit.request_log IS 'Log of requests that are used to modify data. This table contains log entries about following requests: saveDocument, saveDocumentFile, deleteDocumentFile, archieveDocument, deleteDocument, getDocumentHistory, sendDocument, shareDocument, unShareDocument, markDocumentViewed, prepareSignature, confirmSiganture, modifyStatus';
COMMENT ON COLUMN adit.request_log.request IS 'Name of request (e.g. shareDocument)';
COMMENT ON COLUMN adit.request_log.document_id IS 'ID of document that was involved in this request';
COMMENT ON COLUMN adit.request_log.request_date IS 'Date and time of request execution';
COMMENT ON COLUMN adit.request_log.remote_application_short_name IS 'Short name of application that executed this request';
COMMENT ON COLUMN adit.request_log.user_code IS 'Code of person who executed this request';
COMMENT ON COLUMN adit.request_log.organization_code IS 'Code of organization that executed this request';
COMMENT ON COLUMN adit.request_log.additional_information IS 'Additional information about request. For example - in case of "saveDocumentFile" request SOAP attachment ID will be added here.';
COMMENT ON TABLE adit.adit_log IS 'Contains log of all data changes in database (except FILE_DATA column in DOCUMENT_FILES table).';
COMMENT ON COLUMN adit.adit_log.table_name IS 'Name of table in which data was modified';
COMMENT ON COLUMN adit.adit_log.column_name IS 'Name of column in which data was modified';
COMMENT ON COLUMN adit.adit_log.old_value IS 'Old value';
COMMENT ON COLUMN adit.adit_log.new_value IS 'New value';
COMMENT ON COLUMN adit.adit_log.log_date IS 'Date and time the data was modified';
COMMENT ON COLUMN adit.adit_log.primary_key_value IS 'Primary key value of changed record';
COMMENT ON COLUMN adit.adit_log.remote_application_short_name IS 'Application that modified data';
COMMENT ON COLUMN adit.adit_log.xtee_user_code IS 'Personal ID code from X-Road request';
COMMENT ON COLUMN adit.adit_log.xtee_institution_code IS 'Organization code from X-Road request';
COMMENT ON COLUMN adit.adit_log.db_user IS 'Database user name';
COMMENT ON TABLE adit.metadata_request_log IS 'Log of requests that return only document metadata. Execution of following requests is logged here: getDocumentList, getDocument';
COMMENT ON COLUMN adit.metadata_request_log.document_id IS 'ID of document that was viewed';
COMMENT ON COLUMN adit.metadata_request_log.request_date IS 'Date and time of request execution';
COMMENT ON COLUMN adit.metadata_request_log.remote_application_short_name IS 'Short name of application that executed the request';
COMMENT ON COLUMN adit.metadata_request_log.user_code IS 'Code of person who executed the request';
COMMENT ON COLUMN adit.metadata_request_log.organization_code IS 'Code of organization that executed the request';
COMMENT ON TABLE adit.user_notification IS 'Data about notifications ordered by users. Ordered Notifications will be sent using state portals notification calendar service.';
COMMENT ON COLUMN adit.user_notification.user_code IS 'Code of user who ordered this notification';
COMMENT ON COLUMN adit.user_notification.notification_type IS 'Short name of notification type';
COMMENT ON COLUMN adit.document_file_type.description IS 'Description of file type';
COMMENT ON TABLE adit.signature IS 'Metadata of document signatures';
COMMENT ON COLUMN adit.signature.id IS 'Unique identifier';
COMMENT ON COLUMN adit.signature.user_code IS 'Code of user who gave this signature';
COMMENT ON COLUMN adit.signature.document_id IS 'ID of document this signature belongs to';
COMMENT ON COLUMN adit.signature.signer_role IS 'Signers role';
COMMENT ON COLUMN adit.signature.resolution IS 'Signers resolution';
COMMENT ON COLUMN adit.signature.country IS 'Signature production place - country';
COMMENT ON COLUMN adit.signature.county IS 'Signature production place - state/county';
COMMENT ON COLUMN adit.signature.city IS 'Signature production place - town';
COMMENT ON COLUMN adit.signature.post_index IS 'Signature production place - post code';
COMMENT ON COLUMN adit.signature.signer_code IS 'Signers personal ID code';
COMMENT ON COLUMN adit.signature.signer_name IS 'Signers name';
COMMENT ON COLUMN adit.signature.signing_date IS 'Date and time of signing';
COMMENT ON COLUMN adit.signature.user_name IS 'Name of ADIT user who gave this signature';
COMMENT ON TABLE adit.maintenance_job IS 'List of maintenance jobs. Required for maintenance task synchronization between cluster nodes';
COMMENT ON COLUMN adit.maintenance_job.id IS 'Unique ID of job';
COMMENT ON COLUMN adit.maintenance_job.name IS 'Name of job';
COMMENT ON COLUMN adit.maintenance_job.is_running IS 'Indicates if current job is already running';
COMMENT ON TABLE adit.usertype IS 'List of user types. There are three types of users: person, company and istitution';
COMMENT ON COLUMN adit.usertype.short_name IS 'Short name of user type';
COMMENT ON COLUMN adit.usertype.description IS 'Description of user type';
COMMENT ON COLUMN adit.usertype.disk_quota IS 'Default disk quota for this user type (can be overridden in user data)';
COMMENT ON TABLE adit.document_dvk_status IS 'Document DEC status list (same values as in DEC). There are following statuses: 100 = Not set, 101 = Sending, 102 = Sent, 103 = Canceled';
COMMENT ON COLUMN adit.document_dvk_status.id IS 'Unique identifier of DEC status';
COMMENT ON COLUMN adit.document_dvk_status.description IS 'Status description';
COMMENT ON TABLE adit.document_history_type IS 'History event types';
COMMENT ON COLUMN adit.document_history_type.short_name IS 'Name of history event type. e.g. "esmane loomine" or "dokumendi muutmine".';
COMMENT ON COLUMN adit.document_history_type.description IS 'Description of history event type.';
COMMENT ON TABLE adit.document_sharing_type IS 'List of sharing types';
COMMENT ON COLUMN adit.document_sharing_type.short_name IS 'Short name of sharing type';
COMMENT ON COLUMN adit.document_sharing_type.description IS 'Description of sharing type';
COMMENT ON TABLE adit.access_restriction IS 'Access restrictions for user-application combination.';
COMMENT ON COLUMN adit.access_restriction.remote_application IS 'Short name of remote application that uses ADIT for datastore.';
COMMENT ON COLUMN adit.access_restriction.user_code IS 'Personal ID Code of user.';
COMMENT ON COLUMN adit.access_restriction.restriction IS 'Type of restriction to be applied. "WRITE" - user/application cannot add, update or delete data; "READ" - user/application cannot read, add, update or delete any data.';
COMMENT ON COLUMN adit.error_log.document_id IS 'ID of document that was requested or is elsehow related to failed request';
COMMENT ON COLUMN adit.error_log.error_date IS 'Date and time when the error occured';
COMMENT ON COLUMN adit.error_log.remote_application_short_name IS 'Short name of application that executed the request';
COMMENT ON COLUMN adit.error_log.user_code IS 'Code of user who executed the request';
COMMENT ON COLUMN adit.error_log.action_name IS 'Name of failed request';
COMMENT ON COLUMN adit.error_log.error_level IS 'Error level (FATAL, ERROR, WARNING)';
COMMENT ON COLUMN adit.error_log.error_message IS 'Error message';
COMMENT ON TABLE adit.notification IS 'Notifications for notification calendar';
COMMENT ON COLUMN adit.notification.id IS 'Unique identifier of notification';
COMMENT ON COLUMN adit.notification.user_code IS 'Code of user the notification was sent to';
COMMENT ON COLUMN adit.notification.document_id IS 'ID of document this notification concerns';
COMMENT ON COLUMN adit.notification.event_date IS 'Date and time of event this notification concirns';
COMMENT ON COLUMN adit.notification.notification_type IS 'Short name of notification type';
COMMENT ON COLUMN adit.notification.notification_text IS 'Notification text';
COMMENT ON COLUMN adit.notification.notification_id IS 'ID of this notification in state portals notification calendar';
COMMENT ON COLUMN adit.notification.notification_sending_date IS 'Date and time this notification was sent to state portals notification calendar service';
COMMENT ON TABLE adit.document_wf_status IS 'List of possible workflow statuses';
COMMENT ON COLUMN adit.document_wf_status.id IS 'Unique identifier of workflow status';
COMMENT ON COLUMN adit.document_wf_status.description IS 'Description of workflow status';
COMMENT ON COLUMN adit.document_wf_status.name IS 'Name of workflow status';
COMMENT ON TABLE adit.remote_application IS 'List of possible applications (portals) that use this database';
COMMENT ON COLUMN adit.remote_application.short_name IS 'Short name of application';
COMMENT ON COLUMN adit.remote_application.name IS 'Full name of application';
COMMENT ON COLUMN adit.remote_application.organization_code IS 'Registry code of organization that is responsible for this application';
COMMENT ON COLUMN adit.remote_application.can_read IS 'Indicates whether or not this application is allowed to read data';
COMMENT ON COLUMN adit.remote_application.can_write IS 'Indicates whether or not this application is allowed to modify data';

GRANT SELECT, UPDATE, INSERT ON adit.access_restriction TO adit_user;
GRANT SELECT, UPDATE, INSERT ON adit.adit_log TO adit_user;
GRANT SELECT, UPDATE, INSERT ON adit.adit_user TO adit_user;
GRANT SELECT, UPDATE, INSERT ON adit.document TO adit_user;
GRANT SELECT, UPDATE, INSERT ON adit.document_dvk_status TO adit_user;
GRANT SELECT, UPDATE, INSERT ON adit.document_file TO adit_user;
GRANT SELECT, UPDATE, INSERT ON adit.document_history TO adit_user;
GRANT SELECT, UPDATE, INSERT ON adit.document_history_type TO adit_user;
GRANT SELECT, UPDATE, INSERT, DELETE ON adit.document_sharing TO adit_user;
GRANT SELECT, UPDATE, INSERT ON adit.document_sharing_type TO adit_user;
GRANT SELECT, UPDATE, INSERT ON adit.document_type TO adit_user;
GRANT SELECT, UPDATE, INSERT ON adit.document_wf_status TO adit_user;
GRANT SELECT, UPDATE, INSERT ON adit.download_request_log TO adit_user;
GRANT SELECT, UPDATE, INSERT ON adit.error_log TO adit_user;
GRANT SELECT, UPDATE ON adit.maintenance_job TO adit_user;
GRANT SELECT, UPDATE, INSERT ON adit.metadata_request_log TO adit_user;
GRANT SELECT, UPDATE, INSERT, DELETE ON adit.notification TO adit_user;
GRANT SELECT, UPDATE, INSERT ON adit.notification_type TO adit_user;
GRANT SELECT, UPDATE, INSERT ON adit.remote_application TO adit_user;
GRANT SELECT, UPDATE, INSERT ON adit.request_log TO adit_user;
GRANT SELECT, UPDATE, INSERT ON adit.signature TO adit_user;
GRANT SELECT, UPDATE, INSERT ON adit.user_contact TO adit_user;
GRANT SELECT, UPDATE, INSERT, DELETE ON adit.user_notification TO adit_user;
GRANT SELECT, UPDATE, INSERT ON adit.usertype TO adit_user;




--
-- Definition for function set_job_running_status (OID = 16706) : 
--
SET search_path = adit, pg_catalog;
CREATE FUNCTION adit.set_job_running_status (
  job_id bigint,
  is_running bigint
)
RETURNS refcursor
AS 
$body$
DECLARE
	result_rc refcursor;
BEGIN
    update  maintenance_job
    set     is_running = SET_JOB_RUNNING_STATUS.is_running
    where   maintenance_job.id = SET_JOB_RUNNING_STATUS.job_id
            and maintenance_job.is_running <> SET_JOB_RUNNING_STATUS.is_running;

    if found then
	open result_rc for
        select  'ok' as result_code;
        return result_rc;
    else
	open result_rc for
        select  'job_is_aready_in_given_state' as result_code;
        return result_rc;
    end if;
end;
$body$
LANGUAGE plpgsql;
--
-- Definition for function log_access_restriction (OID = 16709) : 
--
SET search_path = aditlog, pg_catalog;
CREATE FUNCTION aditlog.log_access_restriction (
  access_restriction_new adit.access_restriction,
  access_restriction_old adit.access_restriction,
  operation text
)
RETURNS void
AS 
$body$
DECLARE

    usr       varchar(20);
    pkey_col  varchar(50);
    tablename varchar(50) := 'ACCESS_RESTRICTION';
    primary_key_v bigint := access_restriction_old.id;

BEGIN

    IF(coalesce(primary_key_v::text, '') = '') THEN
      primary_key_v := access_restriction_new.id;
    END IF;
   
    -- Current user
    SELECT USER INTO usr ;
  
    -- id changed
    IF(coalesce(access_restriction_new.id, 0) != coalesce(access_restriction_old.id, 0)) THEN
    
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
        access_restriction_old.id,
        access_restriction_new.id,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- remote_application changed
    IF(coalesce(access_restriction_new.remote_application, '') != coalesce(access_restriction_old.remote_application, '')) THEN
    
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
        access_restriction_old.remote_application,
        access_restriction_new.remote_application,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- user_code changed
    IF(coalesce(access_restriction_new.user_code, '') != coalesce(access_restriction_old.user_code, '')) THEN
    
      INSERT INTO ADIT.adit_log(
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
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- restriction changed
    IF(coalesce(access_restriction_new.restriction, '') != coalesce(access_restriction_old.restriction, '')) THEN
    
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
        'restriction',
        access_restriction_old.restriction,
        access_restriction_new.restriction,
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
-- Definition for function log_adit_user (OID = 16711) : 
--
CREATE FUNCTION aditlog.log_adit_user (
  adit_user_new adit.adit_user,
  adit_user_old adit.adit_user,
  operation text
)
RETURNS void
AS 
$body$
DECLARE

    usr       varchar(20);
    pkey_col  varchar(50);
    tablename varchar(50) := 'ADIT_USER';
    primary_key_v adit.adit_user.user_code%TYPE := adit_user_old.user_code;
    test_date timestamp := aditlog.get_test_date();
  
BEGIN
  
    IF(coalesce(primary_key_v::text, '') = '') THEN
      primary_key_v := adit_user_new.user_code;
    END IF;
  
    -- Current user
    SELECT USER INTO usr ;
  
    -- user_code changed
    IF(coalesce(adit_user_new.user_code, '') != coalesce(adit_user_old.user_code, '')) THEN
    
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
        adit_user_old.user_code,
        adit_user_new.user_code,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- full_name changed
    IF(coalesce(adit_user_new.full_name, '') != coalesce(adit_user_old.full_name, '')) THEN
    
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
        'full_name',
        adit_user_old.full_name,
        adit_user_new.full_name,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- usertype changed
    IF(coalesce(adit_user_new.usertype, '') != coalesce(adit_user_old.usertype, '')) THEN
    
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
        'usertype',
        adit_user_old.usertype,
        adit_user_new.usertype,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- active changed
    IF(coalesce(adit_user_new.active, 0) != coalesce(adit_user_old.active, 0)) THEN
    
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
        'active',
        adit_user_old.active,
        adit_user_new.active,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- dvk_org_code changed
    IF(coalesce(adit_user_new.dvk_org_code, '') != coalesce(adit_user_old.dvk_org_code, '')) THEN
    
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
        'dvk_org_code',
        adit_user_old.dvk_org_code,
        adit_user_new.dvk_org_code,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- dvk_subdivision_short_name changed
    IF(coalesce(adit_user_new.dvk_subdivision_short_name, '') != coalesce(adit_user_old.dvk_subdivision_short_name, '')) THEN
    
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
        'dvk_subdivision_short_name',
        adit_user_old.dvk_subdivision_short_name,
        adit_user_new.dvk_subdivision_short_name,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- dvk_occupation_short_name changed
    IF(coalesce(adit_user_new.dvk_occupation_short_name, '') != coalesce(adit_user_old.dvk_occupation_short_name, '')) THEN
    
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
        'dvk_occupation_short_name',
        adit_user_old.dvk_occupation_short_name,
        adit_user_new.dvk_occupation_short_name,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- disk_quota changed
    IF(coalesce(adit_user_new.disk_quota, 0) != coalesce(adit_user_old.disk_quota, 0)) THEN
    
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
        'disk_quota',
        adit_user_old.disk_quota,
        adit_user_new.disk_quota,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- deactivation_date changed
    IF(coalesce(adit_user_new.deactivation_date, test_date) != coalesce(adit_user_old.deactivation_date, test_date)) THEN
    
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
        'deactivation_date',
        adit_user_old.deactivation_date::character varying,
        adit_user_new.deactivation_date::character varying,
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
-- Definition for function log_document_file (OID = 24582) : 
--
CREATE FUNCTION aditlog.log_document_file (
  document_file_new adit.document_file,
  document_file_old adit.document_file,
  operation text
)
RETURNS void
AS 
$body$
DECLARE

    usr       varchar(20);
    pkey_col  varchar(50);
    tablename varchar(50) := 'DOCUMENT_FILE';
    primary_key_v bigint := document_file_old.id;
  
BEGIN
  
    IF(coalesce(primary_key_v::text, '') = '') THEN
      primary_key_v := document_file_new.id;
    END IF;
  
    -- Current user
    SELECT USER INTO usr ;
  
    -- id changed
    IF(coalesce(document_file_new.id, 0) != coalesce(document_file_old.id, 0)) THEN
    
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
        document_file_old.id,
        document_file_new.id,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- document_id changed
    IF(coalesce(document_file_new.document_id, 0) != coalesce(document_file_old.document_id, 0)) THEN
    
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
        document_file_old.document_id,
        document_file_new.document_id,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- file_name changed
    IF(coalesce(document_file_new.file_name, '') != coalesce(document_file_old.file_name, '')) THEN
    
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
        'file_name',
        document_file_old.file_name,
        document_file_new.file_name,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- content_type changed
    IF(coalesce(document_file_new.content_type, '') != coalesce(document_file_old.content_type, '')) THEN
    
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
        'content_type',
        document_file_old.content_type,
        document_file_new.content_type,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- description changed
    IF(coalesce(document_file_new.description, '') != coalesce(document_file_old.description, '')) THEN
    
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
        'description',
        document_file_old.description,
        document_file_new.description,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- file_size_bytes changed
    IF(coalesce(document_file_new.file_size_bytes, 0) != coalesce(document_file_old.file_size_bytes, 0)) THEN
    
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
        'file_size_bytes',
        document_file_old.file_size_bytes,
        document_file_new.file_size_bytes,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- deleted changed
    IF(coalesce(document_file_new.deleted, 0) != coalesce(document_file_old.deleted, 0)) THEN
    
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
        document_file_old.deleted,
        document_file_new.deleted,
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
-- Definition for function log_document (OID = 24583) : 
--
CREATE FUNCTION aditlog.log_document (
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
  
END;
$body$
LANGUAGE plpgsql
SECURITY DEFINER;
--
-- Definition for function log_document_dvk_status (OID = 24584) : 
--
CREATE FUNCTION aditlog.log_document_dvk_status (
  document_dvk_status_new adit.document_dvk_status,
  document_dvk_status_old adit.document_dvk_status,
  operation text
)
RETURNS void
AS 
$body$
DECLARE

    usr       varchar(20);
    pkey_col  varchar(50);
    tablename varchar(50) := 'DOCUMENT_DVK_STATUS';
    primary_key_v bigint := document_dvk_status_old.id;
  
BEGIN
  
    IF(coalesce(primary_key_v::text, '') = '') THEN
      primary_key_v := document_dvk_status_new.id;
    END IF;
  
    -- Current user
    SELECT USER INTO usr ;
  
    -- id changed
    IF(coalesce(document_dvk_status_new.id, 0) != coalesce(document_dvk_status_old.id, 0)) THEN
    
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
        document_dvk_status_old.id,
        document_dvk_status_new.id,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- description changed
    IF(coalesce(document_dvk_status_new.description, '') != coalesce(document_dvk_status_old.description, '')) THEN
    
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
        'description',
        document_dvk_status_old.description,
        document_dvk_status_new.description,
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
-- Definition for function log_document_history (OID = 24585) : 
--
CREATE FUNCTION aditlog.log_document_history (
  document_history_new adit.document_history,
  document_history_old adit.document_history,
  operation text
)
RETURNS void
AS 
$body$
DECLARE

    usr       varchar(20);
    pkey_col  varchar(50);
    tablename varchar(50) := 'DOCUMENT_HISTORY';
    primary_key_v bigint := document_history_old.id;
    test_date timestamp := aditlog.get_test_date();
  
BEGIN
  
    IF(coalesce(primary_key_v::text, '') = '') THEN
      primary_key_v := document_history_new.id;
    END IF;
  
    -- Current user
    SELECT USER INTO usr ;
    
    -- id changed
    IF(coalesce(document_history_new.id, 0) != coalesce(document_history_old.id, 0)) THEN
    
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
        document_history_old.id,
        document_history_new.id,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- document_id changed
    IF(coalesce(document_history_new.document_id, 0) != coalesce(document_history_old.document_id, 0)) THEN
    
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
        document_history_old.document_id,
        document_history_new.document_id,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- document_history_type changed
    IF(coalesce(document_history_new.document_history_type, '') != coalesce(document_history_old.document_history_type, '')) THEN
    
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
        'document_history_type',
        document_history_old.document_history_type,
        document_history_new.document_history_type,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- description changed
    IF(coalesce(document_history_new.description, '') != coalesce(document_history_old.description, '')) THEN
    
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
        'description',
        document_history_old.description,
        document_history_new.description,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- event_date changed
    IF(coalesce(document_history_new.event_date, test_date) != coalesce(document_history_old.event_date, test_date)) THEN
    
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
        'event_date',
        document_history_old.event_date::character varying,
        document_history_new.event_date::character varying,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- user_code changed
    IF(coalesce(document_history_new.user_code, '') != coalesce(document_history_old.user_code, '')) THEN
    
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
        document_history_old.user_code,
        document_history_new.user_code,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- user_name changed
    IF(coalesce(document_history_new.user_name, '') != coalesce(document_history_old.user_name, '')) THEN
    
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
        document_history_old.user_name,
        document_history_new.user_name,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- remote_application changed
    IF(coalesce(document_history_new.remote_application, '') != coalesce(document_history_old.remote_application, '')) THEN
    
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
        document_history_old.remote_application,
        document_history_new.remote_application,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- notification_status changed
    IF(coalesce(document_history_new.notification_status, '') != coalesce(document_history_old.notification_status, '')) THEN
    
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
        'notification_status',
        document_history_old.notification_status,
        document_history_new.notification_status,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- xtee_notification_id changed
    IF(coalesce(document_history_new.xtee_notification_id, '') != coalesce(document_history_old.xtee_notification_id, '')) THEN
    
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
        'xtee_notification_id',
        document_history_old.xtee_notification_id,
        document_history_new.xtee_notification_id,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- xtee_user_code changed
    IF(coalesce(document_history_new.xtee_user_code, '') != coalesce(document_history_old.xtee_user_code, '')) THEN
    
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
        'xtee_user_code',
        document_history_old.xtee_user_code,
        document_history_new.xtee_user_code,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- xtee_user_name changed
    IF(coalesce(document_history_new.xtee_user_name, '') != coalesce(document_history_old.xtee_user_name, '')) THEN
    
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
        'xtee_user_name',
        document_history_old.xtee_user_name,
        document_history_new.xtee_user_name,
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
-- Definition for function log_document_history_type (OID = 24586) : 
--
CREATE FUNCTION aditlog.log_document_history_type (
  document_history_type_new adit.document_history_type,
  document_history_type_old adit.document_history_type,
  operation text
)
RETURNS void
AS 
$body$
DECLARE

    usr       varchar(20);
    pkey_col  varchar(50);
    tablename varchar(50) := 'DOCUMENT_HISTORY_TYPE';
    primary_key_v adit.document_history_type.short_name%TYPE := document_history_type_old.short_name;
  
BEGIN
  
    IF(coalesce(primary_key_v::text, '') = '') THEN
      primary_key_v := document_history_type_new.short_name;
    END IF;
  
    -- Current user
    SELECT USER INTO usr ;
    
    -- short_name changed
    IF(coalesce(document_history_type_new.short_name, '') != coalesce(document_history_type_old.short_name, '')) THEN
    
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
        'short_name',
        document_history_type_old.short_name,
        document_history_type_new.short_name,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- description changed
    IF(coalesce(document_history_type_new.description, '') != coalesce(document_history_type_old.description, '')) THEN
    
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
        'description',
        document_history_type_old.description,
        document_history_type_new.description,
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
CREATE FUNCTION aditlog.log_document_sharing (
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
END;
$body$
LANGUAGE plpgsql
SECURITY DEFINER;
--
-- Definition for function log_document_sharing_type (OID = 24640) : 
--
CREATE FUNCTION aditlog.log_document_sharing_type (
  document_sharing_type_new adit.document_sharing_type,
  document_sharing_type_old adit.document_sharing_type,
  operation text
)
RETURNS void
AS 
$body$
DECLARE

    usr       varchar(20);
    pkey_col  varchar(50);
    tablename varchar(50) := 'DOCUMENT_SHARING_TYPE';
    primary_key_v adit.document_sharing_type.short_name%TYPE := document_sharing_type_old.short_name;
  
BEGIN
  
    IF(coalesce(primary_key_v::text, '') = '') THEN
      primary_key_v := document_sharing_type_new.short_name;
    END IF;
  
    -- Current user
    SELECT USER INTO usr ;
  
    -- short_name changed
    IF(coalesce(document_sharing_type_new.short_name, '') != coalesce(document_sharing_type_old.short_name, '')) THEN
    
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
        'short_name',
        document_sharing_type_old.short_name,
        document_sharing_type_new.short_name,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- description changed
    IF(coalesce(document_sharing_type_new.description, '') != coalesce(document_sharing_type_old.description, '')) THEN
    
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
        'description',
        document_sharing_type_old.description,
        document_sharing_type_new.description,
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
-- Definition for function log_document_type (OID = 24641) : 
--
CREATE FUNCTION aditlog.log_document_type (
  document_type_new adit.document_type,
  document_type_old adit.document_type,
  operation text
)
RETURNS void
AS 
$body$
DECLARE

    usr       varchar(20);
    pkey_col  varchar(50);
    tablename varchar(50) := 'DOCUMENT_TYPE';
    primary_key_v adit.document_type.short_name%TYPE := document_type_old.short_name;
  
BEGIN
  
    IF(coalesce(primary_key_v::text, '') = '') THEN
      primary_key_v := document_type_new.short_name;
    END IF;
  
    -- Current user
    SELECT USER INTO usr ;
  
    -- short_name changed
    IF(coalesce(document_type_new.short_name, '') != coalesce(document_type_old.short_name, '')) THEN
    
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
        'short_name',
        document_type_old.short_name,
        document_type_new.short_name,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- description changed
    IF(coalesce(document_type_new.description, '') != coalesce(document_type_old.description, '')) THEN
    
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
        'description',
        document_type_old.description,
        document_type_new.description,
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
-- Definition for function log_notification (OID = 24643) : 
--
CREATE FUNCTION aditlog.log_notification (
  notification_new adit.notification,
  notification_old adit.notification,
  operation text
)
RETURNS void
AS 
$body$
DECLARE

    usr       varchar(20);
    pkey_col  varchar(50);
    tablename varchar(50) := 'NOTIFICATION';
    primary_key_v adit.notification.id%TYPE := notification_old.id;
    test_date timestamp := aditlog.get_test_date();
  
BEGIN
  
    IF(coalesce(primary_key_v::text, '') = '') THEN
      primary_key_v := notification_new.id;
    END IF;
  
    -- Current user
    SELECT USER INTO usr ;
  
    -- id changed
    IF(coalesce(notification_new.id, 0) != coalesce(notification_old.id, 0)) THEN
    
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
        notification_old.id,
        notification_new.id,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- user_code changed
    IF(coalesce(notification_new.user_code, '') != coalesce(notification_old.user_code, '')) THEN
    
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
        notification_old.user_code,
        notification_new.user_code,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- document_id changed
    IF(coalesce(notification_new.document_id, 0) != coalesce(notification_old.document_id, 0)) THEN
    
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
        notification_old.document_id,
        notification_new.document_id,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- event_date changed
    IF(coalesce(notification_new.event_date, test_date) != coalesce(notification_old.event_date, test_date)) THEN
    
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
        'event_date',
        notification_old.event_date::character varying,
        notification_new.event_date::character varying,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- notification_type changed
    IF(coalesce(notification_new.notification_type, '') != coalesce(notification_old.notification_type, '')) THEN
    
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
        'notification_type',
        notification_old.notification_type,
        notification_new.notification_type,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- notification_text changed
    IF(coalesce(notification_new.notification_text, '') != coalesce(notification_old.notification_text, '')) THEN
    
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
        'notification_text',
        notification_old.notification_text,
        notification_new.notification_text,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- notification_id changed
    IF(coalesce(notification_new.notification_id, 0) != coalesce(notification_old.notification_id, 0)) THEN
    
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
        'notification_id',
        notification_old.notification_id,
        notification_new.notification_id,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- notification_sending_date changed
    IF(coalesce(notification_new.notification_sending_date, test_date) != coalesce(notification_old.notification_sending_date, test_date)) THEN
    
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
        'notification_sending_date',
        notification_old.notification_sending_date::character varying, 
        notification_new.notification_sending_date::character varying,
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
-- Definition for function log_document_wf_status (OID = 24644) : 
--
CREATE FUNCTION aditlog.log_document_wf_status (
  document_wf_status_new adit.document_wf_status,
  document_wf_status_old adit.document_wf_status,
  operation text
)
RETURNS void
AS 
$body$
DECLARE

    usr       varchar(20);
    pkey_col  varchar(50);
    tablename varchar(50) := 'DOCUMENT_WF_STATUS';
    primary_key_v adit.document_wf_status.id%TYPE := document_wf_status_old.id;
  
BEGIN
  
    IF(coalesce(primary_key_v::text, '') = '') THEN
      primary_key_v := document_wf_status_new.id;
    END IF;
  
    -- Current user
    SELECT USER INTO usr ;
  
    -- id changed
    IF(coalesce(document_wf_status_new.id, 0) != coalesce(document_wf_status_old.id, 0)) THEN
    
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
        document_wf_status_old.id,
        document_wf_status_new.id,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- description changed
    IF(coalesce(document_wf_status_new.description, '') != coalesce(document_wf_status_old.description, '')) THEN
    
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
        'description',
        document_wf_status_old.description,
        document_wf_status_new.description,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- name changed
    IF(coalesce(document_wf_status_new.name, '') != coalesce(document_wf_status_old.name, '')) THEN
    
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
        'name',
        document_wf_status_old.name,
        document_wf_status_new.name,
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
-- Definition for function log_notification_type (OID = 24645) : 
--
CREATE FUNCTION aditlog.log_notification_type (
  notification_type_new adit.notification_type,
  notification_type_old adit.notification_type,
  operation text
)
RETURNS void
AS 
$body$
DECLARE

    usr       varchar(20);
    pkey_col  varchar(50);
    tablename varchar(50) := 'NOTIFICATION_TYPE';
    primary_key_v adit.notification_type.short_name%TYPE := notification_type_old.short_name;
  
BEGIN
  
    IF(coalesce(primary_key_v::text, '') = '') THEN
      primary_key_v := notification_type_new.short_name;
    END IF;
  
    -- Current user
    SELECT USER INTO usr ;
    
    -- short_name changed
    IF(coalesce(notification_type_new.short_name, '') != coalesce(notification_type_old.short_name, '')) THEN
    
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
        'short_name',
        notification_type_old.short_name,
        notification_type_new.short_name,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- description changed
    IF(coalesce(notification_type_new.description, '') != coalesce(notification_type_old.description, '')) THEN
    
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
        'description',
        notification_type_old.description,
        notification_type_new.description,
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
-- Definition for function log_remote_application (OID = 24646) : 
--
CREATE FUNCTION aditlog.log_remote_application (
  remote_application_new adit.remote_application,
  remote_application_old adit.remote_application,
  operation text
)
RETURNS void
AS 
$body$
DECLARE

    usr       varchar(20);
    pkey_col  varchar(50);
    tablename varchar(50) := 'REMOTE_APPLICATION';
    primary_key_v adit.remote_application.short_name%TYPE := remote_application_old.short_name;
  
BEGIN
  
    IF(coalesce(primary_key_v::text, '') = '') THEN
      primary_key_v := remote_application_new.short_name;
    END IF;
  
    -- Current user
    SELECT USER INTO usr ;
    
    -- short_name changed
    IF(coalesce(remote_application_new.short_name, '') != coalesce(remote_application_old.short_name, '')) THEN
    
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
        'short_name',
        remote_application_old.short_name,
        remote_application_new.short_name,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- name changed
    IF(coalesce(remote_application_new.name, '') != coalesce(remote_application_old.name, '')) THEN
    
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
        'name',
        remote_application_old.name,
        remote_application_new.name,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- organization_code changed
    IF(coalesce(remote_application_new.organization_code, '') != coalesce(remote_application_old.organization_code, '')) THEN
    
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
        'organization_code',
        remote_application_old.organization_code,
        remote_application_new.organization_code,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- can_read changed
    IF(coalesce(remote_application_new.can_read, 0) != coalesce(remote_application_old.can_read, 0)) THEN
    
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
        'can_read',
        remote_application_old.can_read,
        remote_application_new.can_read,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- can_write changed
    IF(coalesce(remote_application_new.can_write, 0) != coalesce(remote_application_old.can_write, 0)) THEN
    
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
        'can_write',
        remote_application_old.can_write,
        remote_application_new.can_write,
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
-- Definition for function log_signature (OID = 24647) : 
--
CREATE FUNCTION aditlog.log_signature (
  signature_new adit.signature,
  signature_old adit.signature,
  operation text
)
RETURNS void
AS 
$body$
DECLARE

    usr       varchar(20);
    pkey_col  varchar(50);
    tablename varchar(50) := 'SIGNATURE';
    primary_key_v adit.signature.id%TYPE := signature_old.id;
  
BEGIN
  
    IF(coalesce(primary_key_v::text, '') = '') THEN
      primary_key_v := signature_new.id;
    END IF;
  
    -- Current user
    SELECT USER INTO usr ;
  
    -- id changed
    IF(coalesce(signature_new.id, 0) != coalesce(signature_old.id, 0)) THEN
    
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
        signature_old.id,
        signature_new.id,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- user_code changed
    IF(coalesce(signature_new.user_code, '') != coalesce(signature_old.user_code, '')) THEN
    
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
        signature_old.user_code,
        signature_new.user_code,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- document_id changed
    IF(coalesce(signature_new.document_id, 0) != coalesce(signature_old.document_id, 0)) THEN
    
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
        signature_old.document_id,
        signature_new.document_id,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- signer_role changed
    IF(coalesce(signature_new.signer_role, '') != coalesce(signature_old.signer_role, '')) THEN
    
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
        'signer_role',
        signature_old.signer_role,
        signature_new.signer_role,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- resolution changed
    IF(coalesce(signature_new.resolution, '') != coalesce(signature_old.resolution, '')) THEN
    
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
        'resolution',
        signature_old.resolution,
        signature_new.resolution,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- country changed
    IF(coalesce(signature_new.country, '') != coalesce(signature_old.country, '')) THEN
    
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
        'country',
        signature_old.country,
        signature_new.country,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- county changed
    IF(coalesce(signature_new.county, '') != coalesce(signature_old.county, '')) THEN
    
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
        'county',
        signature_old.county,
        signature_new.county,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- city changed
    IF(coalesce(signature_new.city, '') != coalesce(signature_old.city, '')) THEN
    
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
        'city',
        signature_old.city,
        signature_new.city,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- post_index changed
    IF(coalesce(signature_new.post_index, '') != coalesce(signature_old.post_index, '')) THEN
    
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
        'post_index',
        signature_old.post_index,
        signature_new.post_index,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- signer_code changed
    IF(coalesce(signature_new.signer_code, '') != coalesce(signature_old.signer_code, '')) THEN
    
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
        'signer_code',
        signature_old.signer_code,
        signature_new.signer_code,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- signer_name changed
    IF(coalesce(signature_new.signer_name, '') != coalesce(signature_old.signer_name, '')) THEN
    
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
        'signer_name',
        signature_old.signer_name,
        signature_new.signer_name,
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
-- Definition for function log_user_notification (OID = 24648) : 
--
CREATE FUNCTION aditlog.log_user_notification (
  user_notification_new adit.user_notification,
  user_notification_old adit.user_notification,
  operation text
)
RETURNS void
AS 
$body$
DECLARE

    usr       varchar(20);
    pkey_col  varchar(50);
    tablename varchar(50) := 'USER_NOTIFICATION';
    primary_key_v adit.user_notification.user_code%TYPE := user_notification_old.user_code;
  
BEGIN
  
    IF(coalesce(primary_key_v::text, '') = '') THEN
      primary_key_v := user_notification_new.user_code;
    END IF;
  
    -- Current user
    SELECT USER INTO usr ;
  
    -- user_code changed
    IF(coalesce(user_notification_new.user_code, '') != coalesce(user_notification_old.user_code, '')) THEN
    
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
        user_notification_old.user_code,
        user_notification_new.user_code,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
  
    -- notification_type changed
    IF(coalesce(user_notification_new.notification_type, '') != coalesce(user_notification_old.notification_type, '')) THEN
    
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
        'notification_type',
        user_notification_old.notification_type,
        user_notification_new.notification_type,
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
-- Definition for function log_usertype (OID = 24649) : 
--
CREATE FUNCTION aditlog.log_usertype (
  usertype_new adit.usertype,
  usertype_old adit.usertype,
  operation text
)
RETURNS void
AS 
$body$
DECLARE

    usr       varchar(20);
    pkey_col  varchar(50);
    tablename varchar(50) := 'USERTYPE';
    primary_key_v adit.usertype.short_name%TYPE := usertype_old.short_name;
  
BEGIN

    IF(coalesce(primary_key_v::text, '') = '') THEN
      primary_key_v := usertype_new.short_name;
    END IF;
	
    -- Current user
    SELECT USER INTO usr ;

    -- short_name changed
    IF(coalesce(usertype_new.short_name, '') != coalesce(usertype_old.short_name, '')) THEN
    
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
        'short_name',
        usertype_old.short_name,
        usertype_new.short_name,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- description changed
    IF(coalesce(usertype_new.description, '') != coalesce(usertype_old.description, '')) THEN
    
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
        'description',
        usertype_old.description,
        usertype_new.description,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- disk_quota changed
    IF(coalesce(usertype_new.disk_quota, 0) != coalesce(usertype_old.disk_quota, 0)) THEN
    
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
        'disk_quota',
        usertype_old.disk_quota,
        usertype_new.disk_quota,
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
-- Definition for function log_user_contact (OID = 24650) : 
--
CREATE FUNCTION aditlog.log_user_contact (
  user_contact_new adit.user_contact,
  user_contact_old adit.user_contact,
  operation text
)
RETURNS void
AS 
$body$
DECLARE

    usr       varchar(20);
    pkey_col  varchar(50);
    tablename varchar(50) := 'USER_CONTACT';
    primary_key_v adit.user_contact.user_code%TYPE := user_contact_old.user_code;
    test_date timestamp := aditlog.get_test_date();
  
BEGIN
  
    IF(coalesce(primary_key_v::text, '') = '') THEN
      primary_key_v := user_contact_new.user_code;
    END IF;
  
    -- Current user
    SELECT USER INTO usr ;
  
    -- user_code changed
    IF(coalesce(user_contact_new.user_code, '') != coalesce(user_contact_old.user_code, '')) THEN
    
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
        user_contact_old.user_code,
        user_contact_new.user_code,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- contact_code changed
    IF(coalesce(user_contact_new.contact_code, '') != coalesce(user_contact_old.contact_code, '')) THEN
    
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
        'contact_code',
        user_contact_old.contact_code,
        user_contact_new.contact_code,
        LOCALTIMESTAMP,
        aditlog.get_current_setting('aditlog.remote_application_short_name'),
        aditlog.get_current_setting('aditlog.xtee_isikukood'),
        aditlog.get_current_setting('aditlog.xtee_asutus'),
        usr,
        primary_key_v
      );
    END IF;
    
    -- last_used_date changed
    IF(coalesce(user_contact_new.last_used_date, test_date) != coalesce(user_contact_old.last_used_date, test_date)) THEN
    
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
        'last_used_date',
        user_contact_old.last_used_date,
        user_contact_new.last_used_date,
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
-- Definition for function get_test_date (OID = 24655) : 
--
CREATE FUNCTION aditlog.get_test_date (
)
RETURNS date
AS 
$body$
SELECT to_date('1900.01.01','yyyy.mm.dd');
$body$
LANGUAGE sql
IMMUTABLE SECURITY DEFINER;
--
-- Definition for function deflate_file (OID = 24658) : 
--
SET search_path = adit, pg_catalog;
CREATE FUNCTION adit.deflate_file (
  document_id bigint,
  file_id bigint,
  mark_deleted bigint,
  fail_if_signature bigint
)
RETURNS refcursor
AS 
$body$
DECLARE
result_rc refcursor;
item_count bigint := 0;

BEGIN
    select  count(*)
    into    item_count
    from    document_file
    where   document_file.id = file_id;

    if (item_count > 0) then
            select  count(*)
            into    item_count
            from    document_file
            where   document_file.id = DEFLATE_FILE.file_id
                    and document_file.document_id = DEFLATE_FILE.document_id;

            if (item_count > 0) then
                select  count(*)
                into    item_count
                from    document_file
                where   document_file.id = DEFLATE_FILE.file_id
                        and coalesce(document_file.deleted, 0) = 0;

                if (item_count > 0) then
	                select  count(*)
	                into    item_count
	                from    document_file
	                where   document_file.id = DEFLATE_FILE.file_id
	                        and coalesce(document_file.document_file_type_id, 1) > 1;

	                if ((item_count = 0) or (DEFLATE_FILE.fail_if_signature <> 1)) then
	                	-- Calculate MD5 hash
	                    update	document_file
	                    set	    file_data = md5(coalesce(file_data::text, ''))::bytea,
	                            deleted = (case when DEFLATE_FILE.mark_deleted = 1 then 1 else document_file.deleted end)
	                    where   id = DEFLATE_FILE.file_id;

	                    open result_rc for
			    select  'ok' as result_code;
			    return result_rc;
                    else
			    open result_rc for
			    select  'cannot_delete_signature_container' as result_code;
	                    return result_rc;
                    end if;
                else
	            open result_rc for
		    select  'already_deleted' as result_code;
                    return result_rc;
                end if;
            else
                open result_rc for
	        select  'file_does_not_belong_to_document' as result_code;
                return result_rc;
            end if;
    else
	open result_rc for
	select  'file_does_not_exist' as result_code;
        return result_rc;
    end if;
end;
$body$
LANGUAGE plpgsql
SECURITY DEFINER;
--
-- Definition for function remove_signed_file_contents (OID = 24660) : 
--
CREATE FUNCTION adit.remove_signed_file_contents (
  document_id bigint,
  file_id bigint,
  ddoc_start_offset bigint,
  ddoc_end_offset bigint
)
RETURNS refcursor
AS 
$body$
DECLARE
result_rc refcursor;
item_count bigint := 0;
BEGIN
    select  count(*)
    into    item_count
    from    document_file
    where   document_file.id = REMOVE_SIGNED_FILE_CONTENTS.file_id;

    if (item_count > 0) then
            select  count(*)
            into    item_count
            from    document_file
            where   document_file.id = REMOVE_SIGNED_FILE_CONTENTS.file_id
                    and document_file.document_id = REMOVE_SIGNED_FILE_CONTENTS.document_id;

            if (item_count > 0) then
                select  count(*)
                into    item_count
                from    document_file
                where   document_file.id = REMOVE_SIGNED_FILE_CONTENTS.file_id
                        and coalesce(document_file.deleted, 0) = 0;

                if (item_count > 0) then
                    select  count(*)
                    into    item_count
                    from    document_file
                    where   document_file.id = REMOVE_SIGNED_FILE_CONTENTS.file_id
                            and coalesce(document_file.file_data_in_ddoc, 0) = 0;

                    if (item_count > 0) then
	                    -- Calculate MD5 hash
	                    update  document_file
	                    set	    file_data = md5(coalesce(file_data::text, ''))::bytea,
	                            ddoc_datafile_start_offset = REMOVE_SIGNED_FILE_CONTENTS.ddoc_start_offset,
	                            ddoc_datafile_end_offset = REMOVE_SIGNED_FILE_CONTENTS.ddoc_end_offset,
	                            file_data_in_ddoc = 1
	                    where   id = REMOVE_SIGNED_FILE_CONTENTS.file_id;

	                    open result_rc for
			    select  'ok' as result_code;
			    return result_rc;
	                else
			    open result_rc for
			    select  'file_data_already_moved' as result_code;
	                    return result_rc;
	                end if;
                else
                    open result_rc for
		    select  'file_is_deleted' as result_code;
                    return result_rc;
                end if;
            else
		open result_rc for
		select  'file_does_not_belong_to_document' as result_code;
                return result_rc;
            end if;
    else
	open result_rc for
	select  'file_does_not_exist' as result_code;
        return result_rc;
    end if;
end;
$body$
LANGUAGE plpgsql
SECURITY DEFINER;
--
-- Definition for function trigger_fct_set_adit_log_id (OID = 24672) : 
--
SET search_path = adit, pg_catalog;
CREATE FUNCTION adit.trigger_fct_set_adit_log_id (
)
RETURNS trigger
AS 
$body$
BEGIN
  SELECT nextval('adit.adit_log_id_seq')
  INTO NEW.ID
  ;
RETURN NEW;
END
$body$
LANGUAGE plpgsql
SECURITY DEFINER;
--
-- Definition for function trigger_fct_tr_access_restriction_log (OID = 24683) : 
--
CREATE FUNCTION adit.trigger_fct_tr_access_restriction_log (
)
RETURNS trigger
AS 
$body$
DECLARE
  operation varchar(100);
  ACCESS_RESTRICTION_new ADIT.ACCESS_RESTRICTION%ROWTYPE;
  ACCESS_RESTRICTION_old ADIT.ACCESS_RESTRICTION%ROWTYPE;
BEGIN

  if TG_OP = 'INSERT' then
    operation := 'INSERT';
  else
    if TG_OP = 'UPDATE' then
      operation := 'UPDATE';
    else
      operation := 'DELETE';
    end if;
  end if;

  if TG_OP != 'DELETE' then
	  ACCESS_RESTRICTION_new.ID := NEW.ID;
	  ACCESS_RESTRICTION_new.REMOTE_APPLICATION := NEW.REMOTE_APPLICATION;
	  ACCESS_RESTRICTION_new.USER_CODE := NEW.USER_CODE;
	  ACCESS_RESTRICTION_new.RESTRICTION := NEW.RESTRICTION;
  end if;
  
  if TG_OP != 'INSERT' then
	  ACCESS_RESTRICTION_old.ID := OLD.ID;
	  ACCESS_RESTRICTION_old.REMOTE_APPLICATION := OLD.REMOTE_APPLICATION;
	  ACCESS_RESTRICTION_old.USER_CODE := OLD.USER_CODE;
	  ACCESS_RESTRICTION_old.RESTRICTION := OLD.RESTRICTION;
  end if;
  
  PERFORM aditlog.log_access_restriction(
    ACCESS_RESTRICTION_new,
    ACCESS_RESTRICTION_old,
    operation
  );

RETURN NEW;
END
$body$
LANGUAGE plpgsql
SECURITY DEFINER;
--
-- Definition for function trigger_fct_tr_adit_user_log (OID = 24687) : 
--
CREATE FUNCTION adit.trigger_fct_tr_adit_user_log (
)
RETURNS trigger
AS 
$body$
DECLARE
  operation varchar(100);
  ADIT_USER_new ADIT.ADIT_USER%ROWTYPE;
  ADIT_USER_old ADIT.ADIT_USER%ROWTYPE;
BEGIN

  if TG_OP = 'INSERT' then
    operation := 'INSERT';
  else
    if TG_OP = 'UPDATE' then
      operation := 'UPDATE';
    else
      operation := 'DELETE';
    end if;
  end if;
  
  if TG_OP != 'DELETE' then
	  ADIT_USER_new.USER_CODE := NEW.USER_CODE;
	  ADIT_USER_new.FULL_NAME := NEW.FULL_NAME;
	  ADIT_USER_new.USERTYPE := NEW.USERTYPE;
	  ADIT_USER_new.ACTIVE := NEW.ACTIVE;
	  ADIT_USER_new.DVK_ORG_CODE := NEW.DVK_ORG_CODE;
	  ADIT_USER_new.DVK_SUBDIVISION_SHORT_NAME := NEW.DVK_SUBDIVISION_SHORT_NAME;
	  ADIT_USER_new.DVK_OCCUPATION_SHORT_NAME := NEW.DVK_OCCUPATION_SHORT_NAME;
	  ADIT_USER_new.DISK_QUOTA := NEW.DISK_QUOTA;
	  ADIT_USER_new.DEACTIVATION_DATE := NEW.DEACTIVATION_DATE;
  end if;
  
  if TG_OP != 'INSERT' then
	  ADIT_USER_old.USER_CODE := OLD.USER_CODE;
	  ADIT_USER_old.FULL_NAME := OLD.FULL_NAME;
	  ADIT_USER_old.USERTYPE := OLD.USERTYPE;
	  ADIT_USER_old.ACTIVE := OLD.ACTIVE;
	  ADIT_USER_old.DVK_ORG_CODE := OLD.DVK_ORG_CODE;
	  ADIT_USER_old.DVK_SUBDIVISION_SHORT_NAME := OLD.DVK_SUBDIVISION_SHORT_NAME;
	  ADIT_USER_old.DVK_OCCUPATION_SHORT_NAME := OLD.DVK_OCCUPATION_SHORT_NAME;
	  ADIT_USER_old.DISK_QUOTA := OLD.DISK_QUOTA;
	  ADIT_USER_old.DEACTIVATION_DATE := OLD.DEACTIVATION_DATE;
  end if;
  
  PERFORM ADITLOG.LOG_ADIT_USER(
    ADIT_USER_new,
    ADIT_USER_old,
    operation
  );

RETURN NEW;
END
$body$
LANGUAGE plpgsql
SECURITY DEFINER;
--
-- Definition for function trigger_fct_tr_document_file_log (OID = 24709) : 
--
CREATE FUNCTION adit.trigger_fct_tr_document_file_log (
)
RETURNS trigger
AS 
$body$
DECLARE operation varchar(100);
  DOCUMENT_FILE_new ADIT.DOCUMENT_FILE%ROWTYPE;
  DOCUMENT_FILE_old ADIT.DOCUMENT_FILE%ROWTYPE;
BEGIN
  IF TG_OP = 'INSERT' THEN
    operation := 'INSERT';
  ELSE
    IF TG_OP = 'UPDATE' THEN
      operation := 'UPDATE';
    ELSE
      operation := 'DELETE';
    END IF;
  END IF;
  
  if TG_OP != 'DELETE' then
	  DOCUMENT_FILE_new.ID              := NEW.ID;
	  DOCUMENT_FILE_new.DOCUMENT_ID     := NEW.DOCUMENT_ID;
	  DOCUMENT_FILE_new.FILE_NAME       := NEW.FILE_NAME;
	  DOCUMENT_FILE_new.CONTENT_TYPE    := NEW.CONTENT_TYPE;
	  DOCUMENT_FILE_new.DESCRIPTION     := NEW.DESCRIPTION;
	  DOCUMENT_FILE_new.FILE_SIZE_BYTES := NEW.FILE_SIZE_BYTES;
	  DOCUMENT_FILE_new.DELETED         := NEW.DELETED;
  end if;
  
  if TG_OP != 'INSERT' then
	  DOCUMENT_FILE_old.ID              := OLD.ID;
	  DOCUMENT_FILE_old.DOCUMENT_ID     := OLD.DOCUMENT_ID;
	  DOCUMENT_FILE_old.FILE_NAME       := OLD.FILE_NAME;
	  DOCUMENT_FILE_old.CONTENT_TYPE    := OLD.CONTENT_TYPE;
	  DOCUMENT_FILE_old.DESCRIPTION     := OLD.DESCRIPTION;
	  DOCUMENT_FILE_old.FILE_SIZE_BYTES := OLD.FILE_SIZE_BYTES;
	  DOCUMENT_FILE_old.DELETED         := OLD.DELETED;
  end if;
  
  PERFORM ADITLOG.LOG_DOCUMENT_FILE( DOCUMENT_FILE_new, DOCUMENT_FILE_old, operation );
RETURN NEW;
END
$body$
LANGUAGE plpgsql
SECURITY DEFINER;
--
-- Definition for function trigger_fct_tr_document_history_log (OID = 24711) : 
--
CREATE FUNCTION adit.trigger_fct_tr_document_history_log (
)
RETURNS trigger
AS 
$body$
DECLARE
  operation varchar(100);
  DOCUMENT_HISTORY_new ADIT.DOCUMENT_HISTORY%ROWTYPE;
  DOCUMENT_HISTORY_old ADIT.DOCUMENT_HISTORY%ROWTYPE;
BEGIN

  if TG_OP = 'INSERT' then
    operation := 'INSERT';
  else
    if TG_OP = 'UPDATE' then
      operation := 'UPDATE';
    else
      operation := 'DELETE';
    end if;
  end if;
  
  if TG_OP != 'DELETE' then
	  DOCUMENT_HISTORY_new.ID := NEW.ID;
	  DOCUMENT_HISTORY_new.DOCUMENT_ID := NEW.DOCUMENT_ID;
	  DOCUMENT_HISTORY_new.DOCUMENT_HISTORY_TYPE := NEW.DOCUMENT_HISTORY_TYPE;
	  DOCUMENT_HISTORY_new.DESCRIPTION := NEW.DESCRIPTION;
	  DOCUMENT_HISTORY_new.EVENT_DATE := NEW.EVENT_DATE;
	  DOCUMENT_HISTORY_new.USER_CODE := NEW.USER_CODE;
	  DOCUMENT_HISTORY_new.USER_NAME := NEW.USER_NAME;
	  DOCUMENT_HISTORY_new.REMOTE_APPLICATION := NEW.REMOTE_APPLICATION;
	  DOCUMENT_HISTORY_new.NOTIFICATION_STATUS := NEW.NOTIFICATION_STATUS;
	  DOCUMENT_HISTORY_new.XTEE_NOTIFICATION_ID := NEW.XTEE_NOTIFICATION_ID;
	  DOCUMENT_HISTORY_new.XTEE_USER_CODE := NEW.XTEE_USER_CODE;
	  DOCUMENT_HISTORY_new.XTEE_USER_NAME := NEW.XTEE_USER_NAME;
  end if;
  
  if TG_OP != 'INSERT' then
	  DOCUMENT_HISTORY_old.ID := OLD.ID;
	  DOCUMENT_HISTORY_old.DOCUMENT_ID := OLD.DOCUMENT_ID;
	  DOCUMENT_HISTORY_old.DOCUMENT_HISTORY_TYPE := OLD.DOCUMENT_HISTORY_TYPE;
	  DOCUMENT_HISTORY_old.DESCRIPTION := OLD.DESCRIPTION;
	  DOCUMENT_HISTORY_old.EVENT_DATE := OLD.EVENT_DATE;
	  DOCUMENT_HISTORY_old.USER_CODE := OLD.USER_CODE;
	  DOCUMENT_HISTORY_old.USER_NAME := OLD.USER_NAME;
	  DOCUMENT_HISTORY_old.REMOTE_APPLICATION := OLD.REMOTE_APPLICATION;
	  DOCUMENT_HISTORY_old.NOTIFICATION_STATUS := OLD.NOTIFICATION_STATUS;
	  DOCUMENT_HISTORY_old.XTEE_NOTIFICATION_ID := OLD.XTEE_NOTIFICATION_ID;
	  DOCUMENT_HISTORY_old.XTEE_USER_CODE := OLD.XTEE_USER_CODE;
	  DOCUMENT_HISTORY_old.XTEE_USER_NAME := OLD.XTEE_USER_NAME;
  end if;
  
  PERFORM ADITLOG.LOG_DOCUMENT_HISTORY(
    DOCUMENT_HISTORY_new,
    DOCUMENT_HISTORY_old,
    operation
  );

RETURN NEW;
END
$body$
LANGUAGE plpgsql
SECURITY DEFINER;
--
-- Definition for function trigger_fct_tr_document_history_type_log (OID = 24713) : 
--
CREATE FUNCTION adit.trigger_fct_tr_document_history_type_log (
)
RETURNS trigger
AS 
$body$
DECLARE
  operation varchar(100);
  DOCUMENT_HISTORY_TYPE_new ADIT.DOCUMENT_HISTORY_TYPE%ROWTYPE;
  DOCUMENT_HISTORY_TYPE_old ADIT.DOCUMENT_HISTORY_TYPE%ROWTYPE;
BEGIN

  if TG_OP = 'INSERT' then
    operation := 'INSERT';
  else
    if TG_OP = 'UPDATE' then
      operation := 'UPDATE';
    else
      operation := 'DELETE';
    end if;
  end if;
  
  if TG_OP != 'DELETE' then
	  DOCUMENT_HISTORY_TYPE_new.SHORT_NAME := NEW.SHORT_NAME;
	  DOCUMENT_HISTORY_TYPE_new.DESCRIPTION := NEW.SHORT_NAME;
  end if;
  
  if TG_OP != 'INSERT' then
	  DOCUMENT_HISTORY_TYPE_old.SHORT_NAME := OLD.SHORT_NAME;
	  DOCUMENT_HISTORY_TYPE_old.DESCRIPTION := OLD.SHORT_NAME;
  end if;

  PERFORM ADITLOG.LOG_DOCUMENT_HISTORY_TYPE(
    DOCUMENT_HISTORY_TYPE_new,
    DOCUMENT_HISTORY_TYPE_old,
    operation
  );

RETURN NEW;
END
$body$
LANGUAGE plpgsql
SECURITY DEFINER;
--
-- Definition for function trigger_fct_tr_document_log (OID = 24715) : 
--
CREATE FUNCTION adit.trigger_fct_tr_document_log (
)
RETURNS trigger
AS 
$body$
DECLARE
  operation varchar(100);
  DOCUMENT_new ADIT.DOCUMENT%ROWTYPE;
  DOCUMENT_old ADIT.DOCUMENT%ROWTYPE;
BEGIN

  if TG_OP = 'INSERT' then
    operation := 'INSERT';
  else
    if TG_OP = 'UPDATE' then
      operation := 'UPDATE';
    else
      operation := 'DELETE';
    end if;
  end if;

  if TG_OP != 'DELETE' then	
	  DOCUMENT_new.ID := NEW.ID;
	  DOCUMENT_new.GUID := NEW.GUID;
	  DOCUMENT_new.TITLE := NEW.TITLE;
	  DOCUMENT_new.TYPE := NEW.TYPE;
	  DOCUMENT_new.CREATOR_CODE := NEW.CREATOR_CODE;
	  DOCUMENT_new.CREATOR_NAME := NEW.CREATOR_NAME;
	  DOCUMENT_new.CREATOR_USER_CODE := NEW.CREATOR_USER_CODE;
	  DOCUMENT_new.CREATOR_USER_NAME := NEW.CREATOR_USER_NAME;
	  DOCUMENT_new.CREATION_DATE := NEW.CREATION_DATE;
	  DOCUMENT_new.REMOTE_APPLICATION := NEW.REMOTE_APPLICATION;
	  DOCUMENT_new.LAST_MODIFIED_DATE := NEW.LAST_MODIFIED_DATE;
	  DOCUMENT_new.DOCUMENT_DVK_STATUS_ID := NEW.DOCUMENT_DVK_STATUS_ID;
	  DOCUMENT_new.DVK_ID := NEW.DVK_ID;
	  DOCUMENT_new.DOCUMENT_WF_STATUS_ID := NEW.DOCUMENT_WF_STATUS_ID;
	  DOCUMENT_new.PARENT_ID := NEW.PARENT_ID;
	  DOCUMENT_new.LOCKED := NEW.LOCKED;
	  DOCUMENT_new.LOCKING_DATE := NEW.LOCKING_DATE;
	  DOCUMENT_new.SIGNABLE := NEW.SIGNABLE;
	  DOCUMENT_new.DEFLATED := NEW.DEFLATED;
	  DOCUMENT_new.DEFLATE_DATE := NEW.DEFLATE_DATE;
	  DOCUMENT_new.DELETED := NEW.DELETED;
  end if;
  
  if TG_OP != 'INSERT' then
	  DOCUMENT_old.ID := OLD.ID;
	  DOCUMENT_old.GUID := OLD.GUID;
	  DOCUMENT_old.TITLE := OLD.TITLE;
	  DOCUMENT_old.TYPE := OLD.TYPE;
	  DOCUMENT_old.CREATOR_CODE := OLD.CREATOR_CODE;
	  DOCUMENT_old.CREATOR_NAME := OLD.CREATOR_NAME;
	  DOCUMENT_old.CREATOR_USER_CODE := OLD.CREATOR_USER_CODE;
	  DOCUMENT_old.CREATOR_USER_NAME := OLD.CREATOR_USER_NAME;
	  DOCUMENT_old.CREATION_DATE := OLD.CREATION_DATE;
	  DOCUMENT_old.REMOTE_APPLICATION := OLD.REMOTE_APPLICATION;
	  DOCUMENT_old.LAST_MODIFIED_DATE := OLD.LAST_MODIFIED_DATE;
	  DOCUMENT_old.DOCUMENT_DVK_STATUS_ID := OLD.DOCUMENT_DVK_STATUS_ID;
	  DOCUMENT_old.DVK_ID := OLD.DVK_ID;
	  DOCUMENT_old.DOCUMENT_WF_STATUS_ID := OLD.DOCUMENT_WF_STATUS_ID;
	  DOCUMENT_old.PARENT_ID := OLD.PARENT_ID;
	  DOCUMENT_old.LOCKED := OLD.LOCKED;
	  DOCUMENT_old.LOCKING_DATE := OLD.LOCKING_DATE;
	  DOCUMENT_old.SIGNABLE := OLD.SIGNABLE;
	  DOCUMENT_old.DEFLATED := OLD.DEFLATED;
	  DOCUMENT_old.DEFLATE_DATE := OLD.DEFLATE_DATE;
	  DOCUMENT_old.DELETED := OLD.DELETED;
  end if;

  PERFORM ADITLOG.LOG_DOCUMENT(
    DOCUMENT_new,
    DOCUMENT_old,
    operation
  );

RETURN NEW;
END
$body$
LANGUAGE plpgsql
SECURITY DEFINER;
--
-- Definition for function trigger_fct_tr_document_sharing_log (OID = 24717) : 
--
CREATE FUNCTION adit.trigger_fct_tr_document_sharing_log (
)
RETURNS trigger
AS 
$body$
DECLARE
  operation varchar(100);
  DOCUMENT_SHARING_new ADIT.DOCUMENT_SHARING%ROWTYPE;
  DOCUMENT_SHARING_old ADIT.DOCUMENT_SHARING%ROWTYPE;
BEGIN
 
  if TG_OP = 'INSERT' then
    operation := 'INSERT';
  else
    if TG_OP = 'UPDATE' then
      operation := 'UPDATE';
    else
      operation := 'DELETE';
    end if;
  end if; 

  if TG_OP != 'DELETE' then	
	  DOCUMENT_SHARING_new.ID := NEW.ID;
	  DOCUMENT_SHARING_new.DOCUMENT_ID := NEW.DOCUMENT_ID;
	  DOCUMENT_SHARING_new.USER_CODE := NEW.USER_CODE;
	  DOCUMENT_SHARING_new.USER_NAME := NEW.USER_NAME;
	  DOCUMENT_SHARING_new.SHARING_TYPE := NEW.SHARING_TYPE;
	  DOCUMENT_SHARING_new.TASK_DESCRIPTION := NEW.TASK_DESCRIPTION;
	  DOCUMENT_SHARING_new.CREATION_DATE := NEW.CREATION_DATE;
	  DOCUMENT_SHARING_new.DVK_STATUS_ID := NEW.DVK_STATUS_ID;
	  DOCUMENT_SHARING_new.WF_STATUS_ID := NEW.WF_STATUS_ID;
	  DOCUMENT_SHARING_new.FIRST_ACCESS_DATE := NEW.FIRST_ACCESS_DATE;
	  DOCUMENT_SHARING_new.DVK_ID := NEW.DVK_ID;
  end if;	  
  
  if TG_OP != 'INSERT' then
	  DOCUMENT_SHARING_old.ID := OLD.ID;
	  DOCUMENT_SHARING_old.DOCUMENT_ID := OLD.DOCUMENT_ID;
	  DOCUMENT_SHARING_old.USER_CODE := OLD.USER_CODE;
	  DOCUMENT_SHARING_old.USER_NAME := OLD.USER_NAME;
	  DOCUMENT_SHARING_old.SHARING_TYPE := OLD.SHARING_TYPE;
	  DOCUMENT_SHARING_old.TASK_DESCRIPTION := OLD.TASK_DESCRIPTION;
	  DOCUMENT_SHARING_old.CREATION_DATE := OLD.CREATION_DATE;
	  DOCUMENT_SHARING_old.DVK_STATUS_ID := OLD.DVK_STATUS_ID;
	  DOCUMENT_SHARING_old.WF_STATUS_ID := OLD.WF_STATUS_ID;
	  DOCUMENT_SHARING_old.FIRST_ACCESS_DATE := OLD.FIRST_ACCESS_DATE;
	  DOCUMENT_SHARING_old.DVK_ID := OLD.DVK_ID;
  end if;	  

  PERFORM ADITLOG.LOG_DOCUMENT_SHARING(
    DOCUMENT_SHARING_new,
    DOCUMENT_SHARING_old,
    operation
  );

RETURN NEW;
END
$body$
LANGUAGE plpgsql
SECURITY DEFINER;
--
-- Definition for function trigger_fct_tr_document_sharing_type_log (OID = 24719) : 
--
CREATE FUNCTION adit.trigger_fct_tr_document_sharing_type_log (
)
RETURNS trigger
AS 
$body$
DECLARE
  operation varchar(100);
  DOCUMENT_SHARING_TYPE_new ADIT.DOCUMENT_SHARING_TYPE%ROWTYPE;
  DOCUMENT_SHARING_TYPE_old ADIT.DOCUMENT_SHARING_TYPE%ROWTYPE;
BEGIN

  if TG_OP = 'INSERT' then
    operation := 'INSERT';
  else
    if TG_OP = 'UPDATE' then
      operation := 'UPDATE';
    else
      operation := 'DELETE';
    end if;
  end if;

  if TG_OP != 'DELETE' then
	  DOCUMENT_SHARING_TYPE_new.SHORT_NAME := NEW.SHORT_NAME;
	  DOCUMENT_SHARING_TYPE_new.DESCRIPTION := NEW.DESCRIPTION;
  end if;

  if TG_OP != 'INSERT' then  
	  DOCUMENT_SHARING_TYPE_old.SHORT_NAME := OLD.SHORT_NAME;
	  DOCUMENT_SHARING_TYPE_old.DESCRIPTION := OLD.DESCRIPTION;
  end if;

  PERFORM ADITLOG.LOG_DOCUMENT_SHARING_TYPE(
    DOCUMENT_SHARING_TYPE_new,
    DOCUMENT_SHARING_TYPE_old,
    operation
  );

RETURN NEW;
END
$body$
LANGUAGE plpgsql
SECURITY DEFINER;
--
-- Definition for function trigger_fct_tr_document_type_log (OID = 24721) : 
--
CREATE FUNCTION adit.trigger_fct_tr_document_type_log (
)
RETURNS trigger
AS 
$body$
DECLARE
  operation varchar(100);
  DOCUMENT_TYPE_new ADIT.DOCUMENT_TYPE%ROWTYPE;
  DOCUMENT_TYPE_old ADIT.DOCUMENT_TYPE%ROWTYPE;
BEGIN

  if TG_OP = 'INSERT' then
    operation := 'INSERT';
  else
    if TG_OP = 'UPDATE' then
      operation := 'UPDATE';
    else
      operation := 'DELETE';
    end if;
  end if;

  if TG_OP != 'DELETE' then	
	  DOCUMENT_TYPE_new.SHORT_NAME := NEW.SHORT_NAME;
	  DOCUMENT_TYPE_new.DESCRIPTION := NEW.DESCRIPTION;
  end if;	  
  
  if TG_OP != 'INSERT' then
	  DOCUMENT_TYPE_old.SHORT_NAME := OLD.SHORT_NAME;
	  DOCUMENT_TYPE_old.DESCRIPTION := OLD.DESCRIPTION;
  end if;	  

  PERFORM ADITLOG.LOG_DOCUMENT_TYPE(
    DOCUMENT_TYPE_new,
    DOCUMENT_TYPE_old,
    operation
  );

RETURN NEW;
END
$body$
LANGUAGE plpgsql
SECURITY DEFINER;
--
-- Definition for function trigger_fct_tr_document_wf_status_log (OID = 24723) : 
--
CREATE FUNCTION adit.trigger_fct_tr_document_wf_status_log (
)
RETURNS trigger
AS 
$body$
DECLARE
  operation varchar(100);
  DOCUMENT_WF_STATUS_new ADIT.DOCUMENT_WF_STATUS%ROWTYPE;
  DOCUMENT_WF_STATUS_old ADIT.DOCUMENT_WF_STATUS%ROWTYPE;
BEGIN

  if TG_OP = 'INSERT' then
    operation := 'INSERT';
  else
    if TG_OP = 'UPDATE' then
      operation := 'UPDATE';
    else
      operation := 'DELETE';
    end if;
  end if;
  
  if TG_OP != 'DELETE' then
	  DOCUMENT_WF_STATUS_new.ID := NEW.ID;
	  DOCUMENT_WF_STATUS_new.DESCRIPTION := NEW.DESCRIPTION;
	  DOCUMENT_WF_STATUS_new.NAME := NEW.NAME;
  end if;
	  
  if TG_OP != 'INSERT' then
	  DOCUMENT_WF_STATUS_old.ID := OLD.ID;
	  DOCUMENT_WF_STATUS_old.DESCRIPTION := OLD.DESCRIPTION;
	  DOCUMENT_WF_STATUS_old.NAME := OLD.NAME;
  end if;
  
  PERFORM ADITLOG.LOG_DOCUMENT_WF_STATUS(
    DOCUMENT_WF_STATUS_new,
    DOCUMENT_WF_STATUS_old,
    operation
  );

RETURN NEW;
END
$body$
LANGUAGE plpgsql
SECURITY DEFINER;
--
-- Definition for function trigger_fct_tr_document_dvk_status_log (OID = 24725) : 
--
CREATE FUNCTION adit.trigger_fct_tr_document_dvk_status_log (
)
RETURNS trigger
AS 
$body$
DECLARE
  operation varchar(100);
  DOCUMENT_DVK_STATUS_new ADIT.DOCUMENT_DVK_STATUS%ROWTYPE;
  DOCUMENT_DVK_STATUS_old ADIT.DOCUMENT_DVK_STATUS%ROWTYPE;
BEGIN

  if TG_OP = 'INSERT' then
    operation := 'INSERT';
  else
    if TG_OP = 'UPDATE' then
      operation := 'UPDATE';
    else
      operation := 'DELETE';
    end if;
  end if;

  if TG_OP != 'DELETE' then
	  DOCUMENT_DVK_STATUS_new.ID := NEW.ID;
	  DOCUMENT_DVK_STATUS_new.DESCRIPTION := NEW.DESCRIPTION;
  end if;
	  
  if TG_OP != 'INSERT' then
	  DOCUMENT_DVK_STATUS_old.ID := OLD.ID;
	  DOCUMENT_DVK_STATUS_old.DESCRIPTION := OLD.DESCRIPTION;
  end if;	  

  PERFORM ADITLOG.LOG_DOCUMENT_DVK_STATUS(
    DOCUMENT_DVK_STATUS_new,
    DOCUMENT_DVK_STATUS_old,
    operation
  );

RETURN NEW;
END
$body$
LANGUAGE plpgsql
SECURITY DEFINER;
--
-- Definition for function trigger_fct_tr_notification_log (OID = 24727) : 
--
CREATE FUNCTION adit.trigger_fct_tr_notification_log (
)
RETURNS trigger
AS 
$body$
DECLARE
  operation varchar(100);
  NOTIFICATION_new ADIT.NOTIFICATION%ROWTYPE;
  NOTIFICATION_old ADIT.NOTIFICATION%ROWTYPE;
BEGIN

  if TG_OP = 'INSERT' then
    operation := 'INSERT';
  else
    if TG_OP = 'UPDATE' then
      operation := 'UPDATE';
    else
      operation := 'DELETE';
    end if;
  end if;

  if TG_OP != 'DELETE' then  
	  NOTIFICATION_new.ID := NEW.ID;
	  NOTIFICATION_new.USER_CODE := NEW.USER_CODE;
	  NOTIFICATION_new.DOCUMENT_ID := NEW.DOCUMENT_ID;
	  NOTIFICATION_new.EVENT_DATE := NEW.EVENT_DATE;
	  NOTIFICATION_new.NOTIFICATION_TYPE := NEW.NOTIFICATION_TYPE;
	  NOTIFICATION_new.NOTIFICATION_TEXT := NEW.NOTIFICATION_TEXT;
	  NOTIFICATION_new.NOTIFICATION_ID := NEW.NOTIFICATION_ID;
	  NOTIFICATION_new.NOTIFICATION_SENDING_DATE := NEW.NOTIFICATION_SENDING_DATE;
  end if;
  
  if TG_OP != 'INSERT' then
	  NOTIFICATION_old.ID := OLD.ID;
	  NOTIFICATION_old.USER_CODE := OLD.USER_CODE;
	  NOTIFICATION_old.DOCUMENT_ID := OLD.DOCUMENT_ID;
	  NOTIFICATION_old.EVENT_DATE := OLD.EVENT_DATE;
	  NOTIFICATION_old.NOTIFICATION_TYPE := OLD.NOTIFICATION_TYPE;
	  NOTIFICATION_old.NOTIFICATION_TEXT := OLD.NOTIFICATION_TEXT;
	  NOTIFICATION_old.NOTIFICATION_ID := OLD.NOTIFICATION_ID;
	  NOTIFICATION_old.NOTIFICATION_SENDING_DATE := OLD.NOTIFICATION_SENDING_DATE;
  end if;

  PERFORM ADITLOG.LOG_NOTIFICATION(
    NOTIFICATION_new,
    NOTIFICATION_old,
    operation
  );

RETURN NEW;
END
$body$
LANGUAGE plpgsql
SECURITY DEFINER;
--
-- Definition for function trigger_fct_tr_notification_type_log (OID = 24729) : 
--
CREATE FUNCTION adit.trigger_fct_tr_notification_type_log (
)
RETURNS trigger
AS 
$body$
DECLARE
  operation varchar(100);
  NOTIFICATION_TYPE_new ADIT.NOTIFICATION_TYPE%ROWTYPE;
  NOTIFICATION_TYPE_old ADIT.NOTIFICATION_TYPE%ROWTYPE;
BEGIN

  if TG_OP = 'INSERT' then
    operation := 'INSERT';
  else
    if TG_OP = 'UPDATE' then
      operation := 'UPDATE';
    else
      operation := 'DELETE';
    end if;
  end if;

  if TG_OP != 'DELETE' then	
	  NOTIFICATION_TYPE_new.SHORT_NAME := NEW.SHORT_NAME;
	  NOTIFICATION_TYPE_new.DESCRIPTION := NEW.DESCRIPTION;
  end if;
  
  if TG_OP != 'INSERT' then
	  NOTIFICATION_TYPE_old.SHORT_NAME := OLD.SHORT_NAME;
	  NOTIFICATION_TYPE_old.DESCRIPTION := OLD.DESCRIPTION;
  end if;

  PERFORM ADITLOG.LOG_NOTIFICATION_TYPE(
    NOTIFICATION_TYPE_new,
    NOTIFICATION_TYPE_old,
    operation
  );

RETURN NEW;
END
$body$
LANGUAGE plpgsql
SECURITY DEFINER;
--
-- Definition for function trigger_fct_tr_remote_application_log (OID = 24731) : 
--
CREATE FUNCTION adit.trigger_fct_tr_remote_application_log (
)
RETURNS trigger
AS 
$body$
DECLARE
  operation varchar(100);
  REMOTE_APPLICATION_new ADIT.REMOTE_APPLICATION%ROWTYPE;
  REMOTE_APPLICATION_old ADIT.REMOTE_APPLICATION%ROWTYPE;
BEGIN

  if TG_OP = 'INSERT' then
    operation := 'INSERT';
  else
    if TG_OP = 'UPDATE' then
      operation := 'UPDATE';
    else
      operation := 'DELETE';
    end if;
  end if;
  
  if TG_OP != 'DELETE' then
	  REMOTE_APPLICATION_new.SHORT_NAME := NEW.SHORT_NAME;
	  REMOTE_APPLICATION_new.NAME := NEW.NAME;
	  REMOTE_APPLICATION_new.ORGANIZATION_CODE := NEW.ORGANIZATION_CODE;
	  REMOTE_APPLICATION_new.CAN_READ := NEW.CAN_READ;
	  REMOTE_APPLICATION_new.CAN_WRITE := NEW.CAN_WRITE;
  end if;
   
  if TG_OP != 'INSERT' then
	  REMOTE_APPLICATION_old.SHORT_NAME := OLD.SHORT_NAME;
	  REMOTE_APPLICATION_old.NAME := OLD.NAME;
	  REMOTE_APPLICATION_old.ORGANIZATION_CODE := OLD.ORGANIZATION_CODE;
	  REMOTE_APPLICATION_old.CAN_READ := OLD.CAN_READ;
	  REMOTE_APPLICATION_old.CAN_WRITE := OLD.CAN_WRITE;
  end if;
  
  PERFORM ADITLOG.LOG_REMOTE_APPLICATION(
    REMOTE_APPLICATION_new,
    REMOTE_APPLICATION_old,
    operation
  );

RETURN NEW;
END
$body$
LANGUAGE plpgsql
SECURITY DEFINER;
--
-- Definition for function trigger_fct_tr_signature_log (OID = 24733) : 
--
CREATE FUNCTION adit.trigger_fct_tr_signature_log (
)
RETURNS trigger
AS 
$body$
DECLARE
  operation varchar(100);
  SIGNATURE_new ADIT.SIGNATURE%ROWTYPE;
  SIGNATURE_old ADIT.SIGNATURE%ROWTYPE;
BEGIN

  if TG_OP = 'INSERT' then
    operation := 'INSERT';
  else
    if TG_OP = 'UPDATE' then
      operation := 'UPDATE';
    else
      operation := 'DELETE';
    end if;
  end if;

  if TG_OP != 'DELETE' then
	  SIGNATURE_new.ID := NEW.ID;
	  SIGNATURE_new.USER_CODE := NEW.USER_CODE;
	  SIGNATURE_new.DOCUMENT_ID := NEW.DOCUMENT_ID;
	  SIGNATURE_new.SIGNER_ROLE := NEW.SIGNER_ROLE;
	  SIGNATURE_new.RESOLUTION := NEW.RESOLUTION;
	  SIGNATURE_new.COUNTRY := NEW.COUNTRY;
	  SIGNATURE_new.COUNTY := NEW.COUNTY;
	  SIGNATURE_new.CITY := NEW.CITY;
	  SIGNATURE_new.POST_INDEX := NEW.POST_INDEX;
	  SIGNATURE_new.SIGNER_CODE := NEW.SIGNER_CODE;
	  SIGNATURE_new.SIGNER_NAME := NEW.SIGNER_NAME;
  end if;
  
  if TG_OP != 'INSERT' then
	  SIGNATURE_old.ID := OLD.ID;
	  SIGNATURE_old.USER_CODE := OLD.USER_CODE;
	  SIGNATURE_old.DOCUMENT_ID := OLD.DOCUMENT_ID;
	  SIGNATURE_old.SIGNER_ROLE := OLD.SIGNER_ROLE;
	  SIGNATURE_old.RESOLUTION := OLD.RESOLUTION;
	  SIGNATURE_old.COUNTRY := OLD.COUNTRY;
	  SIGNATURE_old.COUNTY := OLD.COUNTY;
	  SIGNATURE_old.CITY := OLD.CITY;
	  SIGNATURE_old.POST_INDEX := OLD.POST_INDEX;
	  SIGNATURE_old.SIGNER_CODE := OLD.SIGNER_CODE;
	  SIGNATURE_old.SIGNER_NAME := OLD.SIGNER_NAME;
  end if;

  PERFORM ADITLOG.LOG_SIGNATURE(
    SIGNATURE_new,
    SIGNATURE_old,
    operation
  );

RETURN NEW;
END
$body$
LANGUAGE plpgsql
SECURITY DEFINER;
--
-- Definition for function trigger_fct_tr_usertype_log (OID = 24735) : 
--
CREATE FUNCTION adit.trigger_fct_tr_usertype_log (
)
RETURNS trigger
AS 
$body$
DECLARE
  operation varchar(100);
  USERTYPE_new ADIT.USERTYPE%ROWTYPE;
  USERTYPE_old ADIT.USERTYPE%ROWTYPE;
BEGIN

  if TG_OP = 'INSERT' then
    operation := 'INSERT';
  else
    if TG_OP = 'UPDATE' then
      operation := 'UPDATE';
    else
      operation := 'DELETE';
    end if;
  end if;

  if TG_OP != 'DELETE' then
	  USERTYPE_new.SHORT_NAME := NEW.SHORT_NAME;
	  USERTYPE_new.DESCRIPTION := NEW.DESCRIPTION;
	  USERTYPE_new.DISK_QUOTA := NEW.DISK_QUOTA;
  end if;

  if TG_OP != 'INSERT' then
	  USERTYPE_old.SHORT_NAME := OLD.SHORT_NAME;
	  USERTYPE_old.DESCRIPTION := OLD.DESCRIPTION;
	  USERTYPE_old.DISK_QUOTA := OLD.DISK_QUOTA;
  end if;

  PERFORM ADITLOG.LOG_USERTYPE(
    USERTYPE_new,
    USERTYPE_old,
    operation
  );

RETURN NEW;
END
$body$
LANGUAGE plpgsql
SECURITY DEFINER;
--
-- Definition for function trigger_fct_tr_user_notification_log (OID = 24737) : 
--
CREATE FUNCTION adit.trigger_fct_tr_user_notification_log (
)
RETURNS trigger
AS 
$body$
DECLARE
  operation varchar(100);
  USER_NOTIFICATION_new ADIT.USER_NOTIFICATION%ROWTYPE;
  USER_NOTIFICATION_old ADIT.USER_NOTIFICATION%ROWTYPE;
BEGIN

  if TG_OP = 'INSERT' then
    operation := 'INSERT';
  else
    if TG_OP = 'UPDATE' then
      operation := 'UPDATE';
    else
      operation := 'DELETE';
    end if;
  end if;

  if TG_OP != 'DELETE' then
	  USER_NOTIFICATION_new.USER_CODE := NEW.USER_CODE;
	  USER_NOTIFICATION_new.NOTIFICATION_TYPE := NEW.NOTIFICATION_TYPE;
  end if;
  
  if TG_OP != 'INSERT' then
	  USER_NOTIFICATION_old.USER_CODE := OLD.USER_CODE;
	  USER_NOTIFICATION_old.NOTIFICATION_TYPE := OLD.NOTIFICATION_TYPE;
  end if;
  
  PERFORM ADITLOG.LOG_USER_NOTIFICATION(
    USER_NOTIFICATION_new,
    USER_NOTIFICATION_old,
    operation
  );

RETURN NEW;
END
$body$
LANGUAGE plpgsql
SECURITY DEFINER;
--
-- Definition for function trigger_fct_tr_user_contact_log : 
--
CREATE FUNCTION trigger_fct_tr_user_contact_log(
)
RETURNS trigger
AS 
$body$
DECLARE
  operation varchar(100);
  USER_CONTACT_new ADIT.USER_CONTACT%ROWTYPE;
  USER_CONTACT_old ADIT.USER_CONTACT%ROWTYPE;
BEGIN

  if TG_OP = 'INSERT' then
    operation := 'INSERT';
  else
    if TG_OP = 'UPDATE' then
      operation := 'UPDATE';
    else
      operation := 'DELETE';
    end if;
  end if;

  if TG_OP != 'DELETE' then
	  USER_CONTACT_new.ID := NEW.ID;
	  USER_CONTACT_new.USER_CODE := NEW.USER_CODE;
	  USER_CONTACT_new.CONTACT_CODE := NEW.CONTACT_CODE;
	  USER_CONTACT_new.LAST_USED_DATE := NEW.LAST_USED_DATE;
  end if;
  
  if TG_OP != 'INSERT' then
	  USER_CONTACT_old.ID := OLD.ID;
	  USER_CONTACT_old.USER_CODE := OLD.USER_CODE;
	  USER_CONTACT_old.CONTACT_CODE := OLD.CONTACT_CODE;
	  USER_CONTACT_old.LAST_USED_DATE := OLD.LAST_USED_DATE;
  end if;
  
  PERFORM ADITLOG.LOG_USER_CONTACT(
    USER_CONTACT_new,
    USER_CONTACT_old,
    operation
  );

RETURN NEW;
END
$body$
LANGUAGE plpgsql
SECURITY DEFINER;
--
-- Definition for function get_current_setting (OID = 24766) : 
--
SET search_path = aditlog, pg_catalog;
CREATE FUNCTION aditlog.get_current_setting (
  variable_name character varying
)
RETURNS varchar
AS 
$body$
DECLARE
    variable_value varchar(500);
BEGIN
	BEGIN
		SELECT current_setting(variable_name) INTO variable_value;
		EXCEPTION
			WHEN undefined_object THEN
			RETURN NULL;
	END;
	
	RETURN variable_value;
END;
$body$
LANGUAGE plpgsql
SECURITY DEFINER;


--
-- Definition for trigger set_adit_log_id (OID = 24767) : 
--
CREATE TRIGGER set_adit_log_id
    BEFORE INSERT ON adit.adit_log
    FOR EACH ROW
    EXECUTE PROCEDURE adit.trigger_fct_set_adit_log_id ();
--
-- Definition for trigger tr_access_restriction_log (OID = 24768) : 
--
CREATE TRIGGER tr_access_restriction_log
    AFTER INSERT OR DELETE OR UPDATE ON adit.access_restriction
    FOR EACH ROW
    EXECUTE PROCEDURE adit.trigger_fct_tr_access_restriction_log ();
--
-- Definition for trigger tr_adit_user_log (OID = 24769) : 
--
CREATE TRIGGER tr_adit_user_log
    AFTER INSERT OR DELETE OR UPDATE ON adit.adit_user
    FOR EACH ROW
    EXECUTE PROCEDURE adit.trigger_fct_tr_adit_user_log ();
--
-- Definition for trigger tr_document_file_log (OID = 24770) : 
--
CREATE TRIGGER tr_document_file_log
    AFTER INSERT OR DELETE OR UPDATE ON adit.document_file
    FOR EACH ROW
    EXECUTE PROCEDURE adit.trigger_fct_tr_document_file_log ();
--
-- Definition for trigger tr_document_history_log (OID = 24771) : 
--
CREATE TRIGGER tr_document_history_log
    AFTER INSERT OR DELETE OR UPDATE ON adit.document_history
    FOR EACH ROW
    EXECUTE PROCEDURE adit.trigger_fct_tr_document_history_log ();
--
-- Definition for trigger tr_document_history_type_log (OID = 24772) : 
--
CREATE TRIGGER tr_document_history_type_log
    AFTER INSERT OR DELETE OR UPDATE ON adit.document_history_type
    FOR EACH ROW
    EXECUTE PROCEDURE adit.trigger_fct_tr_document_history_type_log ();
--
-- Definition for trigger tr_document_log (OID = 24773) : 
--
CREATE TRIGGER tr_document_log
    AFTER INSERT OR DELETE OR UPDATE ON adit.document
    FOR EACH ROW
    EXECUTE PROCEDURE adit.trigger_fct_tr_document_log ();
--
-- Definition for trigger tr_document_sharing_log (OID = 24774) : 
--
CREATE TRIGGER tr_document_sharing_log
    AFTER INSERT OR DELETE OR UPDATE ON adit.document_sharing
    FOR EACH ROW
    EXECUTE PROCEDURE adit.trigger_fct_tr_document_sharing_log ();
--
-- Definition for trigger tr_document_sharing_type_log (OID = 24775) : 
--
CREATE TRIGGER tr_document_sharing_type_log
    AFTER INSERT OR DELETE OR UPDATE ON adit.document_sharing_type
    FOR EACH ROW
    EXECUTE PROCEDURE adit.trigger_fct_tr_document_sharing_type_log ();
--
-- Definition for trigger tr_document_type_log (OID = 24776) : 
--
CREATE TRIGGER tr_document_type_log
    AFTER INSERT OR DELETE OR UPDATE ON adit.document_type
    FOR EACH ROW
    EXECUTE PROCEDURE adit.trigger_fct_tr_document_type_log ();
--
-- Definition for trigger tr_document_wf_status_log (OID = 24777) : 
--
CREATE TRIGGER tr_document_wf_status_log
    AFTER INSERT OR DELETE OR UPDATE ON adit.document_wf_status
    FOR EACH ROW
    EXECUTE PROCEDURE adit.trigger_fct_tr_document_wf_status_log ();
--
-- Definition for trigger tr_document_dvk_status_log (OID = 24778) : 
--
CREATE TRIGGER tr_document_dvk_status_log
    AFTER INSERT OR DELETE OR UPDATE ON adit.document_dvk_status
    FOR EACH ROW
    EXECUTE PROCEDURE adit.trigger_fct_tr_document_dvk_status_log ();
--
-- Definition for trigger tr_notification_log (OID = 24779) : 
--
CREATE TRIGGER tr_notification_log
    AFTER INSERT OR DELETE OR UPDATE ON adit.notification
    FOR EACH ROW
    EXECUTE PROCEDURE adit.trigger_fct_tr_notification_log ();
--
-- Definition for trigger tr_notification_type_log (OID = 24780) : 
--
CREATE TRIGGER tr_notification_type_log
    AFTER INSERT OR DELETE OR UPDATE ON adit.notification_type
    FOR EACH ROW
    EXECUTE PROCEDURE adit.trigger_fct_tr_notification_type_log ();
--
-- Definition for trigger tr_remote_application_log (OID = 24781) : 
--
CREATE TRIGGER tr_remote_application_log
    AFTER INSERT OR DELETE OR UPDATE ON adit.remote_application
    FOR EACH ROW
    EXECUTE PROCEDURE adit.trigger_fct_tr_remote_application_log ();
--
-- Definition for trigger tr_signature_log (OID = 24782) : 
--
CREATE TRIGGER tr_signature_log
    AFTER INSERT OR DELETE OR UPDATE ON adit.signature
    FOR EACH ROW
    EXECUTE PROCEDURE adit.trigger_fct_tr_signature_log ();
--
-- Definition for trigger tr_usertype_log (OID = 24783) : 
--
CREATE TRIGGER tr_usertype_log
    AFTER INSERT OR DELETE OR UPDATE ON adit.usertype
    FOR EACH ROW
    EXECUTE PROCEDURE adit.trigger_fct_tr_usertype_log ();
--
-- Definition for trigger tr_user_notification_log (OID = 24784) : 
--
CREATE TRIGGER tr_user_notification_log
    AFTER INSERT OR DELETE OR UPDATE ON adit.user_notification
    FOR EACH ROW
    EXECUTE PROCEDURE adit.trigger_fct_tr_user_notification_log ();
	
--
-- Definition for trigger tr_user_contact_log : 
--
CREATE TRIGGER tr_user_contact_log
	AFTER INSERT OR DELETE OR UPDATE ON adit.user_contact
	FOR EACH ROW
	EXECUTE PROCEDURE adit.trigger_fct_tr_user_contact_log();