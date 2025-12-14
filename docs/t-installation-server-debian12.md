# Installing Cinnamon 4 server on Debian 12 Linux

> [!NOTE]
> These instructions are based on a minimal Debian 12 installation, typically with only `sshd` installed.

## Install dependencies and other software
* Log on and switch to root privileges.
> [!NOTE]
> Alternatively use `sudo`.

* Install some required or useful software
  ```
  apt install curl less sudo rsync htop zip unzip sshpass gnupg wget
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

    | Question                                                | Answer     |
    |---------------------------------------------------------|------------|
    | Enter the name of the role to add:                      | `cinnamon` |
    | Shall the new role be a superuser?                      | `n`        |
    | Shall the new role be allowed to create databases?      | `n`        |
    | Shall the new role be allowed to create more new roles? | `n`        |

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
> [!NOTE]
> How exactly you do this depends on the compression format that was used for the source folder structure (`zip`, `tar.gz`, ...). Unpack the content of the folder named `content` into `/opt/cinnamon/content/`, so that the folders under `/opt/cinnamon/content/` have two-digit hexadecimal numbers as names.
  
* Change ownership of the folder structure to user `cinnamon`:
  ```
  chown -R cinnamon:cinnamon /opt/cinnamon
  ```


## Run and configure tika
* Verify that `cinnamon-config.xml` has the value `true` in `/CinnamonConfig/cinnamonTikaConfig/useTika`.

* Download and install the tika docker image:
  ```
  apt install docker.io
  docker pull apache/tika:latest-full
  docker create --name tika --restart always -p 127.0.0.1:9998:9998 apache/tika:latest-full
  docker start tika
  ```
* If the network uses a proxy for updates, and docker must use the proxy, follow these steps:
  * Create a directory and service configuration file:
    ```
    mkdir -p /etc/systemd/system/docker.service.d
    nano /etc/systemd/system/docker.service.d/http-proxy.conf
    ```
  * Edit the file and add the following lines:
> [!NOTE]
> Replace `proxyserver:port` with the correct IP and port of the proxy.
    ```
    [Service]
    Environment="HTTP_PROXY=http://proxyserver:port/" "HTTPS_PROXY=http://proxyserver:port/"
    ```
  * Reload the changed settings and restart docker:
    ```
    systemctl daemon-reload
    systemctl restart docker
    ```
* Add the following line to `crontab -e`:
  ```
  @reboot docker start tika
  ```


## Create and test startup script
* Create Cinnamon 4 startup script:
 * Create a file {{{/opt/cinnamon/runc4.sh}}} with the following content:
  ```
#!/bin/bash
export JAVA_HOME=/usr/lib/jvm/jdk-21.0.8-oracle-x64   # make sure to use the correct JAVA_HOME path
export PATH=$PATH:$JAVA_HOME/bin
cd /opt/cinnamon
java -cp "./lib/*:." --add-modules jdk.incubator.vector --enable-native-access=ALL-UNNAMED -jar cinnamon-server.jar --show-config  --config cinnamon-config.xml
  ```
  * Set the file to be executable by the owner.
* Execute `/opt/cinnamon/runc4.sh`.
* Use a browser or curl to access {{{http://localhost:8080/cinnamon}}}.
> [!NOTE]
> Replace You should see a response like this: `Cinnamon 4 Server`.



## Set up Cinnamon service
* Create a file `/etc/systemd/system/cinnamon.service`.
* Copy the following content into the file:
  ```
  [Unit]
  Description=Cinnamon Server
  ;After=network.target
  
  [Service]
  User=cinnamon
  ;Group=groupname
  WorkingDirectory=/opt/cinnamon
  ExecStart=/opt/cinnamon/runc4.sh
  
  [Install]
  WantedBy=multi-user.target
  ```
* Load the service unit and enable and start the service:
  ```
  systemctl daemon-reload
  systemctl enable cinnamon
  systemctl start cinnamon
  ```



## Finalize installation
* Edit the formats and index items according to: https://github.com/dewarim/cinnamon4/blob/master/docs/tika.adoc
* If required, install the CAE and Change Trigger applications on the server and the Client on Windows machines. Find the instructions [here](https://github.com/boris-horner/cinnamon4-clients/blob/main/docs/c-installation-clients.md).
