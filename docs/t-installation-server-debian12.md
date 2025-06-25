# Installing Cinnamon 4 server on Debian 12 Linux

>**NOTE:** These instructions are based on a minimal Debian 12 installation, typically with only `sshd` installed.

## Installing dependencies and other software
* Log on and switch to root privileges.
  >**NOTE:** Alternatively use `sudo`.

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
