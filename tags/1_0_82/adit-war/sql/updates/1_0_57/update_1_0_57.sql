ALTER TABLE &&ADIT_SCHEMA..document ADD (files_size_bytes NUMBER(18,0) DEFAULT 0);
COMMENT ON COLUMN &&ADIT_SCHEMA..document.files_size_bytes IS 'Total size of files in bytes, needed for transient mapping';

ALTER TABLE &&ADIT_SCHEMA..document ADD (sender_receiver varchar2(50));
COMMENT ON COLUMN &&ADIT_SCHEMA..document.sender_receiver IS 'Sender/receiver transient column, needed for transient mapping';
