import csv
from pathlib import Path
import matplotlib.pyplot as plt

root = Path(__file__).resolve().parents[1]
plots = root / 'results/plots'
plots.mkdir(parents=True, exist_ok=True)
with (root / 'results/results.csv').open(newline='') as file:
    rows = list(csv.DictReader(file))


def draw(ax, algorithm, kind, metric, sizes, label):
    points = []
    for row in rows:
        n = int(row['n'])
        if row['algorithm'] == algorithm and row['input'] == kind and row['trial'] == '1' and n in sizes:
            value = int(row[metric])
            if metric == 'comparisons':
                value += int(row['lps_comparisons'])
            points.append((n, value))
    points.sort()
    x = []
    y = []
    for n, value in points:
        x.append(n)
        y.append(value)
    marker = 'x' if algorithm == 'TwoRowsEditDistance' else 'o'
    ax.plot(x, y, marker=marker, label=label)


def save(fig, name):
    fig.tight_layout()
    fig.savefig(plots / name, dpi=180)
    plt.close(fig)


small = [3, 6, 9, 12]
large = [100, 200, 400, 800]
search = [1000, 2000, 4000, 8000, 16000]

fig, ax = plt.subplots(figsize=(9, 3.3))
for kind in ['random', 'mismatch', 'identical']:
    draw(ax, 'RecursiveEditDistance', kind, 'recursive_calls', small, 'Recursive calls: ' + kind)
draw(ax, 'TableEditDistance', 'random', 'cell_updates', small, 'Table updates (all inputs)')
draw(ax, 'TwoRowsEditDistance', 'random', 'cell_updates', small, 'Two Rows updates (all inputs)')
ax.set(title='Edit Distance: counted work', xlabel='String length n = m (characters)',
       ylabel='Count per call (log scale)', yscale='log', xticks=small)
ax.legend(fontsize=9)
ax.grid(alpha=0.2)
save(fig, 'edit_distance.png')

fig, ax = plt.subplots(figsize=(9, 2.9))
draw(ax, 'TableEditDistance', 'random', 'allocated_cells', large, 'Full Table')
draw(ax, 'TwoRowsEditDistance', 'random', 'allocated_cells', large, 'Two Rows')
ax.set(title='DP storage: allocated integer cells, not heap bytes',
       xlabel='String length n = m (characters)', ylabel='Integer cells per call (log scale)',
       yscale='log', xticks=large)
ax.legend()
ax.grid(alpha=0.2)
save(fig, 'memory.png')

fig, axes = plt.subplots(1, 2, figsize=(9, 3.1))
fig.suptitle('String search: character comparisons, pattern length m = 32')
for ax, kind in zip(axes, ['random', 'adversarial']):
    draw(ax, 'NaiveSearch', kind, 'comparisons', search, 'Naive')
    draw(ax, 'KMPSearch', kind, 'comparisons', search, 'KMP search + LPS')
    ax.set(title=kind.capitalize(), xlabel='Text length n (characters)', ylabel='Comparisons per call (count)')
    ax.ticklabel_format(axis='y', style='plain')
    ax.legend()
    ax.grid(alpha=0.2)
save(fig, 'string_search.png')
