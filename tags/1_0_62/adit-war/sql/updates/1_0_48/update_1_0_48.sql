ALTER TABLE &&ADIT_SCHEMA..document ADD (dvk_folder varchar2(1000));
COMMENT ON COLUMN &&ADIT_SCHEMA..document.dvk_folder IS 'DVK dokumendi kausta nimi';

ALTER TABLE &&ADIT_SCHEMA..document_sharing ADD (dvk_folder varchar2(1000));
COMMENT ON COLUMN &&ADIT_SCHEMA..document_sharing.dvk_folder IS 'DVK dokumendi kausta nimi';

CREATE TABLE &&ADIT_SCHEMA..USER_CONTACT
(
    ID                NUMBER(12) NOT NULL,
    user_code           VARCHAR2(50) NOT NULL,         /* Reference to the user (user_code) who owns the contact book */
    contact_code        VARCHAR2(50) NOT NULL,         /* Reference to the user (user_code) whos contact data is shown in the contact book */
    last_used_date				 TIMESTAMP             /* Date and time when given contacts has been used for sharing or sending documents */
) TABLESPACE &&ADIT_TABLE_TABLESPACE.;

COMMENT ON TABLE &&ADIT_SCHEMA..USER_CONTACT                    	IS 'User contact data';
COMMENT ON COLUMN &&ADIT_SCHEMA..USER_CONTACT.ID                	IS 'Unique identifier';
COMMENT ON COLUMN &&ADIT_SCHEMA..USER_CONTACT.user_code       		IS 'Reference to the user (user_code) who owns the contact book';
COMMENT ON COLUMN &&ADIT_SCHEMA..USER_CONTACT.contact_code         	IS 'Reference to the user (user_code) whos contact data is shown in the contact book';
COMMENT ON COLUMN &&ADIT_SCHEMA..USER_CONTACT.last_used_date        IS 'Date and time when given contacts has been used for sharing or sending documents';

ALTER TABLE &&ADIT_SCHEMA..USER_CONTACT ADD CONSTRAINT PK_USER_CONTACTS
    PRIMARY KEY (ID)
 USING INDEX TABLESPACE &&ADIT_INDEX_TABLESPACE.;
 
ALTER TABLE &&ADIT_SCHEMA..USER_CONTACT ADD CONSTRAINT FK_USER_CONTACT_USER
    FOREIGN KEY (user_code) REFERENCES &&ADIT_SCHEMA..ADIT_USER (user_code);
    
ALTER TABLE &&ADIT_SCHEMA..USER_CONTACT ADD CONSTRAINT FK_USER_CONTACT_CONTACT
    FOREIGN KEY (contact_code) REFERENCES &&ADIT_SCHEMA..ADIT_USER (user_code);
    
CREATE SEQUENCE &&ADIT_SCHEMA..USER_CONTACT_ID_SEQ
INCREMENT BY 1
START WITH 1
NOMAXVALUE
MINVALUE 1
NOCYCLE
NOCACHE
NOORDER;

GRANT select, insert, update ON &&ADIT_SCHEMA..USER_CONTACT TO &&ADIT_APP.;
GRANT select ON &&ADIT_SCHEMA..USER_CONTACT_ID_SEQ TO &&ADIT_APP.;