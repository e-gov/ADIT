DROP FUNCTION IF EXISTS test.assert (p_function_name character varying, p_assertion boolean, p_message_on_error character varying);
DROP FUNCTION IF EXISTS test.get_error_message (column_name character varying, expected_value character varying, actual_value character varying, operation character varying);
DROP FUNCTION IF EXISTS test.test_aditlog_get_test_date ();
DROP FUNCTION IF EXISTS test.test_aditlog_log_access_restriction ();
DROP FUNCTION IF EXISTS test.test_aditlog_log_adit_user ();
DROP FUNCTION IF EXISTS test.test_aditlog_log_document ();
DROP FUNCTION IF EXISTS test.test_aditlog_log_document_dvk_status ();
DROP FUNCTION IF EXISTS test.test_aditlog_log_document_file ();
DROP FUNCTION IF EXISTS test.test_aditlog_log_document_history ();
DROP FUNCTION IF EXISTS test.test_aditlog_log_document_history_type ();
DROP FUNCTION IF EXISTS test.test_aditlog_log_document_sharing ();
DROP FUNCTION IF EXISTS test.test_aditlog_log_document_sharing_type ();
DROP FUNCTION IF EXISTS test.test_aditlog_log_document_type ();
DROP FUNCTION IF EXISTS test.test_aditlog_log_document_wf_status ();
DROP FUNCTION IF EXISTS test.test_aditlog_log_notification ();
DROP FUNCTION IF EXISTS test.test_aditlog_log_notification_type ();
DROP FUNCTION IF EXISTS test.test_aditlog_log_remote_application ();
DROP FUNCTION IF EXISTS test.test_aditlog_log_signature ();
DROP FUNCTION IF EXISTS test.test_aditlog_log_user_contact ();
DROP FUNCTION IF EXISTS test.test_aditlog_log_user_notification ();
DROP FUNCTION IF EXISTS test.test_aditlog_log_usertype ();
DROP FUNCTION IF EXISTS test.test_logs ();

DROP SCHEMA IF EXISTS test;


--
-- PostgreSQL database dump
--

-- Dumped from database version 9.4.1
-- Dumped by pg_dump version 9.4.0
-- Started on 2015-04-23 14:27:14

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 7 (class 2615 OID 24656)
-- Name: test; Type: SCHEMA; Schema: -; Owner: adit_admin
--

CREATE SCHEMA test;


ALTER SCHEMA test OWNER TO adit_admin;

SET search_path = test, pg_catalog;

--
-- TOC entry 225 (class 1255 OID 24662)
-- Name: assert(character varying, boolean, character varying); Type: FUNCTION; Schema: test; Owner: adit_admin
--

CREATE FUNCTION assert(p_function_name character varying, p_assertion boolean, p_message_on_error character varying) RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
BEGIN
  IF p_assertion IS NULL THEN
    RAISE EXCEPTION 'Assertion test is null, that is not supported.';
  END IF;
  
  IF NOT p_assertion THEN
    RAISE WARNING 'FAILED Function % with error: %', p_function_name, p_message_on_error;
  END IF;
END;
$$;


ALTER FUNCTION test.assert(p_function_name character varying, p_assertion boolean, p_message_on_error character varying) OWNER TO adit_admin;

--
-- TOC entry 228 (class 1255 OID 32771)
-- Name: get_error_message(character varying, character varying, character varying, character varying); Type: FUNCTION; Schema: test; Owner: adit_admin
--

CREATE FUNCTION get_error_message(column_name character varying, expected_value character varying, actual_value character varying, operation character varying) RETURNS character varying
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
BEGIN
	IF operation ILIKE 'INSERT' THEN
		--RETURN 'Column "' || column_name || '" Expected value: "' || expected_value || '"; Actual value: "' || actual_value || '"';
		RETURN 'Column "' || CAST (coalesce(column_name, '') AS text) || '" Expected value: "' || CAST (expected_value AS TEXT) || '"; Actual value: "' || CAST (coalesce(actual_value, '') AS text) || '"';
	END IF;
END;
$$;


ALTER FUNCTION test.get_error_message(column_name character varying, expected_value character varying, actual_value character varying, operation character varying) OWNER TO adit_admin;

--
-- TOC entry 226 (class 1255 OID 24657)
-- Name: test_aditlog_get_test_date(); Type: FUNCTION; Schema: test; Owner: adit_admin
--

CREATE FUNCTION test_aditlog_get_test_date() RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    test_date timestamp := to_date('1900.01.01','yyyy.mm.dd');
BEGIN
    perform test.assert('aditlog.get_test_date()', test_date != aditlog.get_test_date(), 'test_date values are different');
END;
$$;


ALTER FUNCTION test.test_aditlog_get_test_date() OWNER TO adit_admin;

--
-- TOC entry 280 (class 1255 OID 24758)
-- Name: test_aditlog_log_access_restriction(); Type: FUNCTION; Schema: test; Owner: adit_admin
--

CREATE FUNCTION test_aditlog_log_access_restriction() RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
	ACCESS_RESTRICTION_test ADIT.ACCESS_RESTRICTION%ROWTYPE;
	ADIT_LOG_test ADIT.ADIT_LOG%ROWTYPE;
	operation character varying(20);
	error_message character varying(200);
	primary_key_value character varying(100);
	start_adit_log_id bigint;
	current_adit_log_id bigint;
	column_count int;
	actual_column_count int;
BEGIN
	ACCESS_RESTRICTION_test.id := 99999999;
	ACCESS_RESTRICTION_test.remote_application := 'TEST';
	ACCESS_RESTRICTION_test.user_code := 'EE99999999999';
	ACCESS_RESTRICTION_test.restriction := 'WRITE';
	primary_key_value := ACCESS_RESTRICTION_test.id;
	column_count := 4;

	BEGIN
		start_adit_log_id := nextval('adit_log_id_seq');
		INSERT INTO adit.access_restriction VALUES (ACCESS_RESTRICTION_test.*);
		current_adit_log_id := currval('adit_log_id_seq');
		
		operation = 'INSERT';
		actual_column_count := current_adit_log_id - start_adit_log_id;
		
		IF (actual_column_count = column_count) THEN
			FOR i in (current_adit_log_id - (column_count - 1))..current_adit_log_id LOOP
				SELECT * INTO ADIT_LOG_test FROM adit_log WHERE id = i;
				
				IF ADIT_LOG_test.primary_key_value = primary_key_value THEN				
					IF ADIT_LOG_test.column_name = 'id' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, ACCESS_RESTRICTION_test.id::character varying , ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('log_access_restriction(access_restriction, access_restriction, text)', (ACCESS_RESTRICTION_test.id::character varying  = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'remote_application' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, ACCESS_RESTRICTION_test.remote_application, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('log_access_restriction(access_restriction, access_restriction, text)', (ACCESS_RESTRICTION_test.remote_application = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'user_code' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, ACCESS_RESTRICTION_test.user_code, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('log_access_restriction(access_restriction, access_restriction, text)', (ACCESS_RESTRICTION_test.user_code = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'restriction' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, ACCESS_RESTRICTION_test.restriction, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('log_access_restriction(access_restriction, access_restriction, text)', (ACCESS_RESTRICTION_test.restriction= ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSE
						RAISE WARNING 'Not expected column and its value: % = %', ADIT_LOG_test.column_name, ADIT_LOG_test.new_value;
					END IF;
				ELSE
					RAISE WARNING 'Expected adit_log row primary_key_value: %; Actual primary_key_value: %', primary_key_value, ADIT_LOG_test.primary_key_value;
				END IF;
			END LOOP;
		ELSEIF (actual_column_count < column_count) THEN
			RAISE WARNING 'log_access_restriction(access_restriction, access_restriction, text), fewer log rows where created than Expected: %, Actual row count: %', column_count, actual_column_count;
		ELSEIF (actual_column_count > column_count) THEN
			RAISE WARNING 'log_access_restriction(access_restriction, access_restriction, text), created more log rows than Expected: %, Actual row count: %', column_count, actual_column_count;
		END IF;
		
		--ROLLBACK
		RAISE EXCEPTION '';
		EXCEPTION
			WHEN raise_exception THEN
				RETURN;
	END;
END;
$$;


ALTER FUNCTION test.test_aditlog_log_access_restriction() OWNER TO adit_admin;

--
-- TOC entry 283 (class 1255 OID 36220)
-- Name: test_aditlog_log_adit_user(); Type: FUNCTION; Schema: test; Owner: adit_admin
--

CREATE FUNCTION test_aditlog_log_adit_user() RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
	ADIT_USER_test ADIT.ADIT_USER%ROWTYPE;
	ADIT_LOG_test ADIT.ADIT_LOG%ROWTYPE;
	operation character varying(20);
	error_message character varying(200);
	primary_key_value character varying(100);
	start_adit_log_id bigint;
	current_adit_log_id bigint;
	column_count int;
	actual_column_count int;
BEGIN
	ADIT_USER_test.user_code := 'EE30102030405';
	ADIT_USER_test.full_name := 'Test Testor';
	ADIT_USER_test.usertype := 'person';
	ADIT_USER_test.active := 1;
	ADIT_USER_test.dvk_org_code := '3010203';
	ADIT_USER_test.dvk_subdivision_short_name := 'Testing';
	ADIT_USER_test.dvk_occupation_short_name := 'Tester';
	ADIT_USER_test.disk_quota := 5555555;
	ADIT_USER_test.deactivation_date := NOW();
	ADIT_USER_test.disk_quota_used := 44444;		-- Isn't logged
	primary_key_value := ADIT_USER_test.user_code;
	column_count := 9;

	BEGIN
		start_adit_log_id := nextval('adit_log_id_seq');
		INSERT INTO adit.adit_user VALUES (ADIT_USER_test.*);
		current_adit_log_id := currval('adit_log_id_seq');
		
		operation = 'INSERT';
		actual_column_count := current_adit_log_id - start_adit_log_id;
		
		IF (actual_column_count = column_count) THEN
			FOR i in (current_adit_log_id - (column_count - 1))..current_adit_log_id LOOP
				SELECT * INTO ADIT_LOG_test FROM adit_log WHERE id = i;
				
				IF ADIT_LOG_test.primary_key_value = primary_key_value THEN				
					IF ADIT_LOG_test.column_name = 'user_code' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, ADIT_USER_test.user_code, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('log_adit_user(adit_user, adit_user, text)', (ADIT_USER_test.user_code = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'full_name' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, ADIT_USER_test.full_name, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('log_adit_user(adit_user, adit_user, text)', (ADIT_USER_test.full_name = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'usertype' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, ADIT_USER_test.usertype, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('log_adit_user(adit_user, adit_user, text)', (ADIT_USER_test.usertype = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'active' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, ADIT_USER_test.active::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('log_adit_user(adit_user, adit_user, text)', (ADIT_USER_test.active::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'dvk_org_code' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, ADIT_USER_test.dvk_org_code, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('log_adit_user(adit_user, adit_user, text)', (ADIT_USER_test.dvk_org_code = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'dvk_subdivision_short_name' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, ADIT_USER_test.dvk_subdivision_short_name, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('log_adit_user(adit_user, adit_user, text)', (ADIT_USER_test.dvk_subdivision_short_name = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'dvk_occupation_short_name' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, ADIT_USER_test.dvk_occupation_short_name, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('log_adit_user(adit_user, adit_user, text)', (ADIT_USER_test.dvk_occupation_short_name = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'disk_quota' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, ADIT_USER_test.disk_quota::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('log_adit_user(adit_user, adit_user, text)', (ADIT_USER_test.disk_quota::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'deactivation_date' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, ADIT_USER_test.deactivation_date::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('log_adit_user(adit_user, adit_user, text)', (ADIT_USER_test.deactivation_date::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					--ELSEIF ADIT_LOG_test.column_name = 'disk_quota_used' THEN
					--	error_message := test.get_error_message(ADIT_LOG_test.column_name, ADIT_USER_test.disk_quota_used::character varying, ADIT_LOG_test.new_value, operation);
					--	PERFORM test.assert('log_adit_user(adit_user, adit_user, text)', (ADIT_USER_test.disk_quota_used::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSE
						RAISE WARNING 'Not expected column and its value: % = %', ADIT_LOG_test.column_name, ADIT_LOG_test.new_value;
					END IF;
				ELSE
					RAISE WARNING 'Expected adit_log row primary_key_value: %; Actual primary_key_value: %', primary_key_value, ADIT_LOG_test.primary_key_value;
				END IF;
			END LOOP;
		ELSEIF (actual_column_count < column_count) THEN
			RAISE WARNING 'log_adit_user(adit_user, adit_user, text), fewer log rows where created than Expected: %, Actual row count: %', column_count, actual_column_count;
		ELSEIF (actual_column_count > column_count) THEN
			RAISE WARNING 'log_adit_user(adit_user, adit_user, text), created more log rows than Expected: %, Actual row count: %', column_count, actual_column_count;
		END IF;
		
		--ROLLBACK
		RAISE EXCEPTION '';
		EXCEPTION
			WHEN raise_exception THEN
				RETURN;
	END;
END;
$$;


ALTER FUNCTION test.test_aditlog_log_adit_user() OWNER TO adit_admin;

--
-- TOC entry 284 (class 1255 OID 36208)
-- Name: test_aditlog_log_document(); Type: FUNCTION; Schema: test; Owner: adit_admin
--

CREATE FUNCTION test_aditlog_log_document() RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
	DOCUMENT_test ADIT.DOCUMENT%ROWTYPE;
	PARENT_DOCUMENT_test ADIT.DOCUMENT%ROWTYPE;
	ADIT_LOG_test ADIT.ADIT_LOG%ROWTYPE;
	operation character varying(20);
	error_message character varying(200);
	primary_key_value character varying(100);
	start_adit_log_id bigint;
	current_adit_log_id bigint;
	column_count int;
	actual_column_count int;
BEGIN
	DOCUMENT_test.id := 99999999;
	DOCUMENT_test.guid := 'guid_test';
	DOCUMENT_test.title := 'test title';
	DOCUMENT_test.type := 'letter';
	DOCUMENT_test.creator_code := 'EE30102030405';
	DOCUMENT_test.creator_name := 'Test Testor';
	DOCUMENT_test.creator_user_code := 'EE30102030405';
	DOCUMENT_test.creator_user_name := 'EE30102030405';
	DOCUMENT_test.creation_date := NOW();
	DOCUMENT_test.remote_application := 'TEST';
	DOCUMENT_test.last_modified_date := NOW();
	DOCUMENT_test.document_dvk_status_id := 1;
	DOCUMENT_test.dvk_id := 99999999;
	DOCUMENT_test.document_wf_status_id := 99999999;
	DOCUMENT_test.parent_id := 99999998;
	DOCUMENT_test.locked := 1;
	DOCUMENT_test.locking_date := NOW();
	DOCUMENT_test.signable := 1;
	DOCUMENT_test.deflated := 1;
	DOCUMENT_test.deflate_date := NOW();
	DOCUMENT_test.deleted := 1;
	DOCUMENT_test.invisible_to_owner := 1;			-- Isn't logged
	DOCUMENT_test.signed := 1;				-- Isn't logged	
	DOCUMENT_test.migrated := 1;				-- Isn't logged
	DOCUMENT_test.eform_use_id := 1;			-- Isn't logged
	DOCUMENT_test.files_size_bytes := 50;			-- Isn't logged
	DOCUMENT_test.sender_receiver := 'test_send';		-- Isn't logged
	DOCUMENT_test.content := 'Document content text';	-- Isn't logged
	primary_key_value := DOCUMENT_test.id;
	column_count := 21;

	PARENT_DOCUMENT_test.id := 99999998;
	PARENT_DOCUMENT_test.guid := 'guid_test';
	PARENT_DOCUMENT_test.title := 'test title';
	PARENT_DOCUMENT_test.type := 'letter';
	PARENT_DOCUMENT_test.creator_code := 'EE30102030405';
	PARENT_DOCUMENT_test.creator_name := 'Test Testor';
	PARENT_DOCUMENT_test.creator_user_code := 'EE30102030405';
	PARENT_DOCUMENT_test.creator_user_name := 'EE30102030405';
	PARENT_DOCUMENT_test.creation_date := NOW();
	PARENT_DOCUMENT_test.remote_application := 'TEST';
	PARENT_DOCUMENT_test.last_modified_date := NOW();
	PARENT_DOCUMENT_test.document_dvk_status_id := 1;
	PARENT_DOCUMENT_test.dvk_id := 99999999;
	PARENT_DOCUMENT_test.document_wf_status_id := 99999999;
	PARENT_DOCUMENT_test.parent_id := NULL;
	PARENT_DOCUMENT_test.locked := 1;
	PARENT_DOCUMENT_test.locking_date := NOW();
	PARENT_DOCUMENT_test.signable := 1;
	PARENT_DOCUMENT_test.deflated := 1;
	PARENT_DOCUMENT_test.deflate_date := NOW();
	PARENT_DOCUMENT_test.deleted := 1;
	PARENT_DOCUMENT_test.invisible_to_owner := 1;			-- Isn't logged
	PARENT_DOCUMENT_test.signed := 1;				-- Isn't logged	
	PARENT_DOCUMENT_test.migrated := 1;				-- Isn't logged
	PARENT_DOCUMENT_test.eform_use_id := 1;				-- Isn't logged
	PARENT_DOCUMENT_test.files_size_bytes := 50;			-- Isn't logged
	PARENT_DOCUMENT_test.sender_receiver := 'test_send';		-- Isn't logged
	PARENT_DOCUMENT_test.content := 'Document content text';	-- Isn't logged
	
	BEGIN
		INSERT INTO adit.document VALUES (PARENT_DOCUMENT_test.*);
		start_adit_log_id := nextval('adit_log_id_seq');
		INSERT INTO adit.document VALUES (DOCUMENT_test.*);
		current_adit_log_id := currval('adit_log_id_seq');
		
		operation = 'INSERT';
		actual_column_count := current_adit_log_id - start_adit_log_id;
		
		IF (actual_column_count = column_count) THEN
			FOR i in (current_adit_log_id - (column_count - 1))..current_adit_log_id LOOP
				SELECT * INTO ADIT_LOG_test FROM adit_log WHERE id = i;
				
				IF ADIT_LOG_test.primary_key_value = primary_key_value THEN				
					IF ADIT_LOG_test.column_name = 'id' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_test.id::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document(document, document, text)', (DOCUMENT_test.id::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'guid' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_test.guid, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document(document, document, text)', (DOCUMENT_test.guid = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'title' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_test.title, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document(document, document, text)', (DOCUMENT_test.title = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'type' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_test.type, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document(document, document, text)', (DOCUMENT_test.type = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'creator_code' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_test.creator_code, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document(document, document, text)', (DOCUMENT_test.creator_code = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'creator_name' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_test.creator_name, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document(document, document, text)', (DOCUMENT_test.creator_name = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'creator_user_code' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_test.creator_user_code, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document(document, document, text)', (DOCUMENT_test.creator_user_code = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'creator_user_name' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_test.creator_user_name, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document(document, document, text)', (DOCUMENT_test.creator_user_name = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'creation_date' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_test.creation_date::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document(document, document, text)', (DOCUMENT_test.creation_date::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'remote_application' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_test.remote_application, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document(document, document, text)', (DOCUMENT_test.remote_application = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'last_modified_date' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_test.last_modified_date::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document(document, document, text)', (DOCUMENT_test.last_modified_date::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'document_dvk_status_id' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_test.document_dvk_status_id::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document(document, document, text)', (DOCUMENT_test.document_dvk_status_id::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'dvk_id' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_test.dvk_id::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document(document, document, text)', (DOCUMENT_test.dvk_id::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'document_wf_status_id' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_test.document_wf_status_id::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document(document, document, text)', (DOCUMENT_test.document_wf_status_id::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'parent_id' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_test.parent_id::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document(document, document, text)', (DOCUMENT_test.parent_id::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'locked' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_test.locked::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document(document, document, text)', (DOCUMENT_test.locked::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'locking_date' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_test.locking_date::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document(document, document, text)', (DOCUMENT_test.locking_date::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'signable' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_test.signable::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document(document, document, text)', (DOCUMENT_test.signable::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'deflated' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_test.deflated::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document(document, document, text)', (DOCUMENT_test.deflated::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'deflate_date' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_test.deflate_date::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document(document, document, text)', (DOCUMENT_test.deflate_date::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'deleted' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_test.deleted::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document(document, document, text)', (DOCUMENT_test.deleted::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					/*
					ELSEIF ADIT_LOG_test.column_name = 'invisible_to_owner' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_test.invisible_to_owner::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document(document, document, text)', (DOCUMENT_test.invisible_to_owner::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'signed' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_test.signed::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document(document, document, text)', (DOCUMENT_test.signed::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'migrated' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_test.migrated::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document(document, document, text)', (DOCUMENT_test.migrated::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'eform_use_id' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_test.eform_use_id::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document(document, document, text)', (DOCUMENT_test.eform_use_id::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'files_size_bytes' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_test.files_size_bytes::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document(document, document, text)', (DOCUMENT_test.files_size_bytes::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'sender_receiver' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_test.sender_receiver, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document(document, document, text)', (DOCUMENT_test.sender_receiver = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'content' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_test.content, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document(document, document, text)', (DOCUMENT_test.content = ADIT_LOG_test.new_value) IS TRUE, error_message);
					*/
					ELSE
						RAISE WARNING 'Not expected column and its value: % = %', ADIT_LOG_test.column_name, ADIT_LOG_test.new_value;
					END IF;
				ELSE
					RAISE WARNING 'Expected adit_log row primary_key_value: %; Actual primary_key_value: %', primary_key_value, ADIT_LOG_test.primary_key_value;
				END IF;
			END LOOP;
		ELSEIF (actual_column_count < column_count) THEN
			RAISE WARNING 'aditlog.log_document(document, document, text), fewer log rows where created than Expected: %, Actual row count: %', column_count, actual_column_count;
		ELSEIF (actual_column_count > column_count) THEN
			RAISE WARNING 'aditlog.log_document(document, document, text), created more log rows than Expected: %, Actual row count: %', column_count, actual_column_count;
		END IF;
		
		--ROLLBACK
		RAISE EXCEPTION '';
		EXCEPTION
			WHEN raise_exception THEN
				RETURN;
	END;
END;
$$;


ALTER FUNCTION test.test_aditlog_log_document() OWNER TO adit_admin;

--
-- TOC entry 281 (class 1255 OID 36207)
-- Name: test_aditlog_log_document_dvk_status(); Type: FUNCTION; Schema: test; Owner: adit_admin
--

CREATE FUNCTION test_aditlog_log_document_dvk_status() RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
	DOCUMENT_DVK_STATUS_test ADIT.DOCUMENT_DVK_STATUS%ROWTYPE;
	ADIT_LOG_test ADIT.ADIT_LOG%ROWTYPE;
	operation character varying(20);
	error_message character varying(200);
	primary_key_value character varying(100);
	start_adit_log_id bigint;
	current_adit_log_id bigint;
	column_count int;
	actual_column_count int;
BEGIN
	DOCUMENT_DVK_STATUS_test.id := 99999999;
	DOCUMENT_DVK_STATUS_test.description := 'TEST_STATUS';
	primary_key_value := DOCUMENT_DVK_STATUS_test.id;		
	column_count := 2;
	
	BEGIN
		start_adit_log_id := nextval('adit_log_id_seq');
		INSERT INTO adit.document_dvk_status VALUES (DOCUMENT_DVK_STATUS_test.*);
		current_adit_log_id := currval('adit_log_id_seq');
		
		operation = 'INSERT';
		actual_column_count := current_adit_log_id - start_adit_log_id;
		
		IF (actual_column_count = column_count) THEN
			FOR i in (current_adit_log_id - (column_count - 1))..current_adit_log_id LOOP
				SELECT * INTO ADIT_LOG_test FROM adit_log WHERE id = i;
				
				IF ADIT_LOG_test.primary_key_value = primary_key_value THEN				
					IF ADIT_LOG_test.column_name = 'id' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_DVK_STATUS_test.id::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_dvk_status(document_dvk_status, document_dvk_status, text)', (DOCUMENT_DVK_STATUS_test.id::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'description' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_DVK_STATUS_test.description, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_dvk_status(document_dvk_status, document_dvk_status, text)', (DOCUMENT_DVK_STATUS_test.description = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSE
						RAISE WARNING 'Not expected column and its value: % = %', ADIT_LOG_test.column_name, ADIT_LOG_test.new_value;
					END IF;
				ELSE
					RAISE WARNING 'Expected adit_log row primary_key_value: %; Actual primary_key_value: %', primary_key_value, ADIT_LOG_test.primary_key_value;
				END IF;
			END LOOP;
		ELSEIF (actual_column_count < column_count) THEN
			RAISE WARNING 'aditlog.log_document_dvk_status(document_dvk_status, document_dvk_status, text), fewer log rows where created than Expected: %, Actual row count: %', column_count, actual_column_count;
		ELSEIF (actual_column_count > column_count) THEN
			RAISE WARNING 'aditlog.log_document_dvk_status(document_dvk_status, document_dvk_status, text), created more log rows than Expected: %, Actual row count: %', column_count, actual_column_count;
		END IF;
		
		--ROLLBACK
		RAISE EXCEPTION '';
		EXCEPTION
			WHEN raise_exception THEN
				RETURN;
	END;
END;
$$;


ALTER FUNCTION test.test_aditlog_log_document_dvk_status() OWNER TO adit_admin;

--
-- TOC entry 282 (class 1255 OID 36206)
-- Name: test_aditlog_log_document_file(); Type: FUNCTION; Schema: test; Owner: adit_admin
--

CREATE FUNCTION test_aditlog_log_document_file() RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
	DOCUMENT_FILE_test ADIT.DOCUMENT_FILE%ROWTYPE;
	ADIT_LOG_test ADIT.ADIT_LOG%ROWTYPE;
	operation character varying(20);
	error_message character varying(200);
	primary_key_value character varying(100);
	start_adit_log_id bigint;
	current_adit_log_id bigint;
	column_count int;
	actual_column_count int;
BEGIN
	DOCUMENT_FILE_test.id := 99999999;
	DOCUMENT_FILE_test.document_id := 99999999901;
	DOCUMENT_FILE_test.file_name := 'testfile.jpg';
	DOCUMENT_FILE_test.content_type := 'MIME/JPEG';
	DOCUMENT_FILE_test.description := 'Fail desc';
	DOCUMENT_FILE_test.file_data := 'test'::bytea;		-- Isn't logged
	DOCUMENT_FILE_test.file_size_bytes := 50;
	DOCUMENT_FILE_test.deleted := 1;
	DOCUMENT_FILE_test.document_file_type_id := 1;		-- Isn't logged
	DOCUMENT_FILE_test.file_data_in_ddoc := 1;			-- Isn't logged
	DOCUMENT_FILE_test.ddoc_datafile_id := '223344';	-- Isn't logged
	DOCUMENT_FILE_test.ddoc_datafile_start_offset := 2;	-- Isn't logged
	DOCUMENT_FILE_test.ddoc_datafile_end_offset := 3;	-- Isn't logged
	DOCUMENT_FILE_test.last_modified_date := NOW();		-- Isn't logged
	DOCUMENT_FILE_test.guid := 'test';					-- Isn't logged
	primary_key_value := DOCUMENT_FILE_test.id;
	column_count := 7;
	
	BEGIN
		start_adit_log_id := nextval('adit_log_id_seq');
		INSERT INTO adit.document_file VALUES (DOCUMENT_FILE_test.*);
		current_adit_log_id := currval('adit_log_id_seq');
		
		operation = 'INSERT';
		actual_column_count := current_adit_log_id - start_adit_log_id;
		
		IF (actual_column_count = column_count) THEN
			FOR i in (current_adit_log_id - (column_count - 1))..current_adit_log_id LOOP
				SELECT * INTO ADIT_LOG_test FROM adit_log WHERE id = i;
				
				IF ADIT_LOG_test.primary_key_value = primary_key_value THEN				
					IF ADIT_LOG_test.column_name = 'id' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_FILE_test.id::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_file(document_file, document_file, text)', (DOCUMENT_FILE_test.id::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'document_id' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_FILE_test.document_id::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_file(document_file, document_file, text)', (DOCUMENT_FILE_test.document_id::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'file_name' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_FILE_test.file_name, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_file(document_file, document_file, text)', (DOCUMENT_FILE_test.file_name = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'content_type' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_FILE_test.content_type, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_file(document_file, document_file, text)', (DOCUMENT_FILE_test.content_type = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'description' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_FILE_test.description, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_file(document_file, document_file, text)', (DOCUMENT_FILE_test.description = ADIT_LOG_test.new_value) IS TRUE, error_message);
					--ELSEIF ADIT_LOG_test.column_name = 'file_data' THEN
					--	error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_FILE_test.file_data::character varying, ADIT_LOG_test.new_value, operation);
					--	PERFORM test.assert('aditlog.log_document_file(document_file, document_file, text)', (DOCUMENT_FILE_test.file_data::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'file_size_bytes' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_FILE_test.file_size_bytes::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_file(document_file, document_file, text)', (DOCUMENT_FILE_test.file_size_bytes::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'deleted' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_FILE_test.deleted::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_file(document_file, document_file, text)', (DOCUMENT_FILE_test.deleted::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					--ELSEIF ADIT_LOG_test.column_name = 'document_file_type_id' THEN
					--	error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_FILE_test.document_file_type_id::character varying, ADIT_LOG_test.new_value, operation);
					--	PERFORM test.assert('aditlog.log_document_file(document_file, document_file, text)', (DOCUMENT_FILE_test.document_file_type_id::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					--ELSEIF ADIT_LOG_test.column_name = 'file_data_in_ddoc' THEN
					--	error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_FILE_test.file_data_in_ddoc::character varying, ADIT_LOG_test.new_value, operation);
					--	PERFORM test.assert('aditlog.log_document_file(document_file, document_file, text)', (DOCUMENT_FILE_test.file_data_in_ddoc::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					--ELSEIF ADIT_LOG_test.column_name = 'ddoc_datafile_id' THEN
					--	error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_FILE_test.ddoc_datafile_id, ADIT_LOG_test.new_value, operation);
					--	PERFORM test.assert('aditlog.log_document_file(document_file, document_file, text)', (DOCUMENT_FILE_test.ddoc_datafile_id = ADIT_LOG_test.new_value) IS TRUE, error_message);
					--ELSEIF ADIT_LOG_test.column_name = 'ddoc_datafile_start_offset' THEN
					--	error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_FILE_test.ddoc_datafile_start_offset::character varying, ADIT_LOG_test.new_value, operation);
					--	PERFORM test.assert('aditlog.log_document_file(document_file, document_file, text)', (DOCUMENT_FILE_test.ddoc_datafile_start_offset::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					--ELSEIF ADIT_LOG_test.column_name = 'ddoc_datafile_end_offset' THEN
					--	error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_FILE_test.ddoc_datafile_end_offset::character varying, ADIT_LOG_test.new_value, operation);
					--	PERFORM test.assert('aditlog.log_document_file(document_file, document_file, text)', (DOCUMENT_FILE_test.ddoc_datafile_end_offset::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					--ELSEIF ADIT_LOG_test.column_name = 'last_modified_date' THEN
					--	error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_FILE_test.last_modified_date::character varying, ADIT_LOG_test.new_value, operation);
					--	PERFORM test.assert('aditlog.log_document_file(document_file, document_file, text)', (DOCUMENT_FILE_test.last_modified_date::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					--ELSEIF ADIT_LOG_test.column_name = 'guid' THEN
					--	error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_FILE_test.guid, ADIT_LOG_test.new_value, operation);
					--	PERFORM test.assert('aditlog.log_document_file(document_file, document_file, text)', (DOCUMENT_FILE_test.guid = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSE
						RAISE WARNING 'Not expected column and its value: % = %', ADIT_LOG_test.column_name, ADIT_LOG_test.new_value;
					END IF;
				ELSE
					RAISE WARNING 'Expected adit_log row primary_key_value: %; Actual primary_key_value: %', primary_key_value, ADIT_LOG_test.primary_key_value;
				END IF;
			END LOOP;
		ELSEIF (actual_column_count < column_count) THEN
			RAISE WARNING 'aditlog.log_document_file(document_file, document_file, text), fewer log rows where created than Expected: %, Actual row count: %', column_count, actual_column_count;
		ELSEIF (actual_column_count > column_count) THEN
			RAISE WARNING 'aditlog.log_document_file(document_file, document_file, text), created more log rows than Expected: %, Actual row count: %', column_count, actual_column_count;
		END IF;
		
		--ROLLBACK
		RAISE EXCEPTION '';
		EXCEPTION
			WHEN raise_exception THEN
				RETURN;
	END;
END;
$$;


ALTER FUNCTION test.test_aditlog_log_document_file() OWNER TO adit_admin;

--
-- TOC entry 279 (class 1255 OID 36205)
-- Name: test_aditlog_log_document_history(); Type: FUNCTION; Schema: test; Owner: adit_admin
--

CREATE FUNCTION test_aditlog_log_document_history() RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
	DOCUMENT_HISTORY_test ADIT.DOCUMENT_HISTORY%ROWTYPE;
	ADIT_LOG_test ADIT.ADIT_LOG%ROWTYPE;
	operation character varying(20);
	error_message character varying(200);
	primary_key_value character varying(100);
	start_adit_log_id bigint;
	current_adit_log_id bigint;
	column_count int;
	actual_column_count int;
BEGIN
	DOCUMENT_HISTORY_test.id := 99999999;
	DOCUMENT_HISTORY_test.document_id := 99999999901;
	DOCUMENT_HISTORY_test.document_history_type := NULL;		-- NULL isn't logged
	DOCUMENT_HISTORY_test.description := 'Test description';
	DOCUMENT_HISTORY_test.event_date := NOW();
	DOCUMENT_HISTORY_test.user_code := 'EE99999999999';
	DOCUMENT_HISTORY_test.user_name := 'Test user';
	DOCUMENT_HISTORY_test.remote_application := 'TEST';
	DOCUMENT_HISTORY_test.notification_status := 'Start';
	DOCUMENT_HISTORY_test.xtee_notification_id := '999999999';
	DOCUMENT_HISTORY_test.xtee_user_code := 'EE30102030405';
	DOCUMENT_HISTORY_test.xtee_user_name := 'Test Tester';
	primary_key_value := DOCUMENT_HISTORY_test.id;
	column_count := 11;
	
	BEGIN
		start_adit_log_id := nextval('adit_log_id_seq');
		INSERT INTO adit.document_history VALUES (DOCUMENT_HISTORY_test.*);
		current_adit_log_id := currval('adit_log_id_seq');
		
		operation = 'INSERT';
		actual_column_count := current_adit_log_id - start_adit_log_id;
		
		IF (actual_column_count = column_count) THEN
			FOR i in (current_adit_log_id - (column_count - 1))..current_adit_log_id LOOP
				SELECT * INTO ADIT_LOG_test FROM adit_log WHERE id = i;
				
				IF ADIT_LOG_test.primary_key_value = primary_key_value THEN				
					IF ADIT_LOG_test.column_name = 'id' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_HISTORY_test.id::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_history(document_history, document_history, text)', (DOCUMENT_HISTORY_test.id::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'document_id' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_HISTORY_test.document_id::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_history(document_history, document_history, text)', (DOCUMENT_HISTORY_test.document_id::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'document_history_type' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_HISTORY_test.document_history_type, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_history(document_history, document_history, text)', (DOCUMENT_HISTORY_test.document_history_type = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'description' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_HISTORY_test.description, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_history(document_history, document_history, text)', (DOCUMENT_HISTORY_test.description = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'event_date' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_HISTORY_test.event_date::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_history(document_history, document_history, text)', (DOCUMENT_HISTORY_test.event_date::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'user_code' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_HISTORY_test.user_code, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_history(document_history, document_history, text)', (DOCUMENT_HISTORY_test.user_code = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'user_name' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_HISTORY_test.user_name, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_history(document_history, document_history, text)', (DOCUMENT_HISTORY_test.user_name = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'remote_application' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_HISTORY_test.remote_application, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_history(document_history, document_history, text)', (DOCUMENT_HISTORY_test.remote_application = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'notification_status' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_HISTORY_test.notification_status, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_history(document_history, document_history, text)', (DOCUMENT_HISTORY_test.notification_status = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'xtee_notification_id' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_HISTORY_test.xtee_notification_id, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_history(document_history, document_history, text)', (DOCUMENT_HISTORY_test.xtee_notification_id = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'xtee_user_code' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_HISTORY_test.xtee_user_code, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_history(document_history, document_history, text)', (DOCUMENT_HISTORY_test.xtee_user_code = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'xtee_user_name' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_HISTORY_test.xtee_user_name, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_history(document_history, document_history, text)', (DOCUMENT_HISTORY_test.xtee_user_name = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSE
						RAISE WARNING 'Not expected column and its value: % = %', ADIT_LOG_test.column_name, ADIT_LOG_test.new_value;
					END IF;
				ELSE
					RAISE WARNING 'Expected adit_log row primary_key_value: %; Actual primary_key_value: %', primary_key_value, ADIT_LOG_test.primary_key_value;
				END IF;
			END LOOP;
		ELSEIF (actual_column_count < column_count) THEN
			RAISE WARNING 'aditlog.log_document_history(document_history, document_history, text), fewer log rows where created than Expected: %, Actual row count: %', column_count, actual_column_count;
		ELSEIF (actual_column_count > column_count) THEN
			RAISE WARNING 'aditlog.log_document_history(document_history, document_history, text), created more log rows than Expected: %, Actual row count: %', column_count, actual_column_count;
		END IF;
		
		--ROLLBACK
		RAISE EXCEPTION '';
		EXCEPTION
			WHEN raise_exception THEN
				RETURN;
	END;
END;
$$;


ALTER FUNCTION test.test_aditlog_log_document_history() OWNER TO adit_admin;

--
-- TOC entry 278 (class 1255 OID 36204)
-- Name: test_aditlog_log_document_history_type(); Type: FUNCTION; Schema: test; Owner: adit_admin
--

CREATE FUNCTION test_aditlog_log_document_history_type() RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
	DOCUMENT_HISTORY_TYPE_test ADIT.DOCUMENT_HISTORY_TYPE%ROWTYPE;
	ADIT_LOG_test ADIT.ADIT_LOG%ROWTYPE;
	operation character varying(20);
	error_message character varying(200);
	primary_key_value character varying(100);
	start_adit_log_id bigint;
	current_adit_log_id bigint;
	column_count int;
	actual_column_count int;
BEGIN
	DOCUMENT_HISTORY_TYPE_test.short_name := 'TEST';
	DOCUMENT_HISTORY_TYPE_test.description := 'Test description';
	primary_key_value := DOCUMENT_HISTORY_TYPE_test.short_name;
	column_count := 2;
	
	BEGIN
		start_adit_log_id := nextval('adit_log_id_seq');
		INSERT INTO adit.document_sharing_type VALUES (DOCUMENT_HISTORY_TYPE_test.*);
		current_adit_log_id := currval('adit_log_id_seq');
		
		operation = 'INSERT';
		actual_column_count := current_adit_log_id - start_adit_log_id;
		
		IF (actual_column_count = column_count) THEN
			FOR i in (current_adit_log_id - (column_count - 1))..current_adit_log_id LOOP
				SELECT * INTO ADIT_LOG_test FROM adit_log WHERE id = i;
				
				IF ADIT_LOG_test.primary_key_value = primary_key_value THEN				
					IF ADIT_LOG_test.column_name = 'short_name' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_HISTORY_TYPE_test.short_name, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_history_type(document_history_type, document_history_type, text)', (DOCUMENT_HISTORY_TYPE_test.short_name = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'description' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_HISTORY_TYPE_test.description, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_history_type(document_history_type, document_history_type, text)', (DOCUMENT_HISTORY_TYPE_test.description = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSE
						RAISE WARNING 'Not expected column and its value: % = %', ADIT_LOG_test.column_name, ADIT_LOG_test.new_value;
					END IF;
				ELSE
					RAISE WARNING 'Expected adit_log row primary_key_value: %; Actual primary_key_value: %', primary_key_value, ADIT_LOG_test.primary_key_value;
				END IF;
			END LOOP;
		ELSEIF (actual_column_count < column_count) THEN
			RAISE WARNING 'aditlog.log_document_history_type(document_history_type, document_history_type, text), fewer log rows where created than Expected: %, Actual row count: %', column_count, actual_column_count;
		ELSEIF (actual_column_count > column_count) THEN
			RAISE WARNING 'aditlog.log_document_history_type(document_history_type, document_history_type, text), created more log rows than Expected: %, Actual row count: %', column_count, actual_column_count;
		END IF;
		
		--ROLLBACK
		RAISE EXCEPTION '';
		EXCEPTION
			WHEN raise_exception THEN
				RETURN;
	END;
END;
$$;


ALTER FUNCTION test.test_aditlog_log_document_history_type() OWNER TO adit_admin;

--
-- TOC entry 276 (class 1255 OID 36202)
-- Name: test_aditlog_log_document_sharing(); Type: FUNCTION; Schema: test; Owner: adit_admin
--

CREATE FUNCTION test_aditlog_log_document_sharing() RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
	DOCUMENT_SHARING_test ADIT.DOCUMENT_SHARING%ROWTYPE;
	ADIT_LOG_test ADIT.ADIT_LOG%ROWTYPE;
	operation character varying(20);
	error_message character varying(200);
	primary_key_value character varying(100);
	start_adit_log_id bigint;
	current_adit_log_id bigint;
	column_count int;
	actual_column_count int;
BEGIN
	DOCUMENT_SHARING_test.id := 99999999;
	DOCUMENT_SHARING_test.document_id := 99999999901;
	DOCUMENT_SHARING_test.user_code := 'EE30102030405';
	DOCUMENT_SHARING_test.user_name := 'Tester';
	DOCUMENT_SHARING_test.sharing_type := 'share';
	DOCUMENT_SHARING_test.task_description := 'Task description';
	DOCUMENT_SHARING_test.creation_date := NOW();
	DOCUMENT_SHARING_test.dvk_status_id := 1;
	DOCUMENT_SHARING_test.wf_status_id := 99999999;
	DOCUMENT_SHARING_test.first_access_date := NOW();
	DOCUMENT_SHARING_test.deleted := 1;			-- Isn't logged
	DOCUMENT_SHARING_test.dvk_folder := 'Test/test';	-- Isn't logged
	DOCUMENT_SHARING_test.dvk_id := 1;
	DOCUMENT_SHARING_test.user_email := 'test@test.com';	-- Isn't logged
	DOCUMENT_SHARING_test.comment_text := 'Comment text';	-- Isn't logged
	primary_key_value := DOCUMENT_SHARING_test.id;
	column_count := 11;
	
	BEGIN
		start_adit_log_id := nextval('adit_log_id_seq');
		INSERT INTO adit.document_sharing VALUES (DOCUMENT_SHARING_test.*);
		current_adit_log_id := currval('adit_log_id_seq');
		
		operation = 'INSERT';
		actual_column_count := current_adit_log_id - start_adit_log_id;
		
		IF (actual_column_count = column_count) THEN
			FOR i in (current_adit_log_id - (column_count - 1))..current_adit_log_id LOOP
				SELECT * INTO ADIT_LOG_test FROM adit_log WHERE id = i;
				
				IF ADIT_LOG_test.primary_key_value = primary_key_value THEN				
					IF ADIT_LOG_test.column_name = 'id' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_SHARING_test.id::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_sharing(document_sharing, document_sharing, text)', (DOCUMENT_SHARING_test.id::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'document_id' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_SHARING_test.document_id::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_sharing(document_sharing, document_sharing, text)', (DOCUMENT_SHARING_test.document_id::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'user_code' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_SHARING_test.user_code, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_sharing(document_sharing, document_sharing, text)', (DOCUMENT_SHARING_test.user_code = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'user_name' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_SHARING_test.user_name, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_sharing(document_sharing, document_sharing, text)', (DOCUMENT_SHARING_test.user_name = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'sharing_type' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_SHARING_test.sharing_type, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_sharing(document_sharing, document_sharing, text)', (DOCUMENT_SHARING_test.sharing_type = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'task_description' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_SHARING_test.task_description, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_sharing(document_sharing, document_sharing, text)', (DOCUMENT_SHARING_test.task_description = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'creation_date' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_SHARING_test.creation_date::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_sharing(document_sharing, document_sharing, text)', (DOCUMENT_SHARING_test.creation_date::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'dvk_status_id' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_SHARING_test.dvk_status_id::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_sharing(document_sharing, document_sharing, text)', (DOCUMENT_SHARING_test.dvk_status_id::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'wf_status_id' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_SHARING_test.wf_status_id::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_sharing(document_sharing, document_sharing, text)', (DOCUMENT_SHARING_test.wf_status_id::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'first_access_date' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_SHARING_test.first_access_date::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_sharing(document_sharing, document_sharing, text)', (DOCUMENT_SHARING_test.first_access_date::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					--ELSEIF ADIT_LOG_test.column_name = 'deleted' THEN
					--	error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_SHARING_test.deleted, ADIT_LOG_test.new_value, operation);
					--	PERFORM test.assert('aditlog.log_document_sharing(document_sharing, document_sharing, text)', (DOCUMENT_SHARING_test.deleted = ADIT_LOG_test.new_value) IS TRUE, error_message);
					--ELSEIF ADIT_LOG_test.column_name = 'dvk_folder' THEN
					--	error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_SHARING_test.dvk_folder, ADIT_LOG_test.new_value, operation);
					--	PERFORM test.assert('aditlog.log_document_sharing(document_sharing, document_sharing, text)', (DOCUMENT_SHARING_test.dvk_folder = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'dvk_id' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_SHARING_test.dvk_id::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_sharing(document_sharing, document_sharing, text)', (DOCUMENT_SHARING_test.dvk_id::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					--ELSEIF ADIT_LOG_test.column_name = 'user_email' THEN
					--	error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_SHARING_test.user_email, ADIT_LOG_test.new_value, operation);
					--	PERFORM test.assert('aditlog.log_document_sharing(document_sharing, document_sharing, text)', (DOCUMENT_SHARING_test.user_email = ADIT_LOG_test.new_value) IS TRUE, error_message);
					--ELSEIF ADIT_LOG_test.column_name = 'comment_text' THEN
					--	error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_SHARING_test.comment_text, ADIT_LOG_test.new_value, operation);
					--	PERFORM test.assert('aditlog.log_document_sharing(document_sharing, document_sharing, text)', (DOCUMENT_SHARING_test.comment_text = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSE
						RAISE WARNING 'Not expected column and its value: % = %', ADIT_LOG_test.column_name, ADIT_LOG_test.new_value;
					END IF;
				ELSE
					RAISE WARNING 'Expected adit_log row primary_key_value: %; Actual primary_key_value: %', primary_key_value, ADIT_LOG_test.primary_key_value;
				END IF;
			END LOOP;
		ELSEIF (actual_column_count < column_count) THEN
			RAISE WARNING 'aditlog.log_document_sharing(document_sharing, document_sharing, text), fewer log rows where created than Expected: %, Actual row count: %', column_count, actual_column_count;
		ELSEIF (actual_column_count > column_count) THEN
			RAISE WARNING 'aditlog.log_document_sharing(document_sharing, document_sharing, text), created more log rows than Expected: %, Actual row count: %', column_count, actual_column_count;
		END IF;
		
		--ROLLBACK
		RAISE EXCEPTION '';
		EXCEPTION
			WHEN raise_exception THEN
				RETURN;
	END;
END;
$$;


ALTER FUNCTION test.test_aditlog_log_document_sharing() OWNER TO adit_admin;

--
-- TOC entry 277 (class 1255 OID 36203)
-- Name: test_aditlog_log_document_sharing_type(); Type: FUNCTION; Schema: test; Owner: adit_admin
--

CREATE FUNCTION test_aditlog_log_document_sharing_type() RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
	DOCUMENT_SHARING_TYPE_test ADIT.DOCUMENT_SHARING_TYPE%ROWTYPE;
	ADIT_LOG_test ADIT.ADIT_LOG%ROWTYPE;
	operation character varying(20);
	error_message character varying(200);
	primary_key_value character varying(100);
	start_adit_log_id bigint;
	current_adit_log_id bigint;
	column_count int;
	actual_column_count int;
BEGIN
	DOCUMENT_SHARING_TYPE_test.short_name := 'TEST';
	DOCUMENT_SHARING_TYPE_test.description := 'Test description';
	primary_key_value := DOCUMENT_SHARING_TYPE_test.short_name;
	column_count := 2;
	
	BEGIN
		start_adit_log_id := nextval('adit_log_id_seq');
		INSERT INTO adit.document_sharing_type VALUES (DOCUMENT_SHARING_TYPE_test.*);
		current_adit_log_id := currval('adit_log_id_seq');
		
		operation = 'INSERT';
		actual_column_count := current_adit_log_id - start_adit_log_id;
		
		IF (actual_column_count = column_count) THEN
			FOR i in (current_adit_log_id - (column_count - 1))..current_adit_log_id LOOP
				SELECT * INTO ADIT_LOG_test FROM adit_log WHERE id = i;
				
				IF ADIT_LOG_test.primary_key_value = primary_key_value THEN				
					IF ADIT_LOG_test.column_name = 'short_name' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_SHARING_TYPE_test.short_name, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_sharing_type(document_sharing_type, document_sharing_type, text)', (DOCUMENT_SHARING_TYPE_test.short_name = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'description' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_SHARING_TYPE_test.description, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_sharing_type(document_sharing_type, document_sharing_type, text)', (DOCUMENT_SHARING_TYPE_test.description = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSE
						RAISE WARNING 'Not expected column and its value: % = %', ADIT_LOG_test.column_name, ADIT_LOG_test.new_value;
					END IF;
				ELSE
					RAISE WARNING 'Expected adit_log row primary_key_value: %; Actual primary_key_value: %', primary_key_value, ADIT_LOG_test.primary_key_value;
				END IF;
			END LOOP;
		ELSEIF (actual_column_count < column_count) THEN
			RAISE WARNING 'aditlog.log_document_sharing_type(document_sharing_type, document_sharing_type, text), fewer log rows where created than Expected: %, Actual row count: %', column_count, actual_column_count;
		ELSEIF (actual_column_count > column_count) THEN
			RAISE WARNING 'aditlog.log_document_sharing_type(document_sharing_type, document_sharing_type, text), created more log rows than Expected: %, Actual row count: %', column_count, actual_column_count;
		END IF;
		
		--ROLLBACK
		RAISE EXCEPTION '';
		EXCEPTION
			WHEN raise_exception THEN
				RETURN;
	END;
END;
$$;


ALTER FUNCTION test.test_aditlog_log_document_sharing_type() OWNER TO adit_admin;

--
-- TOC entry 275 (class 1255 OID 36201)
-- Name: test_aditlog_log_document_type(); Type: FUNCTION; Schema: test; Owner: adit_admin
--

CREATE FUNCTION test_aditlog_log_document_type() RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
	DOCUMENT_TYPE_test ADIT.DOCUMENT_TYPE%ROWTYPE;
	ADIT_LOG_test ADIT.ADIT_LOG%ROWTYPE;
	operation character varying(20);
	error_message character varying(200);
	primary_key_value character varying(100);
	start_adit_log_id bigint;
	current_adit_log_id bigint;
	column_count int;
	actual_column_count int;
BEGIN
	DOCUMENT_TYPE_test.short_name := 'TEST';
	DOCUMENT_TYPE_test.description := 'Testing';
	primary_key_value := DOCUMENT_TYPE_test.short_name;
	column_count := 2;
	
	BEGIN
		start_adit_log_id := nextval('adit_log_id_seq');
		INSERT INTO adit.document_type VALUES (DOCUMENT_TYPE_test.*);
		current_adit_log_id := currval('adit_log_id_seq');
		
		operation = 'INSERT';
		actual_column_count := current_adit_log_id - start_adit_log_id;
		
		IF (actual_column_count = column_count) THEN
			FOR i in (current_adit_log_id - (column_count - 1))..current_adit_log_id LOOP
				SELECT * INTO ADIT_LOG_test FROM adit_log WHERE id = i;
				
				IF ADIT_LOG_test.primary_key_value = primary_key_value THEN				
					IF ADIT_LOG_test.column_name = 'short_name' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_TYPE_test.short_name, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_type(document_type, document_type, text)', (DOCUMENT_TYPE_test.short_name = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'description' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_TYPE_test.description, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_type(document_type, document_type, text)', (DOCUMENT_TYPE_test.description = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSE
						RAISE WARNING 'Not expected column and its value: % = %', ADIT_LOG_test.column_name, ADIT_LOG_test.new_value;
					END IF;
				ELSE
					RAISE WARNING 'Expected adit_log row primary_key_value: %; Actual primary_key_value: %', primary_key_value, ADIT_LOG_test.primary_key_value;
				END IF;
			END LOOP;
		ELSEIF (actual_column_count < column_count) THEN
			RAISE WARNING 'aditlog.log_document_type(document_type, document_type, text), fewer log rows where created than Expected: %, Actual row count: %', column_count, actual_column_count;
		ELSEIF (actual_column_count > column_count) THEN
			RAISE WARNING 'aditlog.log_document_type(document_type, document_type, text), created more log rows than Expected: %, Actual row count: %', column_count, actual_column_count;
		END IF;
		
		--ROLLBACK
		RAISE EXCEPTION '';
		EXCEPTION
			WHEN raise_exception THEN
				RETURN;
	END;
END;
$$;


ALTER FUNCTION test.test_aditlog_log_document_type() OWNER TO adit_admin;

--
-- TOC entry 272 (class 1255 OID 36200)
-- Name: test_aditlog_log_document_wf_status(); Type: FUNCTION; Schema: test; Owner: adit_admin
--

CREATE FUNCTION test_aditlog_log_document_wf_status() RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
	DOCUMENT_WF_STATUS_test ADIT.DOCUMENT_WF_STATUS%ROWTYPE;
	ADIT_LOG_test ADIT.ADIT_LOG%ROWTYPE;
	operation character varying(20);
	error_message character varying(200);
	primary_key_value character varying(100);
	start_adit_log_id bigint;
	current_adit_log_id bigint;
	column_count int;
	actual_column_count int;
BEGIN
	DOCUMENT_WF_STATUS_test.id := 9999999;
	DOCUMENT_WF_STATUS_test.description := 'Testing';
	DOCUMENT_WF_STATUS_test.name := 'test';
	primary_key_value := DOCUMENT_WF_STATUS_test.id;
	column_count := 3;
	
	BEGIN
		start_adit_log_id := nextval('adit_log_id_seq');
		INSERT INTO adit.document_wf_status VALUES (DOCUMENT_WF_STATUS_test.*);
		current_adit_log_id := currval('adit_log_id_seq');
		
		operation = 'INSERT';
		actual_column_count := current_adit_log_id - start_adit_log_id;
		
		IF (actual_column_count = column_count) THEN
			FOR i in (current_adit_log_id - (column_count - 1))..current_adit_log_id LOOP
				SELECT * INTO ADIT_LOG_test FROM adit_log WHERE id = i;
				
				IF ADIT_LOG_test.primary_key_value = primary_key_value THEN				
					IF ADIT_LOG_test.column_name = 'id' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_WF_STATUS_test.id::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_wf_status(document_wf_status, document_wf_status, text)', (DOCUMENT_WF_STATUS_test.id::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'description' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_WF_STATUS_test.description, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_wf_status(document_wf_status, document_wf_status, text)', (DOCUMENT_WF_STATUS_test.description = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'name' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, DOCUMENT_WF_STATUS_test.name, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_document_wf_status(document_wf_status, document_wf_status, text)', (DOCUMENT_WF_STATUS_test.name = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSE
						RAISE WARNING 'Not expected column and its value: % = %', ADIT_LOG_test.column_name, ADIT_LOG_test.new_value;
					END IF;
				ELSE
					RAISE WARNING 'Expected adit_log row primary_key_value: %; Actual primary_key_value: %', primary_key_value, ADIT_LOG_test.primary_key_value;
				END IF;
			END LOOP;
		ELSEIF (actual_column_count < column_count) THEN
			RAISE WARNING 'aditlog.log_document_wf_status(document_wf_status, document_wf_status, text), fewer log rows where created than Expected: %, Actual row count: %', column_count, actual_column_count;
		ELSEIF (actual_column_count > column_count) THEN
			RAISE WARNING 'aditlog.log_document_wf_status(document_wf_status, document_wf_status, text), created more log rows than Expected: %, Actual row count: %', column_count, actual_column_count;
		END IF;
		
		--ROLLBACK
		RAISE EXCEPTION '';
		EXCEPTION
			WHEN raise_exception THEN
				RETURN;
	END;
END;
$$;


ALTER FUNCTION test.test_aditlog_log_document_wf_status() OWNER TO adit_admin;

--
-- TOC entry 273 (class 1255 OID 36199)
-- Name: test_aditlog_log_notification(); Type: FUNCTION; Schema: test; Owner: adit_admin
--

CREATE FUNCTION test_aditlog_log_notification() RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
	NOTIFICATION_test ADIT.NOTIFICATION%ROWTYPE;
	ADIT_LOG_test ADIT.ADIT_LOG%ROWTYPE;
	operation character varying(20);
	error_message character varying(200);
	primary_key_value character varying(100);
	start_adit_log_id bigint;
	current_adit_log_id bigint;
	column_count int;
	actual_column_count int;
BEGIN
	NOTIFICATION_test.id := 10000;
	NOTIFICATION_test.user_code := 'EE38711240263';
	NOTIFICATION_test.document_id := 99999999901;
	NOTIFICATION_test.event_date := NOW();
	NOTIFICATION_test.notification_type := 'view';
	NOTIFICATION_test.notification_text := 'Test test';
	NOTIFICATION_test.notification_id := 99999999;
	NOTIFICATION_test.notification_sending_date := NOW();
	primary_key_value := NOTIFICATION_test.id;
	column_count := 8;
	
	BEGIN
		start_adit_log_id := nextval('adit_log_id_seq');
		INSERT INTO adit.notification VALUES (NOTIFICATION_test.*);
		current_adit_log_id := currval('adit_log_id_seq');
		
		operation = 'INSERT';
		actual_column_count := current_adit_log_id - start_adit_log_id;
		
		IF (actual_column_count = column_count) THEN
			FOR i in (current_adit_log_id - (column_count - 1))..current_adit_log_id LOOP
				SELECT * INTO ADIT_LOG_test FROM adit_log WHERE id = i;
				
				IF ADIT_LOG_test.primary_key_value = primary_key_value THEN				
					IF ADIT_LOG_test.column_name = 'id' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, NOTIFICATION_test.id::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_notification(notification, notification, text)', (NOTIFICATION_test.id::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'user_code' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, NOTIFICATION_test.user_code, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_notification(notification, notification, text)', (NOTIFICATION_test.user_code = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'document_id' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, NOTIFICATION_test.document_id::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_notification(notification, notification, text)', (NOTIFICATION_test.document_id::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'event_date' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, NOTIFICATION_test.event_date::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_notification(notification, notification, text)', (NOTIFICATION_test.event_date::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'notification_type' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, NOTIFICATION_test.notification_type, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_notification(notification, notification, text)', (NOTIFICATION_test.notification_type = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'notification_text' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, NOTIFICATION_test.notification_text, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_notification(notification, notification, text)', (NOTIFICATION_test.notification_text = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'notification_id' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, NOTIFICATION_test.notification_id::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_notification(notification, notification, text)', (NOTIFICATION_test.notification_id::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'notification_sending_date' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, NOTIFICATION_test.notification_sending_date::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_notification(notification, notification, text)', (NOTIFICATION_test.notification_sending_date::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSE
						RAISE WARNING 'Not expected column and its value: % = %', ADIT_LOG_test.column_name, ADIT_LOG_test.new_value;
					END IF;
				ELSE
					RAISE WARNING 'Expected adit_log row primary_key_value: %; Actual primary_key_value: %', primary_key_value, ADIT_LOG_test.primary_key_value;
				END IF;
			END LOOP;
		ELSEIF (actual_column_count < column_count) THEN
			RAISE WARNING 'aditlog.log_notification(notification, notification, text), fewer log rows where created than Expected: %, Actual row count: %', column_count, actual_column_count;
		ELSEIF (actual_column_count > column_count) THEN
			RAISE WARNING 'aditlog.log_notification(notification, notification, text), created more log rows than Expected: %, Actual row count: %', column_count, actual_column_count;
		END IF;
		
		--ROLLBACK
		RAISE EXCEPTION '';
		EXCEPTION
			WHEN raise_exception THEN
				RETURN;
	END;
END;
$$;


ALTER FUNCTION test.test_aditlog_log_notification() OWNER TO adit_admin;

--
-- TOC entry 274 (class 1255 OID 36198)
-- Name: test_aditlog_log_notification_type(); Type: FUNCTION; Schema: test; Owner: adit_admin
--

CREATE FUNCTION test_aditlog_log_notification_type() RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
	NOTIFICATION_TYPE_test ADIT.NOTIFICATION_TYPE%ROWTYPE;
	ADIT_LOG_test ADIT.ADIT_LOG%ROWTYPE;
	operation character varying(20);
	error_message character varying(200);
	primary_key_value character varying(100);
	start_adit_log_id bigint;
	current_adit_log_id bigint;
	column_count int;
	actual_column_count int;
BEGIN
	NOTIFICATION_TYPE_test.short_name := 'TEST_TEST';
	NOTIFICATION_TYPE_test.description := 'TESTING_TESTING';
	primary_key_value := NOTIFICATION_TYPE_test.short_name;
	column_count := 2;
	
	BEGIN
		start_adit_log_id := nextval('adit_log_id_seq');
		INSERT INTO adit.notification_type VALUES (NOTIFICATION_TYPE_test.*);
		current_adit_log_id := currval('adit_log_id_seq');
		
		operation = 'INSERT';
		actual_column_count := current_adit_log_id - start_adit_log_id;
		
		IF (actual_column_count = column_count) THEN
			FOR i in (current_adit_log_id - (column_count - 1))..current_adit_log_id LOOP
				SELECT * INTO ADIT_LOG_test FROM adit_log WHERE id = i;
				
				IF ADIT_LOG_test.primary_key_value = primary_key_value THEN				
					IF ADIT_LOG_test.column_name = 'short_name' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, NOTIFICATION_TYPE_test.short_name, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_notification_type(notification_type, notification_type, text)', (NOTIFICATION_TYPE_test.short_name = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'description' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, NOTIFICATION_TYPE_test.description, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_notification_type(notification_type, notification_type, text)', (NOTIFICATION_TYPE_test.description = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSE
						RAISE WARNING 'Not expected column and its value: % = %', ADIT_LOG_test.column_name, ADIT_LOG_test.new_value;
					END IF;
				ELSE
					RAISE WARNING 'Expected adit_log row primary_key_value: %; Actual primary_key_value: %', primary_key_value, ADIT_LOG_test.primary_key_value;
				END IF;
			END LOOP;
		ELSEIF (actual_column_count < column_count) THEN
			RAISE WARNING 'aditlog.log_notification_type(notification_type, notification_type, text), fewer log rows where created than Expected: %, Actual row count: %', column_count, actual_column_count;
		ELSEIF (actual_column_count > column_count) THEN
			RAISE WARNING 'aditlog.log_notification_type(notification_type, notification_type, text), created more log rows than Expected: %, Actual row count: %', column_count, actual_column_count;
		END IF;
		
		--ROLLBACK
		RAISE EXCEPTION '';
		EXCEPTION
			WHEN raise_exception THEN
				RETURN;
	END;
END;
$$;


ALTER FUNCTION test.test_aditlog_log_notification_type() OWNER TO adit_admin;

--
-- TOC entry 271 (class 1255 OID 35791)
-- Name: test_aditlog_log_remote_application(); Type: FUNCTION; Schema: test; Owner: adit_admin
--

CREATE FUNCTION test_aditlog_log_remote_application() RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
	REMOTE_APPLICATION_test ADIT.REMOTE_APPLICATION%ROWTYPE;
	ADIT_LOG_test ADIT.ADIT_LOG%ROWTYPE;
	operation character varying(20);
	error_message character varying(200);
	primary_key_value character varying(100);
	start_adit_log_id bigint;
	current_adit_log_id bigint;
	column_count int;
	actual_column_count int;
BEGIN
	REMOTE_APPLICATION_test.short_name := 'TEST_TEST';
	REMOTE_APPLICATION_test.name := 'TESTING_TESTING';
	REMOTE_APPLICATION_test.organization_code := '000000000';
	REMOTE_APPLICATION_test.can_read := 1;
	REMOTE_APPLICATION_test.can_write:= 1;
	primary_key_value := REMOTE_APPLICATION_test.short_name;
	column_count := 5;
	
	BEGIN
		start_adit_log_id := nextval('adit_log_id_seq');
		INSERT INTO adit.remote_application VALUES (REMOTE_APPLICATION_test.*);
		current_adit_log_id := currval('adit_log_id_seq');
		
		operation = 'INSERT';
		actual_column_count := current_adit_log_id - start_adit_log_id;
		
		IF (actual_column_count = column_count) THEN
			FOR i in (current_adit_log_id - (column_count - 1))..current_adit_log_id LOOP
				SELECT * INTO ADIT_LOG_test FROM adit_log WHERE id = i;
				
				IF ADIT_LOG_test.primary_key_value = primary_key_value THEN				
					IF ADIT_LOG_test.column_name = 'short_name' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, REMOTE_APPLICATION_test.short_name, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_remote_application(remote_application, remote_application, text)', (REMOTE_APPLICATION_test.short_name = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'name' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, REMOTE_APPLICATION_test.name, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_remote_application(remote_application, remote_application, text)', (REMOTE_APPLICATION_test.name = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'organization_code' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, REMOTE_APPLICATION_test.organization_code, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_remote_application(remote_application, remote_application, text)', (REMOTE_APPLICATION_test.organization_code = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'can_read' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, REMOTE_APPLICATION_test.can_read::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_remote_application(remote_application, remote_application, text)', (REMOTE_APPLICATION_test.can_read::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'can_write' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, REMOTE_APPLICATION_test.can_write::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_remote_application(remote_application, remote_application, text)', (REMOTE_APPLICATION_test.can_write::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSE
						RAISE WARNING 'Not expected column and its value: % = %', ADIT_LOG_test.column_name, ADIT_LOG_test.new_value;
					END IF;
				ELSE
					RAISE WARNING 'aditlog.log_remote_application(remote_application, remote_application, text), Expected adit_log row primary_key_value: %; Actual primary_key_value: %', primary_key_value, ADIT_LOG_test.primary_key_value;
				END IF;
			END LOOP;
		ELSEIF (actual_column_count < column_count) THEN
			RAISE WARNING 'aditlog.log_remote_application(remote_application, remote_application, text), fewer log rows where created than Expected: %, Actual row count: %', column_count, actual_column_count;
		ELSEIF (actual_column_count > column_count) THEN
			RAISE WARNING 'aditlog.log_remote_application(remote_application, remote_application, text), created more log rows than Expected: %, Actual row count: %', column_count, actual_column_count;
		END IF;
		
		--ROLLBACK
		RAISE EXCEPTION '';
		EXCEPTION
			WHEN raise_exception THEN
				RETURN;
	END;
END;
$$;


ALTER FUNCTION test.test_aditlog_log_remote_application() OWNER TO adit_admin;

--
-- TOC entry 288 (class 1255 OID 32773)
-- Name: test_aditlog_log_signature(); Type: FUNCTION; Schema: test; Owner: adit_admin
--

CREATE FUNCTION test_aditlog_log_signature() RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
	SIGNATURE_test ADIT.SIGNATURE%ROWTYPE;
	ADIT_LOG_test ADIT.ADIT_LOG%ROWTYPE;
	operation character varying(20);
	error_message character varying(200);
	primary_key_value character varying(100);
	start_adit_log_id bigint;
	current_adit_log_id bigint;
	column_count int;
	actual_column_count int;
BEGIN
	SIGNATURE_test.id := 7065;
	SIGNATURE_test.user_code := 'EE38711240263';
	SIGNATURE_test.document_id := 99999999901;
	SIGNATURE_test.signer_role := 'test_role';
	SIGNATURE_test.resolution := 'test_resolution';
	SIGNATURE_test.country := 'test_country';
	SIGNATURE_test.county := 'test_county';
	SIGNATURE_test.city := 'test_city';
	SIGNATURE_test.post_index := '12345';
	SIGNATURE_test.signer_code := '38711240263';
	SIGNATURE_test.signer_name := 'TEST, TEST';
	SIGNATURE_test.signing_date := NOW(); -- Isn't logged
	SIGNATURE_test.user_name := 'Test Test'; -- Isn't logged
	primary_key_value := SIGNATURE_test.id;
	column_count := 11;
	
	BEGIN
		start_adit_log_id := nextval('adit_log_id_seq');
		INSERT INTO adit.signature VALUES (SIGNATURE_test.*);
		current_adit_log_id := currval('adit_log_id_seq');
		
		operation = 'INSERT';
		actual_column_count := current_adit_log_id - start_adit_log_id;
		
		IF (actual_column_count = column_count) THEN
			FOR i in (current_adit_log_id - (column_count - 1))..current_adit_log_id LOOP
				SELECT * INTO ADIT_LOG_test FROM adit_log WHERE id = i;
				
				IF ADIT_LOG_test.primary_key_value = primary_key_value THEN				
					IF ADIT_LOG_test.column_name = 'id' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, SIGNATURE_test.id::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_signature(signature, signature, text)', (SIGNATURE_test.id::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'user_code' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, SIGNATURE_test.user_code, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_signature(signature, signature, text)', (SIGNATURE_test.user_code = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'document_id' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, SIGNATURE_test.document_id::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_signature(signature, signature, text)', (SIGNATURE_test.document_id::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'signer_role' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, SIGNATURE_test.signer_role, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_signature(signature, signature, text)', (SIGNATURE_test.signer_role = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'resolution' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, SIGNATURE_test.resolution, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_signature(signature, signature, text)', (SIGNATURE_test.resolution = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'country' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, SIGNATURE_test.country, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_signature(signature, signature, text)', (SIGNATURE_test.country = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'county' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, SIGNATURE_test.county, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_signature(signature, signature, text)', (SIGNATURE_test.county = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'city' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, SIGNATURE_test.city, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_signature(signature, signature, text)', (SIGNATURE_test.city = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'post_index' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, SIGNATURE_test.post_index, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_signature(signature, signature, text)', (SIGNATURE_test.post_index = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'signer_code' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, SIGNATURE_test.signer_code, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_signature(signature, signature, text)', (SIGNATURE_test.signer_code = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'signer_name' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, SIGNATURE_test.signer_name, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_signature(signature, signature, text)', (SIGNATURE_test.signer_name = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSE
						RAISE WARNING 'Not expected column and its value: % = %', ADIT_LOG_test.column_name, ADIT_LOG_test.new_value;
					END IF;
				ELSE
					RAISE WARNING 'Expected adit_log row primary_key_value: %; Actual primary_key_value: %', primary_key_value, ADIT_LOG_test.primary_key_value;
				END IF;
			END LOOP;
		ELSEIF (actual_column_count < column_count) THEN
			RAISE WARNING 'aditlog.log_signature(signature, signature, text), fewer log rows where created than Expected: %, Actual row count: %', column_count, actual_column_count;
		ELSEIF (actual_column_count > column_count) THEN
			RAISE WARNING 'aditlog.log_signature(signature, signature, text), created more log rows than Expected: %, Actual row count: %', column_count, actual_column_count;
		END IF;
		
		--ROLLBACK
		RAISE EXCEPTION '';
		EXCEPTION
			WHEN raise_exception THEN
				RETURN;
	END;
END;
$$;


ALTER FUNCTION test.test_aditlog_log_signature() OWNER TO adit_admin;

--
-- TOC entry 227 (class 1255 OID 24760)
-- Name: test_aditlog_log_user_contact(); Type: FUNCTION; Schema: test; Owner: adit_admin
--

CREATE FUNCTION test_aditlog_log_user_contact() RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
	USER_CONTACT_test ADIT.USER_CONTACT%ROWTYPE;
	ADIT_LOG_test ADIT.ADIT_LOG%ROWTYPE;
	operation character varying(20);
	error_message character varying(200);
	primary_key_value character varying(100);
	start_adit_log_id bigint;
	current_adit_log_id bigint;
	column_count int;
	actual_column_count int;
BEGIN
	
	USER_CONTACT_test.id := 99999999;		-- Isn't logged
	USER_CONTACT_test.user_code := 'EE30102030405';
	USER_CONTACT_test.contact_code := 'EE12345678';
	USER_CONTACT_test.last_used_date := NOW();
	primary_key_value := USER_CONTACT_test.user_code;
	column_count := 3;
	
	BEGIN
		start_adit_log_id := nextval('adit_log_id_seq');
		INSERT INTO adit.user_contact VALUES (USER_CONTACT_test.*);
		current_adit_log_id := currval('adit_log_id_seq');
		
		operation = 'INSERT';
		actual_column_count := current_adit_log_id - start_adit_log_id;
		
		IF (actual_column_count = column_count) THEN
			FOR i in (current_adit_log_id - (column_count - 1))..current_adit_log_id LOOP
				SELECT * INTO ADIT_LOG_test FROM adit_log WHERE id = i;
				
				IF ADIT_LOG_test.primary_key_value = primary_key_value THEN				
					IF ADIT_LOG_test.column_name = 'user_code' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, USER_CONTACT_test.user_code, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_user_contact(user_contact, user_contact, text)', (USER_CONTACT_test.user_code = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'contact_code' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, USER_CONTACT_test.contact_code, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_user_contact(user_contact, user_contact, text)', (USER_CONTACT_test.contact_code = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'last_used_date' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, USER_CONTACT_test.last_used_date::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_user_contact(user_contact, user_contact, text)', (USER_CONTACT_test.last_used_date::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					--ELSEIF ADIT_LOG_test.column_name = 'id' THEN
					--	error_message := test.get_error_message(ADIT_LOG_test.column_name, USER_CONTACT_test.id::character varying, ADIT_LOG_test.new_value, operation);
					--	PERFORM test.assert('aditlog.log_user_contact(user_contact, user_contact, text)', (USER_CONTACT_test.id::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSE
						RAISE WARNING 'Not expected column and its value: % = %', ADIT_LOG_test.column_name, ADIT_LOG_test.new_value;
					END IF;
				ELSE
					RAISE WARNING 'Expected adit_log row primary_key_value: %; Actual primary_key_value: %', primary_key_value, ADIT_LOG_test.primary_key_value;
				END IF;
			END LOOP;
		ELSEIF (actual_column_count < column_count) THEN
			RAISE WARNING 'aditlog.log_user_contact(user_contact, user_contact, text), fewer log rows where created than Expected: %, Actual row count: %', column_count, actual_column_count;
		ELSEIF (actual_column_count > column_count) THEN
			RAISE WARNING 'aditlog.log_user_contact(user_contact, user_contact, text), created more log rows than Expected: %, Actual row count: %', column_count, actual_column_count;
		END IF;
		
		--ROLLBACK
		RAISE EXCEPTION '';
		EXCEPTION
			WHEN raise_exception THEN
				RETURN;
	END;
END;
$$;


ALTER FUNCTION test.test_aditlog_log_user_contact() OWNER TO adit_admin;

--
-- TOC entry 239 (class 1255 OID 32772)
-- Name: test_aditlog_log_user_notification(); Type: FUNCTION; Schema: test; Owner: adit_admin
--

CREATE FUNCTION test_aditlog_log_user_notification() RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
	USER_NOTIFICATION_test ADIT.USER_NOTIFICATION%ROWTYPE;
	ADIT_LOG_test ADIT.ADIT_LOG%ROWTYPE;
	operation character varying(20);
	error_message character varying(200);
	primary_key_value character varying(100);
	start_adit_log_id bigint;
	current_adit_log_id bigint;
	column_count int;
	actual_column_count int;
BEGIN
	USER_NOTIFICATION_test.user_code := 'EE38711240263';
	USER_NOTIFICATION_test.notification_type := 'sign';
	primary_key_value := USER_NOTIFICATION_test.user_code;
	column_count := 2;
	
	BEGIN
		start_adit_log_id := nextval('adit_log_id_seq');
		INSERT INTO adit.user_notification VALUES (USER_NOTIFICATION_test.*);
		current_adit_log_id := currval('adit_log_id_seq');
		
		operation = 'INSERT';
		actual_column_count := current_adit_log_id - start_adit_log_id;
		
		IF (actual_column_count = column_count) THEN
			FOR i in (current_adit_log_id - (column_count - 1))..current_adit_log_id LOOP
				SELECT * INTO ADIT_LOG_test FROM adit_log WHERE id = i;
				
				IF ADIT_LOG_test.primary_key_value = primary_key_value THEN				
					IF ADIT_LOG_test.column_name = 'user_code' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, USER_NOTIFICATION_test.user_code, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_user_notification(user_notification, user_notification, text)', (USER_NOTIFICATION_test.user_code = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'notification_type' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, USER_NOTIFICATION_test.notification_type, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_user_notification(user_notification, user_notification, text)', (USER_NOTIFICATION_test.notification_type = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSE
						RAISE WARNING 'Not expected column and its value: % = %', ADIT_LOG_test.column_name, ADIT_LOG_test.new_value;
					END IF;
				ELSE
					RAISE WARNING 'Expected adit_log row primary_key_value: %; Actual primary_key_value: %', primary_key_value, ADIT_LOG_test.primary_key_value;
				END IF;
			END LOOP;
		ELSEIF (actual_column_count < column_count) THEN
			RAISE WARNING 'aditlog.log_user_notification(user_notification, user_notification, text), fewer log rows where created than Expected: %, Actual row count: %', column_count, actual_column_count;
		ELSEIF (actual_column_count > column_count) THEN
			RAISE WARNING 'aditlog.log_user_notification(user_notification, user_notification, text), created more log rows than Expected: %, Actual row count: %', column_count, actual_column_count;
		END IF;
		
		--ROLLBACK
		RAISE EXCEPTION '';
		EXCEPTION
			WHEN raise_exception THEN
				RETURN;
	END;
END;
$$;


ALTER FUNCTION test.test_aditlog_log_user_notification() OWNER TO adit_admin;

--
-- TOC entry 287 (class 1255 OID 24763)
-- Name: test_aditlog_log_usertype(); Type: FUNCTION; Schema: test; Owner: adit_admin
--

CREATE FUNCTION test_aditlog_log_usertype() RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
	USERTYPE_test ADIT.USERTYPE%ROWTYPE;
	ADIT_LOG_test ADIT.ADIT_LOG%ROWTYPE;
	operation character varying(20);
	error_message character varying(200);
	primary_key_value character varying(100);
	start_adit_log_id bigint;
	current_adit_log_id bigint;
	column_count int;
	actual_column_count int;
BEGIN
	USERTYPE_test.short_name := 'test_name';
	USERTYPE_test.description := 'test_desc';
	USERTYPE_test.disk_quota := 52428800;
	primary_key_value := USERTYPE_test.short_name;
	column_count := 3;
	
	BEGIN
		start_adit_log_id = nextval('adit_log_id_seq');
		INSERT INTO adit.usertype VALUES (USERTYPE_test.*);
		current_adit_log_id := currval('adit_log_id_seq');
		
		operation = 'INSERT';
		actual_column_count := current_adit_log_id - start_adit_log_id;
		
		IF (actual_column_count = column_count) THEN
			FOR i in (start_adit_log_id + 1)..current_adit_log_id LOOP
				SELECT * INTO ADIT_LOG_test FROM adit_log WHERE id = i;
				
				IF ADIT_LOG_test.primary_key_value = primary_key_value THEN				
					IF ADIT_LOG_test.column_name = 'short_name' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, USERTYPE_test.short_name, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_usertype(usertype, usertype, text)', (USERTYPE_test.short_name = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'description' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, USERTYPE_test.description, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_usertype(usertype, usertype, text)', (USERTYPE_test.description = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSEIF ADIT_LOG_test.column_name = 'disk_quota' THEN
						error_message := test.get_error_message(ADIT_LOG_test.column_name, USERTYPE_test.disk_quota::character varying, ADIT_LOG_test.new_value, operation);
						PERFORM test.assert('aditlog.log_usertype(usertype, usertype, text)', (USERTYPE_test.disk_quota::character varying = ADIT_LOG_test.new_value) IS TRUE, error_message);
					ELSE
						RAISE WARNING 'Not expected column and its value: % = %', ADIT_LOG_test.column_name, ADIT_LOG_test.new_value;
					END IF;
				ELSE
					RAISE WARNING 'Expected adit_log row primary_key_value: %; Actual primary_key_value: %', primary_key_value, ADIT_LOG_test.primary_key_value;
				END IF;
			END LOOP;
		ELSEIF (actual_column_count < column_count) THEN
			RAISE WARNING 'aditlog.log_usertype(usertype, usertype, text), fewer log rows where created than Expected: %, Actual row count: %', column_count, actual_column_count;
		ELSEIF (actual_column_count > column_count) THEN
			RAISE WARNING 'aditlog.log_usertype(usertype, usertype, text), created more log rows than Expected: %, Actual row count: %', column_count, actual_column_count;
		END IF;
		
		--ROLLBACK
		RAISE EXCEPTION '';
		EXCEPTION
			WHEN raise_exception THEN
				RETURN;
	END;
END;
$$;


ALTER FUNCTION test.test_aditlog_log_usertype() OWNER TO adit_admin;

--
-- TOC entry 285 (class 1255 OID 36221)
-- Name: test_logs(); Type: FUNCTION; Schema: test; Owner: adit_admin
--

CREATE FUNCTION test_logs() RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
BEGIN
	PERFORM test.test_aditlog_log_access_restriction();
	PERFORM test.test_aditlog_log_adit_user();
	PERFORM test.test_aditlog_log_document();
	PERFORM test.test_aditlog_log_document_dvk_status();
	PERFORM test.test_aditlog_log_document_file();
	PERFORM test.test_aditlog_log_document_history();
	PERFORM test.test_aditlog_log_document_history_type();
	PERFORM test.test_aditlog_log_document_sharing();
	PERFORM test.test_aditlog_log_document_sharing_type();
	PERFORM test.test_aditlog_log_document_type();
	PERFORM test.test_aditlog_log_document_wf_status();
	PERFORM test.test_aditlog_log_notification();
	PERFORM test.test_aditlog_log_notification_type();
	PERFORM test.test_aditlog_log_remote_application();
	PERFORM test.test_aditlog_log_signature();
	PERFORM test.test_aditlog_log_user_contact();
	PERFORM test.test_aditlog_log_user_notification();
	PERFORM test.test_aditlog_log_usertype();
END;
$$;


ALTER FUNCTION test.test_logs() OWNER TO adit_admin;

-- Completed on 2015-04-23 14:27:14

--
-- PostgreSQL database dump complete
--

