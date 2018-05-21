# Migration Guide

## From Cinnamon 3.7

    ALTER TABLE users ADD login_type VARCHAR(64) DEFAULT 'CINNAMON' NOT NULL;
    ALTER TABLE users DROP COLUMN account_expired;
    ALTER TABLE users DROP COLUMN obj_version;
    ALTER TABLE users RENAME COLUMN account_locked TO locked;
    ALTER TABLE users RENAME COLUMN language_id TO ui_language_id;
    
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
    alter table ui_languages drop column obj_version
    
    --
    alter table languages drop column obj_version
    alter table languages drop column metadata

    --
    alter table metaset_types drop column obj_version
    alter table metaset_types drop column config
    
    --
    alter table index_items drop column obj_version
    alter table index_items drop column systemic
    alter table index_items drop column index_group_id
    drop table index_groups
    
    --   
    alter table config_entries drop column obj_version;
    -- note: this one needs manual work to check for config_entries containing <public>true</public> in the config field.
    alter table config_entries add column public_visibility boolean not null default false;        