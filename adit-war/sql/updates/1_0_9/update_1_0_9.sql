ALTER TABLE &&ADIT_DVK_SCHEMA..dhl_message ALTER COLUMN recipient_org_code TYPE varchar(50);
ALTER TABLE &&ADIT_DVK_SCHEMA..dhl_message ALTER COLUMN sender_org_code TYPE varchar(50);
ALTER TABLE &&ADIT_DVK_SCHEMA..dhl_message_recipient ALTER COLUMN recipient_org_code TYPE varchar(50);

CREATE SEQUENCE &&ADIT_DVK_SCHEMA..sq_dhl_organisation_id
    INCREMENT 1
    START 1577
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;
    
alter table &&ADIT_DVK_SCHEMA..dhl_organization add dhl_organisation_id integer;

update &&ADIT_DVK_SCHEMA..dhl_organization set dhl_organisation_id=nextval('adit_dvkuk.sq_dhl_organisation_id');

ALTER TABLE &&ADIT_DVK_SCHEMA..dhl_organization DROP CONSTRAINT dhl_organization_pkey;

ALTER TABLE &&ADIT_DVK_SCHEMA..dhl_organization ADD PRIMARY KEY (dhl_organisation_id);

alter table &&ADIT_DVK_SCHEMA..dhl_organization add representor_id integer;

alter table &&ADIT_DVK_SCHEMA..dhl_organization ADD CONSTRAINT dhl_organization_dhl_organizationfk FOREIGN KEY (representor_id) REFERENCES adit_dvkuk.dhl_organization (dhl_organisation_id) MATCH FULL;


alter table &&ADIT_DVK_SCHEMA..dhl_organization 
add subsystem character varying(100),
add member_class character varying(100),
add xroad_instance character varying(100),
add memberClass character varying(100),
add dhx_organisation smallint NOT NULL DEFAULT 0,
add representee_start timestamp without time zone,
add representee_end timestamp without time zone,
add organisation_identificator character varying(100);

GRANT select, update ON &&ADIT_DVK_SCHEMA..sq_dhl_organisation_id TO adit_dvk;

alter table &&ADIT_DVK_SCHEMA..dhl_message 
add dhx_consignment_id character varying(100),
add dhx_receipt_id  character varying(100);
		
		
alter table &&ADIT_DVK_SCHEMA..dhl_message_recipient 
add dhx_consignment_id character varying(100),
add dhx_receipt_id  character varying(100);


alter table &&ADIT_SCHEMA..document 
add dhx_id character varying(100);

alter table &&ADIT_SCHEMA..document_sharing
add dhx_id character varying(100);