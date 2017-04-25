ALTER TABLE adit.document_sharing
  add dhx_receipt_id character varying(100),
  add dhx_consignment_id character varying(100),
  add dhx_fault character varying(2500),
  add dhx_received_date timestamp without time zone,
  add dhx_sent_date timestamp without time zone;

ALTER TABLE adit.document
  add dhx_receipt_id character varying(100),
  add dhx_consignment_id character varying(100);

CREATE TABLE adit.dhx_user
(
    org_code character varying(30)NOT NULL,
    org_name character varying(100) NOT NULL,
    active smallint NOT NULL DEFAULT 0,
    dhx_user_id integer NOT NULL,
    representor_id integer,
    subsystem character varying(100),
    member_class character varying(100),
    xroad_instance character varying(100),
    memberclass character varying(100),
    dhx_organisation smallint NOT NULL DEFAULT 0,
    representee_start timestamp without time zone,
    representee_end timestamp without time zone,
    organisation_identificator character varying(100),
    CONSTRAINT dhx_user_pkey PRIMARY KEY (dhx_user_id),
    CONSTRAINT dhx_user_dhx_userfk FOREIGN KEY (representor_id)
        REFERENCES adit.dhx_user (dhx_user_id) MATCH FULL
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);
CREATE SEQUENCE adit.sq_dhx_user_id
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER TABLE adit.dhx_user OWNER TO adit_admin;
    
GRANT SELECT, UPDATE, INSERT ON adit.dhx_user TO adit_user;
GRANT USAGE ON SEQUENCE adit.sq_dhx_user_id TO adit_user;