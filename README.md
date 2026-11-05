# Assignment 4: Dynamic Programming and String Algorithms

Nurtas Amantay, SE-2506. Java 21, Maven and JUnit 5.11.4.

City Search & Spell-Check uses five independently implemented algorithms:

- Recursive Edit Distance: plain recursion without caching.
- Table Edit Distance: bottom-up DP with a full matrix.
- Two Rows Edit Distance: the same distance with O(m) extra space.
- Naive Search: checks every possible starting position.
- KMP Search: uses a public `lps(String pattern)` method and reuses matched prefixes.

Distance classes share `distance(String a, String b)`; search classes share
`search(String text, String pattern)`. Search returns all indices, including overlaps.
Empty patterns throw `IllegalArgumentException`; longer patterns return an empty list.
Matching is case-sensitive and uses Java UTF-16 character indices. Null inputs are rejected.

## Structure

```text
pom.xml
src/main/java/algorithms/   five algorithms, two family interfaces, Counters
src/main/java/experiment/   InputGenerator, Experiment
src/test/java/algorithms/   reference, edge and random tests
src/test/java/experiment/   generator and CSV tests
results/results.csv        the only CSV; one row per measured trial
results/plots/             edit_distance.png, memory.png, string_search.png
scripts/                   plotting, reproducibility, PDF, ZIP and history checks
REPORT.md, REPORT.pdf      report and four-page A4 version
QA.md                      rubric evidence
```

## Run

Use JDK 21 and Maven. The first build downloads dependencies from Maven Central.

```bash
mvn clean verify
mvn test
java -jar target/assignment04-daa-1.0.jar
```

The build runs 295 tests, including 100 random cases for each algorithm family.
The experiment overwrites `results/results.csv` only after all answers pass validation.
It uses seed `20261105`, five measured trials after warm-up, and medians for comparison.
Generation, answer checking and CSV writing are outside timing. Short calls are batched;
`time_ms` is milliseconds per invocation, and `repetitions` gives the batch size.

Recursive sizes: 3, 6, 9, 12. DP also uses 100, 200, 400, 800.
Search text sizes: 1000, 2000, 4000, 8000, 16000; pattern length: 32.
Edit inputs are random ABCD, full mismatch and identical repetitions.
Search inputs are random ABCD and A^n with A^31B.

Counters reset on each call. `recursive_calls` includes base cases; `cell_updates`
counts interior DP cells. `comparisons` counts main-search character checks;
`lps_comparisons` counts prefix construction, so KMP total is their sum.
`allocated_cells` counts actual integer-array slots, not physical heap bytes.
Zero means no counted work or a metric that does not apply. Other columns identify
the algorithm, input, lengths, trial, master/derived seeds, answer and answer hash.

Install Python plotting/PDF dependencies and generate the three figures:

```bash
python3 -m venv .venv
. .venv/bin/activate
python -m pip install -r scripts/requirements.txt
python scripts/plot_results.py
python scripts/render_report.py
```

To check reproducibility, run `python scripts/check_reproducibility.py` after building.
It runs Java twice, keeps the first result in memory, overwrites the same CSV, and checks
all columns except time. Regenerate plots and update report timings after new measurements.
Counters repeat exactly; elapsed time varies with JVM/JIT activity and system load.

## Submission and AI use

Run `python scripts/package_submission.py` to create
`DAA_Assignment4_Nurtas_Amantay_SE-2506.zip`. It excludes Git metadata, build output and
caches. The extracted project builds with `mvn clean verify`.
Repository: https://github.com/namantai/assignment04-DAA; local branch `main`, tag `v1.0`.
No changes were pushed during this revision.

The 13-commit history was reconstructed from the completed project at the owner's
request. Author and committer dates use artificial 30-60 minute intervals in +05:00;
they do not represent actual development duration. The original history is preserved
in `codex/backup-before-restructure-20261105`. `python scripts/check_history.py` verifies
the commit count, messages, dates, intervals and tag.

AI tools assisted with code drafting, tests, experiments and documentation.
The implementation was checked with automated tests and real measurements.
Review the algorithms and results before the defence, confirm the name/group, and
submit the ZIP and an accessible repository link to Moodle by 08.11.2026 23:59.
