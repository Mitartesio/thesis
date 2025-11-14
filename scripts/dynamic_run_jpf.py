import csv
import os, pathlib, sys, subprocess
from pathlib import Path
from datetime import datetime
from typing import List, Tuple
import pandas as pd

# Fixed path

# Find the right paths:
ROOT = pathlib.Path(__file__).resolve().parents[1]

CUPTEST = ROOT / "CupTest"
BUILD_CLASSES = CUPTEST / "app" / "build" / "classes" / "java" / "main"
BUILD_RES = CUPTEST / "app" / "build" / "resources" / "main"
JPF_JAR = ROOT / "jpf-core" / "build" / "jpf.jar"
JPF_JAR_FOLDER = ROOT / "jpf-core" / "build"


CONFIGS_DIR = ROOT / "configs"


def setup():
    """ Check whether script is run with correct version of java (only checks if its java 11)"""
    result = subprocess.run(["java","-version"], stderr=subprocess.PIPE, text=True)

    output = result.stderr
    # print(output)
    if 'version' in output:
        versionLine = output.splitlines()[0]
        java_version = versionLine.split('"')[1]
        if java_version.split(".")[0] == '11':
            print("Correctly using java 11.xx.xx")
        else:
            print("WARNING: Using wrong version of java. please use java 11")
            sys.exit(1)

    """Here we're building the jars needed for jpf _ NOT CONFIRMED WORKING YET"""

    if JPF_JAR.exists():
        print("JPF JARs already exist, skipping JAR generation")
    else:
        try:
            print("generating JPF jars...")
            subprocess.run(["./jpf-core/gradlew","-p","./jpf-core","buildJars"],check=True, cwd=ROOT)
            print("Finished JPF JAR building")
        except subprocess.CalledProcessError as e:
            sys.exit(f"[error] JAR generation failed: {e}")


    """ here we compile with gradle, which should ensure we've compiled with java 11, as its a demand in the grald build"""

    try:
        print("Compiling with Gradle...")
        subprocess.run(["./CupTest/gradlew","-p","./CupTest","build","-x","test"],check=True, cwd=ROOT)
        print("Finished Gradle compilation")
    except subprocess.CalledProcessError as e:
        sys.exit(f"[error] Gradle build failed: {e}")


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
        csv_name = "results"

    out_file = ROOT / "reports" / f"{csv_name}.csv"
    out_file.parent.mkdir(exist_ok=True)

    if not out_file.exists():
        with out_file.open("w", newline="") as f:
            writer = csv.writer(f)
            writer.writerow(["problem", "k", "violated"])  # <-- header
            problem = csv_name
            k, viol = answers[0]
            writer.writerow([problem, k, viol])
    else:
        with out_file.open("a", newline="") as f:
            writer = csv.writer(f)
            problem = csv_name
            k, viol = answers[0]
            writer.writerow([problem, k, viol])

    print(f" answers -> {out_file.stem}.csv")


def combine_and_convert_csv(
    csv1: str, csv2: str, combinedname: str):
    csv1_path = ROOT / "reports" / f"{csv1}.csv"
    csv2_path = ROOT / "reports" / f"{csv2}.csv"

    output_path = ROOT / "reports" / f"{combinedname}.csv"
    output_path.parent.mkdir(parents=True, exist_ok=True)
    csv1 = pd.read_csv(csv1_path)
    csv2 = pd.read_csv(csv2_path)
    csv1_df = pd.DataFrame(csv1)
    csv2_df = pd.DataFrame(csv2)
    combined_csv = pd.concat([csv1_df, csv2_df], ignore_index=True)
    return combined_csv.to_csv(output_path, index=False, float_format="%.0f")


def handle_jpf(): #uses cmd line args, otherwise utilizes the dictionary of algo to jpf
    # sys.arg[0] python file to run, sys.arg[1] jpf.config, sys.arg[2] amount runs

    if len(sys.argv) > 1:
        config_file = sys.argv[1] #but needs to be sliced or similar, otherwise we get the entire path as the key..
        runs = int(sys.argv[2]) if len(sys.argv) > 2 else 1
        csv_name = pathlib.Path(sys.argv[1]).stem
        for i in range(0, runs):
            print(f"THIS IS THE {i}'TH RUN!!!!")
            results, rc, k = run_jpf(csv_name, config_file, runs)
            populate_csv(csv_name, results)
        print(f'jpf exited with code {rc}')
        sys.exit(rc)

    else:
        # if algo_key is None:
        for key, config_path in algo_to_jpf.items():
            print(f'Running {key} via {config_path}')
            populate_csv(key, run_jpf(key, config_path))
        return


def run_jpf(test_name: str, config_path: str, runs: int):
    config = resolve_config(config_path)
    if not config.exists():
        sys.exit(f'no jpf config provided {config}')

    # Building the HOST JVM classpath, so basically so the JPF jars can recognize our search algorithm(s)
    cp_parts = [
        str(JPF_JAR_FOLDER / "jpf.jar"),
        str(JPF_JAR_FOLDER / "jpf-classes.jar"),
        str(BUILD_CLASSES),
    ]
    if BUILD_RES.exists():
        cp_parts.append(str(BUILD_RES))
    host_cp = os.pathsep.join(cp_parts)

    print(f"Running jpf with {test_name}")

    # target_class = BUILD_CLASSES / "SUT" / "MinimizationTest.class"
    # print("[debugging] target class file:", target_class)
    # print("[debugging] exists? ", target_class.exists())
    #
    # # check: java -cp <BUILD_CLASSES> SUT.MinimizationTest (should not crash on class not found)
    # probe = subprocess.run(
    #     [JAVA, "-cp", str(BUILD_CLASSES), "SUT.MinimizationTest"],
    #     cwd=ROOT, capture_output=True, text=True
    # )
    # For some reason doesnt work with just java, dont know why this one needs caps
    # print("[debug] direct run exit:", probe.returncode)
    # print("[debug] direct run out:", probe.stdout.strip())
    # print("[debug] direct run err:", probe.stderr.strip())

    java_cmd = [
        "java",
        "-cp", host_cp,

        "gov.nasa.jpf.tool.RunJPF",
        str(config)
    ]

    # cmd = [JPF_CMD, str(config)] #changing config Path object to str
    results = []
    # count = 0
    rc = 0
    k_value = 0

    val = None
    violated = False
    subproc = subprocess.Popen(
        java_cmd,
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
        if line.startswith("violated"):
            vio_value = line.split()[1].strip()
            if vio_value == "true":
                violated = True
        if line.startswith("k:"):
            k_value = int(line.split()[1])
    rc = subproc.wait()
    results.append((k_value, int(violated)))
    # count += 1
    return results, rc, k_value

def run_gralde_tests(): # making it more modular
    log_file = ROOT/ "reports" / "minimizationtest-console.log"
    log_file.parent.mkdir(parents=True, exist_ok=True)

    gradle_cmd = [
        "./gradlew",
        "test", 
        "--tests",
        "sut.MinimizationTesting"
    ]

    print("Running Gradle tests...")
    with open(log_file, "w") as f:
        result = subprocess.run(
            gradle_cmd,
            cwd=str(CUPTEST),
            stdout=f,
            stderr=subprocess.STDOUT,
            text=True,
            #check=True makes it so it doesn't create the csv if the build fails
        )

    print(f"Gradle test finished with return code {result.returncode}")
    print(f"Gradle test log saved to {log_file}")
    return log_file

def parse_console_log(log_file: Path, output_csv: Path): #need to make it so it takes str name instead
    output_csv.parent.mkdir(parents=True, exist_ok=True)

    rows = []
    current_rep = None
    current_result = None

    with open(log_file, "r") as f:
        for line in f:
            line = line.strip()
            #   "MinimizationTesting > repetition 123 of 10000"
            if "repetition" in line and "of" in line:
                try:
                    parts = line.split()
                    rep_index = parts.index("repetition") + 1
                    current_rep = int(parts[rep_index])
                    # rep_number = int(parts[3]) # x of n
                except Exception:
                    current_rep = None

            elif line.startswith("RESULT"):
                current_result = 0 if line.split(":", 1)[1].strip().lower() == "true" else 1
                # current_result = line.split(":", 1)[1].strip()

            if current_rep is not None and current_result is not None:
                rows.append([current_rep, current_result])
                current_rep = None
                current_result = None

    with open(output_csv, "w", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["Repetition", "Result"])
        writer.writerows(rows)

    print(f"Parsing done. output -> {output_csv}")


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

# def main():
#     gradle_compile()
#     handle_jpf()


if __name__ == "__main__":

    # use args when calling file to get it to run the experiment you're trying to.
    # if no args provided, utilizes the algo_to_jpf dictionary
    # setup()

    # Give epsilon and p probability
    # handle_jpf()

    # logfile = run_gralde_tests()
    output_csv = ROOT / "reports" / "MinimizationTesting-output.csv"
    logfile = ROOT / "reports" / "minimizationtest-console.log"
    parse_console_log(logfile, output_csv)
