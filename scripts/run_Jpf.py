#!/usr/bin/env python3
import csv
import os, pathlib, sys, subprocess
from datetime import datetime


# Find the right paths:
ROOT = pathlib.Path(__file__).resolve().parents[1]

OUT_DIR = ROOT / 'out'
CONFIGS_DIR = ROOT / 'configs'


# Find JPF location via environm,ent variable (hopefully also works in windows)

JPF_HOME = os.environ.get("JPF_HOME")
if not JPF_HOME:
    sys.exit("[error] Please set JPF_HOME environment variable")

JPF_CMD = str(pathlib.Path(JPF_HOME) / "bin" / "jpf")

# Build compile classpath for javac (JPF jars + repo root so imports resolve) (so its OS dependent and should work on non-unix system, lets see)
CP_SEP = os.pathsep
JPF_CP = f"{JPF_HOME}/build/jpf.jar{CP_SEP}{JPF_HOME}/build/jpf-classes.jar{CP_SEP}{ROOT}"


def latest_mtime(paths):
    latest = 0.0
    for p in paths:
        try:
            ts = p.stat().st_mtime
            if ts > latest:
                latest = ts
        except FileNotFoundError:
            return 0.0
    return latest

def maybe_compile():
    """
    Compile Java sources into out/ only if needed:
    - if out/ is missing, or
    - if any .java file is newer than the newest .class in out/
    """
    # Collect sources (add folders here if you add more Java code)
    src_dirs = [ROOT / "SUT", ROOT / "Listeners", ROOT / "utils"]
    sources = [p for d in src_dirs if d.exists() for p in d.rglob("*.java")]


    if not sources:
        print("[info] no Java sources found to compile (skipping)")
        return


    OUT_DIR.mkdir(parents=True, exist_ok=True)
    class_files = list(OUT_DIR.rglob("*.class"))

    src_mtime = latest_mtime(sources)
    cls_mtime = latest_mtime(class_files) if class_files else 0.0

    if not class_files or src_mtime > cls_mtime:
        print(f"[info] compiling Java sources → {OUT_DIR} "
              f"(sources newer: {datetime.fromtimestamp(src_mtime) if src_mtime else 'n/a'})")
        cmd = ["javac", "--release", "11", "-cp", JPF_CP, "-d", str(OUT_DIR)] + [str(p) for p in sources]
        subprocess.run(cmd, check=True, cwd=ROOT)
    else:
        print("[info] classes up‑to‑date; skipping compile")




def resolve_config(arg: str | None) -> pathlib.Path:
    if arg:
        p = pathlib.Path(arg)
        if not p.is_absolute():
            p = ROOT / p
        return p
    # Default config
    return CONFIGS_DIR / "SimpleTest2.jpf"



def main():

    runs = int(sys.argv[2]) if len(sys.argv) > 2 else 1

    answers = []

    # 1) Maybe compile (only when sources changed)
    maybe_compile()

    count = 0
    # 2) Resolve config path
    config = resolve_config(sys.argv[1] if len(sys.argv) > 1 else None)
    if not config.exists():
        sys.exit(f"[error] JPF config file not found: {config}")


    # 3) Run JPF
    print("Running JPF from:", JPF_CMD)
    cmd = [JPF_CMD, str(config)]
    print("[info] running:", " ".join(cmd))
    rc = 0
    for i in range(runs):
        val = None  # reset per run
        violated = False
        proc = subprocess.Popen(
            cmd,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            cwd=ROOT  # ensure relative paths in .jpf (like +classpath=out) resolve
        )
        for line in proc.stdout:
            sys.stdout.write(line)  # keep printing live
            if line.startswith("JPF_ANSWER "):
                parts = line.strip().split()
                if len(parts) >= 2:
                    try:
                        val = int(parts[1])
                    except ValueError:
                        pass
            if line.startswith("error"):  # <-- add this
                violated = True
        rc = proc.wait()
        answers.append((val, int(violated)))
        count += 1
        print(f"THIS IS RUN NUMBER: {count}")

    out_file = ROOT / "reports" / "simple_uniform.csv"
    out_file.parent.mkdir(exist_ok=True)

    with out_file.open("w", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["run", "answer", "violated"])  # <-- header
        for i, (v, viol) in enumerate(answers, start=1):
            writer.writerow([i, v, viol])

    print(f"[ok] wrote answers to {out_file}")

    csv_path = pathlib.Path(ROOT) / "reports" / "answers.csv"  # adjust if needed
    total = 0
    n = 0
    with csv_path.open() as f:
        r = csv.DictReader(f)
        for row in r:
            n += 1
            # handles empty cells: '', None → 0
            total += int(row.get("violated") or 0)

    print(f"violations: {total}/{n} ({(total / n * 100 if n else 0):.3f}%)")


    print(f"[info] jpf exited with code {rc}")
    sys.exit(rc)

if __name__ == "__main__":
    main()
