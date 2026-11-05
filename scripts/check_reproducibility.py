#!/usr/bin/env python3
"""Run Java twice, comparing one overwritten CSV with an in-memory copy."""
import csv
import hashlib
import io
import json
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
output = ROOT / 'results/results.csv'
command = ['java', '-jar', 'target/assignment04-daa-1.0.jar']
subprocess.run(command, cwd=ROOT, check=True)
first_bytes = output.read_bytes()
first = list(csv.DictReader(io.StringIO(first_bytes.decode())))
subprocess.run(command, cwd=ROOT, check=True)
second_bytes = output.read_bytes()
second = list(csv.DictReader(io.StringIO(second_bytes.decode())))
assert len(first) == len(second) > 0
for i, (a, b) in enumerate(zip(first, second), 1):
    assert a.keys() == b.keys()
    for key in a:
        if key != 'time_ms':
            assert a[key] == b[key], (i, key, a[key], b[key])
proof = {
    'rows_compared': len(first),
    'ignored_column': 'time_ms',
    'all_other_columns_identical': True,
    'first_run_sha256': hashlib.sha256(first_bytes).hexdigest(),
    'results_csv_sha256': hashlib.sha256(second_bytes).hexdigest(),
    'storage': 'The first run was kept in memory; results/results.csv contains the second run.'
}
(ROOT / 'results/reproducibility.json').write_text(json.dumps(proof, indent=2) + '\n')
print(f'All {len(first)} rows match in every non-timing column. One CSV retained.')
