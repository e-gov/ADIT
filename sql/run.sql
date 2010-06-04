-- Creates database and inserts classificator data --
'&ADIT_SCHEMA.' Enter ADIT SQL Schema name: 
'&ADIT_TABLE_TABLESPACE.' Enter ADIT tablespace for storing tables:
'&ADIT_INDEX_TABLESPACE.' Enter ADIT tablespace for storing indexes: 

-- Create database
@@database.sql

-- Insert classificators
@@run_dml.sql