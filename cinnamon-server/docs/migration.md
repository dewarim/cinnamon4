# Migration Guide

## From Cinnamon 3.7

    ALTER TABLE users ADD login_type VARCHAR(64) DEFAULT 'CINNAMON' NOT NULL;
    ALTER TABLE users DROP COLUMN account_expired;
    
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
    