import csv
import os, pathlib, sys, subprocess
from datetime import datetime
from typing import List, Tuple

from legacy_run_Jpf import latest_mtime

# Can we even make the experiments replicable without forcing our readers to both setup JPF and/or set the terminal shortcuts for jpf
# All of this is hardcoded and is reliant on the shortcut path for jpf
# Find the right paths:
ROOT = pathlib.Path(__file__).resolve().parents[1]

OUT_DIR = ROOT / "out"
CONFIGS_DIR = ROOT / "configs"

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
    if csv_name is None:
        csv_name = "answers"

    out_file = ROOT / "reports" / f"{csv_name}.csv"
    out_file.parent.mkdir(exist_ok=True)

    with out_file.open("w", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["run", "answer", "violated"])  # <-- header
        for i, (v, viol) in enumerate(answers, start=1):
            writer.writerow([i, v, viol])

    print(f" answers -> {out_file}")


def handle_jpf(): #uses cmd line args, otherwise utilizes the dictionary of algo to jpf
    # sys.arg[0] python file to run, sys.arg[1] jpf.config, sys.arg[2] amount runs
    # sys.arg[0] doesn't count towards len()

    if len(sys.argv) > 1:
        config_file = sys.argv[1] #but needs to be sliced or similar, otherwise we get the entire path as the key..
        runs = int(sys.argv[2]) if len(sys.argv) > 2 else 1
        csv_name = pathlib.Path(sys.argv[1]).stem
        results, rc = run_jpf(csv_name, config_file, runs)
        populate_csv(csv_name, results)
        print(f'jpf exited with code {rc}')
        sys.exit(rc)

    else:
        # if algo_key is None:
        runs = 1400
        for key, config_path in algo_to_jpf.items():
            print(f'Running {key} via {config_path}')
            populate_csv(key, run_jpf(key, config_path, runs))
        return


def run_jpf(test_name: str, config_path: str, runs: int):
    config = resolve_config(config_path)
    if not config.exists():
        sys.exit(f'no jpf config provided {config}')
    
    print(f"Running jpf with {test_name}")
    cmd = [JPF_CMD, str(config)] #changing config Path object to str
    results = []
    #count = 0
    rc = 0
    
    for i in range(runs):
        val = None
        violated = False
        subproc = subprocess.Popen(
            cmd,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            cwd=ROOT
        )
        for line in subproc.stdout:
            sys.stdout.write(line)
            if line.startswith("JPF_ANSWER "):
                parts = line.strip().split()
                if len(parts) >= 2:
                    try:
                        val = int(parts[1])
                    except ValueError:
                        pass
            if line.startswith("error"):
                violated = True
        rc = subproc.wait()
        results.append((val, int(violated)))
        #count += 1
    return results, rc


# Something like this:
# INSTANCES_preSorted_Adaptive: List[Tuple[str, str]] = {
#     ("levelSort Presort INTEGERS Adaptive", "SortingVariations/app/build/libs/app.jar"),
#     ("binomialSort Presort INTEGERS Adaptive", "SortingVariations/app/build/libs/app.jar"),
#     ("levelSort Presort INTEGERS NonAdaptive", "SortingVariations/app/build/libs/app.jar"),
#     ("binomialSort Presort INTEGERS NonAdaptive", "SortingVariations/app/build/libs/app.jar")
# }
# Mapping of nickname keys for csv names -> to their respective .jpf setup files.
algo_to_jpf = {
    "SimpleTest2Rand": "configs/SimpleTest.jpf",  # resolve_config()?
    "SimpleTest2Uni": "configs/SimpleTest2.jpf",
    "MiniRand": "configs/MinimizationTest.jpf",
    "MiniUni": "configs/MinUniformTest.jpf",
}


if __name__ == "__main__":
    # use args when calling file to get it to run the experiment you're trying to.
    # if no args provided, utilizes the algo_to_jpf dictionary
    handle_jpf()
