# Migration Guide

## From Cinnamon 3.7

    ALTER TABLE users ADD login_type VARCHAR(64) DEFAULT 'CINNAMON' NOT NULL;
    
    -- note: this will set the created date of all existing folders to now()
    ALTER TABLE folders ADD created TIMESTAMP DEFAULT NOW()
    