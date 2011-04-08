ALTER TABLE &&ADIT_SCHEMA..DOCUMENT DROP COLUMN signature_container;

ALTER TABLE &&ADIT_SCHEMA..DOCUMENT ADD (invisible_to_owner NUMBER(1,0) NULL);
COMMENT ON COLUMN &&ADIT_SCHEMA..DOCUMENT.invisible_to_owner IS 'Document has been made invisible to its owner. Is used when document has been sent to someone else and owner wants to delete it from his/her own view.';

ALTER TABLE &&ADIT_SCHEMA..DOCUMENT ADD (signed NUMBER(1,0) NULL);
COMMENT ON COLUMN &&ADIT_SCHEMA..DOCUMENT.signed IS 'Document has been signed.';

ALTER TABLE &&ADIT_SCHEMA..DOCUMENT_SHARING ADD (deleted NUMBER(1,0) NULL);
COMMENT ON COLUMN &&ADIT_SCHEMA..DOCUMENT_SHARING.deleted IS 'Document has been deleted by the user to whom it was sent.';


CREATE TABLE &&ADIT_SCHEMA..DOCUMENT_FILE_TYPE
(
	ID                  NUMBER(18) NOT NULL,	
	DESCRIPTION			VARCHAR2(100) NOT NULL		/* Description of file type */
) TABLESPACE &&ADIT_TABLE_TABLESPACE.;

COMMENT ON COLUMN &&ADIT_SCHEMA..DOCUMENT_FILE_TYPE.description IS 'Description of file type';

INSERT INTO &&ADIT_SCHEMA..DOCUMENT_FILE_TYPE(ID, DESCRIPTION) VALUES(1, 'Document file');
INSERT INTO &&ADIT_SCHEMA..DOCUMENT_FILE_TYPE(ID, DESCRIPTION) VALUES(2, 'Signature container');
INSERT INTO &&ADIT_SCHEMA..DOCUMENT_FILE_TYPE(ID, DESCRIPTION) VALUES(3, 'Signature container draft');


ALTER TABLE &&ADIT_SCHEMA..DOCUMENT_FILE ADD (document_file_type_id NUMBER(18) DEFAULT (1) NOT NULL);
COMMENT ON COLUMN &&ADIT_SCHEMA..DOCUMENT_FILE.document_file_type_id IS 'File type ID';

ALTER TABLE &&ADIT_SCHEMA..DOCUMENT_FILE ADD (file_data_in_ddoc NUMBER(1,0) NULL);
COMMENT ON COLUMN &&ADIT_SCHEMA..DOCUMENT_FILE.file_data_in_ddoc IS 'Shows whether or not file contents should be aquired from signature container';

ALTER TABLE &&ADIT_SCHEMA..DOCUMENT_FILE ADD (ddoc_datafile_id VARCHAR(5) NULL);
COMMENT ON COLUMN &&ADIT_SCHEMA..DOCUMENT_FILE.ddoc_datafile_id IS 'ID of corresponding DataFile in signature container';

ALTER TABLE &&ADIT_SCHEMA..DOCUMENT_FILE ADD (ddoc_datafile_start_offset NUMBER(18) NULL);
COMMENT ON COLUMN &&ADIT_SCHEMA..DOCUMENT_FILE.ddoc_datafile_start_offset IS 'First character index of current file in corresponding signature container';

ALTER TABLE &&ADIT_SCHEMA..DOCUMENT_FILE ADD (ddoc_datafile_end_offset NUMBER(18) NULL);
COMMENT ON COLUMN &&ADIT_SCHEMA..DOCUMENT_FILE.ddoc_datafile_end_offset IS 'Last character index of current file in corresponding signature container';

ALTER TABLE &&ADIT_SCHEMA..SIGNATURE ADD (user_name VARCHAR2(255) NULL);
COMMENT ON COLUMN &&ADIT_SCHEMA..SIGNATURE.user_name IS 'Name of ADIT user who gave this signature';


INSERT INTO &&ADIT_SCHEMA..document_history_type(
	short_name,
	description
) VALUES (
	'extract_file',
	'Digitaalallkirja konteineri lahtipakkimine'
);


-- Stored procedure for doing file deflation directly in database
create or replace
procedure &&ADIT_SCHEMA..REMOVE_SIGNED_FILE_CONTENTS(
    result_rc out sys_refcursor,
    document_id in number,
    file_id in number,
    ddoc_start_offset in number,
    ddoc_end_offset in number
)
as
item_count number(10,0) := 0;
begin
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
                        and nvl(document_file.deleted, 0) = 0;
                
                if (item_count > 0) then
                    select  count(*)
                    into    item_count
                    from    document_file
                    where   document_file.id = REMOVE_SIGNED_FILE_CONTENTS.file_id
                            and nvl(document_file.file_data_in_ddoc, 0) = 0;
                
                    if (item_count > 0) then
	                    -- Calculate MD5 hash
	                    update  document_file
	                    set	    file_data = dbms_crypto.hash(nvl(file_data, empty_blob()), 2),
	                            ddoc_datafile_start_offset = REMOVE_SIGNED_FILE_CONTENTS.ddoc_start_offset,
	                            ddoc_datafile_end_offset = REMOVE_SIGNED_FILE_CONTENTS.ddoc_end_offset,
	                            file_data_in_ddoc = 1
	                    where   id = REMOVE_SIGNED_FILE_CONTENTS.file_id;
	                    
	                    open result_rc for
	                    select  'ok' as result_code
	                    from    dual;
	                else
	                    open result_rc for
	                    select  'file_data_already_moved' as result_code
	                    from    dual;
	                end if;
                else
                    open result_rc for
                    select  'file_is_deleted' as result_code
                    from    dual;
                end if;
            else
                open result_rc for
                select  'file_does_not_belong_to_document' as result_code
                from    dual;
            end if;
    else
        open result_rc for
        select  'file_does_not_exist' as result_code
        from    dual;
    end if;
end;
/

-- Stored procedure for doing file deflation directly in database
create or replace
procedure &&ADIT_SCHEMA..DEFLATE_FILE(
    result_rc out sys_refcursor,
    document_id in number,
    file_id in number,
    mark_deleted in number,
    fail_if_signature in number
)
as
item_count number(10,0) := 0;
begin
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
                        and nvl(document_file.deleted, 0) = 0;
                
                if (item_count > 0) then
	                select  count(*)
	                into    item_count
	                from    document_file
	                where   document_file.id = DEFLATE_FILE.file_id
	                        and nvl(document_file.document_file_type_id, 1) > 1;
                
	                if ((item_count = 0) or (DEFLATE_FILE.fail_if_signature <> 1)) then
	                	-- Calculate MD5 hash
	                    update	document_file
	                    set	    file_data = dbms_crypto.hash(nvl(file_data, empty_blob()), 2),
	                            deleted = (case when DEFLATE_FILE.mark_deleted = 1 then 1 else document_file.deleted end)
	                    where   id = DEFLATE_FILE.file_id;
	                    
	                    open result_rc for
	                    select  'ok' as result_code
	                    from    dual;
                    else
	                    open result_rc for
	                    select  'cannot_delete_signature_container' as result_code
	                    from    dual;
                    end if;
                else
                    open result_rc for
                    select  'already_deleted' as result_code
                    from    dual;
                end if;
            else
                open result_rc for
                select  'file_does_not_belong_to_document' as result_code
                from    dual;
            end if;
    else
        open result_rc for
        select  'file_does_not_exist' as result_code
        from    dual;
    end if;
end;
/


GRANT execute ON &&ADIT_SCHEMA..REMOVE_SIGNED_FILE_CONTENTS TO &&ADIT_APP.;
CREATE OR REPLACE SYNONYM &&ADIT_APP..REMOVE_SIGNED_FILE_CONTENTS FOR &&ADIT_SCHEMA..REMOVE_SIGNED_FILE_CONTENTS;