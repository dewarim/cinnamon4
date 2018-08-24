-------------------------
--- table definitions ---
-------------------------

-- users --
drop table if exists users cascade;
CREATE TABLE users (
  id BIGINT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  pwd VARCHAR(255) NOT NULL,
  obj_version int NOT NULL DEFAULT 0,
  login_type VARCHAR(64) NOT NULL DEFAULT 'CINNAMON',
  activated BOOLEAN NOT NULL DEFAULT TRUE, 
  locked BOOLEAN NOT NULL DEFAULT FALSE,
  ui_language_id BIGINT
);

drop sequence if exists seq_user_id;
create SEQUENCE seq_user_id start with 1;

-- sessions --
drop table if exists sessions cascade;
create table sessions
(
  id bigint PRIMARY KEY,
  expires timestamp,
  ticket varchar(255),
  username varchar(128),
  ui_language_id bigint,
  user_id bigint not null
);

drop sequence if exists seq_session_id;
create SEQUENCE seq_session_id start with 1;

-- ui_languages
drop table if exists ui_languages;
create table ui_languages
(
  id bigint not null
    constraint ui_languages_pkey
    primary key,
  iso_code varchar(32) not null
    constraint ui_languages_iso_code_key
    unique
);
drop sequence if exists seq_ui_language_id;
create SEQUENCE seq_ui_language_id start with 1;


-- languages
drop table if exists languages cascade;
create table languages
(
  id bigint not null
    constraint languages_pkey
    primary key,
  iso_code varchar(32) not null
    constraint languages_iso_code_key
    unique
);
drop sequence if exists seq_language_id;
create SEQUENCE seq_language_id start with 1;


-- acls --
drop table if exists acls cascade ;
create table acls(
  id bigint PRIMARY KEY,
  name varchar(255) UNIQUE
);
drop sequence if exists seq_acl_id;
create SEQUENCE seq_acl_id start with 1;

-- folder types --
drop table if exists folder_types cascade ;
create table folder_types
(
  id bigint not null
    constraint folder_types_pkey
    primary key,
  name varchar(128) not null
    constraint folder_types_name_key
    unique,
  obj_version bigint,
  config varchar(10241024) default '<config />' not null
);
drop sequence if exists seq_folder_type_id;
create sequence seq_folder_type_id start with 1;

-- folders --
drop table if exists folders cascade ;
create table folders
(
  id bigint not null
    constraint folders_pkey
    primary key,
  name varchar(128) not null,
  obj_version bigint,
  created timestamp not null default now(),
  acl_id bigint not null
    constraint fkd74671c53e44742f
    references acls,
  owner_id bigint not null
    constraint fkd74671c5332a19dd
    references users,
  parent_id bigint
    constraint fkd74671c551710e69
    references folders,
  type_id bigint not null
    constraint fkd74671c5b54eccb3
    references folder_types,
  metadata_changed boolean default false not null,
  summary text default '<summary />' not null,
  constraint folders_name_key
  unique (name, parent_id)
);
drop sequence if exists seq_folder_id;
create sequence seq_folder_id start with 1;


-- objtypes --
drop table if exists objtypes cascade ;
create table objtypes
(
  id bigint not null
    constraint objtypes_pkey
    primary key,
  name varchar(255)
    constraint objtypes_name_key
    unique,
  config varchar(10241024) default '<meta />' not null
);
drop sequence if exists seq_obj_type_id;
create sequence seq_obj_type_id start with 1;

-- formats --
drop table if exists formats cascade ;
create table formats
(
  id bigint not null
    constraint formats_pkey
    primary key,
  contenttype varchar(255),
  extension varchar(255),
  name varchar(255)
    constraint formats_name_key
    unique,
  obj_version bigint default 0 not null,
  default_object_type_id bigint
    constraint defaultobjecttype
    references objtypes
);
drop sequence if exists seq_format_id;
create sequence seq_format_id start with 1;

-- lifecycles --
drop table if exists lifecycles cascade ;
create table lifecycles
(
  id bigint not null
    constraint lifecycles_pkey
    primary key,
  name varchar(128) not null
    constraint lifecycles_name_key
    unique,
  default_state_id bigint

);
drop sequence if exists seq_lifecycle_id ;
create sequence seq_lifecycle_id start with 1;

-- lifecycle_state --
drop table if exists lifecycle_states cascade ;
create table lifecycle_states
(
  id bigint not null
    constraint lifecycle_states_pkey
    primary key,
  name varchar(128) not null
    constraint lifecycle_states_name_key
    unique,
  config text not null,
  state_class varchar(128) not null,
  life_cycle_id bigint
    constraint fke3bd9877407f648b
    references lifecycles
);

alter table lifecycles add constraint fk_default_state_id FOREIGN KEY 
  (default_state_id) references lifecycle_states(id);

drop sequence if exists seq_lifecycle_states_id;
create sequence seq_lifecycle_states_id start with 1;

-- lifecycle_state_to_copy_state --
drop table if exists lifecycle_state_to_copy_state cascade ;
create table lifecycle_state_to_copy_state(
  lifecycle_state_id bigint references lifecycle_states(id) unique,
  copy_state_id bigint references lifecycle_states(id)
);

-- objects --
drop table if exists objects cascade ;
create table objects
(
  id bigint not null
    constraint objects_pkey
    primary key,
  appname varchar(255),
  content_path varchar(255),
  content_size bigint,
  created timestamp not null,
  latest_branch boolean not null,
  latest_head boolean not null,
  modified timestamp not null,
  name varchar(128) not null,
  version bigint default 0,
  cmn_version varchar(128) default '1' not null,
  acl_id bigint not null
    constraint fk9d13c5143e44742f
    references acls,
  creator_id bigint not null
    constraint fk9d13c514223f6dc4
    references users,
  format_id bigint
    constraint fk9d13c51442079b85
    references formats,
  language_id bigint not null
    constraint fk9d13c5149c7d5ae9
    references languages,
  locker_id bigint
    constraint fk9d13c51498e76562
    references users,
  modifier_id bigint not null
    constraint fk9d13c51474046739
    references users,
  owner_id bigint
    constraint fk9d13c514332a19dd
    references users,
  parent_id bigint not null
    constraint fk9d13c51451710e69
    references folders,
  predecessor_id bigint
    constraint fk9d13c5147f4850c8
    references objects,
  root_id bigint
    constraint fk9d13c514bf66629
    references objects,
  type_id bigint not null
    constraint fk9d13c51426408304
    references objtypes,
  state_id bigint
    constraint lifecycle_state
    references lifecycle_states,
  content_changed boolean default false not null,
  metadata_changed boolean default false not null,
  summary text default '<summary />' not null,
  content_hash varchar(128)
);
drop sequence if exists seq_objects_id;
create sequence seq_objects_id start with 1;


-- links --
drop table if exists links cascade ;
create table links
(
  id bigint not null
    constraint links_pk
    primary key,
  type varchar(127) not null,
  owner_id bigint not null
    constraint links_owner_id_fk
    references users,
  acl_id bigint not null
    constraint fk_af6bdnno1760xpfbxwly6oxoe
    references acls,
  parent_id bigint not null
    constraint links_parent_id_fk
    references folders,
  folder_id bigint
    constraint links_folder_id_fk
    references folders,
  osd_id bigint
    constraint links_osd_id_fk
    references objects,
  version bigint default 0 not null
);

create index fki_links_folder_id_fk
  on links (folder_id)
;

create index fki_links_osd_id_fk
  on links (osd_id)
;

create index fki_links_parent_id_fk
  on links (parent_id)
;

drop sequence if exists seq_links_id;
create sequence seq_links_id start with 1;


-- groups --
drop table if exists groups cascade ;
create table groups(
  id bigint PRIMARY KEY,
  name varchar(255) UNIQUE,
  group_of_one boolean default false not null,
  parent_id bigint constraint fk_group_parent_id references groups
);
drop sequence if exists seq_groups;
create SEQUENCE seq_groups start with 1;

-- aclentries --

drop table if exists aclentries cascade ;
create table aclentries
(
  id bigint not null
    constraint aclentries_pkey
    primary key,
  acl_id bigint not null
    constraint fk5a6c3cc63e44742f
    references acls,
  group_id bigint not null
    constraint fk5a6c3cc64e6fdacf
    references groups,
  constraint unique_acl_id
  unique (group_id, acl_id)
);
drop sequence if exists seq_acl_entries_id;
create sequence seq_acl_entries_id start with 1;


-- group_users --
drop table if exists group_users cascade ;
create table group_users(
  user_id BIGINT NOT NULL ,
  group_id BIGINT NOT NULL 
);

create UNIQUE INDEX  group_users_user_group
  on group_users(user_id,group_id)
  ;

-- permissions --
drop table if exists permissions cascade ;
create table permissions
(
  id bigint not null
    constraint permissions_pkey
    primary key,
  name varchar(128) not null
    constraint permissions_name_key
    unique
);
drop sequence if exists seq_permission_id;
create sequence seq_permission_id start with 1;

-- aclentry_permissions --
drop table if exists  aclentry_permissions cascade ;
create table aclentry_permissions
(
  id bigint not null
    constraint aclentry_permissions_pkey
    primary key,
  aclentry_id bigint not null
    constraint fk2110acedec2a9305
    references aclentries,
  permission_id bigint not null
    constraint fk2110aceda5501f05
    references permissions,
  constraint unique_aclentry_id
  unique (permission_id, aclentry_id)
);
drop sequence if exists seq_aclentry_permission_id;
create sequence seq_aclentry_permission_id start with 1;

drop table if exists relationtypes cascade ;
create table relationtypes
(
  id bigint not null
    constraint relationtypes_pkey
    primary key,
  leftobjectprotected boolean not null,
  name varchar(128) not null
    constraint relationtypes_name_key
    unique,
  rightobjectprotected boolean not null,
  clone_on_right_copy boolean default false not null,
  clone_on_left_copy boolean default false not null,
  clone_on_left_version boolean default false not null,
  clone_on_right_version boolean default false not null
)
;
drop sequence if exists seq_relationtypes_id;
create sequence seq_relationtypes_id start with 1;

drop table if exists relations cascade ;
create table relations
(
  id bigint not null
    constraint relations_pkey
    primary key,
  left_id bigint not null
    constraint fkff8b45f777be7ff3
    references objects,
  right_id bigint not null
    constraint fkff8b45f714370c8
    references objects,
  type_id bigint
    constraint fkff8b45f78121f481
    references relationtypes,
  metadata text default '<meta/>' not null,
  constraint unique_left_id
  unique (type_id, right_id, left_id)
)
;
drop sequence if exists seq_relations_id;
create sequence seq_relations_id start with 1;

-- metaset_types --
drop table if exists metaset_types cascade ;
create table metaset_types
(
  id bigint not null
    constraint metaset_types_pkey
    primary key,
  name varchar(128) not null
    constraint metaset_types_name_key
    unique
);
drop sequence if exists seq_metaset_types_id;
create sequence seq_metaset_types_id start with 1;

-- index_items --
drop table if exists index_items cascade ;
create table index_items
(
  id bigint not null
    constraint index_items_pkey
    primary key,
  fieldname varchar(255) not null,
  for_content boolean not null,
  for_metadata boolean not null,
  for_sys_meta boolean not null,
  multiple_results boolean not null,
  name varchar(128) not null
    constraint index_items_name_key
    unique,
  search_string text not null,
  va_params text not null,
  index_type_name varchar(255) default '' not null,
  search_condition text default 'true()' not null,
  store_field boolean default false not null
)
;
drop sequence if exists seq_index_item_id;
create sequence seq_index_item_id start with 1;

drop table if exists config_entries cascade ;
create table config_entries
(
  id bigint not null
    constraint config_entry_pkey
    primary key,
  config varchar(2097152) not null,
  name varchar(128) not null
    constraint config_entry_name_key
    unique,
  public_visibility boolean not null default false
)
;
drop sequence if exists seq_config_entries_id;
create sequence seq_config_entries_id start with 1;


--------------------------
--- insert test data:  ---
-- -----------------------

-- #1
INSERT INTO users(id,name,pwd,activated, ui_language_id) 
VALUES ( nextval('seq_user_id'),'admin','$2a$10$VG9LCf6h/Qwb7Y.pafHkaepdnJNgFZUzzuMV3EcyvLbKnueHQ4IW.',true,1);
-- #2
INSERT INTO users(id,name,pwd,activated, ui_language_id)
VALUES ( nextval('seq_user_id'),'doe','$2a$10$VG9LCf6h/Qwb7Y.pafHkaepdnJNgFZUzzuMV3EcyvLbKnueHQ4IW.',true,1);
-- #3
INSERT INTO users(id,name,pwd,activated, ui_language_id) 
VALUES ( nextval('seq_user_id'),'deactivated user','$2a$10$VG9LCf6h/Qwb7Y.pafHkaepdnJNgFZUzzuMV3EcyvLbKnueHQ4IW.',false,1);
-- #4
INSERT INTO users(id,name,pwd,activated, locked, ui_language_id) 
VALUES ( nextval('seq_user_id'),'locked user','$2a$10$VG9LCf6h/Qwb7Y.pafHkaepdnJNgFZUzzuMV3EcyvLbKnueHQ4IW.',true,true,1);

insert into acls(id,name) values(nextval('seq_acl_id'),'_default_acl'); -- 1
insert into acls(id,name) values(nextval('seq_acl_id'),'reviewers.acl'); -- 2
insert into acls(id,name) values(nextval('seq_acl_id'),'delete.me.acl'); -- 3
insert into acls(id,name) values(nextval('seq_acl_id'),'rename.me.acl'); -- 4
insert into acls(id,name) values(nextval('seq_acl_id'),'no-permissions-except-owner.acl'); -- 5
insert into acls(id,name) values(nextval('seq_acl_id'),'no-permissions-except-everyone.acl'); -- 6
insert into acls(id,name) values(nextval('seq_acl_id'),'no-permissions.acl'); -- 7
insert into acls(id,name) values(nextval('seq_acl_id'),'creators.acl'); -- 8
insert into acls(id,name) values(nextval('seq_acl_id'),'browse.but.no.create.acl'); -- 9
insert into acls(id,name) values(nextval('seq_acl_id'),'create.but.no.browse.acl'); -- 10
insert into acls(id,name) values(nextval('seq_acl_id'),'set.acl.allowed'); -- 11

insert into folder_types(id,name) values(nextval('seq_folder_type_id'),'_default_folder_type');

-- #1 root folder
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id)
values(nextval('seq_folder_id'),'root',0,1,1,null,1);

-- #2 home folder inside root folder
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id, summary) 
values(nextval('seq_folder_id'),'home',0,1,1,1,1, '<summary>stuff</summary>');

-- #3 unseen folder inside home folder with acl #7 (no-permissions.acl)
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id) 
values(nextval('seq_folder_id'),'unseen',0,7,1,2,1);

-- #4 archive folder with some objects to test getObjectsByFolderId
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id)
values(nextval('seq_folder_id'),'archive',0,1,1,2,1);

-- #5 deletion folder with some objects to test deletion of links/objects
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id)
values(nextval('seq_folder_id'),'deletion',0,1,1,2,1);

-- #6 creation folder to test creation of links/objects
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id)
values(nextval('seq_folder_id'),'creation',0,8,1,2,1);

-- #7 folder in creation folder#6, acl#10 to test lack of browse permission for links
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id)
values(nextval('seq_folder_id'),'u-no-browse',0,10,1,6,1);

-- #8 folder in creation folder#6, acl#9 to test lack of create object permission for links
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id)
values(nextval('seq_folder_id'),'u-no-create',0,9,1,6,1);

-- #9 folder in creation folder#6, acl#1 to test lack of create object permission for links
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id)
values(nextval('seq_folder_id'),'link-this-folder',0,1,1,6,1);

-- #10 folder in creaton folder#6, only-owner-acl#5 - for create link to owner-folder test
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id)
values(nextval('seq_folder_id'),'only-owner-links-to-me',0,5,2,6,1);

-- #11 folder for setSummary test in creaton folder#6, reviewer-acl#2 - 
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id, summary)
values(nextval('seq_folder_id'),'set-my-summary',0,2,1,6,1, 'no-sum');

-- #12 folder for get/setSummaryMissingPermission test in creaton folder#6, default-acl#1 
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id, summary)
values(nextval('seq_folder_id'),'cannot set-my-summary',0,1,1,6,1, 'no-sum');

-- #13 folder for getSummary test in creaton folder#6, reviewer-acl#2 
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id, summary)
values(nextval('seq_folder_id'),'get-my-summary',0,2,1,6,1, '<sum>folder</sum>');

-- #1
insert into objtypes(id,name) values(nextval('seq_obj_type_id'),'_default_objtype');

insert into groups(id,name) VALUES(nextval('seq_groups'),'_superusers'); -- #1
insert into groups(id,name,group_of_one) VALUES(nextval('seq_groups'),'_1_admin',true); -- #2
insert into groups(id,name,parent_id) VALUES(nextval('seq_groups'),'admin_child_group',2); -- #3
insert into groups(id,name,group_of_one) VALUES(nextval('seq_groups'),'_2_doe',true); -- #4
insert into groups(id,name) values (nextval('seq_groups'),'reviewers'); -- #5
insert into groups(id,name) values (nextval('seq_groups'),'_everyone'); -- #6
insert into groups(id,name) values (nextval('seq_groups'),'_owner'); -- #7

-- #1 admin is member of superuser group:
insert into group_users VALUES(1,1);
-- #2 admin is member of admin group:
insert into group_users VALUES(1,2);
-- #3 doe is member of his own group:
insert into group_users VALUES (2,4);
-- #4 doe is member of reviewers:
insert into group_users values (2,5);

-- #1 link superusers group to default acl:
insert into aclentries(id,acl_id,group_id) values (nextval('seq_acl_entries_id'),1,1);
-- #2 admin's group is connected to reviewers acl:
insert into aclentries(id,acl_id,group_id) values (nextval('seq_acl_entries_id'),2,2);
-- #3 doe's group is connected to reviewers.acl
insert into aclentries(id,acl_id,group_id) values (nextval('seq_acl_entries_id'),2,4);
-- #4 admin's child group is connected to rename.me.acl:
insert into aclentries(id,acl_id,group_id) values (nextval('seq_acl_entries_id'),4,3);
-- #5 reviewers are connected to reviewers acl:
insert into aclentries(id,acl_id,group_id) values (nextval('seq_acl_entries_id'),2,5);
-- #6 doe's group is connected to default acl
insert into aclentries(id,acl_id,group_id) values (nextval('seq_acl_entries_id'),1,4);
-- #7 reviewers also have the no-permissions acl
insert into aclentries(id,acl_id,group_id) values (nextval('seq_acl_entries_id'),1,5);
-- #8 _owner is connected to the no-permission-except-owner.acl
insert into aclentries(id,acl_id,group_id) values (nextval('seq_acl_entries_id'),5,7);
-- #9 _everyone is connected to no-permission-except-everyone.acl
insert into aclentries(id,acl_id,group_id) values (nextval('seq_acl_entries_id'),6,6);
-- #10 doe's group linked to creation acl#8 
insert into aclentries(id,acl_id,group_id) values (nextval('seq_acl_entries_id'),8,4);
-- #11 doe's group linked to no-browse.acl#9 
insert into aclentries(id,acl_id,group_id) values (nextval('seq_acl_entries_id'),9,4);
-- #12 doe's group linked to no-create.acl#10 
insert into aclentries(id,acl_id,group_id) values (nextval('seq_acl_entries_id'),10,4);
-- #13 reviewers connected to set.acl.allowed#
insert into aclentries(id,acl_id,group_id) values (nextval('seq_acl_entries_id'),11,5);


insert into permissions values (nextval('seq_permission_id'),'_browse'); -- #1
insert into permissions values (nextval('seq_permission_id'),'_browse_folder'); -- #2
insert into permissions values (nextval('seq_permission_id'),'_create_folder'); -- #3
insert into permissions values (nextval('seq_permission_id'),'_create_inside_folder'); -- #4
insert into permissions values (nextval('seq_permission_id'),'_delete_folder'); -- #5
insert into permissions values (nextval('seq_permission_id'),'_delete_object'); -- #6
insert into permissions values (nextval('seq_permission_id'),'_edit_folder'); -- #7
insert into permissions values (nextval('seq_permission_id'),'_lock'); -- #8
insert into permissions values (nextval('seq_permission_id'),'_move'); -- #9
insert into permissions values (nextval('seq_permission_id'),'_read_object_content'); -- #10
insert into permissions values (nextval('seq_permission_id'),'_read_object_custom_metadata'); -- #11
insert into permissions values (nextval('seq_permission_id'),'_read_object_sysmeta'); -- #12
insert into permissions values (nextval('seq_permission_id'),'_set_acl'); -- #13
insert into permissions values (nextval('seq_permission_id'),'_version'); -- #14
insert into permissions values (nextval('seq_permission_id'),'_write_object_content'); -- #15
insert into permissions values (nextval('seq_permission_id'),'_write_object_custom_metadata'); -- #16
insert into permissions values (nextval('seq_permission_id'),'_write_object_sysmeta'); -- #17

-- #1 browse permission for doe's group + default_acl:
insert into aclentry_permissions values (nextval('seq_aclentry_permission_id'),6,1);
-- #2 browse_folder permission for doe's group + default_acl:: 
insert into aclentry_permissions values (nextval('seq_aclentry_permission_id'),6,2);
-- #3 create folder permission for reviewers group + reviewers acl:
insert into aclentry_permissions values (nextval('seq_aclentry_permission_id'),5,3);
-- #4 add browse permission for _owner to view the objects with no-permission-except-owner acl:  
insert into aclentry_permissions(id,aclentry_id,permission_id) values (nextval('seq_aclentry_permission_id'),8,1);
-- #5 add browse permission for _everyone to view the objects with no-permission-except-everyone acl
insert into aclentry_permissions(id, aclentry_id,permission_id) values (nextval('seq_aclentry_permission_id'),9,1);
-- #6 delete_object permission for doe's group + default_acl:
insert into aclentry_permissions values (nextval('seq_aclentry_permission_id'),6,6);
-- #7 delete folder permission for doe's group + default_acl:
insert into aclentry_permissions values (nextval('seq_aclentry_permission_id'),6,5);
-- #8 browse permission for doe's group + creation.acl#8:
insert into aclentry_permissions values (nextval('seq_aclentry_permission_id'),10,1);
-- #9 browse_folder permission for doe's group + creation.acl#8: 
insert into aclentry_permissions values (nextval('seq_aclentry_permission_id'),10,2);
-- #10 create object permission for doe's group + creation.acl#8: 
insert into aclentry_permissions values (nextval('seq_aclentry_permission_id'),10,4);
-- #11 browse but no create permission for doe's group + no-creation.acl#9: (testing create link) 
insert into aclentry_permissions values (nextval('seq_aclentry_permission_id'),11,2);
-- #12 create but no browse permission for doe's group + no-creation.acl#10:  (testing create link)
insert into aclentry_permissions values (nextval('seq_aclentry_permission_id'),12,4);
-- #13 add delete_folder permission to no-permission-except-owner acl:
insert into aclentry_permissions(id,aclentry_id,permission_id) values (nextval('seq_aclentry_permission_id'),8,5);
-- #14 add delete_object permission to no-permission-except-owner acl:
insert into aclentry_permissions(id,aclentry_id,permission_id) values (nextval('seq_aclentry_permission_id'),8,6);
-- #15 add write_object_sysmeta permission to reviewers.acl:
insert into aclentry_permissions values (nextval('seq_aclentry_permission_id'),5,17);
-- #16 add write_object_sysmeta to set.acl.allowed acl #11 with reviewers group
insert into aclentry_permissions values (nextval('seq_aclentry_permission_id'),13,17);
-- #17 add set_acl to set.acl.allowed acl #11 with reviewers group
insert into aclentry_permissions values (nextval('seq_aclentry_permission_id'),13,13);
-- #18 add browse permission to set.acl.allowed acl #11 with reviewers group
insert into aclentry_permissions values (nextval('seq_aclentry_permission_id'),13,1);
-- #19 add browse permission to reviewers.acl with reviewer group:
insert into aclentry_permissions values (nextval('seq_aclentry_permission_id'),5,1);
-- #20 add browse_folder permission to set.acl.allowed acl #11 with reviewers group
insert into aclentry_permissions values (nextval('seq_aclentry_permission_id'),13,2);
-- #21 add browse_folder permission for _owner to view folders with no-permission-except-owner acl:  
insert into aclentry_permissions(id,aclentry_id,permission_id) values (nextval('seq_aclentry_permission_id'),8,2);
-- #22 add read_object_metadata permission for _owner to view summary with no-permission-except-owner acl:  
insert into aclentry_permissions(id,aclentry_id,permission_id) values (nextval('seq_aclentry_permission_id'),8,12);
-- #23 add write_object_metadata permission for _owner to view summary with no-permission-except-owner acl:  
insert into aclentry_permissions(id,aclentry_id,permission_id) values (nextval('seq_aclentry_permission_id'),8,17);
-- #24 add read_object_sysmeta permission to reviewers.acl:
insert into aclentry_permissions values (nextval('seq_aclentry_permission_id'),5,12);
-- #25 add write_object_content to reviewers.acl
insert into aclentry_permissions values (nextval('seq_aclentry_permission_id'),5,15);
-- #26 add read_object_content to reviewers.acl
insert into aclentry_permissions values (nextval('seq_aclentry_permission_id'),5,10);
-- #27 add lock permission to reviewers.acl
insert into aclentry_permissions values (nextval('seq_aclentry_permission_id'),5,8);

-- #1 language de
insert into languages (id,iso_code) values (nextval('seq_language_id'), 'de_DE');
-- #2 language: en
insert into languages (id,iso_code) values (nextval('seq_language_id'), 'en_EN');
-- #3 language: multiple
insert into languages (id,iso_code) values (nextval('seq_language_id'), 'mul');
-- #4 language: undetermined
insert into languages (id,iso_code) values (nextval('seq_language_id'), 'und');
-- #5 language: no-language
insert into languages (id,iso_code) values (nextval('seq_language_id'), 'zxx');

-- #1 test object with summary, default acl in root folder
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id, summary)
values (nextval('seq_objects_id'), now(), true, true, now(), 'test-1', 1, 1, 1, 1, 1, 1, 1, '<summary>sum of sum</summary>');
-- #2
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_objects_id'), now(), true, true, now(), 'test-2', 1, 1, 1, 1, 1, 1, 1);
-- #3
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_objects_id'), now(), true, true, now(), 'test-3', 1, 1, 1, 1, 1, 1, 1);

-- #4 object with no permissions, used in getObjectsById tests and create/update-Link test
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_objects_id'), now(), true, true, now(), 'unbrowsable-test', 1, 1, 1, 1, 1, 1, 5);

-- #5 - acl #5 has no permission for user Doe, but allows owners to view the item, so Doe should see it anyway.
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_objects_id'), now(), true, true, now(), 'owned-by-doe', 1, 1, 1, 2, 1, 1, 5);

-- #6 - acl #6 has no permission for user Doe, but allows "_everyone" to view the item, so Doe should see it anyway.
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_objects_id'), now(), true, true, now(), 'acl-for-everyone', 1, 1, 1, 1, 1, 1, 6);

-- #7 - acl #7 has no permission for anyone.
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_objects_id'), now(), true, true, now(), 'see-me-not', 1, 1, 1, 1, 1, 1, 7);

-- #8 parent for #9  default acl in root folder
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_objects_id'), now(), false, false, now(), 'test-parent', 1, 1, 1, 1, 1, 1, 1);

-- #9 child object for #8
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id, root_id)
values (nextval('seq_objects_id'), now(), true, true, now(), 'test-child', 1, 1, 1, 1, 1, 1, 1, 8);

-- #10 parent for osd#11 in archive folder#4
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_objects_id'), now(), false, false, now(), 'test-parent', 1, 1, 1, 1, 4, 1, 1);

-- #11 child object for #10, also in archive folder#4
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id, root_id, summary)
values (nextval('seq_objects_id'), now(), true, true, now(), 'test-child', 1, 1, 1, 1, 4, 1, 1, 10,'<summary>child@archive</summary>');

-- #12 test object for deletion,  default acl in deletion folder#5 -> currently used as target for deleteLink-Test
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_objects_id'), now(), true, true, now(), 'linked-to-me', 1, 1, 1, 1, 5, 1, 1);

-- #13 test object for create link,  default acl in creation folder#6 
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_objects_id'), now(), true, true, now(), 'linked-to-me-2', 1, 1, 1, 1, 6, 1, 1);

-- #14 test object for create link in folder#7 (where doe has no browse permission)  
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_objects_id'), now(), true, true, now(), 'linked-to-me-3', 1, 1, 1, 1, 7, 1, 1);

-- #15 test object for update link to object,  default acl in creation folder#6  
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_objects_id'), now(), true, true, now(), 'linked-to-me-2', 1, 1, 1, 1, 6, 1, 1);

-- #16 test object for getSummaries,  reviewer acl in creation folder#6  
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id, summary)
values (nextval('seq_objects_id'), now(), true, true, now(), '7th-sum-of-a-7th-sum', 1, 1, 1, 1, 6, 1, 2,'<sum>7</sum>');

-- #17 test object for setSummary,  reviewer acl in creation folder#6  
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id, summary)
values (nextval('seq_objects_id'), now(), true, true, now(), 'summ-summ-summ', 1, 1, 1, 1, 6, 1, 2,'no summary');

-- #18 test object for setSummaryNoPermission,  default acl in creation folder#6  
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id, summary)
values (nextval('seq_objects_id'), now(), true, true, now(), 'no-perm-summary', 1, 1, 1, 1, 6, 1, 1,'no summary');

-- #19 test object for relations (as rightId) in creation folder #6
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_objects_id'), now(), true, true, now(), 'right-related', 1, 1, 1, 1, 6, 1, 1);

-- #20 test object for relations (as leftId) in creation folder #6
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_objects_id'), now(), true, true, now(), 'left-related', 1, 1, 1, 1, 6, 1, 1);

-- #21 test object for create-delete relations (target: osd#20) in creation folder #6
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_objects_id'), now(), true, true, now(), 'related', 1, 1, 1, 1, 6, 1, 1);

-- #22 test object for setContent/getContent in creation folder #6
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_objects_id'), now(), true, true, now(), 'content-holder', 1, 1, 1, 1, 6, 1, 2);

-- #23 test object for setContent/getContent without write permission for reviewers in creation folder #6
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_objects_id'), now(), true, true, now(), 'content-holder', 1, 1, 1, 1, 6, 1, 1);

-- #24 test object for getContent without read/write permission for reviewers in creation folder #6
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_objects_id'), now(), true, true, now(), 'content-holder', 1, 1, 1, 1, 6, 1, 1);

-- #25 empty test object for getContent test in creation folder #6
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_objects_id'), now(), true, true, now(), 'content-holder', 1, 1, 1, 1, 6, 1, 2);

-- #26 empty test object for lock/unlock test in creation folder #6
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_objects_id'), now(), true, true, now(), 'lock-me', 1, 1, 1, 1, 6, 1, 2);

-- #27 empty test object without permissions for lock/unlock test in creation folder #6
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_objects_id'), now(), true, true, now(), 'u-no-lock-me', 1, 1, 1, 1, 6, 1, 7);

-- #1 link to osd #1 with default acl (#1)
insert into links(id, type,owner_id,acl_id,parent_id,osd_id) 
values (nextval('seq_links_id'), 'OBJECT',  1,1,1,1);

-- #2 link to folder #2 with default acl (#1)
insert into links(id, type,owner_id,acl_id,parent_id,folder_id) 
values (nextval('seq_links_id'), 'FOLDER',  1,1,1,2);

-- #3 link to osd #1 with no permission except owner acl (#1)
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_links_id'), 'OBJECT',  1,5,1,1);

-- #4 link to osd #7 with no-permission.acl (#7)
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)    
values (nextval('seq_links_id'), 'OBJECT',  1,1,1,7);

-- #5 link to folder #3 with no-permission. acl (#7)
insert into links(id, type,owner_id,acl_id,parent_id,folder_id)
values (nextval('seq_links_id'), 'FOLDER',  1,1,1,3);

-- deprecated (removed resolvers):
-- -- #6 link to osd #8 with latest_head resolver - should return osd#9 when link is queried.
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_links_id'), 'OBJECT', 1,1,1,8);

-- deprecated (removed resolvers):
-- #7 link to osd #10 with latest_head  in folder 'archive' #7 - should return osd#11 when link is queried.
-- used in getObjectsByFolderId
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_links_id'), 'OBJECT', 1,1,4,10);

-- #8 link to osd #11 with fixed resolver - should return osd#11 when link is queried.
-- used in getObjectsByFolderId, but with no_permission.acl #7, should not be seen by normal user.
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_links_id'), 'OBJECT',  1,7,4,11);

-- #9 link to osd#12 for deletion tests: default acl, deletion allowed
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_links_id'), 'OBJECT',  1,1,1,12);

-- #10 link to folder#5 for deletion tests: default acl, deletion allowed
insert into links(id, type,owner_id,acl_id,parent_id,folder_id)
values (nextval('seq_links_id'), 'FOLDER',  1,1,1,5);

-- #11 link to osd#12 for deletion tests: no_permission-acl, browse not allowed
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_links_id'), 'OBJECT',  1,7,1,12);

-- #12 link to osd#12 for deletion tests: reviewer-acl#2, browse allowed, deletion not allowed
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_links_id'), 'OBJECT',  1,7,1,12);

-- #13 link to folder#5 for deletion tests: reviewer acl#2, browse_folder allowed, deletion not allowed
insert into links(id, type,owner_id,acl_id,parent_id,folder_id)
values (nextval('seq_links_id'), 'FOLDER',  1,2,1,5);

-- #14 link to osd#13 for testing owner browse permission: only-owner-acl#5, link owned by doe.
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_links_id'), 'OBJECT',  2, 5, 1, 13);

-- #15 link to osd#13 for testing delete link with just owner permission: only-owner-acl#5, link owned by doe.
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_links_id'), 'OBJECT',  2, 5, 1, 13);

-- #16 link to folder#5 for testing delete link with just owner permission: only-owner-acl#5, link owned by doe.
insert into links(id, type,owner_id,acl_id,parent_id, folder_id)
values (nextval('seq_links_id'), 'FOLDER',  2, 5, 1, 5);

-- #17 link to osd#13 with no-permission-acl #7 to test updateLink without browse permission
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_links_id'), 'OBJECT',  2, 7, 1, 13);

-- #18 link to osd#13 with reviewer-acl # to test updateLink.setAcl without setAcl permission
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_links_id'), 'OBJECT',  1, 2, 1, 13);

-- #19 link to osd#13 with set-acl.allowed-acl #11 to test updateLink.setAcl with setAcl permission
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_links_id'), 'OBJECT',  1, 11, 1, 13);

-- #20 link to folder #5, will try to change this link to folder #3 (unseen folder) 
insert into links(id, type,owner_id,acl_id,parent_id,folder_id)
values (nextval('seq_links_id'), 'FOLDER',  1, 11, 1, 5);

-- #21 link to osd#13 with set-acl.allowed-acl #11 to test non-acl updates
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_links_id'), 'OBJECT',  1, 11, 1, 13);

-- #22 link to folder #5, will try to change this link to osd#13 
insert into links(id, type,owner_id,acl_id,parent_id,folder_id)
values (nextval('seq_links_id'), 'FOLDER',  1, 11, 1, 5);

-- deprecated (removed resolvers):
-- -- #23 link to osd#13, will try to change this link to folder#6 
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_links_id'), 'OBJECT', 1, 11, 1, 13);

-- deprecated (removed resolvers):
-- #24 link to osd#13, will try to change this resolver to fixed 
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_links_id'), 'OBJECT', 1, 11, 1, 13);

-- #1 add format: xml
insert into formats(id, contenttype, extension, name, default_object_type_id) 
VALUES (nextval('seq_format_id'),'application/xml','xml', 'xml', 1); 

-- #2 format: text/plain
insert into formats(id, contenttype, extension, name, default_object_type_id)
VALUES (nextval('seq_format_id'),'text/plain','txt', 'plaintext', 1);

-- #1 relationType: protect all & clone always
insert into relationtypes (id, leftobjectprotected, name, rightobjectprotected,
                           clone_on_right_copy, clone_on_left_copy, clone_on_left_version, clone_on_right_version                           )
VALUES (nextval('seq_relationtypes_id'), true, 'all-protector', true,
        true, true, true, true); 

-- #2 relationType: protect nothing & clone never
insert into relationtypes (id, leftobjectprotected, name, rightobjectprotected,
                           clone_on_right_copy, clone_on_left_copy, clone_on_left_version, clone_on_right_version                           )
VALUES (nextval('seq_relationtypes_id'), true, 'unprotected', true,
        false, false, false, false);    

-- #1 uiLanguage: de
insert into ui_languages (id,iso_code) values (nextval('seq_ui_language_id'), 'DE');

-- #2 uiLanguage: en
insert into ui_languages (id,iso_code) values (nextval('seq_ui_language_id'), 'EN');

-- #1 general_metadata
insert into metaset_types(id,name) values(nextval('seq_metaset_types_id'), 'thumbnail');


-- #1 index_item
insert into index_items(id, fieldname, for_content, for_metadata, for_sys_meta, multiple_results,
   name, search_string, va_params, search_condition, index_type_name, store_field)
values (nextval('seq_index_item_id'), 'acl', false,false,true,false,'index.acl',
  '/sysMeta/object/aclId', '<vaParams type="client.acl.id"/>','true()','DEFAULT_STRING_INDEXER',true 
);

-- #1 lifecycle
insert into lifecycles(id, name, default_state_id) VALUES (nextval('seq_lifecycle_id'), 'review.lc',null);
insert into lifecycles(id, name, default_state_id) VALUES (nextval('seq_lifecycle_id'), 'render.lc',null);

-- #1 relation: type 1 relation
insert into relations(id,left_id, right_id, type_id, metadata) VALUES (nextval('seq_relations_id'),20,19,1,'<meta>important</meta>' );
-- #2 relation: type 2 relation
insert into relations(id,left_id, right_id, type_id, metadata) VALUES (nextval('seq_relations_id'),19,20,2,'<meta>ignore</meta>' );

-- #1 lifecycle_state
insert into lifecycle_states(id, name, config, state_class, life_cycle_id )
    values (nextval('seq_lifecycle_states_id'), 'newRenderTask', '<meta>renderserver:x</meta>', 'NopState', 2);
insert into lifecycle_state_to_copy_state(lifecycle_state_id, copy_state_id)
  values (currval('seq_lifecycle_states_id'), currval('seq_lifecycle_states_id'));
