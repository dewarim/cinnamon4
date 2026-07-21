# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Cinnamon 4 is a Java-based Content Management Server (CMS). It exposes a REST-like XML API over HTTP/HTTPS, backed by PostgreSQL, and stores file content on the filesystem. It was rewritten from scratch (not evolved from Cinnamon 3), deliberately avoiding frameworks like Hibernate or Groovy/Grails. Stack: **Java 25**, **Jetty 12**, **MyBatis**, **PostgreSQL**, **Lucene**, **Jackson (XML/JSON)**.

## Build Commands

Compiling and testing is done through **skills**, not by calling `mvn` directly — they filter
Maven's output down to the errors and always end in an explicit verdict. Invoke them by name:

| Skill | Does | Typical cost |
|---|---|---|
| `maven-compile` | `mvn compile`, prints `0` or just the compiler errors | ~3s |
| `maven-single-test` | one test method, e.g. `AclServletIntegrationTest listAclsTest` | ~5s |
| `maven-test-all` | the whole suite (614 tests) | ~40s |

The full suite is cheap enough to run before declaring anything done. Almost all of those 40s are
integration tests actually hitting Postgres and embedded Jetty — compilation is a small fraction,
so `clean` costs about a second and build-tool swaps (mvnd) buy back only ~3s. Don't bother.

For a faster inner loop while editing, see "Checking work without Maven" below.

```bash
# Full build (runs all integration tests, produces target/cinnamon-server.jar + SBOM):
mvn clean package

# Full build plus the distribution archive target/cinnamon-server.tar.gz:
mvn clean package -Pdist

# Run the server (needs target/lib, see "Packaging" below):
java --add-modules jdk.incubator.vector --enable-native-access=ALL-UNNAMED -jar target/cinnamon-server.jar
```

Use `local/temp` as the temporary folder, not `/tmp` or system temp dirs.

## Checking work without Maven

Two tools answer questions faster than a build cycle. Neither replaces the verdict from
`maven-compile` / `maven-test-all` — use them to iterate, then confirm with Maven.

**IntelliJ MCP (`get_file_problems`)** — full semantic analysis of one file in under a second,
with no Maven invocation. Catches cross-file type errors with resolved generics, and also reports
things javac never will (unused methods, inspection warnings). Requires IDEA running with the
project indexed, and it reflects the IDE's model rather than the real build, so a clean result
here is encouraging, not proof. `rename_refactoring` and `analyze_calls` have no Maven equivalent
and are the right way to do renames and call-graph questions.

**jshell** — for questions about *runtime* library behaviour, where the alternative is writing a
throwaway test. Especially useful for Jackson XML/JSON wire format, which is easy to get wrong by
reading the record alone. Use the `jshell-project` skill, or by hand:

```bash
mvn -q dependency:build-classpath -Dmdep.outputFile=local/temp/cp.txt -DincludeScope=test
jshell --class-path "target/classes:target/test-classes:$(cat local/temp/cp.txt)" --execution local
```

The classpath file only needs regenerating when dependencies change; `target/classes` must be
current, so run `maven-compile` first if sources changed.

## Packaging

The build produces a plain jar, **not** a fat jar: `cinnamon-server.jar` declares a
`Class-Path` of `lib/*.jar` in its manifest and needs its dependencies next to it in `lib/`.
The `dist` profile populates `target/lib` via `dependency:copy-dependencies`; without that
profile the directory has to be created manually before the jar will start:

```bash
mvn dependency:copy-dependencies -DoutputDirectory=target/lib -DincludeScope=runtime
```

(`-DincludeScope=runtime` matters: without it the test dependencies land in `lib/` too, which
no longer matches the jar manifest or the SBOM.)

`mvn package -Pdist` bundles jar, `lib/`, the SBOM and `LICENSE.txt` into
`target/cinnamon-server.tar.gz` (unpacks into a `cinnamon-server/` directory).
The assembly descriptor is `src/build/dist.xml`.

### SBOM

A CycloneDX 1.6 SBOM is generated into `target/cinnamon-server.cdx.json` on every
`mvn package`, for EU CRA / BSI TR-03183-2 compliance. This is the `sbom` profile, which is
active unless `-DskipSbom` is passed.

It requires **`jq` on the PATH**: `cyclonedx-maven-plugin` has no config parameter for the
supplier fields, so `src/build/sbom-supplier.jq` patches them into the generated document.
Use `-DskipSbom` to skip SBOM generation and the jq requirement.

The SBOM covers compile + runtime scope only, so its component list matches both the jar
manifest `Class-Path` and the contents of `lib/` exactly. Keep those three in sync when
changing dependency scopes.

### Vulnerability scanning

```bash
mvn package -Pvulncheck   # -> target/vulnerability-report.md
```

The `vulncheck` profile scans the generated SBOM with
[osv-scanner](https://github.com/google/osv-scanner) (must be on the PATH; needs network
access to osv.dev). It consumes the SBOM, so it cannot be combined with `-DskipSbom`.

The scan is **report-only**: `osv-scanner` exits with code 1 when it finds something, and
that exit code is tolerated so a newly published CVE does not break an unrelated build.
Pass `-Dvulncheck.tolerated.exit.code=0` to make findings fail the build instead.

The report is written to `target/` and deliberately *not* checked into `docs/`: unlike the
SBOM it changes whenever new CVEs are published rather than when dependencies change, so a
versioned copy would go stale.

This is a point-in-time scan of one build. The CRA also expects ongoing monitoring of
already-released versions, which a build-time scan does not provide - feed the SBOM to
something like Dependency-Track for that.

### Generated documentation

The SBOM step also renders `docs/sbom.adoc` (via `src/build/sbom-to-adoc.jq`), a
human-readable dependency/license table that is checked into the repository. It is
**generated - do not edit it by hand**. The rendering is deterministic (no build timestamp),
so it only shows up in `git status` when the dependencies actually change. Note that
`-DskipSbom` leaves the existing file untouched rather than updating it.

## Test Database Setup

Integration tests require a running PostgreSQL instance:

- Database: `cinnamon_test`, user: `cinnamon`, password: `cinnamon`, host: `127.0.0.1:5432`
- Initialize with: `psql -f src/test/resources/sql/CreateTestDB.sql cinnamon_test`
- Tests start an embedded Cinnamon server on port `19999`

## Architecture

### Request Lifecycle

Each HTTP request flows through a chain of Servlet filters before reaching a servlet:

1. **`DbSessionFilter`** — opens a `ThreadLocal` MyBatis `SqlSession`, commits or rolls back after the request, and handles pending index jobs and file deletions
2. **`AuthenticationFilter`** — validates the `ticket` header against the `sessions` table; populates `RequestScope.currentUser`
3. **`ChangeTriggerFilter`** — fires `PRE`/`POST` change triggers (microservice callbacks or no-ops)
4. **Servlet** — handles the actual business logic

`RequestScope` and `ThreadLocalSqlSession` are the two key thread-local holders used throughout request processing. DAOs call `ThreadLocalSqlSession.getSqlSession()` directly — there is no DI container.

### Servlet Layer

All servlets live in `application/servlet/`. Every resource type (Acl, Folder, OSD, Group, etc.) has its own servlet. Servlets implement `CruddyServlet<T>` for standard CRUD and `BaseServlet` for shared permission-check helpers.

All API endpoints are declared in the `UrlMapping` enum — this is the single source of truth for every URL path. Convention: `RESOURCE__METHOD` maps to `/api/<resource>/<method>`.

Request/response objects are in `model/request/` and `model/response/`. The API is XML by default (Jackson XmlMapper); JSON is also supported. All responses are wrapped in a `Wrapper<T>` object.

### DAO Layer

DAOs in `dao/` implement `CrudDao<T>`, which provides default CRUD methods backed by MyBatis XML mappers in `src/main/resources/com/dewarim/cinnamon/*Mapper.xml`. DAOs instantiate themselves (`new FolderDao()`) and call `ThreadLocalSqlSession.getSqlSession()` — no injection.

### Core Domain Objects

- **OSD (ObjectSystemData)** — the primary document/object entity; stores metadata and a reference to file content
- **Folder** — hierarchical container for OSDs
- **ACL / AclGroup / Permission** — access control; every OSD and Folder has an ACL; permissions are checked via `AuthorizationService`
- **Lifecycle / LifecycleState** — workflow state machine for OSDs; state transitions are handled by `StateProvider` implementations (loaded via `ServiceLoader`)
- **Relation / RelationType** — typed links between OSDs
- **Meta / MetasetType** — custom metadata attached to OSDs or Folders (XML blobs)
- **ChangeTrigger** — hooks that fire before/after write operations, either as NOP or microservice HTTP calls

### Content Storage

File content is managed by `ContentProviderService`, which currently supports only `FILE_SYSTEM` via `FileSystemContentProvider`. Files are stored under the configured `dataRoot` directory.

### Indexing & Search

`IndexService` (background thread) watches for `IndexJob` entries created during requests and indexes OSD/Folder content into a Lucene index. `SearchService` queries the Lucene index and applies ACL filtering. Index field types are implemented as `*Indexer` classes under `application/service/index/`.

### Configuration

Server configuration is XML (`CinnamonConfig`) loaded from a file (default: `default-config.xml`). Key sections: `serverConfig`, `databaseConfig`, `securityConfig` (includes LDAP), `mailConfig`. Generate a config template with `java -jar cinnamon-server.jar --write-config my-config.xml`.

### Built-in Client

`client/CinnamonClient` is a Java HTTP client for the API (used heavily in integration tests). `CinnamonIntegrationTest` is the base class for all integration tests — it starts the server on port 19999, sets up DB sessions, and provides pre-authenticated `client` and `adminClient` instances.

## Adding a New API Endpoint

1. Add a `Request` class in `model/request/<resource>/` and a `Response`/`Wrapper` in `model/response/`
2. Add the URL to `UrlMapping` enum
3. Implement the handler method in the appropriate servlet (or create a new servlet)
4. Register the servlet in `CinnamonServer` if new
5. Add the corresponding method to `CinnamonClient`
6. Write an integration test extending `CinnamonIntegrationTest`
