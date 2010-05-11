/* CLEAN-UP

drop table document_history;
drop table document_history_types;
drop table document_sharings;
drop table document_sharing_types;
drop table document_files;
drop table documents;
drop table document_workflow_statuses;
drop table document_dvk_statuses;
drop table document_types;
drop table user_notifications;
drop table notification_types;
drop table access_restrictions;
drop table remote_applications;
drop table users;
drop table usertypes;

*/


-- Siin on tabeli nimi kokku kirjutatud, kuna Oraclel on süsteemne
-- view USER_TYPES, millega muidu päringud kipuvad segi minema
create table usertypes
(
    usertype_short_name varchar2(20) not null primary key,
    description varchar2(1000) null
);

insert into usertypes(usertype_short_name, description) values('eraisik', 'Füüsiline isik, kes omab Eesti isikukoodi');
insert into usertypes(usertype_short_name, description) values('ettevote', 'Juriidiline isik, kes omab Äriregistri registrikoodi (nt aktsiaselts, osaühing, füüsilisest isikust ettevõtja, mittetulundusühing, jms)');
insert into usertypes(usertype_short_name, description) values('asutus', 'Juriidiline isik, kes omab Riigi- ja kohaliku omavalitsuse asutuste riikliku registri registrikoodi');


create table users
(
    user_code varchar2(20) not null primary key,
    full_name varchar2(100) not null,
    usertype_short_name varchar2(20) not null,
    is_active number(1, 0) default 0 not null
);

alter table users
add constraint fk_users_01
foreign key (usertype_short_name)
references usertypes(usertype_short_name);


create table remote_applications
(
    remote_application_short_name varchar2(20) not null primary key,
    remote_application_name varchar2(250) null,
    organization_code varchar2(20) null,
    can_read number(1, 0) default 0 not null,
    can_write number(1, 0) default 0 not null
);

insert into remote_applications(remote_application_short_name, remote_application_name, organization_code, can_read, can_write)
values  ('ad', 'Ametlikud dokumendid', null, 0, 0);
insert into remote_applications(remote_application_short_name, remote_application_name, organization_code, can_read, can_write)
values  ('dvk', 'Dokumendivahetuskeskus', null, 0, 0);
insert into remote_applications(remote_application_short_name, remote_application_name, organization_code, can_read, can_write)
values  ('riigiportaal', 'Riigiportaal (eesti.ee)', '70000007', 1, 1);
insert into remote_applications(remote_application_short_name, remote_application_name, organization_code, can_read, can_write)
values  ('kovtp', 'Kohaliku omavalitsuse teenusportaal', null, 1, 1);


create table access_restrictions
(
    remote_application_short_name varchar2(20) not null,
    user_code varchar2(20) not null,
    can_read number(1, 0) default 0 not null
);

alter table access_restrictions
add constraint pk_access_restrictions
primary key (remote_application_short_name, user_code);

alter table access_restrictions
add constraint fk_access_restrictions_01
foreign key (remote_application_short_name)
references remote_applications(remote_application_short_name);

alter table access_restrictions
add constraint fk_access_restrictions_02
foreign key (user_code)
references users(user_code);


create table notification_types
(
    notification_type_short_name varchar2(20) not null primary key,
    description varchar2(100) null
);

insert into notification_types(notification_type_short_name, description) values('saatmine', 'kasutajale saadetakse dokument');
insert into notification_types(notification_type_short_name, description) values('jagamine', 'kasutajale jagatakse dokument');
insert into notification_types(notification_type_short_name, description) values('vaatamine', 'dokumendi teine osapool on vaadanud dokumenti');
insert into notification_types(notification_type_short_name, description) values('muutmine', 'dokumendi teine osapool on dokumendi staatust muutnud');
insert into notification_types(notification_type_short_name, description) values('allkirjastamine', 'dokumendi teine osapool on dokumendi allkirjastanud');


create table user_notifications
(
    user_code varchar2(20) not null,
    notification_type_short_name varchar2(20) not null
);

alter table user_notifications
add constraint pk_user_notifications
primary key (user_code, notification_type_short_name);

alter table user_notifications
add constraint fk_user_notifications_01
foreign key (notification_type_short_name)
references notification_types(notification_type_short_name);

alter table user_notifications
add constraint fk_user_notifications_02
foreign key (user_code)
references users(user_code);


create table document_types
(
    document_type_short_name varchar2(20) not null primary key,
    description varchar2(1000) null
);


-- Ameerika inglise keeles on sõna "status" mitmus väidetavalt "statuses" :)
create table document_dvk_statuses
(
    document_dvk_status_id number(10, 0) not null primary key,
    description varchar2(100) null
);

insert into document_dvk_statuses(document_dvk_status_id, description) values (100, 'Puudub (lokaalse faili puhul, mida pole veel edastatud)');
insert into document_dvk_statuses(document_dvk_status_id, description) values (101, 'Saatmisel');
insert into document_dvk_statuses(document_dvk_status_id, description) values (102, 'Saadetud');
insert into document_dvk_statuses(document_dvk_status_id, description) values (103, 'Katkestatud');


create table document_workflow_statuses
(
    document_workflow_status_id number(10, 0) not null primary key,
    description varchar2(100) null
);

insert into document_workflow_statuses(document_workflow_status_id, description) values (0, 'Puudub (lokaalse faili puhul)');
insert into document_workflow_statuses(document_workflow_status_id, description) values (1, 'Dokumente on puudu (Pooleli)');
insert into document_workflow_statuses(document_workflow_status_id, description) values (2, 'Järjekorras');
insert into document_workflow_statuses(document_workflow_status_id, description) values (3, 'Ootel');
insert into document_workflow_statuses(document_workflow_status_id, description) values (4, 'Lõpetatud');
insert into document_workflow_statuses(document_workflow_status_id, description) values (5, 'Tagasi lükatud');
insert into document_workflow_statuses(document_workflow_status_id, description) values (6, 'Teha');
insert into document_workflow_statuses(document_workflow_status_id, description) values (7, 'Töötlemisel');
insert into document_workflow_statuses(document_workflow_status_id, description) values (8, 'Aktsepteeritud (Võetud töösse)');
insert into document_workflow_statuses(document_workflow_status_id, description) values (9, 'Salvestatud');
insert into document_workflow_statuses(document_workflow_status_id, description) values (10, 'Arhiveeritud');
insert into document_workflow_statuses(document_workflow_status_id, description) values (11, 'Saadetud');


create table documents
(
    document_id number(38, 0) not null primary key,
    document_guid varchar2(36) null,
    title varchar2(1000) null,
    document_type_short_name varchar2(20) null,
    creator_code varchar2(20) null,
    creation_date date null,
    remote_application_short_name varchar2(20) not null,
    last_modified_date date null,
    document_dvk_status_id number(10, 0) not null,
    dvk_id number(38, 0) null,
    document_workflow_status_id number(10, 0) not null,
    last_access_date date null,
    parent_document_id number(38, 0) null,
    is_locked number(1, 0) default 0 not null,
    locking_date date null,
    is_signable number(1, 0) default 1 not null,
    is_archived number(1, 0) default 0 not null,
    archiving_date date null
);

alter table documents
add constraint fk_documents_01
foreign key (document_type_short_name)
references document_types(document_type_short_name);

alter table documents
add constraint fk_documents_02
foreign key (remote_application_short_name)
references remote_applications(remote_application_short_name);

alter table documents
add constraint fk_documents_03
foreign key (document_dvk_status_id)
references document_dvk_statuses(document_dvk_status_id);

alter table documents
add constraint fk_documents_04
foreign key (document_workflow_status_id)
references document_workflow_statuses(document_workflow_status_id);

alter table documents
add constraint fk_documents_05
foreign key (parent_document_id)
references documents(document_id);


create table document_files
(
    document_file_id number(38, 0) not null primary key,
    document_id number(38, 0) not null,
    file_name varchar2(255) null,
    content_type varchar2(50) null,
    description varchar2(1000) null,
    file_data blob null,
    file_size_bytes number(38, 0) null
);

alter table document_files
add constraint fk_document_files_01
foreign key (document_id)
references documents(document_id);


create table document_sharing_types
(
    sharing_type_short_name varchar(20) not null primary key,
    description varchar2(100) null
);

insert into document_sharing_types(sharing_type_short_name, description) values ('allkirjastamine', 'Allkirjastamine');
insert into document_sharing_types(sharing_type_short_name, description) values ('jagamine', 'Jagamine teenuse kasutajate');
insert into document_sharing_types(sharing_type_short_name, description) values ('dvk_saatmine', 'DVK kaudu saatmine');


create table document_sharings
(
    document_sharing_id number(38, 0) not null primary key,
    document_id number(38, 0) not null,
    user_code varchar2(20) not null,
    sharing_type_short_name varchar2(20) not null,
    task_description varchar2(1000) null,
    creation_date date null,
    document_dvk_status_id number(10, 0) not null,
    document_dvk_id number(38, 0) null,
    document_workflow_status_id number(10, 0) not null,
    last_access_date date null
);

alter table document_sharings
add constraint fk_document_sharings_01
foreign key (document_id)
references documents(document_id);

alter table document_sharings
add constraint fk_document_sharings_02
foreign key (user_code)
references users(user_code);

alter table document_sharings
add constraint fk_document_sharings_03
foreign key (sharing_type_short_name)
references document_sharing_types(sharing_type_short_name);

alter table document_sharings
add constraint fk_document_sharings_04
foreign key (document_dvk_status_id)
references document_dvk_statuses(document_dvk_status_id);

alter table document_sharings
add constraint fk_document_sharings_05
foreign key (document_workflow_status_id)
references document_workflow_statuses(document_workflow_status_id);


create table document_history_types
(
    document_history_type_id number(10, 0) not null primary key,
    description varchar2(100) null
);

insert into document_history_types(document_history_type_id, description) values (1, 'esmane loomine');
insert into document_history_types(document_history_type_id, description) values (2, 'dokumendi muutmine');
insert into document_history_types(document_history_type_id, description) values (3, 'dokumendi faili lisamine');
insert into document_history_types(document_history_type_id, description) values (4, 'dokumendi faili muutmine');
insert into document_history_types(document_history_type_id, description) values (5, 'dokumendi faili kustutamine');
insert into document_history_types(document_history_type_id, description) values (6, 'staatuse muutmine');
insert into document_history_types(document_history_type_id, description) values (7, 'saatmine');
insert into document_history_types(document_history_type_id, description) values (8, 'jagamine');
insert into document_history_types(document_history_type_id, description) values (9, 'lukustamine');
insert into document_history_types(document_history_type_id, description) values (10, 'arhiveerimine');
insert into document_history_types(document_history_type_id, description) values (11, 'dokumendi digitaalne allkirjastamine');


create table document_history
(
    history_event_id number(38, 0) not null primary key,
    document_id number(38, 0) not null,
    document_history_type_id number(10, 0) not null,
    description varchar2(1000) null,
    event_date timestamp default sysdate not null,
    user_code varchar(20) not null,
    remote_application_short_name varchar(20) not null
);

alter table document_history
add constraint fk_document_history_01
foreign key (document_id)
references documents(document_id);

alter table document_history
add constraint fk_document_history_02
foreign key (document_history_type_id)
references document_history_types(document_history_type_id);

alter table document_history
add constraint fk_document_history_03
foreign key (user_code)
references users(user_code);

alter table document_history
add constraint fk_document_history_04
foreign key (remote_application_short_name)
references remote_applications(remote_application_short_name);
