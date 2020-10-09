CREATE ROLE adit_admin LOGIN password 'xxx';
ALTER ROLE adit_admin SET search_path = adit, public;
CREATE ROLE adit_user LOGIN password 'yyy';
ALTER ROLE adit_user SET search_path = adit, public;

CREATE DATABASE adit
WITH OWNER = adit_admin
ENCODING = 'UTF8'
TABLESPACE = pg_default
LC_COLLATE = 'et_EE.UTF-8'
LC_CTYPE = 'et_EE.UTF-8'
CONNECTION LIMIT = -1;

