ALTER TABLE &&ADIT_SCHEMA..DOCUMENT ADD MIGRATED NUMBER (1,0);
COMMENT ON COLUMN &&ADIT_SCHEMA..DOCUMENT.migrated IS 'Indicates if this document has been migrated from state portal';

ALTER TABLE &&ADIT_SCHEMA..DOCUMENT MODIFY TITLE VARCHAR2 (355);
ALTER TABLE &&ADIT_SCHEMA..DOCUMENT_FILE MODIFY FILE_NAME VARCHAR2 (355);

CREATE TABLE &&ADIT_SCHEMA..MAINTENANCE_JOB
(
    ID              NUMBER(12) NOT NULL,                /* Unique ID of job */
    NAME            VARCHAR2(100),                      /* Name of job */
    IS_RUNNING      NUMBER(1,0) DEFAULT (0) NOT NULL    /* Indicates if current job is already running */
) TABLESPACE &&ADIT_TABLE_TABLESPACE.;

COMMENT ON TABLE &&ADIT_SCHEMA..MAINTENANCE_JOB               IS 'List of maintenance jobs. Required for maintenance task synchronization between cluster nodes';
COMMENT ON COLUMN &&ADIT_SCHEMA..MAINTENANCE_JOB.ID           IS 'Unique ID of job';
COMMENT ON COLUMN &&ADIT_SCHEMA..MAINTENANCE_JOB.NAME         IS 'Name of job';
COMMENT ON COLUMN &&ADIT_SCHEMA..MAINTENANCE_JOB.IS_RUNNING   IS 'Indicates if current job is already running';

ALTER TABLE &&ADIT_SCHEMA..MAINTENANCE_JOB ADD CONSTRAINT PK_MAINTENANCE_JOB
    PRIMARY KEY (ID)
USING INDEX TABLESPACE &&ADIT_INDEX_TABLESPACE.;

CREATE INDEX &&ADIT_SCHEMA..maintenance_job_search_idx ON &&ADIT_SCHEMA..maintenance_job (id, is_running) TABLESPACE &&ADIT_INDEX_TABLESPACE.;

INSERT INTO &&ADIT_SCHEMA..MAINTENANCE_JOB(ID, NAME, IS_RUNNING) VALUES(1, 'Send documents to DVK', 0);
INSERT INTO &&ADIT_SCHEMA..MAINTENANCE_JOB(ID, NAME, IS_RUNNING) VALUES(2, 'Receive documents from DVK', 0);
INSERT INTO &&ADIT_SCHEMA..MAINTENANCE_JOB(ID, NAME, IS_RUNNING) VALUES(3, 'Update document status from DVK', 0);
INSERT INTO &&ADIT_SCHEMA..MAINTENANCE_JOB(ID, NAME, IS_RUNNING) VALUES(4, 'Update document status to DVK', 0);
INSERT INTO &&ADIT_SCHEMA..MAINTENANCE_JOB(ID, NAME, IS_RUNNING) VALUES(5, 'Delete documents from DVK', 0);
INSERT INTO &&ADIT_SCHEMA..MAINTENANCE_JOB(ID, NAME, IS_RUNNING) VALUES(6, 'Synchronize users with DVK', 0);
INSERT INTO &&ADIT_SCHEMA..MAINTENANCE_JOB(ID, NAME, IS_RUNNING) VALUES(7, 'Send notifications', 0);
INSERT INTO &&ADIT_SCHEMA..MAINTENANCE_JOB(ID, NAME, IS_RUNNING) VALUES(8, 'Clean temporary files', 0);

-- Stored procedure for setting job running status as fast as possible
create or replace
procedure &&ADIT_SCHEMA..SET_JOB_RUNNING_STATUS(
    result_rc out sys_refcursor,
    job_id in number,
    is_running in number
)
as
item_count number(10,0) := 0;
begin
    update  maintenance_job
    set     is_running = SET_JOB_RUNNING_STATUS.is_running
    where   id = SET_JOB_RUNNING_STATUS.job_id
            and is_running <> SET_JOB_RUNNING_STATUS.is_running;

    if (SQL%ROWCOUNT > 0) then
        open result_rc for
        select  'ok' as result_code
        from    dual;
    else
        open result_rc for
        select  'job_is_aready_in_given_state' as result_code
        from    dual;
    end if;
end;
/

CREATE OR REPLACE SYNONYM &&ADIT_APP..MAINTENANCE_JOB FOR &&ADIT_SCHEMA..MAINTENANCE_JOB;
CREATE OR REPLACE SYNONYM &&ADIT_APP..SET_JOB_RUNNING_STATUS FOR &&ADIT_SCHEMA..SET_JOB_RUNNING_STATUS;
