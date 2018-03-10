# Migration Guide

## From Cinnamon 3.7

    ALTER TABLE public.users ADD login_type VARCHAR(64) DEFAULT 'CINNAMON' NOT NULL;
    