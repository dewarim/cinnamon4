# Installing Cinnamon 4 server on Debian 12 Linux

> [!NOTE]
> These instructions are based on a minimal Debian 12 installation, typically with only `sshd` installed.

## Install dependencies and other software
* Log on and switch to root privileges.
> [!NOTE]
> Alternatively use `sudo`.

* Install some required or useful software
  ```
  apt install curl less sudo daemontools rsync htop zip unzip sshpass gnupg wget
  ```

* Install JDK 21:
  ```
  wget https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.deb
  dpkg -i jdk-21_linux-x64_bin.deb
  rm jdk-21_linux-x64_bin.deb
  ```

* Install PostgreSQL 17:
  ```
  apt install postgresql-17
  ```


## Configure PostgreSQL and create the database
* Set the password for user `postgres`:
> [!NOTE]
> This is required for backup and restore.
  ```
  cd /home/install
  sudo -u postgres psql template1
  ALTER USER postgres WITH ENCRYPTED PASSWORD 'myPassword';
  \q
  ```

* Create new Linux group and user `cinnamon`:
> [!IMPORTANT]
> Choose a safe password on production systems.
  ```
  groupadd cinnamon
  useradd -s /bin/false -g cinnamon -d /opt/cinnamon cinnamon
  mkdir /opt/cinnamon
  passwd cinnamon
  ```

* Create database user `cinnamon`:
  * Start the user creation program:
    ```
    sudo -u postgres createuser --interactive
    ```
  * Answer the questions as follows:

    | Question  | Answer |
    | ------------- | ------------- |
    | Enter the name of the role to add:  | `cinnamon`  |
    | Shall the new role be a superuser?  | `n`  |
    | Shall the new role be allowed to create databases?  | `n`  |
    | Shall the new role be allowed to create more new roles?  | `n`  |

* Set the password for user `cinnamon`:
> [!IMPORTANT]
> Choose a safe password on production systems.
> [!NOTE]
> Replace `new_password` with the safe password you chose.

  ```
  sudo -u postgres psql
  ALTER USER "cinnamon" WITH ENCRYPTED PASSWORD 'new_password';
  ALTER USER cinnamon WITH LOGIN;
  CREATE DATABASE cinnamon WITH OWNER cinnamon;
  GRANT CONNECT ON DATABASE cinnamon TO cinnamon; 
  GRANT ALL PRIVILEGES ON DATABASE cinnamon TO cinnamon;
  GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO cinnamon;
  GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO cinnamon;
  \q
  ```


## Download and configure Cinnamon 4 server
* Find the latest Cinnamon 4 server version:
  * Open the [releases](https://github.com/dewarim/cinnamon4/releases).
  * Find the latest version and copy the URL of `cinnamon.tar.gz`.

* Download and unpack Cinnamon 4 server:
  ```
  cd /opt/cinnamon
  wget <Cinnamon 4 server URL>
  tar -xvzf cinnamon.tar.gz
  rm cinnamon.tar.gz
  ```

* Create the data folder structure:
  ```
  mkdir /opt/cinnamon/data
  mkdir /opt/cinnamon/data/config
  mkdir /opt/cinnamon/data/index
  mkdir /opt/cinnamon/data/content
  chown -R cinnamon:cinnamon /opt/cinnamon
  ```

* Download a default configuration file:
  ```
  wget https://github.com/dewarim/cinnamon4/tree/master/docs/cinnamon-config.xml
  ```

* Edit the file:
  ```
  nano cinnamon-config.xml
  ```

* Find `/CinnamonConfig/databaseConfig/password` and replace the value with the `cinnamon` database user password you've chosen.

## Import data into the server:
* Obtain database and content you want to install:
  * **Option 1:** Download the [default database](https://github.com/dewarim/cinnamon4/blob/master/src/test/resources/sql/CreateTestDB.sql). You don't need content.
  * **Option 2:** Obtain SQL and content from the backup of an existing server.
* Install the database (assuming the SQL file is named `CreateTestDB.sql`):
  ```
  sudo -u postgres psql -f CreateTestDB.sql cinnamon
  rm CreateTestDB.sql      # you can delete the file after you have verified successful import
  ```
* Restore the content folders (if applicable):
> [!NOTE] How exactly you do this depends on the compression format that was used for the source folder structure (`zip`, `tar.gz`, ...). Unpack the content of the folder named `content` into `/opt/cinnamon/content/`, so that the folders under `/opt/cinnamon/content/` have two-digit hexadecimal numbers as names.
  
