# Migration Guide

## From Cinnamon 3.7

    ALTER TABLE users ADD login_type VARCHAR(64) DEFAULT 'CINNAMON' NOT NULL;
    ALTER TABLE users DROP COLUMN account_expired;
    ALTER TABLE users DROP COLUMN obj_version;
    ALTER TABLE users RENAME COLUMN account_locked TO locked;
    ALTER TABLE users RENAME COLUMN language_id TO ui_language_id;
    ALTER TABLE users ALTER COLUMN change_tracking SET DEFAULT true;
    
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
    