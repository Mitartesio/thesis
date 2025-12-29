import csv
import os, pathlib, sys, subprocess
from pathlib import Path
from datetime import datetime
import time
from typing import Dict, List, Tuple
# import pandas as pd
import tempfile
# from utilities import populate_csv

ROOT = pathlib.Path(__file__).resolve().parents[1] #Do we need this???

number_of_runs = 1 #How many times each experiment is run

list_of_probs_correctness = [0.5,0.8,0.9,0.95,0.99,0.999] #P-variables

list_of_probs_time = [0.9999] #time variables

JPF_JAR = ROOT / "jpf-core" / "build" / "jpf.jar"
JPF_RUN_JAR = ROOT / "jpf-core" / "build" / "RunJPF.jar"
JPF_JAR_FOLDER = ROOT / "jpf-core" / "build"
CONFIGS_DIR = ROOT / "configs"

def resolve_package(package, cwd = False):
    if package == 'CupTest':
        CUPTEST = ROOT / "CupTest"
        if cwd:
            return CUPTEST
        TARGET = "sut."
        BUILD_CLASSES = CUPTEST / "app" / "build" / "classes" / "java" / "main"
        BUILD_RES = CUPTEST / "app" / "build" / "resources" / "main"
    elif package == "SctBench":
        SCTBENCH = ROOT / "SctBench"
        if cwd:
            return SCTBENCH
        TARGET = "sctbench.cs.origin."
        BUILD_RES = SCTBENCH / "app" / "build" / "resources" / "main"
        BUILD_CLASSES = SCTBENCH / "app" / "build" / "classes" / "java" / "main"
    else:
        print("Hello hash")
        HASHMAPS = ROOT / "HashMaps"
        if cwd:
            HASHMAPS
        TARGET = "Missing:FIND"
        BUILD_CLASSES = HASHMAPS / "app" / "build" / "classes" / "java" / "main"
        BUILD_RES = HASHMAPS / "app" / "build" / "resources" / "main"
    return BUILD_CLASSES, BUILD_RES, TARGET

def convert_to_jpf(tests, time_exp=False):
    
    jpf_files = {}

    if time_exp:
        list_of_probs = list_of_probs_time
    else:
        list_of_probs = list_of_probs_correctness

    for test, tup in tests.items():
        BUILD_CLASSES, BUILD_RES, TARGET = resolve_package(tup[0])
        print(f"build classes: {BUILD_RES}")
        threads = tup[1]
        print(f"test: {test}, threads: {threads}")
        for p in list_of_probs:
            jpf_conf = [
                # "target = sctbench.cs.origin." + test,
                # "target = cs." + test,
                f"target = {TARGET}{test}",
                f"classpath = {BUILD_CLASSES}",
                # "native_classpath = CupTest/app/build/classes/java/main",
                f"native_classpath = {BUILD_RES}",
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

            if time_exp == True:
                jpf_conf_MC = [
                f"target = {TARGET}{test}",
                f"classpath = {BUILD_CLASSES}",
                f"native_classpath = {BUILD_RES}",
                "vm.args = -ea",
                "listener = gov.nasa.jpf.listener.AssertionProperty",
                "search.class = gov.nasa.jpf.search.DFSearch",
                "search.multiple_errors = false",
                "jpf.report.console.property_violation = error",
                "report.console.finished = result,statistics,error",
                "report.unique_errors = true"
            ]
                if f"{test}_jpf" not in jpf_files:
                    jpf_files[f"{test}_jpf"] = []
                jpf_files[f"{test}_jpf"].append((jpf_conf_MC, p))
    return jpf_files


def run_jpf_files_time(jpf_runs):
    times = []

    for name, test in jpf_runs.items():
        for tup in test:
            result = 0
            success = 0
            for x in range(0,number_of_runs):
                
                
                start = time.time()
                result = run_jpf(tup[0], True)
                end = time.time()
                res_time = end-start
                print(f"hello: {name}, {res_time}")
                if result == 1:
                    times.append((name,res_time))
                else:
                    times.append((name,res_time))

    return times

def run_jpf_files(map_of_tests):

    results = []

    for name, test in map_of_tests.items():
        print(f"Running {name}")

        fullyDoneFlag = False
        for tup in test:
            result = 0
            for x in range(0,number_of_runs):
                results += run_jpf(tup[0],False)
            results.append((name, str(tup[1]), result))
            if result >= number_of_runs and fullyDoneFlag == True:
                break
            elif result >= number_of_runs:
                fullyDoneFlag = True
            else:
                fullyDoneFlag = False

    return results

def run_jpf(jpf_conf, time_exp):
    jpf_jar = str(JPF_RUN_JAR)
    with tempfile.NamedTemporaryFile(mode='w', suffix='.jpf', delete=False) as f:
        jpf_path = f.name
        f.write("\n".join(jpf_conf))

    cmd = [
        "java",
        "-Xmx8g",
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
    if time_exp == True:
        try:
            stdout, stderr = process.communicate(timeout=30)
        except subprocess.TimeoutExpired:
            return -1
    else:
        stdout, stderr = process.communicate()
    output = stdout + stderr

    if "violated true" in output or "search.class = gov.nasa.jpf.search.DFSearch" in jpf_conf:
        print("correct")
        return 1
    else:
        return 0
    

def run_jpf_files(jpf_runs):

    results = []
    jpf_jar = str(JPF_RUN_JAR)

    for name, test in jpf_runs.items():
        print(f"Running {name}")

        fullyDoneFlag = False
        for tup in test:
            result = 0
            for x in range(0,number_of_runs):
                with tempfile.NamedTemporaryFile(mode='w', suffix='.jpf', delete=False) as f:
                    jpf_path = f.name
                    f.write("\n".join(tup[0]))
            
                cmd = [
                    "java",
                    "-Xmx8g",
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
            if result >= number_of_runs and fullyDoneFlag == True:
                break
            elif result >= number_of_runs:
                fullyDoneFlag = True
            else:
                fullyDoneFlag = False
    
    return results

def write_to_csv(csv_name, results):
    with open(f"reports/{csv_name}", "w", newline="") as f:
        writer = csv.writer(f)
        # rows
        
        for test in results:
            writer.writerow(test)

def read_experiment(csv_name):
    tests = {}
    with open(f"reports/{csv_name}", "r", newline="") as f:
        reader = csv.DictReader(f)
        for row in reader:
            name = row["test"]
            threads = row["threads"]
            package = row["package"]

            tests[name] = (package, threads)
    return tests

def run_gradle_tests(gradletestfiles, log_name):
    for name, gradletestfile in gradletestfiles.items():
        x,y,package = resolve_package(gradletestfile[0])
        cwd = resolve_package(gradletestfile[0], True)
        print(f"cwd: {cwd}, package: {package}")
        log_file = ROOT / "reports" / f"{log_name}.log"
        log_file.parent.mkdir(parents=True, exist_ok=True)

        gradle_cmd = [
            "./gradlew",
            "test",
            "--tests",
            f"{package}{name}Test",
        ]

        print(f"Running Gradle tests for {package}{name} in {cwd}...")
        with open("reports/JVM_tests.csv", "w") as f:
            result = subprocess.run(
                gradle_cmd,
                cwd=str(cwd),
                stdout=f,
                stderr=subprocess.STDOUT,
                text=True,
            )

        print(f"Gradle test finished with return code {result.returncode}")
        print(f"Gradle test log saved to {log_name}")
        parse_console_log("JVM_tests.csv", log_name)

def parse_console_log(
    log_file: str, output_csv: str
):  # need to make it so it takes str name instead

    rows = []
    current_rep = None
    current_result = None
    repetition_count = 0
    problem_name = log_file
    is_repetition_test = False

    with open(f"reports/{log_file}", "r") as f:
        for line in f:
            line = line.strip()
            if "repetition" in line:
                if line.endswith("PASSED"):
                    repetition_count += 1
                    rows.append(
                        [problem_name, repetition_count, 0]
                    )  # didnt find for instance deadlock.

                elif line.endswith("FAILED"):
                    repetition_count += 1
                    rows.append([problem_name, repetition_count, 1])

            if current_rep is not None and current_result is not None:
                rows.append([problem_name, current_rep, current_result])
                current_rep = None
                current_result = None

    with open("reports/{output_csv}.csv", "w", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["test", "k", "violated"])
        writer.writerows(rows)

    print(f"Parsing done. output -> {output_csv}")

EXPERIMENTS = [
    # ("SctBench_time_test.csv", True, "time_res2.csv"),
    ("correctness_tests.csv",False, "SctBench_res2.csv"),
    # ("baseline.csv",False, "baseline_experiments2.csv"),
    ("HashMap_tests.csv",False,"HashMap_res_test.csv")
]   

def split_alpha_numeric(s: str):
    i = len(s)
    while i > 0 and s[i-1].isdigit():
        i -= 1
    return s[:i], s[i:]

def read_input():
    tests_to_run = {}
    for x in range (3,len(sys.argv)):
        test_str = sys.argv[x]
        test_name, threads_str = split_alpha_numeric(test_str)

        if not threads_str:
            raise ValueError(f"No thread count found in '{test_str}'")

        test = test_name
        number_of_threads = int(threads_str)

        tests_to_run[test] = (sys.argv[1], number_of_threads)

        print(f"this is hte name: {test}, this is the number: {number_of_threads}")
    
    return tests_to_run

def mini_ccp(P: Tuple[float, float], N=2, eps=0.1):
    violation_sum = sum(P)
    if violation_sum > 1 + 1e-3:
        raise ValueError("Probabilities must sum to <= 1")
    
    k = N
    current_sum = eps

    while current_sum >= eps:
        current_sum = 0.0
        for prob in P:
            current_sum += (1 - prob) ** k
        k += 1

    return k-1


if __name__ == "__main__":

        #sys.argv[2] needs to specify the package
        #sys.argv[3] needs to specify "time" or "correctness"
        #the rest of the arguments needs to be specifiec with test + number of threads i.e. Wronglock1Bad2

    if len(sys.argv) > 1:
        exps = read_input()
        print("Found")

        if sys.argv[2] == "time":
            exps_to_run = convert_to_jpf(exps,True)
            print(f"length: {len(exps_to_run)}")
            results = run_jpf_files_time(exps_to_run)
            for res in results:
                print(f"hello: {res}")
            header = [("test","result")]
            results = header + results
        else:
            exps_to_run = convert_to_jpf(exps,False)
            print(f"length: {len(exps_to_run)}")
            results = run_jpf_files(exps_to_run)
            for res in results:
                print(f"hello: {res}")
            header = [("test","P","violated")]
            results = header + results

        write_to_csv(f"tests_{sys.argv[1]}.csv",results)

    else:
        for exp in EXPERIMENTS:
            print("I am here my guy")
            exps = read_experiment(exp[0])

            if exp[1]:
                exps_to_run = convert_to_jpf(exps,True)
                results = run_jpf_files_time(exps_to_run)
                header = [("test","result")]
                results = header + results
            else:
                print("Hello again")
                exps_to_run = convert_to_jpf(exps,False)
                jpf_res = run_jpf_files(exps_to_run)
                results = []
                for res in jpf_res:
                    results.append((res[0],res[1],res[2],mini_ccp((float(res[1]),1.0-float(res[1])))))
                header = [("test","P","violated","k")]
                results = header + results

                write_to_csv(exp[2],results)
                run_gradle_tests(exps,exp[2])
            




