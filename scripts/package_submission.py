#!/usr/bin/env python3
"""Package only reviewed project files, never Git metadata, build output or caches."""
from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile

ROOT = Path(__file__).resolve().parents[1]
NAME = "DAA_Assignment4_Nurtas_Amantay_SE-2506"
files = [ROOT / name for name in ("pom.xml", ".gitignore", ".gitattributes", "README.md", "REPORT.md", "REPORT.pdf", "QA.md")]
for directory, extensions in (("src", {".java"}), ("scripts", {".py", ".txt"}),
                              ("results", {".csv", ".json", ".txt", ".png"})):
    files.extend(path for path in (ROOT / directory).rglob("*")
                 if path.is_file() and path.suffix in extensions
                 and not any(part.startswith(".") or part == "__pycache__" for part in path.relative_to(ROOT).parts))
for path in files:
    if not path.is_file():
        raise FileNotFoundError(path)
archive = ROOT / (NAME + ".zip")
with ZipFile(archive, "w", compression=ZIP_DEFLATED) as target:
    for path in sorted(files):
        target.write(path, Path(NAME) / path.relative_to(ROOT))
with ZipFile(archive) as target:
    assert target.testzip() is None
print(f"Created {archive} ({len(files)} files, {archive.stat().st_size:,} bytes)")
