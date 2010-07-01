
create or replace
procedure DEFLATE_FILE(
    result_rc out sys_refcursor,
    document_id in number,
    file_id in number,
    mark_deleted in number
)
as
item_count number(10,0) := 0;
begin
    select  count(*)
    into    item_count
    from    document_file
    where   document_file.id = file_id;
    
    if (item_count > 0) then
            select  count(*)
            into    item_count
            from    document_file
            where   document_file.id = DEFLATE_FILE.file_id
                    and document_file.document_id = DEFLATE_FILE.document_id;
            
            if (item_count > 0) then
                select  count(*)
                into    item_count
                from    document_file
                where   document_file.id = DEFLATE_FILE.file_id
                        and nvl(document_file.deleted, 0) = 0;
                
                if (item_count > 0) then
                    -- Calculate MD5 hash
                    update	document_file
                    set		  file_data = dbms_crypto.hash(nvl(file_data, empty_blob()), 2),
                            deleted = (case when DEFLATE_FILE.mark_deleted = 1 then 1 else document_file.deleted end)
                    where   id = DEFLATE_FILE.file_id;
                    
                    open result_rc for
                    select  'ok' as result_code
                    from    dual;
                else
                    open result_rc for
                    select  'already_deleted' as result_code
                    from    dual;
                end if;
            else
                open result_rc for
                select  'file_does_not_belong_to_document' as result_code
                from    dual;
            end if;
    else
        open result_rc for
        select  'file_does_not_exist' as result_code
        from    dual;
    end if;
end;
