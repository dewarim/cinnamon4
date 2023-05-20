-------------------------
--- table definitions ---
-------------------------
drop sequence if exists seq_folder_and_object_ids;
create sequence seq_folder_and_object_ids start with 1;
drop sequence if exists seq_object_id;
drop sequence if exists seq_folder_id;

-- users --
drop table if exists users cascade;
CREATE TABLE users (
  id BIGINT PRIMARY KEY,
  name VARCHAR(255) NOT NULL unique,
  pwd VARCHAR(255) NOT NULL,
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
-- drop sequence if exists seq_folder_id;
-- create sequence seq_folder_id start with 1;


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
  default_object_type_id bigint
    constraint defaultobjecttype
    references object_types,
 index_mode varchar(255) not null default 'NONE'
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
  summary text default '<summary />' not null,
  content_hash varchar(128)
);
-- drop sequence if exists seq_object_id;
-- create sequence seq_object_id start with 1;

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


drop sequence if exists seq_change_trigger_id;
create sequence seq_change_trigger_id;

drop table if exists change_triggers;
create table change_triggers
(
    id                bigint                          not null
        primary key,
    name              varchar(255) unique             not null,
    active            boolean                         not null,
    ranking           integer                         not null,
    action            varchar(255)                    not null,
    pre_trigger       boolean                         not null,
    post_trigger      boolean                         not null,
    copy_file_content boolean                         not null,
    config            text default '<config />'::text not null,
    controller        varchar(255)                    not null,
    trigger_type      varchar(255)                    not null
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
insert into group_users(id, user_id, group_id) VALUES(nextval('seq_group_user_id'),1,1);
-- #2 admin is member of admin group:
insert into group_users(id, user_id, group_id) VALUES(nextval('seq_group_user_id'),1,2);
-- #3 doe is member of his own group:
insert into group_users(id, user_id, group_id) VALUES (nextval('seq_group_user_id'),2,4);
-- #4 doe is member of reviewers:
insert into group_users(id, user_id, group_id) values (nextval('seq_group_user_id'),2,5);

-- #1 link superusers group to default acl:
insert into acl_groups(id,acl_id,group_id) values (nextval('seq_acl_group_id'),1,1);
-- #2 admin's group is connected to reviewers acl:
insert into acl_groups(id,acl_id,group_id) values (nextval('seq_acl_group_id'),2,2);
-- #3 doe's group is connected to reviewers.acl
insert into acl_groups(id,acl_id,group_id) values (nextval('seq_acl_group_id'),2,4);
-- #4 reviewers are connected to reviewers acl:
insert into acl_groups(id,acl_id,group_id) values (nextval('seq_acl_group_id'),2,5);

insert into permissions values (nextval('seq_permission_id'),'node.browse');
insert into permissions values (nextval('seq_permission_id'),'folder.create.folder');
insert into permissions values (nextval('seq_permission_id'),'folder.create.object');
insert into permissions values (nextval('seq_permission_id'),'node.delete');
insert into permissions values (nextval('seq_permission_id'),'node.name.write');
insert into permissions values (nextval('seq_permission_id'),'node.type.write');
insert into permissions values (nextval('seq_permission_id'),'object.lock');
insert into permissions values (nextval('seq_permission_id'),'object.language.write');
insert into permissions values (nextval('seq_permission_id'),'link.target.write');
insert into permissions values (nextval('seq_permission_id'),'parent_folder.write');
insert into permissions values (nextval('seq_permission_id'),'object.content.read');
insert into permissions values (nextval('seq_permission_id'),'node.metadata.read');
insert into permissions values (nextval('seq_permission_id'),'node.sysmetadata.read');
insert into permissions values (nextval('seq_permission_id'),'node.acl.write');
insert into permissions values (nextval('seq_permission_id'),'node.summary.write');
insert into permissions values (nextval('seq_permission_id'),'node.owner.write');
insert into permissions values (nextval('seq_permission_id'),'object.version');
insert into permissions values (nextval('seq_permission_id'),'object.content.write');
insert into permissions values (nextval('seq_permission_id'),'node.metadata.write');
insert into permissions values (nextval('seq_permission_id'),'relation.child.add');
insert into permissions values (nextval('seq_permission_id'),'relation.parent.add');
insert into permissions values (nextval('seq_permission_id'),'relation.child.remove');
insert into permissions values (nextval('seq_permission_id'),'relation.parent.remove');
insert into permissions values (nextval('seq_permission_id'),'object.lifecyclestate.write');

-- #1 default folder type
insert into folder_types(id,name) values(nextval('seq_folder_type_id'),'_default_folder_type');

-- #1 root folder
insert into folders(id,name,acl_id,owner_id,parent_id,type_id)
values(nextval('seq_folder_and_object_ids'),'root',1,1,null,1);

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

-- #1 add format: xml
insert into formats(id, contenttype, extension, name, default_object_type_id, index_mode)
VALUES (nextval('seq_format_id'),'application/xml','xml', 'xml', 1, 'XML');

-- #2 format: text/plain
insert into formats(id, contenttype, extension, name, default_object_type_id, index_mode)
VALUES (nextval('seq_format_id'),'text/plain','txt', 'plaintext', 1, 'PLAIN_TEXT');
-- #3 format: image/png
insert into formats(id, contenttype, extension, name, default_object_type_id, index_mode)
VALUES (nextval('seq_format_id'),'image/png','png', 'image.png', 1, 'TIKA');

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
insert into index_items(id, fieldname, multiple_results,
   name, search_string, search_condition, store_field, index_type)
values (nextval('seq_index_item_id'), 'acl', false,'index.acl',
  '/sysMeta/object/aclId', 'true()',true, 'DEFAULT_INDEXER'
);
insert into index_items(id, fieldname, multiple_results,
   name, search_string, search_condition, store_field, index_type)
values (nextval('seq_index_item_id'), 'xml_content', false,'xml content',
  '/objectSystemData/content/descendant::*', 'boolean(string-length(/objectSystemData/formatId[text()])>0)',true, 'DESCENDING_STRING_INDEXER'
);
insert into index_items(id, fieldname, multiple_results,
   name, search_string, search_condition, store_field, index_type)
values (nextval('seq_index_item_id'), 'xml_content', false,'xml content:tika',
  '/objectSystemData/metasets/meta/content/descendant::*', 'true()',true, 'DESCENDING_STRING_INDEXER'
);
insert into index_items(id, fieldname, multiple_results,
                        name, search_string, search_condition, store_field, index_type)
values (nextval('seq_index_item_id'), 'is_latest_branch', false,'latest branch item',
        '/objectSystemData/latestBranch', 'boolean(string-length(/objectSystemData/latestBranch[text()])>0)'
        ,false, 'BOOLEAN_INDEXER'
       );
insert into index_items(id, fieldname, multiple_results,
   name, search_string, search_condition, store_field, index_type)
values (nextval('seq_index_item_id'), 'osd_name', false,'name item',
  '/objectSystemData/name', 'true()',false, 'COMPLETE_STRING_INDEXER'
);

insert into index_items(id, fieldname, multiple_results,
   name, search_string, search_condition, store_field, index_type)
values (nextval('seq_index_item_id'), 'element_names', false,'element name item',
  '/objectSystemData/content', 'boolean(string-length(/objectSystemData/formatId[text()])>0)',
        false, 'ELEMENT_NAME_INDEXER'
);
insert into index_items(id, fieldname, multiple_results,
   name, search_string, search_condition, store_field, index_type)
values (nextval('seq_index_item_id'), 'osd_created', false,'created date item',
  '/objectSystemData/created', 'true()',false, 'DATE_INDEXER'
);
insert into index_items(id, fieldname, multiple_results,
   name, search_string, search_condition, store_field, index_type)
values (nextval('seq_index_item_id'), 'date_time', false,'created datetime item',
  '/objectSystemData/created', 'true()',false, 'DATE_TIME_INDEXER'
);
insert into index_items(id, fieldname, multiple_results,
   name, search_string, search_condition, store_field, index_type)
values (nextval('seq_index_item_id'), 'osd_id', false,'created id item',
  '/objectSystemData/id', 'true()',false, 'INTEGER_INDEXER'
);

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
                                                             '<properties><property><name>aclName</name><value>creation.acl</value></property></properties>' ||
                                                             '<nextStates><name>published</name></nextStates>' ||
                                                             '</config>', 'com.dewarim.cinnamon.lifecycle.ChangeAclState', 3, currval('seq_lifecycle_state_id'));
update lifecycles set default_state_id=1 where id=3;

-- #3 second lifecycle_state of lc 3 with ChangeAclState
insert into lifecycle_states(id, name, config, state_class, life_cycle_id, copy_state_id )
    values (nextval('seq_lifecycle_state_id'), 'published', '<config>' ||
                                                             '<properties><property><name>aclName</name><value>_default_acl</value></property></properties>' ||
                                                             '<nextStates><name>authoring</name></nextStates>' ||
                                                             '</config>', 'com.dewarim.cinnamon.lifecycle.ChangeAclState', 3, currval('seq_lifecycle_state_id'));

-- #4 failState for lc 4
insert into lifecycle_states(id, name, config, state_class, life_cycle_id, copy_state_id )
values (nextval('seq_lifecycle_state_id'), 'failed', '<config></config>', 'com.dewarim.cinnamon.lifecycle.FailState', 4, currval('seq_lifecycle_state_id'));

-- #1 metaset type 'comment'
insert into metaset_types(id, name, is_unique) VALUES (nextval('seq_metaset_type_id'), 'comment', false );
-- #2 metaset type 'license' (note: in production, this may be a better stored in a relation)
insert into metaset_types(id, name, is_unique) VALUES (nextval('seq_metaset_type_id'), 'license', true);
-- tika metaset type
insert into metaset_types(id, name, is_unique) VALUES (nextval('seq_metaset_type_id'), 'tika', true);

insert into change_triggers(id, name, active, ranking, action, pre_trigger, post_trigger, copy_file_content, config,
                            controller, trigger_type)
values (nextval('seq_change_trigger_id'), 'echo-test', true, 1,'echo',true,true, true,'<config><url>http://localhost:9090/cinnamon/test/echo</url></config>','test','MICROSERVICE');
