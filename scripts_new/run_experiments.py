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

number_of_runs = 10 #How many times each experiment is run

list_of_probs_correctness = [0.5,0.8,0.9,0.95,0.99,0.999] #P-variables

list_of_probs_time = [0.9999] #time variables

JPF_JAR = ROOT / "jpf-core" / "build" / "jpf.jar"
JPF_RUN_JAR = ROOT / "jpf-core" / "build" / "RunJPF.jar"
JPF_JAR_FOLDER = ROOT / "jpf-core" / "build"
CONFIGS_DIR = ROOT / "configs"

def resolve_package(package):

    if package == 'CupTest':
        CUPTEST = ROOT / "CupTest"
        TARGET = "sut."
        BUILD_CLASSES = CUPTEST / "app" / "build" / "classes" / "java" / "main"
        BUILD_RES = CUPTEST / "app" / "build" / "resources" / "main"
    elif package == "SctBench":
        SCTBENCH = ROOT / "SctBench"
        TARGET = "sctbench.cs.origin."
        BUILD_RES = SCTBENCH / "app" / "build" / "resources" / "main"
        BUILD_CLASSES = SCTBENCH / "app" / "build" / "classes" / "java" / "main"
    else:
        HASHMAPS = "HashMaps"
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
        threads = tup[1]
        print(f"test: {test}")
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
        print(f"wrong: {output}")
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
    with open(csv_name, "w", newline="") as f:
        writer = csv.writer(f)
        # rows
        for test in results:
            writer.writerow(test)

def read_experiment(csv_name):
    tests = {}
    with open(csv_name, "r", newline="") as f:
        reader = csv.DictReader(f)
        for row in reader:
            name = row["test"]
            threads = row["threads"]
            package = row["package"]

            tests[name] = (package, threads)
    return tests

EXPERIMENTS = [
    ("time_tests.csv", True, "time_res.csv"),
    ("correctness_tests.csv",False, "SctBench_res.csv"),
    ("baseline.csv",False, "baseline_experiments.csv")
]   


if __name__ == "__main__":
    for exp in EXPERIMENTS:
        exps = read_experiment(exp[0])

        if exp[1]:
            exps_to_run = convert_to_jpf(exps,True)
            results = run_jpf_files_time(exps_to_run)
            header = [("name","result")]
            results = header + results
        else:
            exps_to_run = convert_to_jpf(exps,False)
            results = run_jpf_files(exps_to_run)
            header = [("name","threads","result")]
            results = header + results

        write_to_csv(exp[2],results)
        

