import csv
import os, pathlib, sys, subprocess
from pathlib import Path
from datetime import datetime
import time
from typing import Dict, List, Tuple
import tempfile

SCRIPT_DIR = pathlib.Path(__file__).resolve().parent
PROJECT_ROOT = SCRIPT_DIR.parent
REPORTS_DIR = PROJECT_ROOT / "reports"
CONFIGS_DIR = PROJECT_ROOT / "configs"

REPORTS_DIR.mkdir(parents=True, exist_ok=True)

#This script is used for running all experiments for the report. 
#All experiments can be found in csv files in the reports folder

ROOT = pathlib.Path(__file__).resolve().parents[1]

def setup():
    """ Check whether script is run with correct version of java (only checks if its java 11)"""
    result = subprocess.run(["java","-version"], stderr=subprocess.PIPE, universal_newlines=True)

    output = result.stderr
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

    list_of_projects = ["CupTest", "HashMaps", "SctBench"]

    for project in list_of_projects:
        try:
            print("Compiling with Gradle...")
            subprocess.run([f"./{project}/gradlew","-p",f"./{project}","build","-x","test"],check=True, cwd=ROOT)
            print("Finished Gradle compilation")
        except subprocess.CalledProcessError as e:
            sys.exit(f"[error] Gradle build failed: {e}")

#CHANGE
number_of_runs = 1000 #How many times each experiment is run 

list_of_probs_correctness = [0.5,0.8,0.9,0.95,0.99,0.999] #P-variables


#Ensuring that we find the bug
list_of_probs_time = [0.9999] #time variables

JPF_JAR = ROOT / "jpf-core" / "build" / "jpf.jar"
JPF_RUN_JAR = ROOT / "jpf-core" / "build" / "RunJPF.jar"
JPF_JAR_FOLDER = ROOT / "jpf-core" / "build"
CONFIGS_DIR = ROOT / "configs"

def resolve_package(package, cwd = False):
    '''
    The purpose of this method is to return the correct path to the given experiment in question
    '''

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
        HASHMAPS = ROOT / "HashMaps"
        # /home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/src/main/java/org/example
        if cwd:
            HASHMAPS
        TARGET = "org.example."
        BUILD_CLASSES = HASHMAPS / "app" / "build" / "classes" / "java" / "main"
        BUILD_RES = HASHMAPS / "app" / "build" / "resources" / "main"
    return BUILD_CLASSES, BUILD_RES, TARGET


def convert_to_jpf(tests, time_exp=False):
    '''
    The purpose of this method is to make jpf configurations that can be run through subproccesses
    '''
    jpf_files = {}

    #Check if this is a time experiment or not and provide the correct P-variables
    if time_exp:
        list_of_probs = list_of_probs_time
    else:
        list_of_probs = list_of_probs_correctness

    for test, tup in tests.items():
        BUILD_CLASSES, BUILD_RES, TARGET = resolve_package(tup[0])
        threads = tup[1]
        for p in list_of_probs:
            jpf_conf = [
                f"target = {TARGET}{test}",
                f"classpath = {BUILD_CLASSES}",
                f"native_classpath = {BUILD_RES}",
                "vm.args = -ea",
                "listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
                "search.class = gov.nasa.jpf.search.Reset_Search",
                f"+search_with_reset.probabilities = {str(p)} {str(1.0-p)}",
                "+search_with_reset.eps = 0.1", #Constant epsilon value
                "+numberOfThreads = " + str(threads),
                "search.multiple_errors = false",
                "jpf.report.console.property_violation = error",
                "report.console.finished = result,statistics,error",
                "report.unique_errors = true"
            ]
            if test not in jpf_files:
                jpf_files[test] = []
            jpf_files[test].append((jpf_conf, p))

            #If this is a time experiment also make a jpf file for model checking
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
    '''
    The purpose of this method is to run time experiments provided as paremeter argument
    '''
    times = []
    
    for name, test in jpf_runs.items():
        for tup in test:
            result = 0
            for x in range(0,number_of_runs):
                start = time.time()
                result = run_jpf(tup[0], True)
                end = time.time()
                res_time = end-start
                if result == 1:
                    times.append((name,res_time))
                else:
                    times.append((name,">30"))

    return times

def run_jpf(jpf_conf, time_exp):
    '''
    The purpose of this method is to run the jpf files provided.
    If time_epx is set to true it will run the experiments with a timer and return the timer
    if not it will simply report whether the test failed or not
    '''
    
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
        return 1
    else:
        return 0

def run_jpf_files(map_of_tests):
    '''
    The purpose of this method is to run every jpf file in map_of_tests
    '''
    results = []

    for name, test in map_of_tests.items():

        fullyDoneFlag = False
        for tup in test:
            successes = 0
            for x in range(0,number_of_runs):
                successes += run_jpf(tup[0],False)
            results.append((name, str(tup[1]), successes))
            if successes >= number_of_runs and fullyDoneFlag == True:
                break
            elif successes >= number_of_runs:
                fullyDoneFlag = True
            else:
                fullyDoneFlag = False

    return results



def write_to_csv(csv_name, results):
    '''
    Simple method that writes to a csv files all rows of the list results
    '''
    with open(REPORTS_DIR / csv_name, "w", newline="") as f:
        writer = csv.writer(f)
        
        for test in results:
            writer.writerow(test)

def read_experiment(csv_name):
    '''
    The purpose of this method is to read an experiment from a csv file and return it as a dict
    '''
    tests = {}
    with open(REPORTS_DIR / csv_name, "r", newline="") as f:
        reader = csv.DictReader(f)
        for row in reader:
            name = row["test"]
            threads = row["threads"]
            package = row["package"]

            tests[name] = (package, threads)
    return tests

def run_gradle_tests(gradletestfiles, log_name):
    '''
    The purpose of this method is to run the JVM tests through gradle
    '''
    for name, gradletestfile in gradletestfiles.items():
        x,y,package = resolve_package(gradletestfile[0])
        cwd = resolve_package(gradletestfile[0], True)
        log_file = ROOT / "reports" / f"{log_name}.log"
        log_file.parent.mkdir(parents=True, exist_ok=True)
        gradle_cmd = [
            "./gradlew",
            "test",
            "--tests",
            f"{package}{name}Test",
        ]

        with open(REPORTS_DIR / "JVM_tests.csv", "w") as f:
            result = subprocess.run(
                gradle_cmd,
                cwd=str(cwd),
                stdout=f,
                stderr=subprocess.STDOUT,
                text=True,
            )
        #Parse the result to below method in order to write to a csv
        parse_console_log("JVM_tests.csv", log_name)


def parse_console_log(log_file: str, output_csv: str):
    '''
    This method take the gradle log_file from run_gradle_tests and writes the number of runs per test to 
    '''
    count = {}
    reps = {}

    with open(REPORTS_DIR / log_file, "r") as f:
        for line in f:
            line = line.strip()
            if "repetition" in line:
                name = line.split()[0]
                name = f"{name[:-4]}JVM"
                count.setdefault(name, 0)
                reps.setdefault(name, 0)

                if line.endswith("FAILED"):
                    reps[name] += 1
                if line.endswith("FAILED") or line.endswith("PASSED"):
                    count[name] += 1

    with open(REPORTS_DIR / output_csv, "a", newline="") as f:
        writer = csv.writer(f)
        for name in count:
            #test,P,violated,k
            writer.writerow((name,0, reps[name], count[name]))


def split_alpha_numeric(s: str):
    '''
    Helper method for finding name and number of threads when runnning single files
    '''
    i = len(s)
    while i > 0 and s[i-1].isdigit():
        i -= 1
    return s[:i], s[i:]

def read_input():
    '''
    This method is used when the script is run with sys.argv arguments and has the purpose of reading the cmd arguments
    and pass them along for experiments to be run
    '''
    tests_to_run = {}
    for x in range (3,len(sys.argv)):
        test_str = sys.argv[x]
        test_name, threads_str = split_alpha_numeric(test_str)

        if not threads_str:
            raise ValueError(f"No thread count found in '{test_str}'")

        test = test_name
        number_of_threads = int(threads_str)

        tests_to_run[test] = (sys.argv[1], number_of_threads)

    
    return tests_to_run

def mini_ccp(P: Tuple[float, float], N=2, eps=0.1):
    '''
    Method for calculating the k values for csv files based on a tuple P, n = 2 and eps = 0.1 as default
    '''
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

EXPERIMENTS = [
    ("SctBench_tests_time.csv", True, "SctBench_time_res.csv"),
    ("correctness_tests.csv",False, "SctBench_res.csv"),
    ("baseline.csv",False, "baseline_res.csv"),
    ("HashMap_tests.csv",False,"HashMap_res.csv")
]

if __name__ == "__main__":

    #Check for java 11 
    setup()

        #If this script is run without any sys.argv arguments it will run all experiments else it can be run for single experiment
        #following below pattern
        #sys.argv[2] needs to specify the package
        #sys.argv[3] needs to specify "time" or "correctness"
        #the rest of the arguments needs to be specifiec with test + number of threads i.e. Wronglock1Bad2

    if len(sys.argv) > 1:
        
        exps = read_input()

        if sys.argv[2] == "time":
            exps_to_run = convert_to_jpf(exps,True)
            results = run_jpf_files_time(exps_to_run)
            header = [("test","result")]
            results = header + results
        else:
            exps_to_run = convert_to_jpf(exps,False)
            results = run_jpf_files(exps_to_run)
            header = [("test","P","violated")]
            results = header + results

        write_to_csv(f"tests_{sys.argv[1]}.csv",results)

    else:
        for exp in EXPERIMENTS:
            exps = read_experiment(exp[0])
            
            if exp[1]:
                exps_to_run = convert_to_jpf(exps,True)
                results = run_jpf_files_time(exps_to_run)
                header = [("test","result")]
                results = header + results
                write_to_csv(exp[2],results)
            else:
                exps_to_run = convert_to_jpf(exps,False)
                jpf_res = run_jpf_files(exps_to_run)
                results = []
                for res in jpf_res:
                    results.append((res[0],res[1],res[2],mini_ccp((float(res[1]),1.0-float(res[1])))))
                header = [("test","P","violated","k")]
                results = header + results
                write_to_csv(exp[2],results)
                if "Hash" not in exp[0]: #The hashmap tests do not use the JVM testing
                    run_gradle_tests(exps,exp[2])
            




