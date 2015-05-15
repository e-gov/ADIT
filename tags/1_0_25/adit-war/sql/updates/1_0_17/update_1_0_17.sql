ALTER TABLE signature ADD (signing_date DATE);
COMMENT ON COLUMN signature.signing_date IS 'Allkirja andmise kuupäev ja kellaaeg';
