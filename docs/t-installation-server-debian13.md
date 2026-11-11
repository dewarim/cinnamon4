# Installing Cinnamon 4 Server on Debian 13

> [!NOTE]
> This guide describes installing Cinnamon 4 on a minimal Debian 13 (Trixie) Linux server with Java 25, PostgreSQL 18, and an optional local Apache Tika container. It is independent of virtualization, cloud hosting, network topology, or reverse-proxy products. Commands are run as `root` unless otherwise noted. Replace example passwords, paths, and release URLs as appropriate.

## Software overview

| Component | Purpose | Default installation in this guide |
| --- | --- | --- |
| Cinnamon 4 | Content management server | `/opt/cinnamon`, service user `cinnamon` |
| Java 25 | Java runtime | System Java installation |
| PostgreSQL 18 | Metadata database | Local PostgreSQL cluster, database `cinnamon` |
| Apache Tika (optional) | Document text and metadata extraction | Docker container on `127.0.0.1:9998` |
| systemd | Start and supervise Cinnamon | `cinnamon.service` |

> [!IMPORTANT]
> Verify compatibility of the selected Cinnamon release, Java 25, PostgreSQL 18, and the PostgreSQL JDBC driver before deploying a production system. File paths and command-line options must match the installed Cinnamon release.

## Install system utilities

```bash
apt update
apt full-upgrade -y
apt install -y curl less sudo rsync htop zip unzip gnupg wget ca-certificates nano tar postgresql-client-common
```

Install `sshpass` only if a specific administrative workflow requires it; key-based SSH is preferable. Keep the server updated with Debian security patches.

## Install Java 25

This example uses Oracle's JDK 25 Debian package. Review its licensing terms for your intended deployment. An OpenJDK 25 distribution is an alternative.

```bash
cd /tmp
wget https://download.oracle.com/java/25/latest/jdk-25_linux-x64_bin.deb
dpkg -i jdk-25_linux-x64_bin.deb
rm jdk-25_linux-x64_bin.deb
java -version
readlink -f "$(command -v java)"
```

The startup script below uses `/usr/bin/java` instead of hardcoding a JDK directory. Confirm the installed Java is version 25. If you later change Java versions, verify the `jdk.incubator.vector` module is still available before retaining the module option.

## Install PostgreSQL 18

Debian 13 normally supplies PostgreSQL 17. Install PostgreSQL 18 from the official PostgreSQL APT (PGDG) repository:

```bash
apt install -y postgresql-common ca-certificates
/usr/share/postgresql-common/pgdg/apt.postgresql.org.sh
apt update
apt install -y postgresql-18 postgresql-client-18
pg_lsclusters
sudo -u postgres psql -c 'SELECT version();'
```

The repository setup helper may prompt for confirmation. Check that the intended PostgreSQL 18 cluster is online and listening on port 5432. If another PostgreSQL version was previously installed, inspect `pg_lsclusters` and avoid connecting to the wrong cluster.

### Restrict PostgreSQL network access

This guide assumes Cinnamon and PostgreSQL run on the same server. PostgreSQL should not accept remote connections unless a separate database server is deliberately used and secured.

Check `/etc/postgresql/18/main/postgresql.conf`:

```ini
listen_addresses = 'localhost'
```

Check `/etc/postgresql/18/main/pg_hba.conf`. Keep local peer authentication for PostgreSQL administration, and require password authentication for Cinnamon's TCP connections (a typical Debian setup already provides these entries):

```text
local   all   postgres                         peer
host    cinnamon   cinnamon   127.0.0.1/32      scram-sha-256
host    cinnamon   cinnamon   ::1/128           scram-sha-256
```

Do not remove other necessary local entries without reviewing the full file. Apply changes if needed:

```bash
systemctl restart postgresql
ss -lntp | grep ':5432'
```

> [!NOTE]
> A password for the PostgreSQL superuser `postgres` is **not** required for local backups or restores performed with `sudo -u postgres`, because Debian supports local peer authentication. Do not set one merely for backup purposes.

## Create the Cinnamon Linux service account

Use a non-login account without a password:

```bash
groupadd --system cinnamon
useradd --system --gid cinnamon --home-dir /opt/cinnamon --shell /usr/sbin/nologin cinnamon
install -d -o root -g root -m 755 /opt/cinnamon
install -d -o cinnamon -g cinnamon -m 750 /var/lib/cinnamon
install -d -o cinnamon -g cinnamon -m 750 /var/lib/cinnamon/content
install -d -o cinnamon -g cinnamon -m 750 /var/lib/cinnamon/index
install -d -o cinnamon -g cinnamon -m 750 /var/lib/cinnamon/config
```

> [!NOTE]
> If the account already exists, inspect it with `getent passwd cinnamon` rather than rerunning the account-creation commands. The service account does not need a login password or `sudo` access.

## Create the PostgreSQL application role and database

Choose a strong unique database password. The following SQL example is for a **new installation**, not an existing database restore.

```bash
sudo -u postgres psql
```

```sql
CREATE ROLE cinnamon WITH LOGIN PASSWORD 'REPLACE_WITH_STRONG_PASSWORD'
    NOSUPERUSER NOCREATEDB NOCREATEROLE;
CREATE DATABASE cinnamon OWNER cinnamon;
\q
```

Because the `cinnamon` role owns its database, extra `GRANT ALL PRIVILEGES ON DATABASE` statements are unnecessary for a normal fresh installation. The role should not be a superuser.

Test TCP password authentication (you will be prompted for the password):

```bash
psql -h 127.0.0.1 -U cinnamon -d cinnamon -c 'SELECT current_user, current_database();'
```

> [!IMPORTANT]
> The role and database creation commands are intended to be executed once. Do not repeat them against an existing production database.

## Download Cinnamon 4

Find a suitable release at [Cinnamon 4 releases](https://github.com/dewarim/cinnamon4/releases). Inspect its release notes and verify the archive contents before deployment.

```bash
cd /opt/cinnamon
wget -O /tmp/cinnamon.tar.gz '<ACTUAL_CINNAMON_RELEASE_ARCHIVE_URL>'
tar -tzf /tmp/cinnamon.tar.gz | head -30
tar -xzf /tmp/cinnamon.tar.gz -C /opt/cinnamon
rm /tmp/cinnamon.tar.gz
ls -la /opt/cinnamon
```

Confirm that `cinnamon-server.jar` and any required `lib/` files are in the locations expected by the startup script. Adjust extraction if the release archive has an enclosing directory.

### Obtain and edit Cinnamon configuration

Use the example configuration **supplied with the selected release** or a configuration generated by that release, if supported. Avoid using a GitHub `/tree/` or `/blob/` page URL with `wget`; these return HTML rather than raw XML. The exact configuration-generation command and example file path are release-dependent, so verify them against the selected release.

Place the resulting configuration at `/opt/cinnamon/cinnamon-config.xml`:

```bash
nano /opt/cinnamon/cinnamon-config.xml
```

Verify at least these settings (actual XML structure and element names depend on the release):

- Database URL points to `127.0.0.1:5432/cinnamon` and uses the `cinnamon` role.
- Database password matches the password set above.
- Cinnamon content, index, and configuration paths point to the intended writable directories under `/var/lib/cinnamon` (or another deliberately chosen consistent layout).
- The HTTP listener binds to the intended server interface; use port 8080 if that matches your deployment.
- Tika is enabled if required and its URL points to `http://127.0.0.1:9998`.

Restrict the XML configuration, which contains credentials:

```bash
chown root:cinnamon /opt/cinnamon/cinnamon-config.xml
chmod 640 /opt/cinnamon/cinnamon-config.xml
```

> [!IMPORTANT]
> Do not assume `/opt/cinnamon/content` and `/opt/cinnamon/data/content` are interchangeable. The configured content path must match the directory into which content is restored. Likewise, do not change storage paths on an existing installation without migrating the data.

## Initialize or restore Cinnamon data

Choose **one** of the following procedures.

### Option A: Fresh test database

Download the [upstream example SQL](https://github.com/dewarim/cinnamon4/blob/master/src/test/resources/sql/CreateTestDB.sql) if it is compatible with the release you installed. Use the **raw** GitHub URL when downloading a file:

```bash
wget -O /tmp/CreateTestDB.sql \
  https://raw.githubusercontent.com/dewarim/cinnamon4/master/src/test/resources/sql/CreateTestDB.sql
```

Review the SQL before running it, especially any `CREATE DATABASE`, `CREATE ROLE`, ownership, or schema statements. If it contains statements that conflict with the role/database created above, adapt the import process instead of blindly executing it.

For a schema-only or data SQL script intended to run **inside an existing** `cinnamon` database:

```bash
psql -h 127.0.0.1 -U cinnamon -d cinnamon \
  -v ON_ERROR_STOP=1 -f /tmp/CreateTestDB.sql
```

This creates new objects under the application role when the SQL does not explicitly override ownership. Do not assume the example script is production-ready; it is test data.

### Option B: Restore an existing Cinnamon installation

Obtain a consistent database backup, content backup, and configuration from the source installation. Check PostgreSQL dump format and version before restoring.

For a custom-format `pg_dump -Fc` backup, a typical restore into the existing database is:

```bash
sudo -u postgres pg_restore \
  --dbname=cinnamon \
  --no-owner --no-acl \
  --exit-on-error \
  /path/to/cinnamon.dump
```

This example is suitable only if the backup and target database are compatible and the restore will not collide with existing objects. For a clean migration, restore into an empty database and inspect ownership and extensions. Since the restore is performed as `postgres`, objects may be owned by `postgres` despite `--no-owner`; **explicitly correct ownership before starting Cinnamon**, or restore as `cinnamon` where permissions and dump contents permit. Check the resulting ownership and privileges:

```bash
sudo -u postgres psql -d cinnamon -c '\dt+'
sudo -u postgres psql -d cinnamon -c '\ds+'
```

Do not grant broad rights to unrelated databases. If you are using a plain SQL backup, use `psql` rather than `pg_restore` and review any role/ownership commands in the dump.

### Restore the content directory

Restore the source installation's hexadecimal content subdirectories to the **exact path specified in Cinnamon configuration**. In this guide that path is:

```text
/var/lib/cinnamon/content/
```

The directory should contain two-digit hexadecimal subdirectories where expected by Cinnamon. Preserve filenames and directory structure.

Set ownership only on writable data paths:

```bash
chown -R cinnamon:cinnamon /var/lib/cinnamon
chmod 750 /var/lib/cinnamon /var/lib/cinnamon/content /var/lib/cinnamon/index
```

Keep application binaries owned by `root`, and ensure the `cinnamon` user can read the JAR and libraries.

## Install and run Apache Tika

Tika parses potentially untrusted documents. Keep its REST endpoint local and do not expose port 9998 publicly. Use a **tested, explicitly pinned** Tika image tag rather than `latest-full`.

```bash
apt install -y docker.io
systemctl enable --now docker
```

Replace `<TESTED_TIKA_FULL_TAG>` with the actual tag validated for your Cinnamon release:

```bash
docker pull apache/tika:<TESTED_TIKA_FULL_TAG>
docker run -d \
  --name tika \
  --restart unless-stopped \
  --memory 3g \
  --cpus 2 \
  -p 127.0.0.1:9998:9998 \
  apache/tika:<TESTED_TIKA_FULL_TAG>
```

Verify:

```bash
docker ps
docker logs --tail 50 tika
curl -f http://127.0.0.1:9998/version
```

The 3 GB memory and 2 CPU limits are examples only. Adjust them for available hardware, OCR workloads, and document sizes after measuring actual usage. Docker's restart policy replaces the old `@reboot docker start tika` cron entry.

If Docker needs an **outbound HTTP proxy** for image downloads, create `/etc/systemd/system/docker.service.d/http-proxy.conf`:

```ini
[Service]
Environment="HTTP_PROXY=http://proxyserver:port/" "HTTPS_PROXY=http://proxyserver:port/"
```

Then:

```bash
systemctl daemon-reload
systemctl restart docker
```

This is only needed when the server must use an outbound HTTP proxy to access image registries.

## Create the Cinnamon startup script

Create `/opt/cinnamon/runc4.sh`:

```bash
cat > /opt/cinnamon/runc4.sh <<'EOF_SCRIPT'
#!/bin/bash
set -e
cd /opt/cinnamon
exec /usr/bin/java \
  -Xms1g -Xmx4g \
  --add-modules jdk.incubator.vector \
  --enable-native-access=ALL-UNNAMED \
  -jar cinnamon-server.jar \
  --config cinnamon-config.xml
EOF_SCRIPT
chmod 755 /opt/cinnamon/runc4.sh
chown root:root /opt/cinnamon/runc4.sh
```

> [!NOTE]
> Verify `--config cinnamon-config.xml` and the Java module flags against your installed Cinnamon and JDK release. The heap settings are examples and must be adjusted to available memory and workload. Avoid `--show-config` during routine startup because configuration output may disclose secrets.

### Test Cinnamon manually

```bash
sudo -u cinnamon /opt/cinnamon/runc4.sh
```

In another terminal, test the listener (adjust the endpoint if the release uses a different health/status route):

```bash
curl -i http://127.0.0.1:8080/cinnamon
```

Stop the foreground process with `Ctrl+C` after confirming startup. If Cinnamon binds to a non-loopback address, use that address instead of `127.0.0.1`.

## Configure the systemd service

Create `/etc/systemd/system/cinnamon.service`:

```ini
[Unit]
Description=Cinnamon 4 CCMS Server
Wants=network-online.target
After=network-online.target postgresql.service docker.service

[Service]
Type=simple
User=cinnamon
Group=cinnamon
WorkingDirectory=/opt/cinnamon
ExecStart=/opt/cinnamon/runc4.sh
Restart=on-failure
RestartSec=10
TimeoutStopSec=120
LimitNOFILE=65535
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=full
ProtectHome=true
ReadWritePaths=/var/lib/cinnamon

[Install]
WantedBy=multi-user.target
```

`After=` controls ordering, not readiness: Cinnamon may still need to retry if PostgreSQL or Tika is not ready. If Cinnamon writes logs, caches, or temporary files under `/opt/cinnamon`, adjust those paths or the sandbox configuration before enabling the hardening options.

```bash
systemctl daemon-reload
systemctl enable --now cinnamon
systemctl status cinnamon --no-pager
journalctl -u cinnamon -n 100 --no-pager
```

Check that the service survives a controlled restart:

```bash
systemctl restart cinnamon
systemctl is-active cinnamon
```

## Configure network access

Cinnamon typically serves HTTP on port `8080`; confirm the actual listener and path in the selected release's configuration. Configure the listener address and firewall rules according to your deployment environment.

- For local testing, use the loopback address when supported.
- For remote access, expose only the Cinnamon application port to trusted clients or a suitably configured HTTPS reverse proxy.
- Keep PostgreSQL (`5432`) and Tika (`9998`) bound to loopback in the single-server installation described here.
- Use HTTPS for production access, either through a supported application configuration or a reverse proxy/load balancer.
- Restrict SSH to authorized administrators; do not expose database or Tika ports to untrusted networks.

The choice of public DNS, TLS termination, VPN, firewalls, virtualization, and reverse-proxy configuration is outside the scope of this Cinnamon installation guide.

## Security and operational hardening

- Keep SSH restricted to VPN administration where practical; use SSH keys and disable password authentication after confirming key access.
- Protect `cinnamon-config.xml`, database backups, and any credentials in environment files or logs.
- Run Cinnamon as the non-login `cinnamon` user, never as `root` or PostgreSQL `postgres`.
- Keep PostgreSQL and Tika bound to loopback in single-server installations, and use HTTPS for externally accessed Cinnamon endpoints.
- Pin and periodically update Cinnamon, Java, Tika, and PostgreSQL versions in a tested maintenance process.
- Review Tika's processing capabilities and container limits for untrusted files. Do not grant the Tika container privileged mode or unnecessary host mounts.
- Monitor disk usage for PostgreSQL, content storage, logs, and indexes; consider separate storage volumes if content growth warrants it.
- Review systemd security settings with `systemd-analyze security cinnamon.service`, but test any additional sandboxing against Cinnamon's actual filesystem requirements.

## Backups and recovery

A working Cinnamon backup must include **both database and content**, along with the configuration needed to restore the service.

### Example PostgreSQL backup

```bash
install -d -m 700 /var/backups/cinnamon
sudo -u postgres pg_dump -Fc -d cinnamon -f /tmp/cinnamon.dump
mv /tmp/cinnamon.dump /var/backups/cinnamon/cinnamon.dump
chmod 600 /var/backups/cinnamon/cinnamon.dump
```

This is a basic manual example; `/tmp` is not ideal for large or sensitive production backups. For scheduled backups, write directly to a protected location accessible to the backup process, enforce retention, and copy backups off the server.

### Content and configuration

Coordinate the database and content snapshots so they represent a consistent state (for example, use an application maintenance window or a verified snapshot procedure). Include:

- PostgreSQL database backup (`pg_dump -Fc` or another tested method).
- `/var/lib/cinnamon/content` and any other non-rebuildable application data.
- `/opt/cinnamon/cinnamon-config.xml`, startup script, and systemd unit.
- A record of the Cinnamon release, Java version, PostgreSQL version, and Tika image tag.

Do not assume indexes must be backed up if they can be reliably rebuilt, but verify that rebuilding works before excluding them. Test a full restore on an isolated test system periodically.

## Final verification

Run on the Cinnamon server:

```bash
java -version
pg_lsclusters
sudo -u postgres psql -d cinnamon -c 'SELECT current_database();'
psql -h 127.0.0.1 -U cinnamon -d cinnamon -c 'SELECT 1;'
docker ps
curl -f http://127.0.0.1:9998/version
systemctl status cinnamon --no-pager
journalctl -u cinnamon -n 50 --no-pager
ss -lntp | grep -E ':(8080|5432|9998)\b'
```

Confirm that Cinnamon responds on the configured listening address and that any separately configured HTTPS endpoint works as expected.

## Optional Cinnamon components

Install CAE, Change Trigger, and client applications only if required. Refer to the [Cinnamon clients installation documentation](https://github.com/boris-horner/cinnamon4-clients/blob/main/docs/c-installation-clients.md) and review the release-specific [Tika/indexing documentation](https://github.com/dewarim/cinnamon4/blob/master/docs/tika.adoc) before configuring formats and index definitions.
