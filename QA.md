# Submission review

This is an evidence checklist, not an awarded grade. The teacher decides the final score.

| Rubric criterion | Weight | Evidence and assessment |
|---|---:|---|
| Edit Distance | 25% | Three independent implementations, common interface, explicit state/transition/base comments, empty strings and reference answers. 157 JUnit invocations include 100 random pairs, symmetry checks, bounds and hand-counted counters. No known missing requirement. |
| String Search | 25% | Naive and KMP return all overlaps; public LPS passes both required arrays. 135 JUnit invocations include 100 seeded random texts and an independent test oracle. Code inspection confirms KMP never decreases the text index. No known missing requirement. |
| Experiment Design | 20% | The original six-field design card was committed in 9750411 before measurements; that original history is preserved in codex/backup-before-restructure-20261105. Artificial dates in the reconstructed main history do not document actual work time. Every algorithm has at least four sizes spanning at least 4x, with three edit types and two search types. Hard cases are justified; counters increment inside algorithms. Five trials, warm-up, batching, fixed seeds and full answer checks are implemented. |
| Plots and Conclusions | 20% | Three PNGs generated from the primary Java CSV, labeled axes and units, legends and log scales. The four-page PDF was rendered and visually checked. Captions, predictions versus observations, four answers of four sentences each, timing discussion and limitations are present. Storage is integer-cell counts, not measured heap usage. |
| Tests and Repository | 10% | 295 JUnit invocations passed with no failures/errors/skips, including three generator/benchmark tests. Maven builds on Java 21, README documents commands and AI assistance, and main is reconstructed as 13 logical commits with explicitly artificial dates. The original history is preserved in the backup branch; no push is performed. |

## Checks and evidence

- `mvn clean verify`: 157 Edit Distance + 135 Search + 3 Experiment = 295 tests.
- Primary experiment: 400 measured rows, 80 combinations, five trials each; all times are positive.
- Second complete JVM run: 400 rows match in every column except `time_ms`; the first run is held in memory and only the second CSV is retained, with comparison evidence in JSON.
- `scripts/plot_results.py`: validates sizes, input types, trial coverage, counters and matching results before calculating summaries in memory and generating three plots.
- `REPORT.pdf`: four A4 pages with embedded fonts; the PDF contains the Markdown report and its three figures.
- Core measured evidence: mismatch n=12 gives 377,393,953 recursive calls; n=800 gives 641,601 versus 1,602 allocated DP cells; adversarial search n=16,000 gives 511,008 versus 32,030 comparisons including LPS.
- `scripts/package_submission.py`: explicit file allowlist, ZIP integrity check, and no `.git`, `target`, caches or IDE configuration.
- Clean archive extraction: `mvn clean verify` passed all 295 tests in a separate temporary directory; no project-local build output or Git metadata was needed.

## Limits and manual submission checks

The run samples one input per size/type. Timing includes instrumentation and small harness overhead, and the CPU was not isolated. No maximum feasible recursive input beyond n=12 is claimed. Test coverage and observed results are evidence of correctness, not a proof for every possible string or a guarantee of 100/100.

Before submitting, confirm the spelling of Nurtas Amantay and group SE-2506, verify that the teacher can access the GitHub repository and v1.0 tag, and review the code and report for the defence. Upload the final ZIP and repository link to Moodle by 08.11.2026 23:59. Moodle upload is a separate student action.
