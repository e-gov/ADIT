-- DOCUMENT_SHARING_TYPE --

INSERT INTO &&ADIT_SCHEMA..document_sharing_type (
	short_name,
	description
) VALUES (
	'sign',
	'Dokumendi allkirjastamine'
);

INSERT INTO &&ADIT_SCHEMA..document_sharing_type (
	short_name,
	description
) VALUES (
	'share',
	'Dokumendi jagamine'
);

INSERT INTO &&ADIT_SCHEMA..document_sharing_type (
	short_name,
	description
) VALUES (
	'send_dvk',
	'Dokumendi saatmine DVK kaudu (DVK kasutajale)'
);

INSERT INTO &&ADIT_SCHEMA..document_sharing_type (
	short_name,
	description
) VALUES (
	'send_adit',
	'Dokumendi saatmine ADIT kaudu (ADIT kasutajale)'
);