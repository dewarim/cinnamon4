# Changelog

Detailed record of notable changes, with the reasoning behind them.

This complements the terse per-release notes in `docs/changes/<date>__<version>.txt`: those say
*what* shipped in a release, this says *why* and *how* for changes where the reasoning matters.
Newest first.

---

## Unreleased (after v2.0.17)

### Security review of parsers on user-supplied data paths

A review of the parsers reachable from user input (XML, HTTP, Lucene), since users can supply
arbitrary content and metadata. Findings and fixes below. Benchmarks for the search rework are in
`docs/ai-improvement/benchmark.md`.

#### Removed `woodstox-core-asl` 4.4.1 (commit `8b8b5c41`)

The build pulled in an unused `org.codehaus.woodstox:woodstox-core-asl:4.4.1` alongside Jackson's
current `com.fasterxml.woodstox:woodstox-core:7.2.1`. Both declare the same 200 `com.ctc.wstx.*`
classes, so classpath order decided which XML parser implementation actually ran - a 2011 parser
could silently shadow the current one. Removed, together with its transitive
`javax.xml.stream:stax-api:1.0-2`. SBOM components: 50 -> 48.

#### XML parser limits are now explicit and configurable (commit `00b9b2cc`)

User-supplied XML (document content, metadata) is parsed with dom4j/SAX in `ParamParser`. The JDK's
JAXP secure-processing limits - entity expansion, total entity size, element depth, attribute count
and so on - were left at whatever the running JRE's `conf/jaxp.properties` happened to contain. That
made protection against entity-expansion bombs ("billion laughs"), quadratic blowup and deeply
nested XML a property of the JRE *installation* rather than of Cinnamon.

These limits are now set explicitly on the `SAXReader` from a new `XmlParserConfig`
(`<xmlParserConfig>` in the server config). Defaults match JDK 25; `0` means "no limit".

| setting | default |
|---|---|
| `entityExpansionLimit` | 2500 |
| `totalEntitySizeLimit` | 100000 |
| `maxGeneralEntitySizeLimit` | 100000 |
| `maxParameterEntitySizeLimit` | 15000 |
| `entityReplacementLimit` | 100000 |
| `elementAttributeLimit` | 200 |
| `maxElementDepth` | 100 |
| `maxXMLNameLimit` | 1000 |

Note: `ParamParser.DOCTYPE_OR_ENTITY_PATTERN` strips a DOCTYPE only once and can be bypassed by
nesting (`<!DOC<!ENTITY>TYPE`). The parser limits, not that regex, are the actual protection - which
is why making them explicit matters. Request bodies (Jackson/Woodstox) were already safe: DTDs are
disabled there, and all XXE and entity-bomb probes were rejected in milliseconds.

#### Search survives deeply nested queries (commit `00b9b2cc`)

Lucene's recursive-descent query and regex parsers overflow the stack on deeply nested input (for
example 200 000 nested parentheses in a `RegexQuery`). `SearchService` now catches
`StackOverflowError` from `CoreParser.parse` and converts it into a `CinnamonException`, so a hostile
query fails like any other unparseable query instead of producing a raw 500 and an unstable thread.

`IndexSearcher.setMaxClauseCount(10000)` was deliberately left unchanged: the Windows client builds
very complex queries with many thousands of clauses, so lowering it is not an option.

#### HTTP request body size limit and reduced information disclosure (commit `97977b7c`)

New `HttpLimitsConfig` (`<httpLimitsConfig>`). Sizes in bytes, `0` means unlimited.

| setting | default | effect |
|---|---|---|
| `maxRequestBodySize` | 67108864 (64 MB) | bounds the in-memory read of non-multipart bodies |
| `maxMultipartRequestSize` | 0 | total size cap for content uploads |
| `maxMultipartFileSize` | 0 | per-file cap for content uploads |
| `multipartFileSizeThreshold` | 1048576 | spill multipart parts to disk above this |
| `sendServerVersion` | false | suppresses the `Server: Jetty(...)` header |
| `showStackTraces` | false | keeps stack traces and version off error pages |
| `requestHeaderSize` | 8192 | maximum HTTP request header size |

- **Unauthenticated heap exhaustion.** `CinnamonRequest` buffered non-multipart bodies with
  `getInputStream().readAllBytes()`, unbounded. A 700 MB body sent to `/cinnamon/connect` - which
  requires no authentication - drove the server into `OutOfMemoryError`. The new
  `readBodyBounded()` rejects an over-limit declared `Content-Length` up front *and* enforces the cap
  while reading, because `Content-Length` may be absent (chunked) or untruthful. Over-limit requests
  now get the new `REQUEST_PAYLOAD_TOO_LARGE` (HTTP 413) in about 0.1 s with no OOM.
- **Error pages.** A custom ee10 `ErrorHandler` on the webapp context disables `showStacks`,
  `showMessageInTitle` and `showCauses`, so 404 and 500 pages no longer disclose stack traces, the
  exact Jetty version or the "Powered by Jetty" footer.
- **Server header.** `createHttpConnector` previously had no `HttpConfiguration` at all, so the plain
  HTTP connector leaked the version header even though the HTTPS one did not. Both connectors now
  share one `baseHttpConfiguration()`.

> **Trap worth remembering:** `MultipartConfigElement` treats `0` as *zero bytes allowed*; unlimited
> is `-1`. Passing the config default `0` straight through rejected **every** file upload.
> `addServletWithMultipartConfig` therefore translates `<= 0` to `-1`.

#### Search: collect ids instead of documents, push ACL filtering into the query

The largest change of the review. Measurements: `docs/ai-improvement/benchmark.md`.

**Problem.** `ResultCollector` materialised one Lucene `Document` per hit - about 460-477 bytes each,
measured - into a `HashSet`, with no upper bound, and ACL filtering ran *afterwards* in Java. Any
authenticated user, *including one with no browse permissions at all*, could therefore make the
server load every stored document in the index into the heap with a cheap query such as
`<WildcardQuery fieldName="name">*</WildcardQuery>`. On a 10M-object repository that is roughly
4.5 GB. There was no result limit at any layer: `SearchServlet.searchIds` passes the query straight
through and `SearchIdsRequest` has no page size.

Three supporting defects: `Document` overrides neither `equals` nor `hashCode`, so the `HashSet` had
identity semantics and deduplicated nothing; the `Set<Integer> hits` field was populated on every hit
but never read; and `searcher.doc(doc + docBase)` resolved the leaf through the top-level reader on
every single hit.

**Why not simply cap the result set.** Callers need exhaustive sets - "every image contained in a
technical DITA handbook" - and a user legitimately may own 10 million documents. Truncating or
erroring would break the feature. So the fix had to make completeness *affordable* rather than
forbid it.

**What was done.**

1. **ACL and type filters pushed into the Lucene query.** `BrowsableAcls.hasBrowsePermission` is
   `objectAcls.contains(aclId) || (owner.equals(userId) && ownerAcls.contains(aclId))`, and `acl`,
   `owner` and `id` are all indexed as `LongPoint` while `cinnamon_class` is a `StringField`. That
   predicate therefore translates exactly into
   `(acl IN objectAcls) OR (owner == userId AND acl IN ownerAcls)`. Browse permissions are now
   resolved *before* searching, so Lucene never collects a document the user may not browse. A user
   who can browse nothing skips the search entirely.
   - Verified equivalent to the Java predicate over randomised ACL/owner distributions.
   - `LongPoint.newSetQuery` builds a single `PointInSetQuery`, **not** one boolean clause per value,
     so even thousands of ACLs cost **zero** clauses and cannot collide with the high
     `maxClauseCount` the Windows client needs. A 5000-ACL filter was verified to work with
     `maxClauseCount` forced to 8.
2. **Only ids are retained**, in a growable `long[]`: 8-10 bytes per hit instead of ~460.
3. **New `NumericDocValuesField("id")`** on OSD and folder documents, so the id of each hit is read
   from a columnar per-segment iterator instead of a decompressed stored field. Falls back to the
   stored field when the field is absent, so an index written by an older version keeps working.
4. **`ResultCollectorManager`** replaces the deprecated `search(Query, Collector)` with
   `search(Query, CollectorManager)`, and merges per-slice results by sorting and deduplicating.
   Results are now **deterministically ordered**; the previous `HashSet` iteration order was not.
   Deduplication is by Cinnamon id, which is what the old `Collectors.toSet()` effectively did, and
   still guards against a stale duplicate document for an id (see issue #414).
5. `SearchType.ALL` runs one search per type instead of one search plus a post-hoc class filter.
6. Removed the dead `hits` set and the per-hit `log.debug` call, which wrote one log line per
   matching document when debug logging was enabled.

**Effect:** 1M-document match-all search goes from 2 851 ms / 437 MB to 19 ms / 8.2 MB; with a user
who may browse ~5% of documents, from 2 799 ms / 437 MB to 3 ms / 0.3 MB.

**Upgrade note:** a **reindex is recommended** after upgrading, so documents gain the `id`
NumericDocValues field. Search works without it - the collector falls back to stored fields - but the
speed-up for large result sets depends on it. Existing indexes improve as they are rebuilt.

#### CJK text is searchable: new CinnamonAnalyzer

Cinnamon aims at technical documentation - machine manuals and the like - and customers plausibly
handle documents originating from Chinese partner factories. Such documents are mixed-script:
Chinese prose with Latin part numbers, model codes and units.

`StandardTokenizer` follows UAX#29 and therefore emits **one token per Han character**, so
`液压泵` ("hydraulic pump") was indexed as `[液, 压, 泵]`. A `TermQuery` for the word itself matched
nothing, because no such term existed, and relevance was poor because single Han characters occur in
enormous numbers of compounds. Chinese and Japanese word search was effectively broken.

New `CinnamonAnalyzer`, used by **both** `IndexService` and `SearchService` (they must agree, or
analyzed queries stop matching what was indexed):

```
StandardTokenizer -> CJKWidthFilter -> LowerCaseFilter -> CJKBigramFilter
```

- `CJKBigramFilter` indexes overlapping bigrams of CJK runs: `液压泵` -> `[液压, 压泵]`.
- `CJKWidthFilter` folds full-width Latin to ASCII and half-width Katakana to full width. Documents
  authored in Asia routinely write part numbers in full width, so **a search for `ABC-123` now finds
  a document containing `ＡＢＣ－１２３`**, which it did not before. This is a worthwhile gain on its own,
  independent of Chinese word search.
- New `<cjkOutputUnigrams>` in `luceneConfig`, default **false**. Words of two or more characters -
  practically all CJK vocabulary and all technical terminology - are searchable without it. Turning
  it on also indexes every single CJK character, which roughly doubles the number of CJK terms; it is
  only needed where a lone character must be findable as a term of its own, as in a parts list.

**Latin content is unaffected.** `CJKBigramFilter` only combines tokens whose script is Han,
Hiragana, Katakana or Hangul; everything else passes through untouched. `CinnamonAnalyzerUnitTest`
asserts the output is byte-for-byte identical to `StandardAnalyzer`'s for German and English input,
including part numbers such as `P/N 4711-A` and `XJ-900/B rev. 2.1`, because that was the entire risk
of the change.

**Not** Lucene's ready-made `CJKAnalyzer`: it bundles an English stop-word list, which drops the
trailing `A` of `P/N 4711-A` - in technical documentation usually a variant or revision designator.
A test pins that down, including a check that the stock analyzer really does drop it, so nobody
"simplifies" this later.

No new dependency: all three filters are already in `lucene-analysis-common`. The 255-character
token-length cap that `StandardAnalyzer` applies is preserved explicitly and asserted by a test,
since it is what stops a single million-character "word" from being buffered whole.

Effect on existing clients is an improvement with no regression. A `TermQuery` for a two-character
word now matches, because its bigram term is exactly that word; a `TermQuery` for a three-character
word still does not match, but it did not before either (the index held single characters).
Phrase and analyzed (`UserQuery`) searches become considerably more precise.

**Upgrade note:** requires a reindex, as does any later change to `cjkOutputUnigrams`.

Deliberately not done: `ICUTokenizer`, `SmartChineseAnalyzer` (Simplified Chinese only) and Kuromoji
(Japanese only) all offer dictionary-grade segmentation and better precision, but each adds a
dependency, and ICU in particular reintroduces exactly the Unicode implementation complexity whose
absence made Cinnamon's analysis chain easy to clear during this review. For occasional documents
from Chinese partners, bigrams are the better trade. If a customer eventually needs dictionary-grade
Chinese search, that is the point at which per-language analyzers earn their keep - as a refinement
on top of this, not instead of it.

#### Deprecated Lucene APIs removed

`javac -Xlint:deprecation` over all main sources reported exactly two deprecated Lucene usages, both
removed by the search rework:

- `IndexSearcher.doc(int)` -> per-leaf `LeafReader.storedFields()` with leaf-local docIds
- `IndexSearcher.search(Query, Collector)` -> `search(Query, CollectorManager)`

No deprecated Lucene API remains. (Two unrelated warnings remain in
`MicroserviceChangeTrigger`, from Apache HttpClient 5's SSL builder.)

#### Confirmed safe, no change needed

- **Tokenizers on adversarial Unicode.** `StandardAnalyzer` is linear and bounded: token length is
  capped at 255 characters, and there is no ICU dependency. See the table in
  `docs/ai-improvement/benchmark.md`.
- **`RegexpQuery` / `WildcardQuery` cost.** Both are built without an explicit determinize work
  limit, so they use Lucene's default of 10 000 and raise `TooComplexToDeterminizeException`, which
  `doSearch` already converts into a `CinnamonException`.
- **OSD and folder ids are globally unique.** Both `FolderMapper.xml` and
  `ObjectSystemDataMapper.xml` draw from the single `seq_folder_and_object_ids` sequence; the older
  per-type `seq_object_id` and `seq_folder_id` are dropped by the schema, and `docs/migration.md`
  creates the combined sequence starting at 10000000, above the previous maxima. Search does not
  depend on this - each type is queried separately and deduplicated within its own result set - but
  it confirms an id collision between an OSD and a folder cannot occur.

#### Indexable content size is now limited

`IndexService.indexOsd` read file content with an unbounded `readAllBytes()`. Indexing then converts
that to a String and parses it into a dom4j DOM, needing roughly **ten times** the file size in heap,
with up to `threadPoolSize` (8) documents in flight at once - so a single large upload could exhaust
the heap. Two new settings in `luceneConfig`:

| setting | default | meaning |
|---|---|---|
| `maxIndexableContentSizeMB` | 2048 | absolute limit, in **megabytes**; 0 = no limit |
| `maxIndexableContentPercentOfHeap` | 0 | the same limit as a percentage of maximum heap; 0 = not used |

If both are set the **smaller** applies, so the absolute value is a ceiling and the percentage a
safety net on machines with a small heap. The percentage is a share of the JVM's maximum heap
(`Runtime.maxMemory()`), not of physical RAM, because running out of memory while indexing is a heap
event - RAM beyond `-Xmx` cannot help.

Over-sized content is **skipped rather than failed**: the object is still indexed with its metadata,
so it stays findable by name and metadata, and only its content is not searchable. A warning naming
the object and both sizes is logged. Failing the job instead would have made the object invisible
entirely and then retried forever.

The check uses the recorded `contentSize` before the stream is even opened, and `readContentBounded`
enforces the limit again while reading, for the case where that recorded size is missing or wrong.

Note when choosing a value: with the ~10x multiplier, 2048 MB of content needs considerably more than
2 GB of heap. The default is a backstop against pathological cases, not a tight bound.

#### Smaller cleanups

- **Locale-dependent `toLowerCase()` fixed in all eight places**, now `toLowerCase(Locale.ROOT)`:
  `IndexService.addOsdFields` / `addFolderFields` (`name`, `folderpath`), `CompleteStringIndexer`,
  `BooleanIndexer` - and, found while fixing those, `CinnamonRequest` and `OsdServlet`, which
  lowercase the **Content-Type header** to detect multipart requests. HTTP header values are
  case-insensitive, so a client sending `MULTIPART/form-data` to a Turkish-locale server would have
  produced `multıpart/` with a dotless i, the request would not have been recognised as multipart,
  and **file upload would have broken** - a worse consequence than the indexing issue that led here.
  `CinnamonAnalyzerUnitTest` asserts analysis is identical under `ROOT`, `en_US`, `de_DE`, `tr_TR`,
  `az` and `lt`.
- **`LimitTokenCountAnalyzer` now actually limits.** It was wired to `Integer.MAX_VALUE`, making the
  wrapper pure decoration. New `maxQueryTokenCount` (default 100 000, 0 = no limit) bounds how many
  tokens one analyzed query clause may produce. Deliberately generous: the Windows client builds very
  complex queries and never comes close.
- **Redundant searcher-refresh guard removed.** `doSearch` and `countDocs` wrapped
  `maybeRefreshBlocking()` in `if (!isSearcherCurrent())`, duplicating the check that method performs
  itself - and inconsistently with `isIndexed`, which always called it unconditionally.
- **Dead `indexReader` field removed** from `SearchService`. `SearcherManager` takes ownership of the
  reader and reopens it on refresh, so the field was stale after the first refresh. Now a local
  variable.

### Known issues, not yet addressed

Recorded here so they are not rediscovered from scratch.

- ~~**Unbounded content read while indexing.**~~ Fixed, see "Indexable content size is now limited".
- ~~**`LimitTokenCountAnalyzer(analyzer, Integer.MAX_VALUE)`**~~ Fixed, see "Smaller cleanups".
- ~~**Locale-dependent lowercasing.**~~ Fixed in all eight places, see "Smaller cleanups". The
  measurements behind it are kept here because they are the reference for what locale can and cannot
  affect.

  Measured exhaustively rather than assumed. Every Unicode codepoint against every language the JVM
  knows (1 112 064 x 245 = 272 455 680 comparisons), plus every cased codepoint followed by every
  combining mark (1460 x 2020 x 245 = 722 554 000 comparisons):

  - The affected languages are exactly **`tr`, `az`, `lt`** - nothing else in Unicode or in the JVM's
    245 languages, contextually or otherwise.
  - `tr`/`az`: `U+0049 I` and `U+0130 İ`, i.e. **plain ASCII** - ordinary names like `INVOICE`,
    `TITLE`, `DIAGRAM` are affected.
  - `lt`: `I`/`J` followed by **any** combining mark inserts `U+0307` (7611 such sequences). Not only
    the precomposed `Ì Í Ĩ`.
  - Only 1460 of 1 112 064 codepoints (0.13%) have any lowercase mapping at all. **Emoji, CJK, kana
    and Hangul are not cased**, so they are identical under every locale - searching for `中文💩` is
    unaffected by the server locale.

  Scope is narrower than it first looks: analyzed fields go through Lucene's `LowerCaseFilter`, which
  is locale-independent (`"Bericht 中文 INVOICE"` -> `[bericht, 中, 文, invoice]` in any locale). Only
  the four hand-lowercased places above can be affected.

  Caveat: `Locale.ROOT` removes the machine dependency but does **not** make the two
  mechanisms identical. For `U+0130 İ`, `String.toLowerCase(ROOT)` yields `i` + `U+0307` (Unicode
  full case mapping, two codepoints) while `LowerCaseFilter` yields `i` (`Character.toLowerCase` is
  per-codepoint and cannot expand). Harmless for `name`/`folderpath`, which are their own
  non-analyzed fields matched exactly, but do not assume the two become interchangeable.

- ~~**CJK is tokenized one term per character.**~~ Fixed, see "CJK text is searchable" below.
- **Search filters by BROWSE permission only.** Content and metadata of objects a user may browse but
  may not read are still searchable. Whether that is acceptable is a product decision - written up
  in detail, with what a fix would cost, in `ai-improvement/open-questions.md`, question 1.
  Awaiting an inspection of real customer ACL configurations.
- ~~**`IndexMode.TIKA` and `NONE`** are not handled in `indexOsd`'s switch.~~ Not a defect:
  `TikaService` stores the extracted text in the `tika` metaset and queues a fresh index job, so the
  text arrives as custom metadata on the next pass. See `ai-improvement/open-questions.md`,
  question 2.
- ~~**The JSON index path bypasses `readContentBounded`.**~~ Resolved as a comment fix, not a code
  change: `content_size` comes from `Files.copy`'s return value and content files are never
  rewritten, so it is authoritative and a second layer would guard a state the application cannot
  produce. Two comments that claimed the bounded read backstops a *wrong* stored size were
  corrected. See `ai-improvement/open-questions.md`, question 3.

### Race conditions in the index/search and database workflow

Diagnosis of long-standing integration test instability. Items 1 and 2 are fixed, 3 and 4 are not.

#### 1. Stale reads came from MyBatis, not Postgres (fixed)

`mybatis-config.xml` did not set `localCacheScope`, so MyBatis used its default of `SESSION`: a
repeated identical query within one `SqlSession` was answered from the session's local cache and
never reached the database. The transaction isolation level is correctly `READ_COMMITTED`, so
Postgres would have returned fresh rows - MyBatis simply did not ask.

This is invisible for a request-scoped session (`DbSessionFilter` opens a fresh one per request, and
any write clears the cache anyway), but the integration test thread keeps **one** `ThreadLocal`
session for a whole test class and never refreshes it. As soon as a server thread committed, the test
thread's cached reads went stale - which looks exactly like the database returning outdated rows. It
is also why `ThreadLocalSqlSession.refreshSession()` appears as a workaround in some test setups.

Fixed by setting `localCacheScope` to `STATEMENT` in both `mybatis-config.xml` and
`mybatis-logging-config.xml`, so the local cache lives only for the duration of one statement.

#### 2. `waitUntilIndexed` reported success when it had failed (fixed)

Two paths completed as if the item were searchable when it was not:

- `if (currentJob.getFailed() >= config.getMaxIndexAttempts()) { log.error(...); return; }` - a plain
  `return`, indistinguishable from a successful wait.
- retry exhaustion, which threw into a `catch` that only called `log.warn`.

There was also an **off-by-one**: `IndexService` selects jobs with `failed <= maxIndexAttempts`, so a
job is only abandoned once it has failed *more* often than that. Giving up at
`>= maxIndexAttempts` stopped waiting one attempt too early - and since `maxIndexAttempts` defaults
to **1**, a single transient failure made `waitUntilIndexed` return immediately while the indexer was
still going to retry.

Fixed: the permanent-failure case now throws a dedicated `IndexJobFailedPermanentlyException`, which
the retry policy aborts on, and the two outcomes are logged distinguishably at error level. The
give-up threshold now matches the indexer (`failed > maxIndexAttempts`).

Deliberately *not* changed: this still cannot fail the request. The data is already committed, so
telling the client the operation failed would be less accurate than telling it the object exists -
it just is not searchable yet.

#### Tests no longer sleep and hope (fixed)

`CinnamonIntegrationTest.awaitCondition(description, condition)` polls until an asynchronous
operation has caught up and fails with a useful message if it never does. Indexing runs on its own
schedule, so a fixed sleep is simultaneously too short (flaky under load) and too long (the common
case finishes in milliseconds).

Converted: the `IndexAndSearchServletIntegrationTest` class setup (6 s), both reindex tests (3 s
each, which only existed so later tests would not race against a running reindex - they now wait for
the job count to drain back to its pre-reindex baseline) and `updateMetaTest` (2 x 1.3 s). That
removes **14.6 s of fixed sleeps**.

Three tests marked `@Disabled("manual only since it sleeps")` are now enabled and take 3.9 s in
total instead of the 32 s they used to sleep: `lockAndUnlockAndSearch`, `deleteAndSearch` and
`deleteFolderAndSearch`. Note that sleeping was not their only problem - `lockAndUnlockAndSearch`
and `deleteAndSearch` asserted a **global** count ("all osds locked by this user is exactly 2"),
which only holds on a pristine index; other tests in the suite leave locked objects behind. Their
assertions are now scoped to the ids the test creates, and they additionally verify that unlocking or
deleting one version does not affect the other.

Suite: 654 tests, 11 skipped (was 14), three consecutive runs at 37.2 / 38.4 / 37.2 s.

#### 3. `waitUntilSearchable` defaults to `false` (not changed)

So by default nothing waits. This no longer affects the tests, which now poll for the state they
need rather than depending on the server having waited.

#### 4. The index wait ran after the response was written (fixed, but it was latent)

Filter order is `DbSessionFilter` (outermost) -> `AuthenticationFilter` -> `RequestResponseFilter`
-> `ChangeTriggerFilter` -> servlet. `RequestResponseFilter` commits and then calls
`renderResponseIfNecessary()`, which writes the body - but `waitUntilIndexed` ran in
`DbSessionFilter`, outside it, and therefore only after the body had been written. The existing
`// TODO: RequestResponseFilter also commits... due to http5client being so fast` in
`DbSessionFilter` records the same ordering problem, already observed for the database commit.

`waitUntilIndexed` now runs in `RequestResponseFilter`, after the commit and the post-commit change
triggers but **before** the response is written. All index jobs that are waited for are created while
the servlet runs, so the commit has already made them visible to `IndexService`; post-commit triggers
only call external services and do not add index jobs. `DbSessionFilter` no longer waits (and no
longer needs the `SearchService`).

**How reachable was it?** Less than it first appeared, and this was worth measuring rather than
assuming. Jetty keeps a response in its output buffer (32 KB by default) until the request completes,
which happens after every filter has returned - so for a small response the late wait still finished
in time. Only an early-flushed response lets the client run ahead, and the endpoints that create
index jobs all return small responses. An attempt to force a large write response (a 64 KB summary)
did not reproduce it either, because the summary is set by a separate call whose response does not
echo it. So this was a **latent** ordering bug, not a demonstrated failure.

It is still worth fixing: correctness no longer depends on response size and on Jetty's buffer
configuration, either of which could change without anyone connecting it to search behaviour.

Trade-off introduced: the request's `SqlSession` now stays open across the wait, so its pooled
connection is held for roughly `millisToWaitBetweenRuns` longer than before, when `DbSessionFilter`
closed the session first. This only applies when `waitUntilSearchable` is switched on, which already
documents itself as making responses about a second slower.

Test: `waitUntilSearchableMakesItemSearchableBeforeTheResponseArrives` pins the guarantee down -
enable the setting, create an object, search for it with no sleeping and no polling. Note that it
does **not** fail against the old ordering, for the buffering reason above; it is a guarantee test,
not a regression test.

Both minor items noted here earlier - the redundant `isSearcherCurrent()` guard around
`maybeRefreshBlocking()` and the dead `SearchService.indexReader` field - have since been fixed, see
"Smaller cleanups".

### Ideas for future search improvements

- **Per-language analyzers for content.** Partly superseded: the motivating case was CJK, and that is
  solved globally by `CinnamonAnalyzer` without any per-document language handling, because
  `CJKBigramFilter` only touches CJK script runs and leaves everything else alone. So the
  per-language-field design previously sketched here is *not* needed for CJK, and the simpler change
  was the right one.

  It would still be the way to add language-specific **stemming** (German compound splitting,
  English/German snowball stemming), which genuinely cannot be done in one global chain. The
  mechanism remains: `PerFieldAnalyzerWrapper` works per *field*, not per document, so content would
  be indexed into per-language fields (`content_de`, `content_en`, ...) with metadata fields left on
  the current analyzer. The OSD already carries `languageId`. The open question is unchanged -
  content field names come from the DB-configured `IndexItem`, so the indexer must become
  language-aware via a new `IndexType` or a flag on `IndexItem`, which is a schema plus config
  decision rather than a mechanical change. Worth doing only when a customer actually wants stemming;
  note that stemming is a mixed blessing for technical documentation, where exact part numbers and
  model designations matter more than morphological recall.
- **Response size for very large result sets.** Even with the collector rework, 10 million ids
  serialise to roughly 200 MB of XML, buffered through `CinnamonResponse`. At that scale the response
  path, not the search, is the bottleneck. A streaming or paged variant would be an API change.
- **Concurrent search.** `ResultCollectorManager` already supports being sliced across threads; the
  `IndexSearcher` is currently constructed without an executor.
