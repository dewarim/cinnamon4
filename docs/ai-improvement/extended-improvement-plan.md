# Cinnamon — Erweiterter Code-Qualitäts-Verbesserungsplan

**Datum:** 2026-04-28
**Umfang:** Alle Nicht-DAO-Quellcode-Dateien — Servlets, Filter, Services, Modelle, Konfiguration, `pom.xml`
**Status:** Ergänzung zu `dao_improvement_plan.md` — keine der Punkte unten ist dort abgedeckt.

---

## Executive Summary

Diese Analyse deckt **10 Kategorien** von Problemen auf, die über den vorhandenen DAO-Plan hinausgehen. Darunter befinden sich bestätigte Bugs mit hohem Schweregrad (Authentifizierungsfehler, Race-Conditions, Ressourcen-Leaks), mittelschwere Sicherheitslücken sowie Architektur- und API-Inkonsistenzen.

| Bisher gefundene HIGHs | Betroffene Schicht |
|---|---|
| LDAP-Login immer fehlschlagend | Security / `LdapLoginProvider` |
| `DeletionTask` unlock ohne Lock + Connection-Leak | Background Thread |
| `FileInputStream` Resource-Leak | Servlet |
| `IndexService.stopped` / `isInitialized` nicht `volatile` | Concurrency |
| `indexItems`-Liste ohne Synchronisation beschreibbar | Concurrency |
| Invertierte `mkdirs()`-Prüfung im `IndexService` | Logic |

---

## Kategorie A: Thread-Safety & Volatile-Visibility-Bugs

### A1 — `IndexService.stopped` fehlt `volatile` [HIGH]

**Datei:** `application/service/IndexService.java`, Zeile ~57

Das Feld `stopped` wird von `CinnamonServer.stop()` auf dem Haupt-Thread auf `true` gesetzt, während die `while (!stopped)`-Schleife auf dem IndexService-Thread läuft. Ohne `volatile` darf die JVM den Wert im CPU-Register cachen; der Loop sieht die Änderung möglicherweise nie und läuft nach `stop()` endlos weiter.

**Aktuell (FALSCH):**
```java
private boolean stopped = false;
```

**Empfohlen:**
```java
private volatile boolean stopped = false;
```

---

### A2 — `IndexService.isInitialized` fehlt `volatile` [HIGH]

**Datei:** `application/service/IndexService.java`, Zeile ~56

`SearchService` pollt dieses Flag von einem anderen Thread. Ohne `volatile` kann der Schreibvorgang des IndexService-Threads nie in den Hauptspeicher gespült werden.

**Aktuell (FALSCH):**
```java
public static boolean isInitialized = false;
```

**Empfohlen:**
```java
public static volatile boolean isInitialized = false;
```

---

### A3 — `IndexService.indexItems` wird von Servlet-Threads ohne Synchronisation mutiert [HIGH]

**Datei:** `application/service/IndexService.java`, Zeilen ~638–655

`indexItems` ist eine einfache `ArrayList`. Sie wird im `run()`-Kontext (IndexService-Thread) gelesen und via `addIndexItems()`, `removeIndexItems()`, `updateIndexItems()` aus `IndexItemServlet` (Servlet-Threads) geschrieben. Das ist unsynchronisierter konkurrenter Zugriff auf eine nicht thread-sichere Collection — Risiko von `ConcurrentModificationException` oder stiller Datenverfälschung.

**Empfohlen:**
```java
// Option A: CopyOnWriteArrayList (bei seltenem Schreiben, häufigem Lesen)
private final List<IndexItem> indexItems = new CopyOnWriteArrayList<>();

// Option B: explizites synchronized auf allen Zugriffsstellen
private final List<IndexItem> indexItems = new ArrayList<>();

private synchronized void addIndexItems(List<IndexItem> items) { indexItems.addAll(items); }
private synchronized void removeIndexItems(List<Long> ids) { ... }
// run() iteriert nur über synchronisierte Kopie
```

---

### A4 — `AccessFilter.getInstance` hat redundante verschachtelte `synchronized`-Blöcke [MEDIUM]

**Datei:** `security/authorization/AccessFilter.java`, Zeilen ~48–55

Das `synchronized` auf der Methode (Lock auf `AccessFilter.class`) ist vollständig redundant, da der innere `synchronized (INITIALIZING)`-Block die Initialisierung bereits schützt. Jede Authentifizierungsanfrage serialisiert dabei alle Threads durch den Class-Monitor.

**Aktuell (FALSCH):**
```java
public static synchronized AccessFilter getInstance(UserAccount user) {
    synchronized (INITIALIZING) { ... }
    return new AccessFilter(user);
}
```

**Empfohlen:**
```java
public static AccessFilter getInstance(UserAccount user) {  // ✅ synchronized entfernt
    synchronized (INITIALIZING) { ... }
    return new AccessFilter(user);
}
```

---

### A5 — `AccessFilter` Browsing-Permission-Cache: check-then-put ohne Atomarität [LOW]

**Datei:** `security/authorization/AccessFilter.java`, Zeilen ~150–165

Zwei parallele Threads für denselben `userId` können beide gleichzeitig `!containsKey()` sehen und beide `generateObjectAclSet()` aufrufen (eine `static synchronized` Methode). Die Doppelberechnung ist Verschwendung, kein Korrektheitsfehler; idiomatisches `computeIfAbsent` vermeidet ihn.

**Empfohlen:**
```java
userAclsWithBrowsePermissionCache.computeIfAbsent(userId, id -> generateObjectAclSet(...));
```

---

## Kategorie B: Bestätigte Logik-Bugs

### B1 — `IndexService` Konstruktor: `mkdirs()`-Ergebnisprüfung ist invertiert [HIGH]

**Datei:** `application/service/IndexService.java`, Zeilen ~70–75

`mkdirs()` gibt `true` bei **Erfolg** zurück. Die aktuelle Bedingung wirft eine Exception genau dann, wenn das Verzeichnis *erfolgreich* angelegt wurde, und fährt stillschweigend fort, wenn die Erstellung *fehlschlug*.

**Aktuell (BUG):**
```java
boolean madeDirs = indexPath.toFile().mkdirs();
if (madeDirs) {  // ❌ true = ERFOLG — Exception wird beim Erfolg geworfen!
    throw new IllegalStateException("Could not create path to index: " + ...);
}
```

**Empfohlen:**
```java
boolean madeDirs = indexPath.toFile().mkdirs();
if (!madeDirs && !indexPath.toFile().exists()) {  // ✅ Exception nur bei echtem Fehler
    throw new IllegalStateException("Could not create path to index: " + indexPath);
}
```

---

### B2 — `DeletionTask`: `lock.unlock()` im `finally` ohne sicherzustellen, dass der Lock gehalten wird [HIGH]

**Datei:** `application/DeletionTask.java`, Zeilen ~30–60

Wenn `tryLock()` `false` zurückgibt, verlässt `return` den `try`-Block, triggert `finally` und ruft `lock.unlock()` auf einem Lock auf, der nie gehalten wurde → `IllegalMonitorStateException`.

**Aktuell (BUG):**
```java
public void run() {
    try {
        boolean hasLock = lock.tryLock();
        if (!hasLock) {
            return;  // ← verlässt try → finally wird ausgeführt mit unlock ohne Lock!
        }
        try { ... } catch (Exception e) { ... }
    } finally {
        lock.unlock();  // ❌ auch aufgerufen wenn hasLock == false
    }
}
```

**Empfohlen:**
```java
public void run() {
    boolean hasLock = lock.tryLock();
    if (!hasLock) {
        return;  // ✅ früh heraus, kein finally
    }
    try {
        ...
    } finally {
        lock.unlock();  // ✅ nur hier, wenn Lock tatsächlich gehalten wird
    }
}
```

---

### B3 — `OsdServlet.getObjectsByFolderId`: Custom-Metadata wird zweifach pro OSD geladen [MEDIUM]

**Datei:** `application/servlet/OsdServlet.java`, Zeilen ~917–921

`checkPermissionAndAddCustomMetadata` lädt bereits für jedes OSD die Metadaten und setzt sie. Die nachfolgende `forEach` überschreibt das Ergebnis mit einer identischen DB-Abfrage. Bei 100 OSDs in einem Ordner verdoppelt das die DB-Aufrufe für Metadaten.

**Aktuell (BUG):**
```java
if (includeMeta) {
    checkPermissionAndAddCustomMetadata(filteredOsds, user); // ← lädt Metas und setzt sie
    filteredOsds.forEach(osd -> osd.setMetas(metaDao.listByOsd(osd.getId()))); // ← lädt sie NOCHMAL
}
```

**Empfohlen:** Die zweite `forEach`-Zeile vollständig entfernen.

---

### B4 — `OsdServlet.unlock`: `response.responseIsGenericOkay()` doppelt aufgerufen [LOW]

**Datei:** `application/servlet/OsdServlet.java`, Zeilen ~621–631

`responseIsGenericOkay()` wird im Erfolgs-Branch aufgerufen und dann nochmals bedingungslos danach. Da der `else`-Branch eine Exception wirft, trifft der zweite Aufruf immer den Erfolgspfad. Dead Code.

---

### B5 — `OsdServlet.copyOsd`: `setLatestHead`/`setLatestBranch` doppelt gesetzt [LOW]

**Datei:** `application/servlet/OsdServlet.java`, Zeilen ~335–339

```java
copy.setLatestHead(true);
copy.setLatestBranch(true);
...
copy.setLatestHead(true);    // ❌ identisches Duplikat
copy.setLatestBranch(true);  // ❌ identisches Duplikat
```

Beide Flags werden zweimal mit identischem Wert gesetzt. Außerdem sollte `latestBranch` auf dem Kopie-OSD basierend auf dem Versions-String gesetzt werden (wie in `newVersion()`), nicht bedingungslos auf `true`.

---

### B6 — `FolderServlet.loadFolders`: redundante Content-Prüfung mit falschen IDs [LOW]

**Datei:** `application/servlet/FolderServlet.java`, Zeilen ~148–154

```java
if (recursively) {
    for (Folder folder : folders) {
        List<Folder> subFolders = folderDao.getDirectSubFolders(folder.getId(), false);
        if (subFolders.size() > 0) {
            checkFoldersForContent(ids, deleteContent, folderDao); // ← `ids` sind ELTERN-IDs, nicht Unterordner-IDs!
        }
    }
}
```

Die Absicht ist, Unterordner auf Inhalt zu prüfen. Tatsächlich werden die ursprünglichen Eltern-IDs geprüft — Unterordner werden nie auf Inhalt geprüft, bevor sie in die Lösch-Menge aufgenommen werden.

---

## Kategorie C: Sicherheitsprobleme

### C1 — `HashMaker.createDigest` loggt den BCrypt-Hash auf DEBUG-Level [MEDIUM]

**Datei:** `security/HashMaker.java`, Zeile ~26

```java
log.debug("digest:{}", digest);
```

BCrypt-Hashes kodieren den Cost-Faktor und Salt im Klartext innerhalb des Hash-Strings. Das Loggen bei jeder Passwort-Erstellung/-Änderung gibt den Hash an jeden weiter, der Zugriff auf Debug-Logs hat. Selbst auf `DEBUG` sollte diese Zeile entfernt werden — Log-Dateien werden oft monatelang aufbewahrt und können durch Log-Aggregatoren indiziert werden.

**Empfohlen:** Die `log.debug`-Zeile vollständig entfernen.

---

### C2 — `OsdServlet.getContent`: Dateiname im `Content-Disposition`-Header nicht gegen CRLF gesäubert [MEDIUM]

**Datei:** `application/servlet/OsdServlet.java`, Zeile ~652

```java
response.setHeader(CONTENT_DISPOSITION,
    "attachment; filename=\"" + osd.getName().replace("\"", "%22") + "\"");
```

Nur Anführungszeichen werden maskiert. Ein OSD mit einem Namen, der `\r\n` enthält, erlaubt HTTP-Response-Header-Injection. Nicht-ASCII-Zeichen in Dateinamen verletzen RFC 6266.

**Empfohlen:**
```java
String safeName = osd.getName()
    .replace("\"", "%22")
    .replace("\r", "")
    .replace("\n", "");
// Für nicht-ASCII: RFC 5987 Encoding verwenden
response.setHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + safeName + "\"");
```

---

### C3 — `TestServlet` ist ein produktiver Endpunkt [MEDIUM]

**Datei:** `application/servlet/TestServlet.java`, registriert in `CinnamonServer.addServlets()`

`TestServlet` ist unter `/api/test/*` in der Produktion gemappt. Es bietet u. a. einen Endpunkt, der absichtlich eine `RuntimeException` wirft, sowie Status-Endpunkte. Diese Endpunkte sind authentifiziert (der `AuthenticationFilter` deckt `/api/*` ab), aber jeder gültige Benutzer kann sie auslösen.

**Empfohlen:** `TestServlet` aus dem Produktionscode und der Servlet-Registrierung entfernen. Für Integrationstests die Registrierung hinter einem Debug-/Test-Flag kapseln:
```java
if (config.isEnableTestEndpoints()) {
    addServlet(TestServlet.class, "/api/test/*");
}
```

---

### C4 — `StaticServlet`: Pfad-Traversal-Prüfung ist unvollständig [LOW]

**Datei:** `application/servlet/StaticServlet.java`, Zeilen ~30–32

```java
if (request.getRequestURI().contains("../")) {
    ErrorCode.STATIC__NO_PATH_TRAVERSAL.throwUp();
}
```

URL-kodierte Varianten werden nicht gestoppt: `..%2F`, `%2E%2E/`, `%2E%2E%2F`. Praktisch ist das Risiko gering (Ressourcen werden aus dem JAR-Classpath gelesen, nicht dem Filesystem), aber die Prüfung suggeriert falsche Sicherheit und sollte entweder vollständig sein oder durch einen Kommentar erklärt werden.

---

## Kategorie D: Ressourcen-Leaks

### D1 — `OsdServlet.storeFileUpload`: `FileInputStream` wird nie geschlossen [HIGH]

**Datei:** `application/servlet/OsdServlet.java`, Zeile ~709

```java
ContentMetadata metadata = contentProvider.writeContentStream(osd, new FileInputStream(tempOutputFile));
```

Der `FileInputStream` wird inline erstellt und an `writeContentStream` übergeben. Falls `writeContentStream` eine Exception wirft, wird der Stream nie geschlossen und der Dateideskriptor leckt.

**Empfohlen:**
```java
try (FileInputStream fis = new FileInputStream(tempOutputFile)) {
    ContentMetadata metadata = contentProvider.writeContentStream(osd, fis);
    ...
}
```

---

### D2 — `DeletionTask` nutzt `ThreadLocalSqlSession` in einem Background-Thread — Connection-Leak [HIGH]

**Datei:** `application/DeletionTask.java`, Zeilen ~37, 49, 52–53

`DeletionTask` wird an `CinnamonServer.executorService` (ein `ThreadPoolExecutor`) übergeben. Diese Hintergrundthreads werden nie von `DbSessionFilter` verwaltet. `ThreadLocalSqlSession.getSqlSession()` öffnet daher eine neue `SqlSession` auf einem Thread-Local des Hintergrundthreads. Diese Session wird commited, aber **nie geschlossen**. Die zugrundeliegende JDBC-Verbindung wird dauerhaft aus dem Pool ausgecheckt — eine pro Hintergrundthread, der jemals eine `DeletionTask` ausführt. Über Zeit erschöpft das den Connection-Pool.

**Empfohlen:** Das in `IndexService` verwendete Muster übernehmen:
```java
public void run() {
    boolean hasLock = lock.tryLock();
    if (!hasLock) { return; }
    try (SqlSession sqlSession = CinnamonServer.getSqlSession()) {  // ✅ explizite Session
        DeletionDao dao = new DeletionDao(sqlSession);
        ...
        sqlSession.commit();
    } finally {
        lock.unlock();
    }
}
```

---

## Kategorie E: Exception-Handling-Anti-Pattern

### E1 — `OsdServlet.newVersion`: unbekannte Exceptions werden auf generischen Fehlercode gemappt [MEDIUM]

**Datei:** `application/servlet/OsdServlet.java`, Zeilen ~1007–1010

```java
} catch (Exception e) {
    // TODO: should not handle unknown exceptions - those are probably bugs.
    errorCodes.add(ErrorCode.INTERNAL_SERVER_ERROR_TRY_AGAIN_LATER);
}
```

Jede `RuntimeException` (NPE, `ClassCastException` etc.) innerhalb der Metaset-Erstellungsschleife wird still verschluckt. Der Client sieht einen generischen Fehler; das Server-Log enthält nichts; das Exception-Objekt wird verworfen. Das mitgelieferte TODO bestätigt, dass dies falsch ist.

**Empfohlen:** Entweder weiterwerfen (dem äußeren Error-Handler überlassen) oder mindestens mit vollständigem Stack-Trace loggen:
```java
} catch (Exception e) {
    log.error("Unexpected exception during metaset creation for OSD {}", osd.getId(), e);
    errorCodes.add(ErrorCode.INTERNAL_SERVER_ERROR_TRY_AGAIN_LATER);
}
```

---

### E2 — `DbSessionFilter` / `RequestResponseFilter`: Zwei-Phasen-Commit — Änderungen sichtbar bevor Post-Trigger abgeschlossen [MEDIUM]

**Datei:** `filter/DbSessionFilter.java`, Zeile ~47 und `filter/RequestResponseFilter.java`, Zeile ~43

`RequestResponseFilter` committet die Session (Zeile 43) *bevor* `postCommitChangeTriggerHandler.executeTriggers()` (Zeile 44) aufgerufen wird. Alle DB-Schreibvorgänge eines Post-Commit-Triggers landen in einem zweiten, impliziten Commit durch `DbSessionFilter` (Zeile 47). Schlägt der Post-Commit-Trigger halbwegs fehl, sind seine partiellen Schreibvorgänge bereits in der Datenbank sichtbar (committed), während der Trigger-Fehler als Rollback behandelt wird. Das TODO-Kommentar in `DbSessionFilter` Zeile ~46 bestätigt diese Inkonsistenz.

**Empfohlen:** Post-Commit-Trigger vor dem finalen Commit ausführen, oder die Trigger-Logik in eine dedizierte, unabhängige Transaktion auslagern, die auch bei Fehler sauber rollback-fähig ist.

---

### E3 — `CinnamonServlet.CinnamonVersion`-Konstruktor fängt falsche Exception-Typ [LOW]

**Datei:** `application/servlet/CinnamonServlet.java`, Zeilen ~187–194

```java
try {
    properties.load(getClass().getResourceAsStream("/buildNumber.properties"));
    // ...
} catch (IOException e) {
    throw new CinnamonException("Could not load build number.");
}
```

Wenn `/buildNumber.properties` nicht im Classpath vorhanden ist, gibt `getResourceAsStream` `null` zurück. `properties.load(null)` wirft dann eine `NullPointerException`, **keine** `IOException` — der Catch-Block greift nicht, die NPE propagiert unkontrolliert und lässt die Servlet-Initialisierung crashen.

**Empfohlen:**
```java
InputStream stream = getClass().getResourceAsStream("/buildNumber.properties");
if (stream == null) {
    throw new CinnamonException("buildNumber.properties not found in classpath.");
}
try (stream) {
    properties.load(stream);
} catch (IOException e) {
    throw new CinnamonException("Could not load build number: " + e.getMessage());
}
```

---

## Kategorie F: API-Design / Konsistenzprobleme

### F1 — `FolderServlet.getSummaries` und `OsdServlet.getSummaries` verhalten sich unterschiedlich bei fehlenden Rechten [LOW]

**Datei:** `application/servlet/FolderServlet.java`, Zeilen ~514–518 vs. `OsdServlet.java`, Zeilen ~732–738

`FolderServlet.getSummaries` wirft `NO_BROWSE_PERMISSION` beim ersten Ordner ohne Berechtigung und bricht die gesamte Anfrage ab. `OsdServlet.getSummaries` überspringt OSDs ohne Berechtigung stillschweigend. Diese Inkonsistenz ist eine API-Überraschung für Clients.

**Empfohlen:** Einheitliches Verhalten definieren und in beiden Servlets gleich implementieren. Der "Partial-Result"-Ansatz (stillschweigendes Überspringen) ist benutzerfreundlicher; er sollte in `FolderServlet` übernommen werden, oder die Dokumentation muss den Unterschied explizit beschreiben.

---

### F2 — `FolderServlet.setSummary`: `validateRequest()` fehlt [LOW]

**Datei:** `application/servlet/FolderServlet.java`, Zeile ~493

```java
// TODO: add SetSummaryRequest.validateRequest
SetSummaryRequest summaryRequest = request.getMapper().readValue(..., SetSummaryRequest.class);
```

Jede andere Request-Verarbeitung in `FolderServlet` und `OsdServlet` kettet `.validateRequest().orElseThrow(...)`. Die fehlende Validierung führt bei einer Anfrage mit `null`-ID zu einer `NullPointerException` aus dem DAO statt einer sauberen `INVALID_REQUEST`-Fehlerantwort.

**Empfohlen:** `validateRequest()` auf `SetSummaryRequest` implementieren und nach dem Parsen aufrufen.

---

### F3 — `MetaService.updateMetadataChanged`: Unchecked Casts + String-basiertes Dispatch [MEDIUM]

**Datei:** `application/service/MetaService.java`, Zeilen ~159–165

```java
if (dao.getTypeClassName().equals(ObjectSystemData.class.getName())) {
    List<ObjectSystemData> osds = (List<ObjectSystemData>) ownables;  // ❌ Unchecked Cast
    ((OsdDao) dao).update(osds);
} else {
    List<Folder> folders = (List<Folder>) ownables;  // ❌ Unchecked Cast
    ((FolderDao) dao).update(folders);
}
```

Verwendet das bereits im DAO-Plan beschriebene String-Anti-Pattern, ergänzt durch unkontrollierte Casts, die bei jedem dritten `Ownable`-Typ eine `ClassCastException` zur Laufzeit erzeugen würden.

**Empfohlen:** Ein `updateOwnable(OwnableWithMetadata item)`-Interface oder eine überladene `update()`-Methode einführen, die das String-Dispatch und die Casts eliminiert:
```java
// In einem gemeinsamen Interface:
public interface OwnableUpdatable<T extends OwnableWithMetadata> {
    void update(List<T> items);
}
```

---

## Kategorie G: Authentifizierungs-Bug

### G1 — `LdapLoginProvider`: `passwordIsCorrect` ist immer `false` — LDAP-Login ist defekt [HIGH]

**Datei:** `security/LdapLoginProvider.java`, Zeilen ~48–51

```java
UserAccount user = userService.createOrUpdateUserAccount(username, cinnamonGroups, LoginType.LDAP, ...);
boolean passwordIsCorrect = HashMaker.compareWithHash(password, userAccount.getPasswordHash()); // ← benutzt 'userAccount', nicht 'user'!
return CinnamonLoginResult.createLoginResult(passwordIsCorrect, user.isNewUser());
```

`userAccount` ist das **dummy** `LoginUser`-Objekt, das in `CinnamonServlet.connect()` erstellt wird:
```java
UserAccount loginUser = new UserAccount();
loginUser.setName(username);
```

Dieses Dummy-Objekt hat `passwordHash = null`. `HashMaker.compareWithHash(password, null)` gibt `false` zurück. Damit ist `passwordIsCorrect` immer `false`, und `CinnamonLoginResult.createLoginResult(false, ...)` produziert ein Ergebnis mit `isValidUser() == false` — kein LDAP-Benutzer kann sich je einloggen.

**Empfohlen:**
```java
// LDAP hat das Passwort bereits gegen den LDAP-Server verifiziert.
// Hier muss nur noch der DB-User zurückgegeben werden.
return CinnamonLoginResult.createLoginResult(true, user.isNewUser());  // ✅
```

---

## Kategorie H: Statischer / Globaler Mutabler Zustand

### H1 — `CinnamonServer.config` und `executorService` sind `public static` mutable Felder [MEDIUM]

**Datei:** `application/CinnamonServer.java`, Zeilen ~75–77

```java
public static CinnamonConfig  config          = new CinnamonConfig();
public static ExecutorService executorService;
public static CinnamonStats   cinnamonStats   = new CinnamonStats();
```

Schreibbare globale Felder erlauben parallelen Integrationstests, diesen Zustand gegenseitig zu korrumpieren. `executorService` als `public` erlaubt beliebigen Klassen, arbitrary tasks einzureichen oder den Background-Thread-Pool zu shutdownen.

**Empfohlen:**
```java
private static CinnamonConfig  config = new CinnamonConfig();
private static ExecutorService executorService;

public static CinnamonConfig getConfig() { return config; }

// Nur testzwecke/konfiguration:
static void setConfig(CinnamonConfig newConfig) { config = newConfig; }
```

---

### H2 — `OsdServlet.getTikaMetasetTypeId()` Lazy-Init ist nicht thread-sicher [MEDIUM]

**Datei:** `application/servlet/OsdServlet.java`, Zeilen ~104–118

```java
private Long getTikaMetasetTypeId() {
    if (tikaMetasetTypeId == null) {  // ❌ nicht volatile, nicht synchronized
        ...
        tikaMetasetTypeId = tikaMetasetType.get().getId();
    }
    return tikaMetasetTypeId;
}
```

`OsdServlet` ist ein Servlet-Singleton. `tikaMetasetTypeId` ist ein Instanzfeld, das von konkurrenten Request-Threads ohne Synchronisation gelesen und geschrieben wird.

**Empfohlen:**
```java
private volatile Long tikaMetasetTypeId;  // Option A: volatile für einfaches Lazy-Init

// Option B: einmalige Initialisierung in init():
@Override
public void init() throws ServletException {
    super.init();
    // tikaMetasetTypeId einmalig laden
}
```

---

## Kategorie I: Build- / Dependency-Probleme

### I1 — `woodstox-core-asl` 4.4.1 ist vom aufgegebenen Codehaus-Fork [LOW]

**Datei:** `pom.xml`, Zeile ~169

```xml
<groupId>org.codehaus.woodstox</groupId>
<artifactId>woodstox-core-asl</artifactId>
<version>4.4.1</version>
```

Die `org.codehaus.woodstox`-Gruppe wird nicht mehr gewartet. Der aktiv gewartete Artefakt ist `com.fasterxml.woodstox:woodstox-core` (5.x / 7.x). Die veraltete Version kann nicht behobene CVEs enthalten und ist bereits eine transitive Dependency über `jackson-dataformat-xml`.

**Empfohlen:** Explizite Abhängigkeit entfernen (Jackson verwaltet sie als transitive Dep) oder ersetzen:
```xml
<dependency>
    <groupId>com.fasterxml.woodstox</groupId>
    <artifactId>woodstox-core</artifactId>
    <version>7.1.0</version>
</dependency>
```

---

### I2 — `httpcomponents.version`-Eigenschaft referenziert `4.5.14`, aber verwendeter Client ist `httpclient5` (`5.5`) [LOW]

**Datei:** `pom.xml`, Zeilen ~23 und ~213

```xml
<httpcomponents.version>4.5.14</httpcomponents.version>
...
<artifactId>httpclient5</artifactId>
<version>5.5</version>
```

Die Property `httpcomponents.version` deklariert `4.5.x` (HttpClient 4-Serie), aber die tatsächliche Dependency nutzt `5.5` (HttpClient 5-Serie) mit einer hardcodierten Version. Die Property wird nicht verwendet — das schafft Wartungsverwirrung.

**Empfohlen:**
```xml
<!-- Entweder: Property korrekt benennen und nutzen -->
<httpclient5.version>5.5</httpclient5.version>
...
<version>${httpclient5.version}</version>

<!-- Oder: veraltete Property entfernen -->
```

---

## Kategorie J: Toter Code und fehlplatzierten Belangen

### J1 — `OsdServlet`-Konstruktor ruft `ThreadLocalSqlSession.refreshSession()` auf [MEDIUM]

**Datei:** `application/servlet/OsdServlet.java`, Zeilen ~66–69

```java
public OsdServlet() {
    super();
    ThreadLocalSqlSession.refreshSession();  // ❌ Toter Code im Servlet-Konstruktor
}
```

Servlet-Konstruktoren werden einmalig vom Container während der Servlet-Registrierung aufgerufen, nicht pro Anfrage. `refreshSession()` öffnet eine frische DB-Session auf dem ThreadLocal des Initialisierungs-Threads — ein Thread, der nie Anfragen verarbeitet. Die hier erstellte Session wird weder genutzt noch geschlossen und verschwendet eine DB-Verbindung.

**Empfohlen:** Den `ThreadLocalSqlSession.refreshSession()`-Aufruf aus dem Konstruktor entfernen.

---

### J2 — `DbSessionFilter.deletionDao` ist eine gemeinsam genutzte Instanz — inkonsistent mit dem Rest des Codes [LOW]

**Datei:** `filter/DbSessionFilter.java`, Zeile ~32

```java
private final DeletionDao deletionDao = new DeletionDao();
```

Dieses Feld wird einmalig bei der `DbSessionFilter`-Erstellung initialisiert und über alle Anfragen geteilt. Jede andere DAO-Nutzung im Code instantiiert das DAO pro Handler-Methode. Das Muster ist inkonsistent und kann Maintainer verwirren.

---

### J3 — `CinnamonResponse.renderResponseIfNecessary`: Response wird zweifach in `String` dann `byte[]` serialisiert [LOW]

**Datei:** `application/CinnamonResponse.java`, Zeilen ~107–108

```java
responseAsString = objectMapper.writeValueAsString(wrapper != null ? wrapper : response);
servletResponse.getOutputStream().write(responseAsString.getBytes());
```

Jede Response wird zuerst in einen heap-allozierten `String` serialisiert, bevor sie in den Output-Stream geschrieben wird. Für große XML-Responses (z. B. hunderte OSD-Objekte mit Metadaten) verdreifacht das den Peak-Speicherbedarf: einmal im `ObjectMapper`-Buffer, einmal im `String`, einmal im `byte[]` aus `getBytes()`.

**Empfohlen:** Direkt in den `OutputStream` serialisieren:
```java
objectMapper.writeValue(servletResponse.getOutputStream(), wrapper != null ? wrapper : response);
```
`responseAsString` nur behalten, wenn er von der Change-Trigger-Inspektion tatsächlich benötigt wird, und dann nur auf diesem Pfad befüllen.

---

## Prioritäts-Implementierungsfahrplan

### Phase 1: Sofort-Fixes (Woche 1) — Kritische Bugs ohne Verhaltensänderung

Diese Korrekturen sind trivial in der Umsetzung und eliminieren die schwerwiegendsten Risiken:

1. **G1** — LDAP-Login-Bug beheben: `userAccount` durch `user` ersetzen (`LdapLoginProvider.java`) — 1 Wort
2. **B1** — Invertierte `mkdirs()`-Prüfung korrigieren (`IndexService.java`) — Vorzeichen invertieren
3. **B2** — `DeletionTask.lock.unlock()` in `finally` ohne Lock fixieren — Lock-Scope korrigieren
4. **D1** — `FileInputStream`-Leak in `OsdServlet.storeFileUpload` schließen — try-with-resources
5. **A1 + A2** — `stopped` und `isInitialized` als `volatile` markieren (`IndexService.java`) — 2 Keywords

**Aufwand:** ~2 Stunden  
**Risiko:** Sehr niedrig — keine Logikänderungen außer beim LDAP-Bug

---

### Phase 2: Thread-Safety-Fixes (Woche 2)

1. **A3** — `indexItems` auf `CopyOnWriteArrayList` umstellen
2. **H2** — `tikaMetasetTypeId` als `volatile` markieren oder in `init()` initialisieren
3. **A4** — Redundantes `synchronized` von `AccessFilter.getInstance` entfernen
4. **D2** — `DeletionTask` auf explizite `SqlSession` umstellen (folgt dem `IndexService`-Muster)
5. **H1** — `CinnamonServer.config` / `executorService` auf `private static` beschränken

**Aufwand:** ~4 Stunden  
**Risiko:** Niedrig — Verhalten identisch; nur Synchronisation und Session-Lifecycle-Scope ändern sich

---

### Phase 3: Sicherheits- und Fehlerbehebungen (Woche 3)

1. **C1** — BCrypt-Hash-Logging entfernen
2. **C2** — CRLF-Sanitisierung im `Content-Disposition`-Header
3. **C3** — `TestServlet` hinter Konfigurations-Flag kapseln
4. **B3** — Doppeltes Metadata-Laden in `OsdServlet.getObjectsByFolderId` entfernen
5. **E1** — Unbekannte Exceptions mit Stack-Trace loggen statt still verschlucken
6. **E3** — `NullPointerException` bei fehlendem `buildNumber.properties` korrekt behandeln
7. **J1** — `ThreadLocalSqlSession.refreshSession()` aus `OsdServlet`-Konstruktor entfernen

**Aufwand:** ~4 Stunden  
**Risiko:** Sehr niedrig

---

### Phase 4: API-Konsistenz und Architektur (Woche 4)

1. **F3** — Unchecked Casts in `MetaService` durch Type-safe Interface ersetzen
2. **F2** — `validateRequest()` auf `SetSummaryRequest` implementieren
3. **F1** — Einheitliches `getSummaries`-Verhalten in `FolderServlet` und `OsdServlet`
4. **J3** — `CinnamonResponse` direkt in `OutputStream` serialisieren
5. **E2** — Post-Commit-Trigger-Reihenfolge dokumentieren und ggf. Transaktions-Architektur klären
6. **I1 + I2** — Dependency-Bereinigung in `pom.xml`

**Aufwand:** ~8 Stunden  
**Risiko:** Niedrig bis mittel für F3 (braucht Interface-Änderung); niedrig für den Rest

---

## Test-Strategie für neue Findings

### Integrationstests (empfohlen)

```java
// G1 — LDAP-Login-Bug (nach Fix prüfen ob Login erfolgreich)
@Test
public void testLdapLogin_NewUser_ReturnsValidUser() throws Exception {
    // LDAP-Mock konfigurieren der Credentials akzeptiert
    CinnamonLoginResult result = ldapLoginProvider.connect(loginUser, "correctPassword", ...);
    assertTrue(result.isValidUser(), "LDAP-Benutzer muss sich einloggen können");
}

// D2 — DeletionTask Connection-Leak (nach mehreren Läufen Pool-Erschöpfung messen)
@Test
public void testDeletionTask_NoConnectionPoolExhaustion() throws Exception {
    int poolSizeBefore = getActiveConnections();
    for (int i = 0; i < 10; i++) {
        new DeletionTask().run();
    }
    assertEquals(poolSizeBefore, getActiveConnections(), "Keine Connection darf lecken");
}

// B3 — Doppeltes Meta-Laden (DB-Aufruf-Anzahl verifizieren)
@Test
public void testGetObjectsByFolderId_MetaLoadedExactlyOnce() {
    // SQL-Counter aktivieren; metaDao.listByOsd Aufrufzahl messen
    int osdCount = 5;
    osdServlet.getObjectsByFolderId(request, response);
    assertEquals(osdCount, metaQueryCounter.get(), "Metas dürfen nur einmal pro OSD geladen werden");
}
```

### Unit-Tests (für Pure-Java-Logik sinnvoll)

```java
// B2 — DeletionTask unlock-Bug
@Test
public void testDeletionTask_DoesNotUnlockUnheldLock() {
    ReentrantLock lock = new ReentrantLock();
    // DeletionTask mit besetztem Lock starten (tryLock() gibt false zurück)
    lock.lock();
    Thread other = new Thread(new DeletionTask(lock, ...));
    other.start();
    other.join(1000);
    assertFalse(other.isAlive() && other.getState() == Thread.State.BLOCKED,
        "Task darf keine IllegalMonitorStateException werfen");
}

// A1/A2 — volatile Sichtbarkeit (JVM-Memory-Model)
@Test
public void testIndexService_StoppedFlagVisibleAcrossThreads() throws Exception {
    IndexService service = new IndexService(...);
    Thread t = new Thread(service);
    t.start();
    Thread.sleep(100);
    service.setStopped(true);
    t.join(2000);
    assertFalse(t.isAlive(), "IndexService-Thread muss nach setStopped(true) enden");
}
```

---

## Gesamtübersicht aller Findings

| # | Kategorie | Schweregrad | Datei (gekürzt) | Beschreibung |
|---|-----------|-------------|-----------------|--------------|
| A1 | Thread-Safety | HIGH | `IndexService.java:57` | `stopped` nicht `volatile` |
| A2 | Thread-Safety | HIGH | `IndexService.java:56` | `isInitialized` nicht `volatile` |
| A3 | Thread-Safety | HIGH | `IndexService.java:638` | `indexItems` ohne Synchronisation beschreibbar |
| A4 | Thread-Safety | MEDIUM | `AccessFilter.java:48` | Redundante `synchronized`-Blöcke |
| A5 | Thread-Safety | LOW | `AccessFilter.java:152` | Race im check-then-put Cache-Pattern |
| B1 | Logik-Bug | HIGH | `IndexService.java:73` | Invertierte `mkdirs()`-Prüfung |
| B2 | Logik-Bug | HIGH | `DeletionTask.java:59` | `unlock()` ohne Lock → `IllegalMonitorStateException` |
| B3 | Logik-Bug | MEDIUM | `OsdServlet.java:919` | Metadata zweifach pro OSD geladen |
| B4 | Logik-Bug | LOW | `OsdServlet.java:631` | `responseIsGenericOkay()` doppelt aufgerufen |
| B5 | Logik-Bug | LOW | `OsdServlet.java:336` | `latestHead`/`latestBranch` doppelt gesetzt |
| B6 | Logik-Bug | LOW | `FolderServlet.java:151` | Redundante Content-Prüfung mit falschen IDs |
| C1 | Sicherheit | MEDIUM | `HashMaker.java:26` | BCrypt-Hash auf DEBUG geloggt |
| C2 | Sicherheit | MEDIUM | `OsdServlet.java:652` | CRLF-Injection in `Content-Disposition` |
| C3 | Sicherheit | MEDIUM | `TestServlet.java` | Test-Endpunkt in Produktion aktiv |
| C4 | Sicherheit | LOW | `StaticServlet.java:30` | Unvollständige Pfad-Traversal-Prüfung |
| D1 | Ressourcen-Leak | HIGH | `OsdServlet.java:709` | `FileInputStream` nicht geschlossen |
| D2 | Ressourcen-Leak | HIGH | `DeletionTask.java:37` | `ThreadLocalSqlSession` im Hintergrundthread → Connection-Leak |
| E1 | Exception-Handling | MEDIUM | `OsdServlet.java:1007` | Unbekannte Exceptions still verschluckt |
| E2 | Exception-Handling | MEDIUM | `DbSessionFilter.java:47` | Post-Trigger-Änderungen vor Trigger-Abschluss sichtbar |
| E3 | Exception-Handling | LOW | `CinnamonServlet.java:191` | Falscher Exception-Typ gefangen (`IOException` vs `NPE`) |
| F1 | API-Konsistenz | LOW | `FolderServlet.java:517` | Unterschiedliches Verhalten bei fehlenden Rechten (Fehler vs. Überspringen) |
| F2 | API-Konsistenz | LOW | `FolderServlet.java:493` | `validateRequest()` auf `SetSummaryRequest` fehlt |
| F3 | API-Design | MEDIUM | `MetaService.java:159` | Unchecked Casts + String-Dispatch für Typ-Routing |
| G1 | Auth-Bug | HIGH | `LdapLoginProvider.java:50` | LDAP-Login immer fehlschlagend (`passwordIsCorrect` immer `false`) |
| H1 | Globaler Zustand | MEDIUM | `CinnamonServer.java:75` | `config` / `executorService` als `public static` mutable |
| H2 | Globaler Zustand | MEDIUM | `OsdServlet.java:104` | Lazy `tikaMetasetTypeId`-Init nicht thread-sicher |
| I1 | Dependency | LOW | `pom.xml:169` | `woodstox-core-asl` ist aufgegebener Codehaus-Fork |
| I2 | Dependency | LOW | `pom.xml:23` | Ungenutzte Property `httpcomponents.version=4.5.14` |
| J1 | Toter Code | MEDIUM | `OsdServlet.java:68` | `refreshSession()` im Servlet-Konstruktor |
| J2 | Toter Code | LOW | `DbSessionFilter.java:32` | Gemeinsame `DeletionDao`-Instanz, inkonsistent mit Rest |
| J3 | Performance | LOW | `CinnamonResponse.java:107` | Response zweifach serialisiert (`String` + `byte[]`) |

---

**Geschätzter Gesamtaufwand:** 18–20 Entwicklerstunden für alle Punkte; die 7 HIGH-Severity-Findings (G1, B1, B2, D1, D2, A1, A2) können in ~3 Stunden behoben werden und eliminieren die schwerwiegendsten Risiken sofort.

