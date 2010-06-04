-- NOTIFICATION TYPE --

INSERT INTO &&ADIT_SCHEMA..notification_type (
	short_name,
	description
) VALUES (
	'send',
	'Dokumendi saatmine'
);

INSERT INTO &&ADIT_SCHEMA..notification_type (
	short_name,
	description
) VALUES (
	'share',
	'Dokumendi jagamine'
);

INSERT INTO &&ADIT_SCHEMA..notification_type (
	short_name,
	description
) VALUES (
	'view',
	'Dokumendi vaatamine'
);

INSERT INTO &&ADIT_SCHEMA..notification_type (
	short_name,
	description
) VALUES (
	'modify',
	'Dokumendi muutmine'
);

INSERT INTO notification_type (
	short_name,
	description
) VALUES (
	'sign',
	'Dokumendi allkirjastamine'
);