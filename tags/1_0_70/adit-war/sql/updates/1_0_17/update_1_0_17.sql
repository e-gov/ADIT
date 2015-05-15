ALTER TABLE &&ADIT_SCHEMA..signature ADD (signing_date DATE);
COMMENT ON COLUMN &&ADIT_SCHEMA..signature.signing_date IS 'Allkirja andmise kuupäev ja kellaaeg';
