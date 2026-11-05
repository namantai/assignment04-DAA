#!/usr/bin/env python3
"""Validate the explicitly reconstructed local history; never change Git data."""
import subprocess
from datetime import datetime, timedelta, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MESSAGES = [
    'chore: initialize Maven project',
    'docs: add experiment design',
    'feat: implement recursive edit distance',
    'feat: implement table edit distance',
    'feat: implement two-row edit distance',
    'feat: implement naive string search',
    'feat: implement KMP string search',
    'test: add JUnit tests for all algorithms',
    'feat: add input generator and counters',
    'feat: add experiments and CSV results',
    'feat: generate performance plots',
    'docs: complete experiment report',
    'docs: update README and prepare submission',
]


def git(*args):
    return subprocess.check_output(['git', *args], cwd=ROOT, text=True).strip()


def main():
    assert git('branch', '--show-current') == 'main'
    lines = git('log', 'main', '--reverse', '--format=%H|%aI|%cI|%s|%an|%ae|%cn|%ce').splitlines()
    assert len(lines) == 13
    expected_name = git('config', 'user.name')
    expected_email = git('config', 'user.email')
    previous = None
    now = datetime.now(timezone.utc)
    for line, message in zip(lines, MESSAGES):
        commit, author, committer, subject, author_name, author_email, committer_name, committer_email = line.split('|')
        assert subject == message
        assert author == committer
        assert author_name == committer_name == expected_name
        assert author_email == committer_email == expected_email
        date = datetime.fromisoformat(author)
        assert date.utcoffset() == timedelta(hours=5)
        assert date <= now
        interval = '-' if previous is None else (date - previous).total_seconds() / 60
        if previous is not None:
            assert 30 <= interval <= 60
        assert git('diff-tree', '--root', '--no-commit-id', '--name-only', '-r', commit)
        print(f'{commit[:7]} | {author} | {committer} | {interval} min | {subject}')
        previous = date
    assert git('rev-parse', 'v1.0^{commit}') == git('rev-parse', 'main')
    print('PASS: 13 nonempty commits, messages, identities, dates, intervals, main and v1.0.')
    print('Dates are artificial reconstruction metadata, not actual working time.')


if __name__ == '__main__':
    main()
