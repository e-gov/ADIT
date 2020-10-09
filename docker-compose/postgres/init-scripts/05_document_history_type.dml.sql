-- DOCUMENT_HISTORY_TYPE --

\c adit
SET search_path TO adit;

INSERT INTO document_history_type(
	short_name,
	description
) VALUES (
	'create',
	'Dokumendi esmane loomine'
);

INSERT INTO document_history_type(
	short_name,
	description
) VALUES (
	'modify',
	'Dokumendi muutmine'
);

INSERT INTO document_history_type(
	short_name,
	description
) VALUES (
	'add_file',
	'Dokumendi faili lisamine'
);

INSERT INTO document_history_type(
	short_name,
	description
) VALUES (
	'modify_file',
	'Dokumendi faili muutmine'
);

INSERT INTO document_history_type(
	short_name,
	description
) VALUES (
	'delete_file',
	'Dokumendi faili kustutamine'
);

INSERT INTO document_history_type(
	short_name,
	description
) VALUES (
	'modify_status',
	'Dokumendi staatuse muutmine'
);

INSERT INTO document_history_type(
	short_name,
	description
) VALUES (
	'send',
	'Dokumendi saatmine'
);

INSERT INTO document_history_type(
	short_name,
	description
) VALUES (
	'share',
	'Dokumendi jagamine'
);

INSERT INTO document_history_type(
	short_name,
	description
) VALUES (
	'lock',
	'Dokumendi lukustamine'
);

INSERT INTO document_history_type(
	short_name,
	description
) VALUES (
	'deflate',
	'Dokumendi arhiveerimine (deflate)'
);

INSERT INTO document_history_type(
	short_name,
	description
) VALUES (
	'sign',
	'Dokumendi digitaalne allkirjastamine'
);

INSERT INTO document_history_type(
	short_name,
	description
) VALUES (
	'delete',
	'Dokumendi kustutamine'
);

INSERT INTO document_history_type(
	short_name,
	description
) VALUES (
	'mark_viewed',
	'Dokumendi vaadatuks märkimine'
);

INSERT INTO document_history_type(
	short_name,
	description
) VALUES (
	'unshare',
	'Dokumendi jagamise lõpetamine'
);

INSERT INTO document_history_type(
	short_name,
	description
) VALUES (
	'unlock',
	'Dokumendi lukust vabastamine'
);

INSERT INTO document_history_type(
	short_name,
	description
) VALUES (
	'extract_file',
	'Digitaalallkirja konteineri lahtipakkimine'
);
