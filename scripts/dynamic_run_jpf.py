import csv
import os, pathlib, sys, subprocess
from pathlib import Path
from datetime import datetime
import time
from typing import List, Tuple
import pandas as pd
from utilities import resolve_config, setup, populate_csv, parse_console_log, run_gradle_tests
from new_version import convert_to_jpf, run_jpf_files, time
from path_setup import*

# Fixed path
def handle_jpf(): #uses cmd line args, otherwise utilizes the dictionary of algo to jpf
    # sys.arg[0] python file to run, sys.arg[1] jpf.config, sys.arg[2] amount runs

    if len(sys.argv) > 1:
        config_file = sys.argv[1] #but needs to be sliced or similar, otherwise we get the entire path as the key..
        runs = int(sys.argv[2]) if len(sys.argv) > 2 else 1
        csv_name = pathlib.Path(sys.argv[1]).stem
        for i in range(0, runs):
            print(f"THIS IS THE {i}'TH RUN!!!!")
            start = time.time()
            results, rc, k = run_jpf(f"{csv_name}", config_file, runs)
            end = time.time()
            timelist = []
            timelist.append(end - start)
            populate_csv(csv_name, results)
            populate_csv(f"{csv_name}_time", timelist)

        print(f'jpf exited with code {rc}')
        sys.exit(rc)

    else:
        # if algo_key is None:
        for key, config_path in algo_to_jpf.items():
            print(f'Running {key} via {config_path}')
            populate_csv(key, run_jpf(key, config_path))
        return


# def time_jpf():
#     results, timelist = run_jpf_files()
#     populate_csv(f"{results[0]}_time", timelist)


def run_jpf(test_name: str, config_path: str, runs: int):
    config = resolve_config(config_path)
    if not config.exists():
        sys.exit(f'no jpf config provided {config}')
    # Building the HOST JVM classpath, so basically so the JPF jars can recognize our search algorithm(s)
    cp_parts = [
        str(JPF_JAR_FOLDER / "jpf.jar"),
        str(JPF_JAR_FOLDER / "jpf-classes.jar")
        # ,
        # str(BUILD_CLASSES),
    ]
    if BUILD_RES.exists():
        cp_parts.append(str(BUILD_RES))
    host_cp = os.pathsep.join(cp_parts)
    print(f"Running jpf with {test_name}")
    java_cmd = [
        "java",
        "-cp", host_cp,
        "gov.nasa.jpf.tool.RunJPF",
        str(config)
    ]
    print(java_cmd)
    results = []
    rc = 0
    k_value = 0
    val = None
    violated = False

    # IF we run the JaConTeBe config this boolean catches it and fixed the correct root path needed for jacontebe. It's a rough fix, but probably the simplest.
    is_jacontebe = "JaConTeBe/" in str(config)
    work_directory = JACONTEBE_ROOT if is_jacontebe else ROOT
    subproc = subprocess.Popen(
        java_cmd,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        cwd=work_directory
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
        if line.startswith("violated") or line.startswith("Deadlock"):
            vio_value = line.split()[1].strip()
            if vio_value == "true":
                violated = True
        if line.startswith("k:"):
            k_value = int(line.split()[1])
    rc = subproc.wait()
    results.append((k_value, int(violated)))
    return results, rc, k_value


def time_jpf():
    results, timelist = run_jpf_files(dict_of_experiments)
    for (name, p, result), t in zip(results, timelist):
        populate_csv(f"{name}_time", p, [t])


dict_of_experiments = {
    "MinimizationTest": {"package": "sut", "cwd": CUPTEST, "threads": 2},
    # "MinimizationTestWithNoise": {"package": "sut", "cwd": CUPTEST, "threads": 2},
    # "DeadlockExample": {"package": "sut", "cwd": CUPTEST, "threads": 2},
    # "AccountBadTest":      {"package": "sctbench.cs.origin", "cwd": SCTBENCH, "threads": 2},
    # "Carter01BadTest":     {"package": "sctbench.cs.origin", "cwd": SCTBENCH, "threads": 2},
    # #"FsbenchBadTest":      {"package": "sctbench.cs.origin", "cwd": SCTBENCH, "threads": 2},
    # "Phase01BadTest":      {"package": "sctbench.cs.origin", "cwd": SCTBENCH, "threads": 2},
    # "StackBadTest":        {"package": "sctbench.cs.origin", "cwd": SCTBENCH, "threads": 2},
    # "TokenRingBadTest":    {"package": "sctbench.cs.origin", "cwd": SCTBENCH, "threads": 2},
    # "Twostage100BadTest":  {"package": "sctbench.cs.origin", "cwd": SCTBENCH, "threads": 2},
    # "TwostageBadTest":     {"package": "sctbench.cs.origin", "cwd": SCTBENCH, "threads": 2},
    # "WronglockBadTest":    {"package": "sctbench.cs.origin", "cwd": SCTBENCH, "threads": 2},
    # "Wronglock1BadTest":   {"package": "sctbench.cs.origin", "cwd": SCTBENCH, "threads": 2},
    # "Wronglock3BadTest":   {"package": "sctbench.cs.origin", "cwd": SCTBENCH, "threads": 2},
}

if __name__ == "__main__":

    # use args when calling file to get it to run the experiment you're trying to.
    # if no args provided, utilizes the algo_to_jpf dictionary
    setup()

    # Give epsilon and p probability
    #handle_jpf()
    time_jpf()
