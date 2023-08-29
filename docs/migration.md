# Migration Guide

## From Cinnamon 3.7

### Changes in behavior

* created & modified dates on OSDs are now formatted as yyyy-MM-dd'T'HH:mm:ssZ (2021-09-05T11:27:39+0000)

### Database changes

Make sure you have a complete backup of database & file system before starting the migration.
It's recommended to use a copy of production for testing.

    ALTER TABLE users ADD login_type VARCHAR(64) DEFAULT 'CINNAMON' NOT NULL;
    ALTER TABLE users DROP COLUMN account_expired;
    ALTER TABLE users DROP COLUMN obj_version;
    ALTER TABLE users RENAME COLUMN account_locked TO locked;
    ALTER TABLE users RENAME COLUMN language_id TO ui_language_id;
    ALTER TABLE users ALTER COLUMN change_tracking SET DEFAULT true;
    ALTER TABLE users DROP COLUMN description;
    
    -- note: this will set the created date of all existing folders to now()
    ALTER TABLE folders ADD created TIMESTAMP DEFAULT NOW();
    
    -- links no longer have an internal version - if two people update the same link,
    -- last one wins.
    ALTER TABLE links DROP COLUMN version;
    
    -- transformers are not used anywhere, a legacy idea from Cinnamon 2
    DROP TABLE transformers;
    
    -- customtables are no longer used, should be implemented via microservies
    DROP TABLE customtables;
    
    -- update relationtypes:    
    alter table relationtypes drop column left_resolver_id;
    alter table relationtypes drop column right_resolver_id;
    alter table relationtypes drop column obj_version;
    
    -- 
    alter table folder_types drop column obj_version;
    alter table folder_types drop column config;
    
    --
    alter table ui_languages drop column obj_version;
    
    --
    alter table languages drop column obj_version;
    alter table languages drop column metadata;

    --
    alter table metaset_types drop column obj_version;
    alter table metaset_types drop column config;
    alter table metaset_types add column is_unique boolean not null default true;
    
    --
    alter table index_items drop column obj_version;
    alter table index_items drop column systemic;
    alter table index_items drop column index_group_id;
    drop table index_groups;
    
    --   
    alter table config_entries drop column obj_version;
    -- note: this one needs manual work to check for config_entries containing <public>true</public> in the config field.
    alter table config_entries add column public_visibility boolean not null default false;
    
    --
    alter table lifecycle_states drop column obj_version;
    
    --
    create table lifecycle_state_to_copy_state(
      lifecycle_state_id bigint references lifecycle_states(id) unique,
      copy_state_id bigint references lifecycle_states(id)
    ); 
    insert into lifecycle_state_to_copy_state(
    select
       id,life_cycle_state_for_copy_id
      from lifecycle_states
    );
    alter table lifecycle_states drop column life_cycle_state_for_copy_id;
    

    --
    alter table objects add column content_hash varchar(128);           
    
    --
    alter table sessions drop column ui_language_id;
    alter table sessions drop column obj_version;

    -- root folder now has null parent instead of itself.
    update folders set parent_id=null where parent_id=id;
    
    -- metaset_types table:
    alter table drop column obj_version; 
    
    -- create tables: osd_meta and folder_meta
    
    create table osd_meta(
    id bigint not null
    constraint osd_meta_pkey
    primary key,
    content text not null,
    type_id int not null
    constraint fke5345bd6abf96b0c
    references metaset_types
    );
    drop sequence if exists seq_osd_meta_id;
    create sequence seq_osd_meta_id;

    create table folder_meta(
    id bigint not null
    constraint folder_meta_pkey
    primary key,
    content text not null,
    type_id int not null
    constraint fke5345bd6abf96b0c
    references metaset_types
    ):
    drop sequence if exists seq_folder_meta_id;
    create sequence seq_folder_meta_id;

    -- migrate metasets
    insert into osd_meta (id, osd_id, content, type_id)
    select m.id, o.osd_id, m.content, m.type_id
    from metasets m
           join osd_metasets o on o.metaset_id = m.id;
    
    insert into folder_meta (id, folder_id, content, type_id)
    select m.id, f.folder_id, m.content, m.type_id
    from metasets m
           join folder_metasets f on f.metaset_id = m.id;
    
    -- (optional: remove obsolete tables)
    drop table osd_metasets;
    drop table folder_metasets;
    drop table metasets;         
    
    -- remove objects.appname column:
    alter table objects drop column appname;

    -- remove sudoer/sudoable columns:
    alter table users drop column sudoer;
    alter table users drop column sudoable;
    alter table users drop column token_age;

    alter table aclentries rename to acl_groups;
    alter table aclentry_permissions rename to acl_group_permissions;
    alter table acl_group_permissions rename column aclentry_id to acl_group_id;
    alter table acl_group_permissions drop column obj_version;

    -- create per-table sequences:
    drop sequence if exists seq_user_id;
    create sequence seq_user_id start with 1;
    select setval('seq_user_id', (select MAX(id) FROM users));
    
    drop sequence if exists seq_session_id;
    create sequence seq_session_id start with 1;
    select setval('seq_session_id', (select MAX(id) FROM sessions));
    
    drop sequence if exists seq_ui_language_id;
    create sequence seq_ui_language_id start with 1;
    select setval('seq_ui_language_id', (select MAX(id) FROM ui_languages));
    
    drop sequence if exists seq_language_id;
    create sequence seq_language_id start with 1;
    select setval('seq_language_id', (select MAX(id) FROM languages));
    
    drop sequence if exists seq_acl_id;
    create sequence seq_acl_id start with 1;
    select setval('seq_acl_id', (select MAX(id) FROM acls));
    
    drop sequence if exists seq_folder_type_id;
    create sequence seq_folder_type_id start with 1;
    select setval('seq_folder_type_id', (select MAX(id) FROM folder_types));
    
    drop sequence if exists seq_folder_id;
    create sequence seq_folder_id start with 1;
    select setval('seq_folder_id', (select MAX(id) FROM folders));
    
    drop sequence if exists seq_obj_type_id;
    create sequence seq_obj_type_id start with 1;
    select setval('seq_obj_type_id', (select MAX(id) FROM objtypes));
    
    drop sequence if exists seq_format_id;
    create sequence seq_format_id start with 1;
    select setval('seq_format_id', (select MAX(id) FROM formats));
    
    drop sequence if exists seq_lifecycle_id ;
    create sequence seq_lifecycle_id start with 1;
    select setval('seq_lifecycle_id', (select MAX(id) FROM lifecycles));
    
    drop sequence if exists seq_lifecycle_state_id;
    create sequence seq_lifecycle_state_id start with 1;
    select setval('seq_lifecycle_state_id', (select MAX(id) FROM lifecycle_states));
    
    drop sequence if exists seq_object_id;
    create sequence seq_object_id start with 1;
    select setval('seq_object_id', (select MAX(id) FROM objects));
    
    drop sequence if exists seq_link_id;
    create sequence seq_link_id start with 1;
    select setval('seq_link_id', (select MAX(id) FROM links));
    
    drop sequence if exists seq_group_id;
    create sequence seq_group_id start with 1;
    select setval('seq_group_id', (select MAX(id) FROM groups));
    
    drop sequence if exists seq_acl_entry_id;
    create sequence seq_acl_group_id start with 1;
    select setval('seq_acl_group_id', (select MAX(id) FROM acl_groups));
    
    drop sequence if exists seq_permission_id;
    create sequence seq_permission_id start with 1;
    select setval('seq_permission_id', (select MAX(id) FROM permissions));
    
    drop sequence if exists seq_aclentry_permission_id;
    create sequence seq_acl_group_permission_id start with 1;
    select setval('seq_acl_group_permission_id', (select MAX(id) FROM acl_group_permissions));
    
    drop sequence if exists seq_relationtype_id;
    create sequence seq_relationtype_id start with 1;
    select setval('seq_relationtype_id', (select MAX(id) FROM relationtypes));
    
    drop sequence if exists seq_relation_id;
    create sequence seq_relation_id start with 1;
    select setval('seq_relation_id', (select MAX(id) FROM relations));
    
    drop sequence if exists seq_metaset_type_id;
    create sequence seq_metaset_type_id start with 1;
    select setval('seq_metaset_type_id', (select MAX(id) FROM metaset_types));
    
    drop sequence if exists seq_index_item_id;
    create sequence seq_index_item_id start with 1;
    select setval('seq_index_item_id', (select MAX(id) FROM index_items));
    
    drop sequence if exists seq_config_entry_id;
    create sequence seq_config_entry_id start with 1;
    select setval('seq_config_entry_id', (select MAX(id) FROM config_entries ));
    
    drop sequence if exists seq_osd_meta_id;
    create sequence seq_osd_meta_id;
    select setval('seq_osd_meta_id', (select MAX(id) FROM osd_meta ));
    
    drop sequence if exists seq_folder_meta_id;
    create sequence seq_folder_meta_id;
    select setval('seq_folder_id', (select MAX(id) FROM folder_meta ));

    -- we no longer automatically create single-user personal groups
    alter table groups drop column group_of_one;
    alter table groups drop column obj_version;

    -- group_users no longer needs obj_version
    alter table group_users drop column obj_version;

    alter table acl_groups drop column obj_version;

    alter table objtypes rename to object_types;

    -- update relationtypes table for consistency
    alter table relationtypes rename to relation_types;
    alter table relation_types rename leftobjectprotected to left_object_protected;
    alter table relation_types rename rightobjectprotected to right_object_protected;

    -- 
    alter table lifecycle_states add column copy_state_id bigint references lifecycle_states;
    update lifecycle_states ls set copy_state_id=(select copy_state_id from lifecycle_state_to_copy_state where lifecycle_state_id=ls.id);
    drop table if exists lifecycle_state_to_copy_state;

    --
    insert into permissions values (nextval('seq_permission_id'),'relation.child.add');
    insert into permissions values (nextval('seq_permission_id'),'relation.parent.add');
    insert into permissions values (nextval('seq_permission_id'),'relation.child.remove');
    insert into permissions values (nextval('seq_permission_id'),'relation.parent.remove'); 

    ---
    alter table users add column activate_triggers boolean not null default true;

    ---
    alter table users add column config text default '<config/>' not null;

    ---
    alter table index_items drop column index_type_name;

    ---
    alter table folders drop column obj_version;
    alter table objects drop column obj_version;
    alter table folder_types drop column obj_version;

    --
    alter table formats add column index_mode varchar(255) not null default 'NONE';

    --
    alter table index_items drop column for_sys_meta;
    alter table index_items drop column for_content;
    alter table index_items drop column for_metadata;

    -- // the new combined sequence should start with x > max( objects.id, folder.id)
    create sequence seq_folder_and_object_ids start with 10000000;

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

    -- tika metaset type
    -- if necessary:
    --- insert into metaset_types(id, name, is_unique) VALUES (nextval('seq_metaset_type_id'), 'tika', true);

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


    alter table index_jobs add column update_tika_metaset boolean default false