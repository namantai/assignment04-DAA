#!/usr/bin/env python3
"""Validate the Java CSV, summarize real trials and plot them. No algorithm simulation."""
import csv
import math
import os
from collections import defaultdict
from pathlib import Path
from statistics import median

os.environ.setdefault("MPLCONFIGDIR", "/tmp/daa-matplotlib")
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

ROOT = Path(__file__).resolve().parents[1]
RESULTS = ROOT / "results"
PLOTS = RESULTS / "plots"
PLOTS.mkdir(parents=True, exist_ok=True)


def load_results():
    with (RESULTS / "results.csv").open(newline="") as source:
        rows = list(csv.DictReader(source))
    groups = defaultdict(list)
    for row in rows:
        for key in row:
            if key not in ("algorithm", "input"):
                row[key] = float(row[key]) if key == "time_ms" else int(row[key])
        assert math.isfinite(row["time_ms"]) and row["time_ms"] > 0
        groups[(row["algorithm"], row["input"], row["n"], row["m"])].append(row)
    summary = []
    for key, trials in groups.items():
        assert sorted(r["trial"] for r in trials) == [1, 2, 3, 4, 5], key
        for field in trials[0]:
            if field not in ("time_ms", "trial"):
                assert len({r[field] for r in trials}) == 1, (key, field)
        point = trials[0].copy()
        point.pop("trial")
        times = [r["time_ms"] for r in trials]
        point["time_ms"] = median(times)
        point["min_time_ms"] = min(times)
        point["max_time_ms"] = max(times)
        point["total_comparisons"] = point["comparisons"] + point["lps_comparisons"]
        summary.append(point)
    expected_algorithms = {"RecursiveEditDistance", "TableEditDistance", "TwoRowsEditDistance", "NaiveSearch", "KMPSearch"}
    assert {r["algorithm"] for r in summary} == expected_algorithms
    for algorithm in expected_algorithms:
        points = [r for r in summary if r["algorithm"] == algorithm]
        assert len({r["input"] for r in points}) >= 2
        for kind in {r["input"] for r in points}:
            sizes = {r["n"] for r in points if r["input"] == kind}
            assert len(sizes) >= 4 and max(sizes) >= 4 * min(sizes), (algorithm, kind)
    answers = defaultdict(set)
    for row in summary:
        family = "edit" if "EditDistance" in row["algorithm"] else "search"
        answers[(family, row["input"], row["n"], row["m"])].add((row["result"], row["result_hash"], row["case_seed"]))
    assert all(len(values) == 1 for values in answers.values())
    print(f"Validated {len(rows)} trials / {len(summary)} combinations")
    return summary


def series(data, algorithm, kind, field, small=None):
    selected = [r for r in data if r["algorithm"] == algorithm and r["input"] == kind
                and (small is None or (r["n"] <= 12) == small)]
    selected.sort(key=lambda r: r["n"])
    return [r["n"] for r in selected], [r[field] for r in selected]


def line(ax, data, algorithm, kind, field, label, small=None, marker="o"):
    x, y = series(data, algorithm, kind, field, small)
    ax.plot(x, y, marker=marker, linewidth=1.8, markersize=4, label=label)
    ax.grid(True, which="both", alpha=0.2)


def save(fig, name):
    fig.savefig(PLOTS / name, dpi=180, bbox_inches="tight")
    plt.close(fig)
    print(PLOTS / name)


def main():
    data = load_results()
    plt.rcParams.update({"font.size": 11, "axes.titlesize": 11, "axes.labelsize": 11,
                         "legend.fontsize": 9, "figure.facecolor": "white"})
    fig, ax = plt.subplots(figsize=(9, 3.3), constrained_layout=True)
    for kind in ("random", "mismatch", "identical"):
        line(ax, data, "RecursiveEditDistance", kind, "recursive_calls", "Recursive calls: " + kind, True)
    line(ax, data, "TableEditDistance", "random", "cell_updates", "Table updates (all inputs)", True, "s")
    line(ax, data, "TwoRowsEditDistance", "random", "cell_updates", "Two Rows updates (all inputs)", True, "x")
    ax.set(title="Edit Distance: counted work on shared sizes",
           xlabel="String length n = m (characters)", ylabel="Count per call (log scale)",
           yscale="log", xticks=[3, 6, 9, 12])
    ax.legend(loc="upper left")
    save(fig, "edit_distance.png")

    fig, ax = plt.subplots(figsize=(9, 2.9), constrained_layout=True)
    line(ax, data, "TableEditDistance", "random", "allocated_cells", "Full Table", False)
    line(ax, data, "TwoRowsEditDistance", "random", "allocated_cells", "Two Rows", False, "s")
    ax.set(title="DP storage: allocated integer cells (not measured heap bytes)",
           xlabel="String length n = m (characters)", ylabel="Integer cells per call (log scale)",
           yscale="log", xticks=[100, 200, 400, 800])
    ax.legend()
    save(fig, "memory.png")

    fig, axes = plt.subplots(1, 2, figsize=(9, 3.1), constrained_layout=True)
    fig.suptitle("String search: total character comparisons, pattern length m = 32", fontsize=12)
    for ax, kind in zip(axes, ("random", "adversarial")):
        line(ax, data, "NaiveSearch", kind, "total_comparisons", "Naive")
        line(ax, data, "KMPSearch", kind, "total_comparisons", "KMP search + LPS", marker="s")
        ax.set(title=kind.capitalize(), xlabel="Text length n (characters)", ylabel="Comparisons per call (count)")
        ax.ticklabel_format(axis="y", style="plain")
        ax.legend()
    save(fig, "string_search.png")



if __name__ == "__main__":
    main()
