create or replace
trigger &&ADIT_DVK_SCHEMA..tr_dhl_message_id
    before insert
    on &&ADIT_DVK_SCHEMA..dhl_message
    for each row
begin
    if (:new.dhl_message_id < 1) then
        select  sq_dhl_message_id.nextval
        into    globalPkg.identity
        from    dual;
        :new.dhl_message_id := globalPkg.identity;
    end if;
end;
/

create or replace
trigger &&ADIT_DVK_SCHEMA..tr_dhl_message_recipient_id
    before insert
    on &&ADIT_DVK_SCHEMA..dhl_message_recipient
    for each row
begin
    if (:new.dhl_message_recipient_id < 1) then
        select  sq_dhl_message_recipient_id.nextval
        into    globalPkg.identity
        from    dual;
        :new.dhl_message_recipient_id := globalPkg.identity;
    end if;
end;
/

create or replace
trigger &&ADIT_DVK_SCHEMA..tr_dhl_setfldr_id
    before insert
    on &&ADIT_DVK_SCHEMA..dhl_settings_folders
    for each row
begin
    if (:new.id < 1) then
        select  sq_dhl_setfldr_id.nextval
        into    globalPkg.identity
        from    dual;
        :new.id := globalPkg.identity;
    end if;
end;
/

create or replace
trigger &&ADIT_DVK_SCHEMA..tr_dhl_status_history_id
    before insert
    on &&ADIT_DVK_SCHEMA..dhl_status_history
    for each row
begin
    if (:new.dhl_status_history_id < 1) then
        select  sq_dhl_status_history_id.nextval
        into    globalPkg.identity
        from    dual;
        :new.dhl_status_history_id := globalPkg.identity;
    end if;
end;
/
