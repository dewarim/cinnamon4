# Search benchmarks

Measurements taken while reworking search result collection (see `docs/changelog.md`,
"Search: collect ids instead of documents"). They compare the implementation before the rework
("old", as of commit `97977b7c`) against the new one, on the same index.

## What changed, in one sentence

Old: one Lucene search collected **every** matching document as a `Document` object (via the
deprecated `IndexSearcher.doc(int)`) into a `HashSet`, and ACL/type filtering happened afterwards in
Java. New: the ACL and type filters are pushed into the Lucene query, and only the Cinnamon `id` of
each surviving hit is collected into a `long[]`, read from a `NumericDocValues` field.

## Method

- Index built with the same fields `IndexService.addOsdFields` writes (`cinnamon_class`, `acl`,
  `owner`, `id` as LongPoint + stored + NumericDocValues, plus an analyzed `summary`).
- On-disk `FSDirectory`, force-merged to a single segment. A RAM directory was deliberately *not*
  used: stored-field decompression is exactly what the new path avoids, and a RAM directory would
  flatter the old path.
- Query: `MatchAllDocsQuery`, i.e. the pathological "give me everything" case, restricted to
  `cinnamon_class = OSD`.
- 40 ACLs, 12 owners, distributed pseudo-randomly with a fixed seed.
- Two permission profiles: a user who may browse **all** 40 ACLs, and one who may browse **2 of 40**
  (~5% of documents).
- Both implementations run in the same JVM on the same reader; results asserted identical every run.
- `time` is wall clock for the whole operation (collect + filter). `live heap` is the retained set
  measured while the result is still strongly reachable, after `System.gc()`. `allocated` is
  `ThreadMXBean.getCurrentThreadAllocatedBytes()`, which is deterministic and GC-independent.

Benchmark source: `local/temp/SearchBenchmark.java` (not part of the build).

> Measuring the live set only works while the result is still reachable. An earlier run sampled the
> heap *after* the old method returned and reported ~5 MB, because the `Document` set had already
> become collectable. The figures below hold the result live.

## Results

### 200 000 documents

| metric | old | new | factor |
|---|---|---|---|
| time, user browses all ACLs | 888 ms | **4 ms** | 222x |
| time, user browses ~5% | 935 ms | **1 ms** | 935x |
| live heap, browses all | 91.0 MB | **2.0 MB** | 45x |
| live heap, browses ~5% | 90.8 MB | **0.1 MB** | ~1000x |
| allocated, browses all | 277.2 MB | **5.5 MB** | 50x |
| allocated, browses ~5% | 262.4 MB | **0.3 MB** | 752x |
| live heap per materialised doc | 477 B | **10 B** | |

### 1 000 000 documents

| metric | old | new | factor |
|---|---|---|---|
| time, user browses all ACLs | 2 851 ms | **19 ms** | 150x |
| time, user browses ~5% | 2 799 ms | **3 ms** | 933x |
| live heap, browses all | 437.3 MB | **8.2 MB** | 54x |
| live heap, browses ~5% | 437.4 MB | **0.3 MB** | 1450x |
| allocated, browses all | 1 413.6 MB | **31.3 MB** | 45x |
| allocated, browses ~5% | 1 344.3 MB | **1.8 MB** | 753x |
| live heap per materialised doc | 459 B | **9 B** | |

### Documents materialised

The asymmetry that produces the ~900x numbers: with the restrictive profile on a 1M index, the old
code materialised **1 000 000** documents and then discarded all but 49 855 of them. The new code
materialises **49 855** ids, because Lucene never collects what the user may not browse.

| profile | old | new |
|---|---|---|
| browses all ACLs (1M index) | 1 000 000 | 1 000 000 |
| browses ~5% (1M index) | 1 000 000 | 49 855 |

### Extrapolation

At ~460 B per hit, a complete result set on a repository the size Cinnamon is expected to serve:

| repository | old live heap | new live heap |
|---|---|---|
| 1 M objects | ~440 MB | ~8 MB |
| 5 M objects | ~2.2 GB | ~40 MB |
| 10 M objects | ~4.5 GB | ~80 MB |

Result sets stay **unbounded and complete** - no cap and no truncation were introduced, because
callers need exhaustive sets (for example "every image contained in a technical handbook").

## Tokenizer robustness

Separate probe (`local/temp/TokenizerProbe.java`) of `StandardAnalyzer`, the analyzer used for both
indexing and query parsing, against adversarial Unicode. Relevant because tokenizers run over
practically all content and metadata.

| input (~1M chars) | tokens | max token length | time |
|---|---|---|---|
| single unbroken "word" | 3 922 | 255 | 51 ms |
| combining marks (a + U+0301) | 3 907 | 255 | 20 ms |
| bare combining marks | 0 | - | 2 ms |
| astral surrogate pairs (U+1F600) | 500 000 | 2 | 25 ms |
| lone/unpaired high surrogates | 0 | - | 10 ms |
| ZWJ interleaved | 3 907 | 255 | 10 ms |
| bidi overrides (RLO/LRO) | 3 918 | 255 | 7 ms |
| soft hyphens interleaved | 3 907 | 255 | 10 ms |
| Thai / Khmer / Hangul jamo, no spaces | 1 953 | 255 | 5-7 ms |
| script alternation (break every char) | 666 001 | 2 | 14 ms |

Scaling is linear: 250k chars 3 ms, 1M 13 ms, 2M 27 ms, 4M 54 ms.

Two properties make this surface safe: `StandardTokenizer` caps token length at **255 characters**,
so even a 1M-character unbroken string never buffers as a single token; and there is **no ICU
dependency** (only `lucene-core`, `lucene-analysis-common`, `lucene-queryparser`), so none of ICU's
normalization and collation complexity is reachable. No change was needed here.

`LimitTokenCountAnalyzer(analyzer, Integer.MAX_VALUE)` was confirmed to be a no-op: 500 000 tokens
passed through unchanged, while the same wrapper with a limit of 10 000 truncated at 10 000.

## Reproducing

```bash
mvn -q dependency:build-classpath -Dmdep.outputFile=local/temp/cp.txt -DincludeScope=test
java -Xmx6g --class-path "target/classes:$(cat local/temp/cp.txt)" \
     local/temp/SearchBenchmark.java 1000000
```

The benchmark creates its index under `local/temp/bench-index-<numDocs>` and reuses it on later
runs; delete that directory to rebuild. Absolute numbers depend on hardware - the factors between
old and new are the point, not the milliseconds.
