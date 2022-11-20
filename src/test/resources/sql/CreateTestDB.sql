-------------------------
--- table definitions ---
-------------------------

-- users --
drop table if exists users cascade;
CREATE TABLE users (
  id BIGINT PRIMARY KEY,
  name VARCHAR(255) NOT NULL unique,
  pwd VARCHAR(255) NOT NULL,
  obj_version int NOT NULL DEFAULT 0,
  login_type VARCHAR(64) NOT NULL DEFAULT 'CINNAMON',
  activated BOOLEAN NOT NULL DEFAULT TRUE,
  locked BOOLEAN NOT NULL DEFAULT FALSE,
  ui_language_id BIGINT,
  fullname varchar(255) NOT NULL,
  email varchar(255),
  change_tracking BOOLEAN NOT NULL DEFAULT TRUE,
  token varchar(255),
  tokens_today int,
  password_expired BOOLEAN NOT NULL DEFAULT FALSE,
  activate_triggers BOOLEAN NOT NULL DEFAULT TRUE,
  config text default '<config/>' not null
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
  iso_code varchar(32) not null unique
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
  config text default '<config />' not null
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


-- object_types --
drop table if exists object_types cascade ;
create table object_types
(
  id bigint not null
    constraint object_types_pkey
    primary key,
  name varchar(255)
    constraint object_types_name_key
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
    references object_types
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
    references lifecycles,
  copy_state_id bigint references lifecycle_states
);

alter table lifecycles add constraint fk_default_state_id FOREIGN KEY
  (default_state_id) references lifecycle_states(id);

drop sequence if exists seq_lifecycle_state_id;
create sequence seq_lifecycle_state_id start with 1;

-- objects --
drop table if exists objects cascade ;
create table objects
(
  id bigint not null
    constraint objects_pkey
    primary key,
  content_path varchar(255),
  content_size bigint,
  created timestamp not null,
  latest_branch boolean not null,
  latest_head boolean not null,
  modified timestamp not null,
  name varchar(128) not null,
  version bigint default 0,
  cmn_version varchar(1024) default '1' not null,
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
    references object_types,
  state_id bigint
    constraint lifecycle_state
    references lifecycle_states,
  content_changed boolean default false not null,
  metadata_changed boolean default false not null,
  summary text default '<summary/>' not null,
  content_hash varchar(128)
);
drop sequence if exists seq_object_id;
create sequence seq_object_id start with 1;


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

drop sequence if exists seq_link_id;
create sequence seq_link_id start with 1;


-- groups --
drop table if exists groups cascade ;
create table groups(
  id bigint PRIMARY KEY,
  name varchar(255) UNIQUE,
  parent_id bigint constraint fk_group_parent_id references groups
);
drop sequence if exists seq_group_id;
create SEQUENCE seq_group_id start with 1;

-- acl_groups --

drop table if exists acl_groups cascade ;
create table acl_groups
(
  id bigint not null
    constraint acl_groups_pkey
    primary key,
  acl_id bigint not null
    constraint fk5a6c3cc63e44742f
    references acls,
  group_id bigint not null
    constraint fk5a6c3cc64e6fdacf
    references groups,
  constraint unique_acl_group_id
  unique (group_id, acl_id)
);
drop sequence if exists seq_acl_group_id;
create sequence seq_acl_group_id start with 1;


-- group_users --
drop table if exists group_users cascade ;
create table group_users(
  id BIGINT NOT NULL,
  user_id BIGINT NOT NULL ,
  group_id BIGINT NOT NULL
);

drop sequence if exists seq_group_user_id;
create sequence seq_group_user_id start with 1;

create UNIQUE INDEX  group_users_user_group
  on group_users(user_id,group_id);

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

-- acl_group_permissions --
drop table if exists  acl_group_permissions cascade ;
create table acl_group_permissions
(
  id bigint not null
    constraint acl_group_permissions_pkey
    primary key,
  acl_group_id bigint not null
    constraint fk2110acedec2a9305
    references acl_groups,
  permission_id bigint not null
    constraint fk2110aceda5501f05
    references permissions,
  constraint unique_acl_group_permission_id
  unique (permission_id, acl_group_id)
);
drop sequence if exists seq_acl_group_permission_id;
create sequence seq_acl_group_permission_id start with 1;

drop table if exists relation_types cascade ;
create table relation_types
(
  id bigint not null
    constraint relation_types_pkey
    primary key,
  left_object_protected boolean not null,
  name varchar(128) not null
    constraint relation_types_name_key
    unique,
  right_object_protected boolean not null,
  clone_on_right_copy boolean default false not null,
  clone_on_left_copy boolean default false not null,
  clone_on_left_version boolean default false not null,
  clone_on_right_version boolean default false not null
)
;
drop sequence if exists seq_relation_type_id;
create sequence seq_relation_type_id start with 1;

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
    references relation_types,
  metadata text default '<meta/>' not null,
  constraint unique_left_id
  unique (type_id, right_id, left_id)
)
;
drop sequence if exists seq_relation_id;
create sequence seq_relation_id start with 1;

-- metaset_types --
drop table if exists metaset_types cascade ;
create table metaset_types
(
  id bigint not null
    constraint metaset_types_pkey
    primary key,
  name varchar(128) not null
    constraint metaset_types_name_key
    unique,
  is_unique boolean not null default true
);
drop sequence if exists seq_metaset_type_id;
create sequence seq_metaset_type_id start with 1;

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
  search_condition text default 'true()' not null,
  store_field boolean default false not null,
  index_type varchar(64) not null
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
drop sequence if exists seq_config_entry_id;
create sequence seq_config_entry_id start with 1;

drop table if exists osd_meta cascade;
create table osd_meta
(
  id bigint not null
    constraint osd_meta_pkey
    primary key,
  osd_id bigint not null
    constraint fk_osd_meta_osd
    references objects,
  content text not null,
  type_id int not null
    constraint fke5345bd6abf96b0c
    references metaset_types
)
;
drop sequence if exists seq_osd_meta_id;
create sequence seq_osd_meta_id;

drop table if exists folder_meta cascade;
create table folder_meta
(
  id bigint not null
    constraint folder_meta_pkey
    primary key,
  folder_id bigint not null
    constraint fk_folder_meta_folder
    references folders,
  content text not null,
  type_id int not null
    constraint fke5345bd6abf96b0c
    references metaset_types
)
;
drop sequence if exists seq_folder_meta_id;
create sequence seq_folder_meta_id;

drop table if exists deletions cascade;
create table deletions
(
    osd_id        bigint       not null unique,
    content_path  varchar(255) not null,
    is_deleted    boolean default false,
    delete_failed boolean default false
);

drop sequence if exists seq_index_job_id;
create sequence seq_index_job_id;

drop table if exists index_jobs cascade;
create table index_jobs(
  id bigint not null constraint index_job_pkey primary key,
    job_type varchar(127) not null,
    item_id bigint not null,
    failed int not null default 0,
    action varchar(127) not null
);

--------------------------
--- insert test data:  ---
-- -----------------------

-- #1
INSERT INTO users(id,name,pwd,activated, ui_language_id, fullname, change_tracking, activate_triggers)
VALUES ( nextval('seq_user_id'),'admin','$2a$10$VG9LCf6h/Qwb7Y.pafHkaepdnJNgFZUzzuMV3EcyvLbKnueHQ4IW.',true,1,'The Admin',false, false);
-- #2
INSERT INTO users(id,name,pwd,activated, ui_language_id, fullname, activate_triggers)
VALUES ( nextval('seq_user_id'),'doe','$2a$10$VG9LCf6h/Qwb7Y.pafHkaepdnJNgFZUzzuMV3EcyvLbKnueHQ4IW.',true,1,'Jane Doe', true);
-- #3
INSERT INTO users(id,name,pwd,activated, ui_language_id, fullname, activate_triggers)
VALUES ( nextval('seq_user_id'),'deactivated user','$2a$10$VG9LCf6h/Qwb7Y.pafHkaepdnJNgFZUzzuMV3EcyvLbKnueHQ4IW.',false,1, 'inactive', true);
-- #4
INSERT INTO users(id,name,pwd,activated, locked, ui_language_id, fullname, activate_triggers)
VALUES ( nextval('seq_user_id'),'locked user','$2a$10$VG9LCf6h/Qwb7Y.pafHkaepdnJNgFZUzzuMV3EcyvLbKnueHQ4IW.',true,true,1, 'locked', true);

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
insert into acls(id,name) values(nextval('seq_acl_id'),'edit_folder.allowed'); -- 12
insert into acls(id,name) values(nextval('seq_acl_id'),'no.move.allowed'); -- 13
insert into acls(id,name) values(nextval('seq_acl_id'),'no.set.acl.allowed'); -- 14


-- #1
insert into object_types(id,name) values(nextval('seq_obj_type_id'),'_default_objtype');

insert into groups(id,name) VALUES(nextval('seq_group_id'),'_superusers'); -- #1
insert into groups(id,name) VALUES(nextval('seq_group_id'),'_1_admin'); -- #2
insert into groups(id,name,parent_id) VALUES(nextval('seq_group_id'),'admin_child_group',2); -- #3
insert into groups(id,name) VALUES(nextval('seq_group_id'),'_2_doe'); -- #4
insert into groups(id,name) values (nextval('seq_group_id'),'reviewers'); -- #5
insert into groups(id,name) values (nextval('seq_group_id'),'_everyone'); -- #6
insert into groups(id,name) values (nextval('seq_group_id'),'_owner'); -- #7

-- #1 admin is member of superuser group:
insert into group_users VALUES(nextval('seq_group_user_id'),1,1);
-- #2 admin is member of admin group:
insert into group_users VALUES(nextval('seq_group_user_id'),1,2);
-- #3 doe is member of his own group:
insert into group_users VALUES (nextval('seq_group_user_id'),2,4);
-- #4 doe is member of reviewers:
insert into group_users values (nextval('seq_group_user_id'),2,5);

-- #1 link superusers group to default acl:
insert into acl_groups(id,acl_id,group_id) values (nextval('seq_acl_group_id'),1,1);
-- #2 admin's group is connected to reviewers acl:
insert into acl_groups(id,acl_id,group_id) values (nextval('seq_acl_group_id'),2,2);
-- #3 doe's group is connected to reviewers.acl
insert into acl_groups(id,acl_id,group_id) values (nextval('seq_acl_group_id'),2,4);
-- #4 admin's child group is connected to rename.me.acl:
insert into acl_groups(id,acl_id,group_id) values (nextval('seq_acl_group_id'),4,3);
-- #5 reviewers are connected to reviewers acl:
insert into acl_groups(id,acl_id,group_id) values (nextval('seq_acl_group_id'),2,5);
-- #6 doe's group is connected to default acl
insert into acl_groups(id,acl_id,group_id) values (nextval('seq_acl_group_id'),1,4);
-- #7 reviewers also have the default acl
insert into acl_groups(id,acl_id,group_id) values (nextval('seq_acl_group_id'),1,5);
-- #8 _owner is connected to the no-permission-except-owner.acl
insert into acl_groups(id,acl_id,group_id) values (nextval('seq_acl_group_id'),5,7);
-- #9 _everyone is connected to no-permission-except-everyone.acl
insert into acl_groups(id,acl_id,group_id) values (nextval('seq_acl_group_id'),6,6);
-- #10 doe's group linked to creation acl#8 
insert into acl_groups(id,acl_id,group_id) values (nextval('seq_acl_group_id'),8,4);
-- #11 doe's group linked to no-browse.acl#9 
insert into acl_groups(id,acl_id,group_id) values (nextval('seq_acl_group_id'),9,4);
-- #12 doe's group linked to no-create.acl#10 
insert into acl_groups(id,acl_id,group_id) values (nextval('seq_acl_group_id'),10,4);
-- #13 reviewers connected to set.acl.allowed#
insert into acl_groups(id,acl_id,group_id) values (nextval('seq_acl_group_id'),11,5);
-- #14 reviewers connected to set.edit_folder but not write sys meta
insert into acl_groups(id,acl_id,group_id) values (nextval('seq_acl_group_id'),12,5);
-- #15 reviewers connected to no.move.allowed acl (with setAcl allowed)
insert into acl_groups(id,acl_id,group_id) values (nextval('seq_acl_group_id'),13,5);
-- #16 reviewers connected to acl#14 no set acl allowed
insert into acl_groups(id,acl_id,group_id) values (nextval('seq_acl_group_id'),14,5);


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
insert into permissions values (nextval('seq_permission_id'),'relation.child.add'); -- #18
insert into permissions values (nextval('seq_permission_id'),'relation.parent.add'); -- #19
insert into permissions values (nextval('seq_permission_id'),'relation.child.remove'); -- #20
insert into permissions values (nextval('seq_permission_id'),'relation.parent.remove'); -- #21

-- note: personal user groups are deprecated. Add user to normal groups instead.
-- #1 browse permission for doe's group + default_acl:
insert into acl_group_permissions values (nextval('seq_acl_group_permission_id'),6,1);
-- #2 browse_folder permission for doe's group + default_acl:: 
insert into acl_group_permissions values (nextval('seq_acl_group_permission_id'),6,2);
-- #3 create folder permission for reviewers group + reviewers acl:
insert into acl_group_permissions values (nextval('seq_acl_group_permission_id'),5,3);
-- #4 add browse permission for _owner to view the objects with no-permission-except-owner acl:  
insert into acl_group_permissions(id,acl_group_id,permission_id) values (nextval('seq_acl_group_permission_id'),8,1);
-- #5 add browse permission for _everyone to view the objects with no-permission-except-everyone acl
insert into acl_group_permissions(id, acl_group_id,permission_id) values (nextval('seq_acl_group_permission_id'),9,1);
-- #6 delete_object permission for doe's group + default_acl:
insert into acl_group_permissions values (nextval('seq_acl_group_permission_id'),6,6);
-- #7 delete folder permission for doe's group + default_acl:
insert into acl_group_permissions values (nextval('seq_acl_group_permission_id'),6,5);
-- #8 browse permission for doe's group + creation.acl#8:
insert into acl_group_permissions values (nextval('seq_acl_group_permission_id'),10,1);
-- #9 browse_folder permission for doe's group + creation.acl#8: 
insert into acl_group_permissions values (nextval('seq_acl_group_permission_id'),10,2);
-- #10 create object permission for doe's group + creation.acl#8: 
insert into acl_group_permissions values (nextval('seq_acl_group_permission_id'),10,4);
-- #11 browse but no create permission for doe's group + no-creation.acl#9: (testing create link) 
insert into acl_group_permissions values (nextval('seq_acl_group_permission_id'),11,2);
-- #12 create but no browse permission for doe's group + no-creation.acl#10:  (testing create link)
insert into acl_group_permissions values (nextval('seq_acl_group_permission_id'),12,4);
-- #13 add delete_folder permission to no-permission-except-owner acl:
insert into acl_group_permissions(id,acl_group_id,permission_id) values (nextval('seq_acl_group_permission_id'),8,5);
-- #14 add delete_object permission to no-permission-except-owner acl:
insert into acl_group_permissions(id,acl_group_id,permission_id) values (nextval('seq_acl_group_permission_id'),8,6);
-- #15 add write_object_sysmeta permission to reviewers.acl:
insert into acl_group_permissions values (nextval('seq_acl_group_permission_id'),5,17);
-- #16 add version permission to reviewers.acl
insert into acl_group_permissions values(nextval('seq_acl_group_permission_id'), 5,14);
-- #17 write-sys-metadata for doe's group + creation.acl#8
insert into acl_group_permissions values (nextval('seq_acl_group_permission_id'),10,17);
-- #18 version for doe's group + creation.acl#8
insert into acl_group_permissions values (nextval('seq_acl_group_permission_id'),10,14);


---- set.acl.allowed acl#11
-- #16 add write_object_sysmeta to set.acl.allowed acl #11 with reviewers group
insert into acl_group_permissions values (nextval('seq_acl_group_permission_id'),13,17);
-- #17 add set_acl to set.acl.allowed acl #11 with reviewers group
insert into acl_group_permissions values (nextval('seq_acl_group_permission_id'),13,13);
-- #18 add browse permission to set.acl.allowed acl #11 with reviewers group
insert into acl_group_permissions values (nextval('seq_acl_group_permission_id'),13,1);

-- #19 add browse permission to reviewers.acl with reviewer group:
insert into acl_group_permissions values (nextval('seq_acl_group_permission_id'),5,1);
-- #20 add browse_folder permission to set.acl.allowed acl #11 with reviewers group
insert into acl_group_permissions values (nextval('seq_acl_group_permission_id'),13,2);
-- #21 add browse_folder permission for _owner to view folders with no-permission-except-owner acl:  
insert into acl_group_permissions(id,acl_group_id,permission_id) values (nextval('seq_acl_group_permission_id'),8,2);
-- #22 add read_object_metadata permission for _owner to view summary with no-permission-except-owner acl:  
insert into acl_group_permissions(id,acl_group_id,permission_id) values (nextval('seq_acl_group_permission_id'),8,12);
-- #23 add write_object_metadata permission for _owner to view summary with no-permission-except-owner acl:  
insert into acl_group_permissions(id,acl_group_id,permission_id) values (nextval('seq_acl_group_permission_id'),8,17);
-- #24 add read_object_sysmeta permission to reviewers.acl:
insert into acl_group_permissions values (nextval('seq_acl_group_permission_id'),5,12);
-- #25 add write_object_content to reviewers.acl
insert into acl_group_permissions values (nextval('seq_acl_group_permission_id'),5,15);
-- #26 add read_object_content to reviewers.acl
insert into acl_group_permissions values (nextval('seq_acl_group_permission_id'),5,10);
-- #27 add lock permission to reviewers.acl
insert into acl_group_permissions values (nextval('seq_acl_group_permission_id'),5,8);
-- #28 add read_custom_metadata to reviewers.acl
insert into acl_group_permissions values (nextval('seq_acl_group_permission_id'),5,11);
-- #29 add edit_folder to edit_folder acl:
insert into acl_group_permissions(id, acl_group_id, permission_id) values (nextval('seq_acl_group_permission_id'),14,7);
-- #30 add edit_folder to reviewers.acl
insert into acl_group_permissions(id, acl_group_id, permission_id) values (nextval('seq_acl_group_permission_id'),5,7);

-- permissions for reviewers on no.move.allowed acl#13
-- #31 write sys meta
insert into acl_group_permissions(id, acl_group_id, permission_id) values (nextval('seq_acl_group_permission_id'),15,17);
-- #32 create inside folder
insert into acl_group_permissions(id, acl_group_id, permission_id) values (nextval('seq_acl_group_permission_id'),15,3);
-- #33 edit_folder
insert into acl_group_permissions(id, acl_group_id, permission_id) values (nextval('seq_acl_group_permission_id'),15,7);

-- #34 set_acl for reviewers.acl
insert into acl_group_permissions(id, acl_group_id, permission_id) values (nextval('seq_acl_group_permission_id'),5,13);

-- permissions for reviewers on no.set.acl.allowed acl#14
-- #35 write sys meta
insert into acl_group_permissions(id, acl_group_id, permission_id) values (nextval('seq_acl_group_permission_id'),16,17);
-- #36 edit_folder
insert into acl_group_permissions(id, acl_group_id, permission_id) values (nextval('seq_acl_group_permission_id'),16,7);
-- #37 browse
insert into acl_group_permissions(id, acl_group_id, permission_id) values (nextval('seq_acl_group_permission_id'),16,1);

-- #38 move permission for reviewers on reviewer.acl
insert into acl_group_permissions(id, acl_group_id, permission_id) values (nextval('seq_acl_group_permission_id'),5,9);

-- #39 create permission for reviewers on create.acl
insert into acl_group_permissions(id, acl_group_id, permission_id) values (nextval('seq_acl_group_permission_id'),10,3);

-- #40 delete permission for reviewers on reviewer.acl #2
insert into acl_group_permissions(id, acl_group_id, permission_id) values (nextval('seq_acl_group_permission_id'),5,6);


-- #1 default folder type
insert into folder_types(id,name) values(nextval('seq_folder_type_id'),'_default_folder_type');
-- #2 archive folder type
insert into folder_types(id,name) values(nextval('seq_folder_type_id'),'_archive_folder_type');

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

-- #14 folder in creation folder#6 for getFolderByPath
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id)
values(nextval('seq_folder_id'),'some-sub-folder',0,1,1,6,1);

-- #15 folder in creation folder#6 for getMeta / createMeta test without permissions
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id)
values(nextval('seq_folder_id'),'u-no-read-meta',0,7,1,6,1);

-- #16 folder in creation folder#6 for getMetaHappyPath test (other getMeta-Tests: see OsdServletIntegrationTest)
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id)
values(nextval('seq_folder_id'),'have-me-some-meta',0,2,1,6,1);

-- #17 folder in creation folder#6 for createMeta test
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id)
values(nextval('seq_folder_id'),'create-me-a-meta',0,2,1,6,1);

-- #18 folder in creation folder#6 for createMeta test: has unique meta set
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id)
values(nextval('seq_folder_id'),'has-a-meta-already',0,2,1,6,1);

-- #19 folder in creation folder#6 for createMeta test: has non-unique meta set
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id)
values(nextval('seq_folder_id'),'comment-metaset',0,2,1,6,1);

-- #20 folder in creation folder#6 for deleteMeta test: has no permissions
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id)
values(nextval('seq_folder_id'),'delete-my-meta-no-permission',0,7,1,6,1);

-- #21 folder in creation folder#6 for createMeta test: has three metasets (2x comment, 1x license)
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id)
values(nextval('seq_folder_id'),'delete-my-meta',0,2,1,6,1);

-- #22 folder in creation folder#6 for updateFolder tests: has no permissions
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id)
values(nextval('seq_folder_id'),'update-without-permission',0,7,1,6,1);

-- #23 folder in creation folder#6 for updateFolder test: has no set_acl permission
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id)
values(nextval('seq_folder_id'),'update-acl-without-permission',0,14,1,6,1);

-- #24 folder in creation folder#6 for updateFolder test: has set_acl permission
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id)
values(nextval('seq_folder_id'),'update-acl-with-permission',0,2,1,6,1);

-- #25 folder in creation folder#6 for updateFolder test: happy path
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id)
values(nextval('seq_folder_id'),'happy update folder',0,2,1,6,1);

-- #26 folder in creation folder#6 for updateFolder test: no write sysmeta permission
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id)
values(nextval('seq_folder_id'),'folder-update no write sysmeta',0,12,1,6,1);

-- #27 folder in creation folder6 as target for moving a folder to (happy path)
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id)
values(nextval('seq_folder_id'),'move here',0,2,1,6,1);

-- #28 folder in creation folder6 has no-move-permission
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id)
values(nextval('seq_folder_id'),'unmovable move me',0,13,1,6,1);

-- #29 folder in creation folder6 for duplicate name check
insert into folders(id,name,obj_version,acl_id,owner_id,parent_id,type_id)
values(nextval('seq_folder_id'),'duplicate name',0,2,1,6,1);


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
values (nextval('seq_object_id'), now(), true, true, now(), 'test-1', 1, 1, 1, 1, 1, 1, 1, '<summary>sum of sum</summary>');
-- #2
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'test-2', 1, 1, 1, 1, 1, 1, 1);
-- #3
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'test-3', 1, 1, 1, 1, 1, 1, 1);

-- #4 object with no permissions, used in getObjectsById tests and create/update-Link test
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'unbrowsable-test', 1, 1, 1, 1, 1, 1, 5);

-- #5 - acl #5 has no permission for user Doe, but allows owners to view the item, so Doe should see it anyway.
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'owned-by-doe', 1, 1, 1, 2, 1, 1, 5);

-- #6 - acl #6 has no permission for user Doe, but allows "_everyone" to view the item, so Doe should see it anyway.
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'acl-for-everyone', 1, 1, 1, 1, 1, 1, 6);

-- #7 - acl #7 has no permission for anyone.
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'see-me-not', 1, 1, 1, 1, 1, 1, 7);

-- #8 parent for #9  default acl in root folder
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), false, false, now(), 'test-parent', 1, 1, 1, 1, 1, 1, 1);

-- #9 child object for #8
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id, root_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'test-child', 1, 1, 1, 1, 1, 1, 1, 8);

-- #10 parent for osd#11 in archive folder#4
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), false, false, now(), 'test-parent', 1, 1, 1, 1, 4, 1, 1);

-- #11 child object for #10, also in archive folder#4
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id, root_id, summary)
values (nextval('seq_object_id'), now(), true, true, now(), 'test-child', 1, 1, 1, 1, 4, 1, 1, 10,'<summary>child@archive</summary>');

-- #12 test object for deletion,  default acl in deletion folder#5 -> currently used as target for deleteLink-Test
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'linked-to-me', 1, 1, 1, 1, 5, 1, 1);

-- #13 test object for create link,  default acl in creation folder#6 
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'linked-to-me-2', 1, 1, 1, 1, 6, 1, 1);

-- #14 test object for create link in folder#7 (where doe has no browse permission)  
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'linked-to-me-3', 1, 1, 1, 1, 7, 1, 1);

-- #15 test object for update link to object,  default acl in creation folder#6  
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'linked-to-me-2', 1, 1, 1, 1, 6, 1, 1);

-- #16 test object for getSummaries,  reviewer acl in creation folder#6  
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id, summary)
values (nextval('seq_object_id'), now(), true, true, now(), '7th-sum-of-a-7th-sum', 1, 1, 1, 1, 6, 1, 2,'<sum>7</sum>');

-- #17 test object for setSummary,  reviewer acl in creation folder#6  
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id, summary)
values (nextval('seq_object_id'), now(), true, true, now(), 'summ-summ-summ', 1, 1, 1, 1, 6, 1, 2,'no summary');

-- #18 test object for setSummaryNoPermission,  default acl in creation folder#6  
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id, summary)
values (nextval('seq_object_id'), now(), true, true, now(), 'no-perm-summary', 1, 1, 1, 1, 6, 1, 1,'no summary');

-- #19 test object for relations (as rightId) in creation folder #6
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'right-related', 1, 1, 1, 1, 6, 1, 1);

-- #20 test object for relations (as leftId) in creation folder #6
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'left-related', 1, 1, 1, 1, 6, 1, 1);

-- #21 test object for create-delete relations (target: osd#20) in creation folder #6
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'related', 1, 1, 1, 1, 6, 1, 1);

-- #22 test object for setContent/getContent in creation folder #6
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'content-holder', 1, 1, 1, 1, 6, 1, 2);

-- #23 test object for setContent/getContent without write permission for reviewers in creation folder #6
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'content-holder', 1, 1, 1, 1, 6, 1, 1);

-- #24 test object for getContent without read/write permission for reviewers in creation folder #6
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id, locker_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'content-holder', 1, 1, 1, 1, 6, 1, 1, 2);

-- #25 empty test object for getContent test in creation folder #6
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'content-holder', 1, 1, 1, 1, 6, 1, 2);

-- #26 empty test object for lock/unlock test in creation folder #6
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'lock-me', 1, 1, 1, 1, 6, 1, 2);

-- #27 empty test object without permissions for lock/unlock and attachLifecycle/getNextStates tests in creation folder #6
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'u-no-lock-me', 1, 1, 1, 1, 6, 1, 7);

-- #28 empty test object to test lifecycle state changes (in creation folder #6)
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'lifecycle-test', 1, 1, 1, 1, 6, 1, 2);

-- #29 empty test object to test detach lifecycle  (in creation folder #6)
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'lifecycle-detach-test', 1, 1, 1, 1, 6, 1, 2);

-- #30 empty test object to test attaching ChangeAclState.published in lifecycle  (in creation folder #6)
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'acl-changing-lifecycle-test', 1, 1, 1, 1, 6, 1, 2);

-- #31 empty test object to test ChangeAclState in lifecycle  (in creation folder #6)
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'acl-changing-lifecycle-test', 1, 1, 1, 1, 6, 1, 2);

-- #32 empty test object to test change state with FailState in lifecycle  (in creation folder #6)
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'fail-state-lifecycle-test', 1, 1, 1, 1, 6, 1, 2);

-- #33 empty test object to test attach FailState in lifecycle  (in creation folder #6)
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'fail-state-attach-test', 1, 1, 1, 1, 6, 1, 2);

-- #34 empty test object to test unhappy path in change lifecycle  state (in creation folder #6)
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'fail-state-attach-test', 1, 1, 1, 1, 6, 1, 2);

-- #35 empty test object to test getNextState lifecycle  state (in creation folder #6)
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'has-lifecycle-state-authoring', 1, 1, 1, 1, 6, 1, 2);

-- #36 empty test object with osd_meta for getMeta test
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'has-meta', 1, 1, 1, 1, 6, 1, 2);

-- #37 object with no permissions, used in getMeta and createMeta test
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'no-read-custom-meta-permission', 1, 1, 1, 1, 1, 1, 5);

-- #38 empty test object for createMeta
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'create-me-a-meta', 1, 1, 1, 1, 6, 1, 2);

-- #39 empty test object for createMeta with existing license metaset
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'has-a-license-meta-already', 1, 1, 1, 1, 6, 1, 2);

-- #40 empty test object for createMeta with existing non-unique comment metaset
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'has-a-comment-meta-already', 1, 1, 1, 1, 6, 1, 2);

-- #41 empty test object for deleteMeta with one metaset by name, one by id
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'has-deletable-meta', 1, 1, 1, 1, 6, 1, 2);

-- #42 empty test object for deleteMeta without permission
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'has-deletable-meta-but-no-permission', 1, 1, 1, 1, 6, 1, 5);

-- #43 test object for unlocked setContent/getContent in creation folder #6
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'content-holder', 1, 1, 1, 1, 6, 1, 2);

-- #44 test object for version request in creation folder #6 (no permission)
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'version-test', 1, 1, 1, 1, 6, 1, 1);

-- #45 test object for version request in creation folder #6
-- state_id 1 is set below via update.
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'version-test', 1, 1, 1, 1, 6, 1, 2);

-- #46 test object for version request in creation folder #6
-- state_id 4 is set below via update to test failed state.
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'version-test', 1, 1, 1, 1, 6, 1, 2);

-- #47 test object for version label tests in creation folder #6
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'test-version-numbering', 1, 1, 1, 1, 6, 1, 2);

-- #48 test object for version test with metadata in creation folder #6
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'version-with-metadata', 1, 1, 1, 1, 6, 1, 2);

-- #49 test object for delete happy path (delete a single OSD)
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_object_id'), now(), true, true, now(), 'delete-me', 1, 1, 1, 1, 6, 1, 2);


-- #1 link to osd #1 with default acl (#1)
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_link_id'), 'OBJECT',  1,1,1,1);

-- #2 link to folder #2 with default acl (#1)
insert into links(id, type,owner_id,acl_id,parent_id,folder_id)
values (nextval('seq_link_id'), 'FOLDER',  1,1,1,2);

-- #3 link to osd #1 with no permission except owner acl (#1)
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_link_id'), 'OBJECT',  1,5,1,1);

-- #4 link to osd #7 with no-permission.acl (#7)
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_link_id'), 'OBJECT',  1,1,1,7);

-- #5 link to folder #3 with no-permission. acl (#7)
insert into links(id, type,owner_id,acl_id,parent_id,folder_id)
values (nextval('seq_link_id'), 'FOLDER',  1,1,1,3);

-- deprecated (removed resolvers):
-- -- #6 link to osd #8 with latest_head resolver - should return osd#9 when link is queried.
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_link_id'), 'OBJECT', 1,1,1,8);

-- deprecated (removed resolvers):
-- #7 link to osd #10 with latest_head  in folder 'archive' #7 - should return osd#11 when link is queried.
-- used in getObjectsByFolderId
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_link_id'), 'OBJECT', 1,1,4,10);

-- #8 link to osd #11 with fixed resolver - should return osd#11 when link is queried.
-- used in getObjectsByFolderId, but with no_permission.acl #7, should not be seen by normal user.
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_link_id'), 'OBJECT',  1,7,4,11);

-- #9 link to osd#12 for deletion tests: default acl, deletion allowed
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_link_id'), 'OBJECT',  1,1,1,12);

-- #10 link to folder#5 for deletion tests: default acl, deletion allowed
insert into links(id, type,owner_id,acl_id,parent_id,folder_id)
values (nextval('seq_link_id'), 'FOLDER',  1,1,1,5);

-- #11 link to osd#12 for deletion tests: no_permission-acl, browse not allowed
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_link_id'), 'OBJECT',  1,7,1,12);

-- #12 link to osd#12 for deletion tests: reviewer-acl#2, browse allowed, deletion not allowed
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_link_id'), 'OBJECT',  1,7,1,12);

-- #13 link to folder#5 for deletion tests: reviewer acl#2, browse_folder allowed, deletion not allowed
insert into links(id, type,owner_id,acl_id,parent_id,folder_id)
values (nextval('seq_link_id'), 'FOLDER',  1,2,1,5);

-- #14 link to osd#13 for testing owner browse permission: only-owner-acl#5, link owned by doe.
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_link_id'), 'OBJECT',  2, 5, 1, 13);

-- #15 link to osd#13 for testing delete link with just owner permission: only-owner-acl#5, link owned by doe.
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_link_id'), 'OBJECT',  2, 5, 1, 13);

-- #16 link to folder#5 for testing delete link with just owner permission: only-owner-acl#5, link owned by doe.
insert into links(id, type,owner_id,acl_id,parent_id, folder_id)
values (nextval('seq_link_id'), 'FOLDER',  2, 5, 1, 5);

-- #17 link to osd#13 with no-permission-acl #7 to test updateLink without browse permission
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_link_id'), 'OBJECT',  2, 7, 1, 13);

-- #18 link to osd#13 with reviewer-acl # to test updateLink.setAcl without setAcl permission
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_link_id'), 'OBJECT',  1, 14, 1, 13);

-- #19 link to osd#13 with set-acl.allowed-acl #11 to test updateLink.setAcl with setAcl permission
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_link_id'), 'OBJECT',  1, 11, 1, 13);

-- #20 link to folder #5, will try to change this link to folder #3 (unseen folder) 
insert into links(id, type,owner_id,acl_id,parent_id,folder_id)
values (nextval('seq_link_id'), 'FOLDER',  1, 11, 1, 5);

-- #21 link to osd#13 with set-acl.allowed-acl #11 to test non-acl updates
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_link_id'), 'OBJECT',  1, 11, 1, 13);

-- #22 link to folder #5, will try to change this link to osd#13 
insert into links(id, type,owner_id,acl_id,parent_id,folder_id)
values (nextval('seq_link_id'), 'FOLDER',  1, 11, 1, 5);

-- deprecated (removed resolvers):
-- -- #23 link to osd#13, will try to change this link to folder#6 
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_link_id'), 'OBJECT', 1, 11, 1, 13);

-- deprecated (removed resolvers):
-- #24 link to osd#13, will try to change this resolver to fixed 
insert into links(id, type,owner_id,acl_id,parent_id,osd_id)
values (nextval('seq_link_id'), 'OBJECT', 1, 11, 1, 13);

-- #1 add format: xml
insert into formats(id, contenttype, extension, name, default_object_type_id)
VALUES (nextval('seq_format_id'),'application/xml','xml', 'xml', 1);

-- #2 format: text/plain
insert into formats(id, contenttype, extension, name, default_object_type_id)
VALUES (nextval('seq_format_id'),'text/plain','txt', 'plaintext', 1);

-- #1 relation_type: protect all & clone always
insert into relation_types (id, left_object_protected, name, right_object_protected,
                           clone_on_right_copy, clone_on_left_copy, clone_on_left_version, clone_on_right_version                           )
VALUES (nextval('seq_relation_type_id'), true, 'all-protector', true,
        true, true, true, true);

-- #2 relation_type: protect nothing & clone never
insert into relation_types (id, left_object_protected, name, right_object_protected,
                           clone_on_right_copy, clone_on_left_copy, clone_on_left_version, clone_on_right_version                           )
VALUES (nextval('seq_relation_type_id'), true, 'unprotected', true,
        false, false, false, false);

-- #1 uiLanguage: de
insert into ui_languages (id,iso_code) values (nextval('seq_ui_language_id'), 'DE');

-- #2 uiLanguage: en
insert into ui_languages (id,iso_code) values (nextval('seq_ui_language_id'), 'EN');

-- #1 index_item
insert into index_items(id, fieldname, for_content, for_metadata, for_sys_meta, multiple_results,
   name, search_string, search_condition, store_field, index_type)
values (nextval('seq_index_item_id'), 'acl', false,false,true,false,'index.acl',
  '/sysMeta/object/aclId', 'true()',true, 'DEFAULT_INDEXER'
);

-- #1 relation: type 1 relation
insert into relations(id,left_id, right_id, type_id, metadata) VALUES (nextval('seq_relation_id'),20,19,1,'<meta>important</meta>' );;
-- #2 relation: type 2 relation
insert into relations(id,left_id, right_id, type_id, metadata) VALUES (nextval('seq_relation_id'),19,20,2,'<meta>ignore</meta>' );


-- #1 lifecycle review.lc (lifecycle_state #1 will be configured as default state, see below).
insert into lifecycles(id, name, default_state_id) VALUES (nextval('seq_lifecycle_id'), 'review.lc',null);
-- #2 lifecycle render.lc (without any states, to test missing default state )
insert into lifecycles(id, name, default_state_id) VALUES (nextval('seq_lifecycle_id'), 'render.lc',null);
-- #3 lifecycle acl.lc (to test with ChangeAclState)
insert into lifecycles(id, name, default_state_id) VALUES (nextval('seq_lifecycle_id'), 'acl.lc',null);
-- #4 lifecycle fail.lc (to test failed attach / changeState )
insert into lifecycles(id, name, default_state_id) VALUES (nextval('seq_lifecycle_id'), 'fail.lc',null);

-- #1 lifecycle_state of lc #1
insert into lifecycle_states (id, name, config, state_class, life_cycle_id, copy_state_id)
values (nextval('seq_lifecycle_state_id'), 'newRenderTask', '<config>' ||
                                                             '<properties><property><name>render.server.host</name><value>localhost</value></property></properties>' ||
                                                             '<nextStates/>' ||
                                                             '</config>', 'com.dewarim.cinnamon.lifecycle.NopState', 2, currval('seq_lifecycle_state_id'));
update lifecycles set default_state_id=1 where id=1;

-- #2 first lifecycle_state of lc 3 with ChangeAclState
insert into lifecycle_states(id, name, config, state_class, life_cycle_id, copy_state_id )
    values (nextval('seq_lifecycle_state_id'), 'authoring', '<config>' ||
                                                             '<properties><property><name>aclName</name><value>reviewers.acl</value></property></properties>' ||
                                                             '<nextStates><name>published</name></nextStates>' ||
                                                             '</config>', 'com.dewarim.cinnamon.lifecycle.ChangeAclState', 3, currval('seq_lifecycle_state_id'));
update lifecycles set default_state_id=1 where id=3;
update objects set state_id=2 where id=31;
update objects set state_id=2 where id=35;
update objects set state_id=1 where id=45;

-- #3 second lifecycle_state of lc 3 with ChangeAclState
insert into lifecycle_states(id, name, config, state_class, life_cycle_id, copy_state_id )
    values (nextval('seq_lifecycle_state_id'), 'published', '<config>' ||
                                                             '<properties><property><name>aclName</name><value>_default_acl</value></property></properties>' ||
                                                             '<nextStates><name>authoring</name></nextStates>' ||
                                                             '</config>', 'com.dewarim.cinnamon.lifecycle.ChangeAclState', 3, currval('seq_lifecycle_state_id'));

-- #4 failState for lc 4
insert into lifecycle_states(id, name, config, state_class, life_cycle_id, copy_state_id )
values (nextval('seq_lifecycle_state_id'), 'failed', '<config></config>', 'com.dewarim.cinnamon.lifecycle.FailState', 4, currval('seq_lifecycle_state_id'));
-- osd#32 with FailState lifecycle state: should fail on state.exit()
update objects set state_id=4 where id=32;
update objects set state_id=4 where id=46;

-- #1 metaset type 'comment'
insert into metaset_types(id, name, is_unique) VALUES (nextval('seq_metaset_type_id'), 'comment', false );
-- #2 metaset type 'license' (note: in production, this may be a better stored in a relation)
insert into metaset_types(id, name, is_unique) VALUES (nextval('seq_metaset_type_id'), 'license', true);

-- #1 osd_meta with metaset_type#1 comment #1 for osd#36
insert into osd_meta(id, osd_id, content, type_id)
values (nextval('seq_osd_meta_id'), 36, '<metaset><p>Good Test</p></metaset>', 1);

-- #2 osd meta with metaset_type#2 license for osd#36
insert into osd_meta(id, osd_id, content, type_id)
values (nextval('seq_osd_meta_id'), 36, '<metaset><license>GPL</license></metaset>', 2);

-- #3 osd_meta
insert into osd_meta(id,osd_id,content,type_id)
values (nextval('seq_osd_meta_id'), 39, '<metaset><licesne>AGPL</license></metaset>',2);

-- #4 osd_meta
insert into osd_meta(id,osd_id,content,type_id)
values (nextval('seq_osd_meta_id'), 40, '<metaset><comment>foo</comment></metaset>',1);

-- #5 osd_meta for osd#41, deletion by name (and list)
insert into osd_meta(id,osd_id,content,type_id)
values (nextval('seq_osd_meta_id'), 41, '<metaset><comment>bar</comment></metaset>',1);

-- #6 osd_meta for osd#41, deletion by name (and list)
insert into osd_meta(id,osd_id,content,type_id)
values (nextval('seq_osd_meta_id'), 41, '<metaset><comment>fuzz</comment></metaset>',1);

-- #7 osd_meta for osd#41, deletion by id
insert into osd_meta(id,osd_id,content,type_id)
values (nextval('seq_osd_meta_id'), 41, '<license>MPL</license>>',2);

-- #8 osd_meta for osd#42, deletion without permission
insert into osd_meta(id,osd_id,content,type_id)
values (nextval('seq_osd_meta_id'), 42, '<license>LGPL</license>>',2);


-- #1 folder_meta
insert into folder_meta(id, folder_id, content, type_id)
values (nextval('seq_folder_meta_id'), 16, '<metaset><p>Good Folder Meta Test</p></metaset>', 1);

-- #2 folder_meta - unique metaset_type
insert into folder_meta(id, folder_id, content, type_id)
values (nextval('seq_folder_meta_id'), 18, 'duplicate license meta (forbidden)', 2);

-- #3 folder_meta - non-unique metaset_type
insert into folder_meta(id, folder_id, content, type_id)
values (nextval('seq_folder_meta_id'), 19, 'duplicate comment (allowed)', 1);

-- #4 folder_meta - no permission (to delete)
insert into folder_meta(id, folder_id, content, type_id)
values (nextval('seq_folder_meta_id'), 20, 'do not delete', 1);

-- #5 folder_meta - comment #1 for deleteMeta
insert into folder_meta(id, folder_id, content, type_id)
values (nextval('seq_folder_meta_id'), 21, 'comment #2', 1);

-- #6 folder_meta - comment #2 for deleteMeta
insert into folder_meta(id, folder_id, content, type_id)
values (nextval('seq_folder_meta_id'), 21, 'comment #2', 1);

-- #7 folder_meta - license #1 for deleteMeta
insert into folder_meta(id, folder_id, content, type_id)
values (nextval('seq_folder_meta_id'), 21, '<license>public domain</license>', 2);


