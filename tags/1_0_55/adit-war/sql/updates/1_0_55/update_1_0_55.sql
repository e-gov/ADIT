ALTER TABLE &&ADIT_SCHEMA..document_file ADD (guid varchar2(50));
COMMENT ON COLUMN &&ADIT_SCHEMA..document_file.guid IS 'Faili globaalselt unikaalne identifikaator';