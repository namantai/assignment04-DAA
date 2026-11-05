# Assignment 4: Dynamic Programming and String Algorithms

Nurtas Amantay, SE-2525. Java 21, Maven and JUnit 5.

The project implements Recursive, Table and Two Rows Edit Distance, plus Naive and
KMP string search. Distance classes share `distance(String a, String b)`. Search
classes return all indices, including overlaps, through `search(String text, String pattern)`.
KMP also provides `lps(String pattern)`. Empty patterns throw `IllegalArgumentException`.
Matching is case-sensitive and uses Java UTF-16 character indices.

## Structure

```text
src/main/java/algorithms/   five algorithms, two family interfaces, Counters
src/main/java/experiment/   InputGenerator, Experiment
src/test/java/algorithms/   distance and search tests
src/test/java/experiment/   generator and experiment tests
results/results.csv        all measured trials
results/plots/             three PNG graphs
scripts/plot_results.py    the only Python script
pom.xml
REPORT.md, REPORT.pdf
```

## Run

From the project root, with JDK 21 and Maven:

```bash
mvn clean verify
mvn test
java -jar target/assignment04-daa-1.0.jar
```

The build runs 295 tests, including 100 random cases per algorithm family.
The experiment overwrites the single CSV after checking all answers. It uses seed
`20261105`, warm-up and five measured trials. Short calls are batched; `time_ms`
is milliseconds per invocation and `repetitions` is the batch size.

Create the plots with Python 3 and Matplotlib:

```bash
python3 -m pip install matplotlib
python3 scripts/plot_results.py
```
