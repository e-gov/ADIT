ALTER TABLE &&ADIT_SCHEMA..DOCUMENT_FILE ADD (last_modified_date DATE NULL);
COMMENT ON COLUMN &&ADIT_SCHEMA..DOCUMENT_FILE.last_modified_date IS 'Date and time of last modification';

UPDATE &&ADIT_SCHEMA..DOCUMENT_FILE SET last_modified_date = (SELECT last_modified_date FROM &&ADIT_SCHEMA..DOCUMENT WHERE &&ADIT_SCHEMA..DOCUMENT.ID = &&ADIT_SCHEMA..DOCUMENT_FILE.document_id);
