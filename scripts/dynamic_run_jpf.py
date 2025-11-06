import csv
import os, pathlib, sys, subprocess
from datetime import datetime
from typing import List, Tuple

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
        csv_name = "answers"

    out_file = ROOT / "reports" / f"{csv_name}.csv"
    out_file.parent.mkdir(exist_ok=True)

    # if we need runs back... to have a sample space to make mean or median performance
    # with out_file.open("w", newline="") as f:
    #     writer = csv.writer(f)
    #     writer.writerow(["run", "k", "violated"])  # <-- header
    #     for i, (k, viol) in enumerate(answers, start=1):
    #         writer.writerow([k, viol])

    with out_file.open("w", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["k", "violated"])  # <-- header
        k, viol = answers
        writer.writerow([k, viol])

    print(f" answers -> {out_file.stem}.csv")


def handle_jpf(): #uses cmd line args, otherwise utilizes the dictionary of algo to jpf
    # sys.arg[0] python file to run, sys.arg[1] jpf.config, sys.arg[2] amount runs

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

    for i in range(runs):
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
                violated = line.split()[1].strip()
                if violated == "true":
                    violated = True
                else:
                    violated = False
            if line.startswith("k:"):
                k_value = int(line.split()[1])
        rc = subproc.wait()
        results.append((k_value, int(violated)))
        # count += 1
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

# def main():
#     gradle_compile()
#     handle_jpf()


if __name__ == "__main__":


    # from pathlib import Path
    #
    # JAVA = os.environ.get("JAVA_HOME")
    # JAVA = str(Path(JAVA) / "bin" / "java") if JAVA else "java"
    #
    # # Cehck which java we're running from
    # print("[debug] using java:", JAVA)


    # use args when calling file to get it to run the experiment you're trying to.
    # if no args provided, utilizes the algo_to_jpf dictionary

    setup()

    # Give epsilon and p probability
    handle_jpf()
