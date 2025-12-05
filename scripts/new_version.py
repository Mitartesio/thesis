import csv
import os, pathlib, sys, subprocess
from pathlib import Path
from datetime import datetime
from typing import Dict, List, Tuple
import pandas as pd
import tempfile

numberOfRuns = 1000

# Fixed path

# Find the right paths:
ROOT = pathlib.Path(__file__).resolve().parents[1]

CUPTEST = ROOT / "CupTest"
BUILD_CLASSES = CUPTEST / "app" / "build" / "classes" / "java" / "main"
BUILD_RES = CUPTEST / "app" / "build" / "resources" / "main"
JPF_JAR = ROOT / "jpf-core" / "build" / "jpf.jar"
JPF_JAR_FOLDER = ROOT / "jpf-core" / "build"


CONFIGS_DIR = ROOT / "configs"

list_of_probs = [0.999,0.99,0.95,0.9,0.8,0.7,0.5,0.3]

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


    """ here we compile with gradle, which should ensure we've compiled with java 11, as its a demand in the gradle build"""

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

def writeToCsv(tests: Dict):
    with open("results.csv",mode="w", newline="") as file:
        writer = csv.writer(file)

        writer.writerow(["test","number of successes"])

        for test, result in tests.items():
            writer.writerow([test, result])
    




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


# def handle_jpf(): #uses cmd line args, otherwise utilizes the dictionary of algo to jpf
#     # sys.arg[0] python file to run, sys.arg[1] jpf.config, sys.arg[2] amount runs

#     if len(sys.argv) > 1:
#         config_file = sys.argv[1] #but needs to be sliced or similar, otherwise we get the entire path as the key..
#         runs = int(sys.argv[2]) if len(sys.argv) > 2 else 1
#         csv_name = pathlib.Path(sys.argv[1]).stem
#         for i in range(0, runs):
#             print(f"THIS IS THE {i}'TH RUN!!!!")
#             results, rc, k = run_jpf(csv_name, config_file, runs)
#             populate_csv(csv_name, results)
#         print(f'jpf exited with code {rc}')
#         sys.exit(rc)

#     else:
#         # if algo_key is None:
#         for key, config_path in algo_to_jpf.items():
#             print(f'Running {key} via {config_path}')
#             populate_csv(key, run_jpf(key, config_path))
#         return

def read_input():
    tests_to_run = {}
    for x in range (1,len(sys.argv)):
        test = sys.argv[x][:-1]
        number_of_threads = int(sys.argv[x][-1])

        tests_to_run[test] = number_of_threads
    
    return tests_to_run

#We generate a dictionary with the of the test as key and list of .jpf instructions as value
def convert_to_jpf():
    tests = read_input()
    jpf_files = []
    for test, threads in tests.items():
        for p in list_of_probs:
            jpf_conf = [
                "target = sut." + test,
                "classpath = CupTest/app/build/classes/java/main",
                "native_classpath = CupTest/app/build/resources/main",
                # "native_classpath = out",
                "vm.args = -ea",
                "listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
                # "search.class = SearchAlgorithms.Reset_Search",
                "search.class = gov.nasa.jpf.search.Reset_Search",
                f"+search_with_reset.probabilities = {str(p)} {str(1.0-p)}",
                "+search_with_reset.eps = 0.1", #We should do some logic here with inserting eps and probabilities
                "+numberOfThreads = " + str(threads),
                "search.multiple_errors = false",
                "jpf.report.console.property_violation = error",
                "report.console.finished = result,statistics,error",
                "report.unique_errors = true"
            ]
            name = test + str(p)
            jpf_files[name] = jpf_conf
    return jpf_files

def run_jpf():

    map_of_tests = convert_to_jpf()

    results = {}
    jpf_jar = "jpf-core/build/RunJPF.jar"

    
    for name, test in map_of_tests.items():
        print(f"Running {name}")
        results[name] = 0
        for x in range(0,numberOfRuns):
            with tempfile.NamedTemporaryFile(mode='w', suffix='.jpf', delete=False) as f:
                jpf_path = f.name
                f.write("\n".join(test))
        
            cmd = [
                "java",
                "-Xmx4g",
                "-ea",
                "-jar",
                jpf_jar,
                jpf_path
                ]
            
            process = subprocess.Popen(
                    cmd,
                    stdout=subprocess.PIPE,
                    stderr=subprocess.PIPE,
                    text=True
                )

            

            stdout, stderr = process.communicate()

            if "violated true" in stdout:
                results[name] += 1

                
                
    for test, successes in results.items():
        print(f"This test: {test} had this many sucesses: {successes}")
    return results

#I assume we already have a method for this but basically we just need to run all tests in the map and write to csv
def run_all(map: Dict):
    for test_name, spec in map.items():
        print(f"Running test: {test_name}")
        # subprocess.run(spec)


# def main():
#     gradle_compile()
#     handle_jpf()


if __name__ == "__main__":

    # use args when calling file to get it to run the experiment you're trying to.
    # if no args provided, utilizes the algo_to_jpf dictionary
    setup()

    writeToCsv(run_jpf())

    # log_file = run_gradle_tests("MinimizationTesting")
    # outputcsv = ROOT / "reports" / "MinimizationTesting.csv"
    # parse_console_log(log_file, outputcsv)
    # logfile = run_gradle_tests("DeadlockTesting")
    # output_csv = ROOT / "reports" / "DeadlockTesting.csv"
    # parse_console_log(logfile, output_csv)
