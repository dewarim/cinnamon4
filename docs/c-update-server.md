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
  * Open a web browser and open ```<server_url>/cinnamon/info```, or on the server, use ```curl localhost:8080/cinnamon/info```. ```8080``` is the default port, adapt it to your setup. This returns:
```
<CinnamonServer>
  <version>1.13.1</version>
  <build>538</build>
</CinnamonServer>
```
