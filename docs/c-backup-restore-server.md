# Backup and restore of Cinnamon 4 server
## Backup
```
# Dump DB into cinnamon/data folder
export PGPASSWORD=<password>
pg_dump -U cinnamon  -h localhost  -w cinnamon > /opt/cinnamon/data/cinnamon.sql
export PGPASSWORD=

# rsync content, DB dump and secondary services
# adapt this to your needs
rsync -av --delete /opt/cinnamon /target/cinnamon
rsync -av --delete /opt/cae/ /target/cae
rsync -av --delete /opt/changetrigger /target/changetrigger
```


## Restore
