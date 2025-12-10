import csv
import os, pathlib, sys, subprocess
from pathlib import Path
from datetime import datetime
from typing import Dict, List, Tuple
# import pandas as pd
import tempfile

numberOfRuns = 1000

# Fixed path

# Find the right paths:
ROOT = pathlib.Path(__file__).resolve().parents[1]

CUPTEST = ROOT / sys.argv[1]
BUILD_CLASSES = CUPTEST / "app" / "build" / "classes" / "java" / "main"
BUILD_RES = CUPTEST / "app" / "build" / "resources" / "main"
JPF_JAR = ROOT / "jpf-core" / "build" / "jpf.jar"
JPF_RUN_JAR = ROOT / "jpf-core" / "build" / "RunJPF.jar" 
JPF_JAR_FOLDER = ROOT / "jpf-core" / "build"


CONFIGS_DIR = ROOT / "configs"

list_of_probs = [0.5,0.8,0.9,0.95,0.99,0.999]



def setup():
    """ Check whether script is run with correct version of java (only checks if its java 11)"""
    result = subprocess.run(["java","-version"], stderr=subprocess.PIPE, universal_newlines=True)

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
        subprocess.run([f"./{sys.argv[1]}/gradlew","-p",f"./{sys.argv[1]}","build","-x","test"],check=True, cwd=ROOT)
        print("Finished Gradle compilation")
    except subprocess.CalledProcessError as e:
        sys.exit(f"[error] Gradle build failed: {e}")


# Not sure if we need this:
# def resolve_config(arg: str | None) -> pathlib.Path:
#     if arg:
#         p = pathlib.Path(arg)
#         if not p.is_absolute():
#             p = ROOT / p
#         return p
#     # Default config
#     return CONFIGS_DIR / "SimpleTest2.jpf"

def writeToCsv(tests):
    with open("results.csv",mode="w", newline="") as file:
        writer = csv.writer(file)
    
        writer.writerow(["test","P", "number of successes"])

        for result in tests:
            # print(f"{result[0]} with this many p as {result[1]} has: result: {result[2]}")
            writer.writerow([result[0], result[1],result[2]])
    

def split_alpha_numeric(s: str):
    i = len(s)
    while i > 0 and s[i-1].isdigit():
        i -= 1
    return s[:i], s[i:]

def read_input():
    tests_to_run = {}
    for x in range (2,len(sys.argv)):
        test_str = sys.argv[x]
        test_name, threads_str = split_alpha_numeric(test_str)

        if not threads_str:
            raise ValueError(f"No thread count found in '{test_str}'")

        test = test_name
        number_of_threads = int(threads_str)

        tests_to_run[test] = number_of_threads

        print(f"this is hte name: {test}, this is the number: {number_of_threads}")
    
    return tests_to_run

#We generate a dictionary with the of the test as key and list of .jpf instructions as value
def convert_to_jpf():
    tests = read_input()
    jpf_files = {}
    for test, threads in tests.items():
        for p in list_of_probs:
            jpf_conf = [
                "target = sctbench.cs.origin." + test,
                # "target = cs." + test,
                f"classpath = {BUILD_CLASSES}",
                "native_classpath = SctBench/app/build/classes/java/main",
                # f"native_classpath = {BUILD_RES}",
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
            if test not in jpf_files:
                jpf_files[test] = []
            jpf_files[test].append((jpf_conf, p))
    return jpf_files

def run_jpf_files():

    map_of_tests = convert_to_jpf()

    results = []
    jpf_jar = str(JPF_RUN_JAR)

    for name, test in map_of_tests.items():
        print(f"Running {name}")

        fullyDoneFlag = False
        for tup in test:
            result = 0
            for x in range(0,numberOfRuns):
                with tempfile.NamedTemporaryFile(mode='w', suffix='.jpf', delete=False) as f:
                    jpf_path = f.name
                    f.write("\n".join(tup[0]))
            
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
                        universal_newlines=True
                    )

                stdout, stderr = process.communicate()

                output = stdout + stderr

                if "violated true" in output:
                    result += 1
            results.append((name, str(tup[1]), result))
            if result >= numberOfRuns and fullyDoneFlag == True:
                break
            elif result >= numberOfRuns:
                fullyDoneFlag = True
            else:
                fullyDoneFlag = False

    for test in results:
        # print(f"This test: {test[0]} with p as {test[1]} had this many sucesses: {test[2]}")
        print(f"{test[0]},{test[1]},{test[2]}")
    return results


if __name__ == "__main__":

    # use args when calling file to get it to run the experiment you're trying to.
    # if no args provided, utilizes the algo_to_jpf dictionary
    setup()

    writeToCsv(run_jpf_files())

    # log_file = run_gradle_tests("MinimizationTesting")
    # outputcsv = ROOT / "reports" / "MinimizationTesting.csv"
    # parse_console_log(log_file, outputcsv)
    # logfile = run_gradle_tests("DeadlockTesting")
    # output_csv = ROOT / "reports" / "DeadlockTesting.csv"
    # parse_console_log(logfile, output_csv)
