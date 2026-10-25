# Open questions

Questions raised during the 2026-10 security review of parsers and search that need a product
decision or a look at real deployments before any code is written. Each entry records what was
established, what is *not* known, and what would settle it.

See `changelog.md` for what was actually changed, and `benchmark.md` for the search measurements.

---

## 1. Search filters by BROWSE only — is that the intended permission boundary?

**Status:** waiting on an inspection of real customer ACL configurations (Ingo, 2026-10-25).

### What the code does

`SearchService` filters every hit through exactly one permission, `BROWSE`:

```java
objectAcls.contains(aclId) || (owner.equals(userId) && ownerAcls.contains(aclId))
```

Since the ACL push-down (changelog, "Push ACL and type filtering into the Lucene query") this is
expressed as Lucene query clauses rather than a post-filter, but the *predicate* is unchanged — the
rewrite was proven to return exactly the same hits (11,147 == 11,147 on a 60k-document index). So
this question is as old as the current permission model; the rewrite neither introduced nor widened
it.

No other permission is consulted. Cinnamon also has `READ_OBJECT_CONTENT` and
`READ_OBJECT_CUSTOM_METADATA`, and search ignores both.

This is at least partly deliberate: `DefaultPermission.BROWSE`'s own javadoc says the server will
list the object "when queried for the content of a folder **or when a search for turns up this
object**". So "may appear in search results" is documented BROWSE semantics. The open question is
narrower than that:

> Does BROWSE also imply *may use this object's content and custom metadata as query predicates*?

### What follows if the answer is "no"

The index holds, for every object whose format has an `index_mode`, the full text of its content and
all of its custom metadata. For `IndexMode.TIKA` that includes text Tika extracted from binary
formats (PDF, Office), stored in the `tika` metaset. A user with BROWSE but without
`READ_OBJECT_CONTENT` can therefore:

- confirm that an object containing a given phrase exists, and obtain its id;
- narrow an unknown value down across successive queries — wildcard and range queries make this
  efficient — and so reconstruct content they cannot fetch;
- do the same for custom metadata they have no permission to read.

The content is never returned in the response — a search yields only ids — but hit/no-hit answers
make search an **oracle**. For the technical documentation Cinnamon targets, the sensitive values are
exactly the short, guessable ones: tolerances, part numbers, prices, supplier terms in a restricted
annex of a manual whose other sections are broadly browsable.

### What is not known

Whether any real deployment grants BROWSE on an object without also granting the read permissions.
If browse is only ever granted together with content read, the gap is theoretical and this becomes a
documentation item. If browse is used as a broad navigation permission while content permissions are
the real control, then search bypasses the real control.

### What a fix would look like

Mechanically cheap: index content-derived fields and custom-metadata-derived fields into a separate
field set, and add the corresponding permission as a further query clause. The ACL push-down makes
this natural — the ACL filter is a `LongPoint.newSetQuery`, a `PointInSetQuery`, so additional ACL
sets cost no boolean clauses at all and the high clause count the Windows client needs is unaffected.

Semantically expensive, and the reason not to do it speculatively:

- it is **client-visible**: any user who today holds browse-without-read starts getting fewer
  results, which is exactly the kind of change `CLAUDE.md`'s no-breaking-API-changes stance says to
  treat carefully;
- it needs a **reindex**, because the field layout changes;
- it needs a decision on *partial* visibility: should an object matched only by a restricted field
  disappear entirely, or still appear when it also matches a permitted field?

### Decision needed

1. Does BROWSE mean "may know this exists" or "may read this"?
2. If the former: fix, with a migration note and a release note about changed result sets — or
   document the behaviour and leave enforcement to ACL configuration?

---

## 2. Answered while reviewing: `IndexMode.TIKA` and `NONE` are not handled in `indexOsd`

Not an open question after all; recorded here because it looked like one.

`indexOsd`'s switch over `format.getIndexMode()` handles `XML`, `JSON` and `PLAIN_TEXT`. `TIKA` and
`NONE` fall through and the document is indexed with `NO_CONTENT`.

`NONE` is self-evidently intended. `TIKA` is correct too: the external Tika service extracts the
text asynchronously, stores it in the `tika` metaset (`TIKA_METASET_NAME`) via `OsdMetaDao`, and
queues a **fresh** index job. The text therefore reaches the index as custom metadata on the next
pass, not through this switch. Handling `TIKA` here would be wrong, not merely redundant.

Worth noting for question 1: this means Tika-extracted full text of binary documents is governed by
`READ_OBJECT_CUSTOM_METADATA`, not `READ_OBJECT_CONTENT`.

---

## 3. Minor: the JSON index path does not honour `contentIsSmallEnoughToIndex`'s own contract

Not a security item, and the first version of this note overstated it. Kept only because the code
contradicts one of its own comments, which is worth tidying.

### `content_size` is trustworthy

That first version justified `readContentBounded`'s second check by claiming `content_size` might
disagree with the file on disk. The reasoning does not hold:

- it is not a client-supplied value. `FileSystemContentProvider.writeContentStream` sets it from
  `Files.copy`'s return value, which is the number of bytes that actually landed on disk;
- `OsdServlet.storeContent` sets hash, path and size together from that single return value, so they
  cannot be written out of step with one another;
- content files are named by a random UUID and are never rewritten. New content means a new file, so
  there is no in-place update for the recorded size to drift from;
- `ContentProviderService` is a hardcoded switch with a single case, so there is no second provider
  to migrate between and no third-party provider that might report sizes differently.

For anything Cinnamon 4 wrote, `content_size` is authoritative. Divergence takes filesystem
corruption or a hand-edited `dataRoot`.

### `contentHash` cannot substitute for the size check

Verifying a SHA-256 means reading the whole file, which is exactly the cost the size limit exists to
avoid: the check would have to read the file to decide whether the file is too big to read. The hash
is the right tool for integrity verification and the wrong tool for a size bound.

### What is actually left

The trigger is a MISSING size, not a wrong one. `content_size` is a nullable `bigint`, and
`contentIsSmallEnoughToIndex` deliberately returns `true` when it is null, commented
"size unknown - readContentBounded() still enforces the limit". That promise holds for `XML` and
`PLAIN_TEXT`; it is false for `JSON`, where `convertJsonToXml` calls
`JSON_MAPPER.readTree(jsonStream)` and parses the whole stream unbounded, after which
`XML_MAPPER.writeValueAsBytes` makes a second full copy.

Cinnamon 4 never produces a non-null `content_path` with a null `content_size`, so this is reachable
only through a raw-SQL migration from Cinnamon 3 or a hand-edited database.

### Decision needed

Make the code match the comment, or the comment match the code:

- pass the bounded bytes to `convertJsonToXml` instead of the stream, which needs a decision on
  whether a JSON file that hits the limit should skip its content, as the oversize path does, or
  fail the job; or
- drop the "readContentBounded still enforces the limit" clause from the comment and accept the
  up-front check as the only guard, given that `content_size` is reliable.

Independent of either: the limit is compared against the RAW FILE SIZE, while the heap cost is a
per-mode multiple of it - roughly ten times for the dom4j path, more for `readTree` plus XML
re-serialization. That is a calibration note about the default, already flagged in
`cinnamon-config.xml`, rather than a correctness problem.
