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
