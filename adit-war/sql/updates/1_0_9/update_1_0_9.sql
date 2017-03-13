ALTER TABLE &&ADIT_SCHEMA..document_sharing
  add dhx_receipt_id character varying(100),
  add dhx_consignment_id character varying(100),
  add dhx_fault character varying(2500),
  add dhx_received_date timestamp without time zone,
  add dhx_sent_date timestamp without time zone;

ALTER TABLE &&ADIT_SCHEMA..document
  add dhx_receipt_id character varying(100),
  add dhx_consignment_id character varying(100);

CREATE TABLE &&ADIT_SCHEMA..dhx_user
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
        REFERENCES &&ADIT_SCHEMA..dhx_user (dhx_user_id) MATCH FULL
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);
CREATE SEQUENCE &&ADIT_SCHEMA..sq_dhx_user_id
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

GRANT SELECT, UPDATE, INSERT ON &&ADIT_SCHEMA..dhx_user TO &&ADIT_APP.;
GRANT USAGE ON SEQUENCE &&ADIT_SCHEMA..sq_dhx_user_id TO &&ADIT_APP.;