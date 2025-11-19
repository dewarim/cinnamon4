# Backup and restore of Cinnamon 4 server
## Backup
* Create a script ```backup.sh``` and run it with crontab:
  ```
  # Dump DB into cinnamon/data folder
  systemctl stop cinnamon    # this is optional, but ensures a consistent state
  export PGPASSWORD=<password>
  pg_dump -U cinnamon  -h localhost  -w cinnamon > /opt/cinnamon/data/cinnamon.sql
  export PGPASSWORD=
  systemctl start cinnamon    # optional
  
  # rsync content, DB dump and secondary services
  # adapt this to your needs
  rsync -av --delete /opt/cinnamon /target/cinnamon
  rsync -av --delete /opt/cae/ /target/cae
  rsync -av --delete /opt/changetrigger /target/changetrigger
  ```


## Restore
* Delete the existing database and create a new one:
  ```
  systemctl stop cinnamon
  sudo -u postgres psql
  drop database cinnamon;
  \q
  sudo -u postgres psql template1
  create database cinnamon with owner=cinnamon;
  \q
  sudo -u postgres psql cinnamon < /path/to/cinnamon.sql
  ```
* Restore the content and the secondary services:
  ```
  # adapt this to your needs
  rsync -avW /target/cinnamon /opt/cinnamon
  rsync -avW /target/cae /opt/cae/
  rsync -avW /target/changetrigger /opt/changetrigger 
  ```
