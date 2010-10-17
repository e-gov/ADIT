WHENEVER SQLERROR CONTINUE
-- Creates database and inserts classificator data --

prompt 'Enter ADIT database schema name - tables owner (using ADIT by default): ';
accept ADIT_SCHEMA default 'ADIT';

prompt 'Enter ADIT_APP database schema name - application user (using ADIT_APP by default): ';
accept ADIT_APP default 'ADIT_APP';

prompt 'Enter ADIT tablespace to use for tables (using ADIT_DATA by default): ';
accept ADIT_TABLE_TABLESPACE default 'ADIT_DATA';

prompt 'Enter ADIT tablespace to use for indexes (using ADIT_INDX by default): ';
accept ADIT_INDEX_TABLESPACE default 'ADIT_INDX';

-- Drop existing database
@@drop_database.sql

WHENEVER SQLERROR EXIT ROLLBACK SQL.SQLCODE

-- Create database
@@database.sql

-- Create logging procedures
@@log_proc.sql
@@log_proc_body.sql

-- Create logging triggers
@@log_triggers.sql

-- Grant rights to ADIT application user
@@grant.sql

-- Create synonyms for ADIT_APP
@@synonyms.sql

-- Insert classificators
@@run_dml.sql

commit;

PROMPT ADIT database successfully created.