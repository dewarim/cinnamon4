-------------------------
--- table definitions ---
-------------------------

-- users --
CREATE TABLE users (
  id BIGINT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  pwd VARCHAR(255) NOT NULL,
  obj_version int NOT NULL DEFAULT 0,
  login_type VARCHAR(64) NOT NULL DEFAULT 'CINNAMON',
  activated BOOLEAN NOT NULL DEFAULT TRUE 
);

create SEQUENCE seq_user_id start with 1;

-- sessions --
create table sessions
(
  id bigint PRIMARY KEY,
  expires timestamp,
  ticket varchar(255),
  username varchar(128),
  ui_language_id bigint,
  user_id bigint not null
);

create SEQUENCE seq_session_id start with 1;

-- acls --
create table acls(
  id bigint PRIMARY KEY,
  name varchar(255) UNIQUE
);

create SEQUENCE seq_acl_id start with 1;

-- folder types --
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

create sequence seq_folder_type_id start with 1;

-- folders --
create table folders
(
  id bigint not null
    constraint folders_pkey
    primary key,
  name varchar(128) not null,
  obj_version bigint,
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

create sequence seq_folder_id start with 1;


-- objtypes --
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

create sequence seq_obj_type_id start with 1;

-- formats --
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

create sequence seq_format_id start with 1;

-- languages --
create table languages
(
  id bigint not null
    constraint languages_pkey
    primary key,
  iso_code varchar(32) not null
    constraint languages_iso_code_key
    unique,
  obj_version bigint,
  metadata text default '<meta />' not null
);

create sequence seq_language_id start with 1;

-- lifecycles --
create table lifecycles
(
  id bigint not null
    constraint lifecycles_pkey
    primary key,
  name varchar(128) not null
    constraint lifecycles_name_key
    unique,
  default_state_id bigint,

  obj_version bigint default 0
);

create sequence seq_lifecycle_id start with 1;

-- lifecycle_state --
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
  life_cycle_state_for_copy_id bigint
    constraint lifecycle_state_for_copy_fk
    references lifecycle_states,
  obj_version bigint default 0
);

alter table lifecycles add constraint fk_default_state_id FOREIGN KEY 
  (default_state_id) references lifecycle_states(id);

create sequence seq_lifecycle_states_id start with 1;

-- objects --
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
  summary text default '<summary />' not null
);

create sequence seq_objects_id start with 1;


-- links --
create table links
(
  id bigint not null
    constraint links_pk
    primary key,
  type varchar(127) not null,
  resolver varchar(127) not null,
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

create sequence seq_links_id start with 1;


-- groups --
create table groups(
  id bigint PRIMARY KEY,
  name varchar(255) UNIQUE,
  group_of_one boolean default false not null,
  parent_id bigint constraint fk_group_parent_id references groups
);
create SEQUENCE seq_groups start with 1;

-- aclentries --

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

create sequence seq_acl_entries_id start with 1;


-- group_users --
create table group_users(
  user_id BIGINT NOT NULL ,
  group_id BIGINT NOT NULL 
);

create UNIQUE INDEX  group_users_user_group
  on group_users(user_id,group_id)
  ;

-- permissions --
create table permissions
(
  id bigint not null
    constraint permissions_pkey
    primary key,
  name varchar(128) not null
    constraint permissions_name_key
    unique
);
create sequence seq_permission_id start with 1;

-- aclentry_permissions --
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

create sequence seq_aclentry_permission_id start with 1;

--------------------------
--- insert test data:  ---
-- -----------------------

INSERT INTO users(id,name,pwd,activated) VALUES ( nextval('seq_user_id'),'admin','$2a$10$VG9LCf6h/Qwb7Y.pafHkaepdnJNgFZUzzuMV3EcyvLbKnueHQ4IW.',true);
INSERT INTO users(id,name,pwd,activated) VALUES ( nextval('seq_user_id'),'doe','$2a$10$VG9LCf6h/Qwb7Y.pafHkaepdnJNgFZUzzuMV3EcyvLbKnueHQ4IW.',true);

insert into acls(id,name) values(nextval('seq_acl_id'),'_default_acl'); -- 1
insert into acls(id,name) values(nextval('seq_acl_id'),'reviewers.acl'); -- 2
insert into acls(id,name) values(nextval('seq_acl_id'),'delete.me.acl'); -- 3
insert into acls(id,name) values(nextval('seq_acl_id'),'rename.me.acl'); -- 4
insert into acls(id,name) values(nextval('seq_acl_id'),'no-permissions-except-owner.acl'); -- 5
insert into acls(id,name) values(nextval('seq_acl_id'),'no-permissions-except-everyone.acl'); -- 6

insert into folder_types(id,name) values(nextval('seq_folder_type_id'),'_default_folder_type');

insert into folders values(nextval('seq_folder_id'),'root',0,1,1,null,1,false,'<summary/>');

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

insert into permissions values (nextval('seq_permission_id'),'_browse');
insert into permissions values (nextval('seq_permission_id'),'_browse_folder');
insert into permissions values (nextval('seq_permission_id'),'_create_folder');
insert into permissions values (nextval('seq_permission_id'),'_create_inside_folder');
insert into permissions values (nextval('seq_permission_id'),'_delete_folder');
insert into permissions values (nextval('seq_permission_id'),'_delete_object');
insert into permissions values (nextval('seq_permission_id'),'_edit_folder');
insert into permissions values (nextval('seq_permission_id'),'_lock');
insert into permissions values (nextval('seq_permission_id'),'_move');
insert into permissions values (nextval('seq_permission_id'),'_read_object_content');
insert into permissions values (nextval('seq_permission_id'),'_read_object_custom_metadata');
insert into permissions values (nextval('seq_permission_id'),'_read_object_metadata');
insert into permissions values (nextval('seq_permission_id'),'_set_acl');
insert into permissions values (nextval('seq_permission_id'),'_version');
insert into permissions values (nextval('seq_permission_id'),'_write_object_content');
insert into permissions values (nextval('seq_permission_id'),'_write_object_custom_metadata');
insert into permissions values (nextval('seq_permission_id'),'_write_object_sysmeta');

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

insert into languages values (nextval('seq_language_id'),'DE',0,'<meta/>');

insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id, summary)
values (nextval('seq_objects_id'), now(), true, true, now(), 'test-1', 1, 1, 1, 1, 1, 1, 1, '<summary>sum of sum</summary>');
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_objects_id'), now(), true, true, now(), 'test-2', 1, 1, 1, 1, 1, 1, 1);
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_objects_id'), now(), true, true, now(), 'test-3', 1, 1, 1, 1, 1, 1, 1);

insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_objects_id'), now(), true, true, now(), 'unbrowsable-test', 1, 1, 1, 1, 1, 1, 5);

-- acl #5 has no permission for user Doe, but allows owners to view the item, so Doe should see it anyway.
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_objects_id'), now(), true, true, now(), 'owned-by-doe', 1, 1, 1, 2, 1, 1, 5);

-- acl #6 has no permission for user Doe, but allows "_everyone" to view the item, so Doe should see it anyway.
insert into objects (id, created, latest_branch, latest_head, modified, name, creator_id, language_id, modifier_id,
                     owner_id, parent_id, type_id, acl_id)
values (nextval('seq_objects_id'), now(), true, true, now(), 'acl-for-everyone', 1, 1, 1, 1, 1, 1, 6);