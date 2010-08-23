WHENEVER SQLERROR CONTINUE
-- Creates database and inserts classificator data --

prompt 'Enter ADIT database schema name: ';
accept ADIT_SCHEMA default 'ADIT';

prompt 'Enter ADIT tablespace to use for tables: ';
accept ADIT_TABLE_TABLESPACE default 'ADIT_DATA';

prompt 'Enter ADIT tablespace to use for indexes: ';
accept ADIT_INDEX_TABLESPACE default 'ADIT_INDX';

-- Drop existing database objects
@@drop_database.sql

-- Create database
@@database.sql

-- Insert classificators
@@run_dml.sql

