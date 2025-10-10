import csv
import os, pathlib, sys, subprocess
from datetime import datetime
from typing import List

# Function 1
# Can we even make the experiments replicable without forcing our readers to both setup JPF and/or set the terminal shortcuts for jpf
# Find the right paths:
ROOT = pathlib.Path(__file__).resolve().parents[1]

OUT_DIR = ROOT / "out"
CONFIGS_DIR = ROOT / "configs"

# Function 2
# Find JPF location via environm,ent variable (hopefully also works in windows)
JPF_HOME = os.environ.get("JPF_HOME")
if not JPF_HOME:
    sys.exit("[error] Please set JPF_HOME environment variable")

JPF_CMD = str(pathlib.Path(JPF_HOME) / "bin" / "jpf")

# Build compile classpath for javac (JPF jars + repo root so imports resolve) (so its OS dependent and should work on non-unix system, lets see)
CP_SEP = os.pathsep
JPF_CP = f"{JPF_HOME}/build/jpf.jar{CP_SEP}{JPF_HOME}/build/jpf-classes.jar{CP_SEP}{ROOT}"


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

    # Is this just the statistics printing?
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    class_files = list(OUT_DIR.rglob("*.class"))

    src_mtime = latest_mtime(sources)
    cls_mtime = latest_mtime(class_files) if class_files else 0.0

    if not class_files or src_mtime > cls_mtime:
        print(
            f"[info] compiling Java sources → {OUT_DIR} "
            f"(sources newer: {datetime.fromtimestamp(src_mtime) if src_mtime else 'n/a'})"
        )
        cmd = ["javac", "--release", "11", "-cp", JPF_CP, "-d", str(OUT_DIR)] + [
            str(p) for p in sources
        ]
        subprocess.run(cmd, check=True, cwd=ROOT)
    else:
        print("[info] classes up‑to‑date; skipping compile")


# Not sure if we need this:
def resolve_config(arg: str | None) -> pathlib.Path:
    if arg:
        p = pathlib.Path(arg)
        if not p.is_absolute():
            p = ROOT / p
        return p
    # Default config
    return CONFIGS_DIR / "SimpleTest2.jpf"


# Populate csv, can be useful. Not sure if better to continue the root structure or convert to just finding it normally.
def populate_csv(csv_name: str, answers: List[int]):
    out_file = ROOT / "reports" / "{csv_name}.csv"
    out_file.parent.mkdir(exist_ok=True)

    with out_file.open("w", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["run", "answer", "violated"])  # <-- header
        for i, (v, viol) in enumerate(answers, start=1):
            writer.writerow([i, v, viol])

    print(f"[ok] wrote answers to {out_file}")

#Mapping of nickname keys for csv names -> to their respective .jpf setup files.
algo_to_jpf = {
    "SimpleTest2Rand": "configs/SimpleTest.jpf",  # resolve_config()?
    "SimpleTest2Uni": "configs/SimpleTest2.jpf",
    "MiniRand": "configs/MinimizationTest.jpf",
    "MiniUni": "configs/MinUniformTest.jpf",
}


if __name__ == "__main__":
    pass
