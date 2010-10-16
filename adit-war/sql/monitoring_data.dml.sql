INSERT INTO &&ADIT_SCHEMA..adit_user (
  user_code,
  usertype
) VALUES (
  'EE00000000000',
  'person'
);

INSERT INTO &&ADIT_SCHEMA..remote_application (
	short_name,
	organization_code,
	can_read,
	can_write
) VALUES (
	'MONITOR_TEST_APP',
	'00000000000',
	1,
	1
);

INSERT INTO &&ADIT_SCHEMA..document (
  ID,
  title,
  type,
  creator_code
) values (
  999999999999,
  'TestDocument - used for monitoring purposes - DO NOT DELETE',
  'letter',
  'EE00000000000'
);

INSERT INTO &&ADIT_SCHEMA..document_file (
  id,
  document_id,
  file_name,
  content_type,
  file_size_bytes
) VALUES (
  999999999999,
  999999999999,
  'test.txt',
  'text/plain',
  100
);
/