# Update Cinnamon 4 Server to a newer version
## General notes
Cinnamon Server updates generally consist of these steps:
1. Important: Identify the server version that is currently running ("previous version").
1. Stop Cinnamon Server and other services.
1. Remove the existing binaries.
1. Download and unpack the version to be installed ("new version").
1. If applicable: Apply all required changes to the database between previous and new version. The changes are documented with each release.
1. If applicable: Apply any other configuration changes in the file-based Cinnamon Server configuration.
1. Start Cinnamon Server and peripheral services.
1. If applicable: Start Cinnamon Desktop Client and apply any required configuration changes in the Cinnamon repository.

"If applicable" means: configuration changes of this type are not always required and need to be done only if they are listed in the Cinnamon Server version's release notes.


## Detailed update instructions
### Preparation
1. Determine the server version currently installed.
  * Log on to the server.
  * Use ```curl localhost:8080/cinnamon/info```. ```8080``` is the default port, adapt it to your setup. This returns:
```
<CinnamonServer>
  <version>1.13.1</version>
  <build>538</build>
</CinnamonServer>
```
2. Find the version that is currently installed in the Releases page ("previous version").
1. Identify all SQL statements between previous and new version.
1. Identify all configuration changes between previous and new version.
1. Switch to ```root``` privileges.
1. Stop CAE and Cinnamon Server:
```
systemctl stop cae.timer && systemctl stop cae && systemctl stop cinnamon
```

### Software update
1. Go to Cinnamon Server folder and switch to ```cinnamon``` user:
```
cd /opt/cinnamon
sudo -u cinnamon bash
```
2. Navigate to https://github.com/dewarim/cinnamon4/releases, copy the URL of the ```cinnamon.tar.gz``` file of the version you want to install and download it using ```wget```:
```
wget <copied URL>
```
The compressed server will be downloaded.

3. Unpack the server and delete the downloaded file:
```
tar xvzf cinnamon.tar.gz
rm cinnamon.tar.gz
```

### Database and configuration changes
If the documentation of the releases from the starting to the target release contains changes to the database and / or the configuration, follow them carefully.
#### Database changes (if applicable)
1. Log in to the Postgres client:
```
sudo -u postgres psql cinnamon
```
2. The documentation contains changes to be performed as SQL. Copy the SQL code into the Postgres client and press Enter.

#### Configuration changes (if applicable)
1. Edit the file ```/opt/cinnamon/cinnamon-config.xml``` with the editor of your choice.
2. Apply the changes as explained in the documentation.
3. Save and exit the file.

### Starting services
1. Make sure you're logged in as user ```cinnamon```.
2. Execute ```/opt/cinnamon/runc4.sh```.
3. Observe the console output for error messages.
4. Stop execution (```ctrl-C```).
5. Run the following commands as root (or prepend ```sudo```):
```
systemctl start cinnamon && systemctl start cae.timer && systemctl start cae
```
