
import os, pathlib, sys, subprocess


ROOT = pathlib.Path(__file__).resolve().parents[1]


if sys.argv[0] == 'CupTest':
    CUPTEST = ROOT / "CupTest"
    BUILD_CLASSES = CUPTEST / "app" / "build" / "classes" / "java" / "main"
    BUILD_RES = CUPTEST / "app" / "build" / "resources" / "main"
elif sys.argv[0] == "SctBench":
    SCTBENCH = ROOT / "SctBench"
    BUILD_RES = SCTBENCH / "app" / "build" / "resources" / "main"
    BUILD_CLASSES = SCTBENCH / "app" / "build" / "classes" / "java" / "main"
else:
    HASHMAPS = "HashMaps"
    BUILD_CLASSES = HASHMAPS / "app" / "build" / "classes" / "java" / "main"
    BUILD_RES = HASHMAPS / "app" / "build" / "resources" / "main"

JPF_JAR = ROOT / "jpf-core" / "build" / "jpf.jar"
JPF_RUN_JAR = ROOT / "jpf-core" / "build" / "RunJPF.jar"
JPF_JAR_FOLDER = ROOT / "jpf-core" / "build"
CONFIGS_DIR = ROOT / "configs"



list_of_probs = []

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

# We generate a dictionary with the of the test as key and list of .jpf instructions as value
def convert_to_jpf(testdict=None):
    jpf_files = {}
    if testdict == None:
        tests = read_input()
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
    else:
        tests = testdict
        for test_name, info in tests.items():
            pack = info["package"]
            threads = info["threads"]
            cwd = info["cwd"] # cuptest or one of the others

            for p in list_of_probs:
                jpf_conf = [
                    f"target = {pack}.{test_name}",
                    # "target = cs." + test,
                    f"classpath = {cwd}/app/build/classes/java/main",
                    #f"native_classpath = {cwd}/app/build/classes/java/main",
                    # f"native_classpath = {BUILD_RES}",
                    # "native_classpath = out",
                    "vm.args = -ea",
                    "listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
                    # "search.class = SearchAlgorithms.Reset_Search",
                    "search.class = gov.nasa.jpf.search.Reset_Search",
                    f"+search_with_reset.probabilities = {str(p)} {str(1.0-p)}",
                    "+search_with_reset.eps = 0.1",  # We should do some logic here with inserting eps and probabilities
                    "+numberOfThreads = " + str(threads),
                    "search.multiple_errors = false",
                    "jpf.report.console.property_violation = error",
                    "report.console.finished = result,statistics,error",
                    "report.unique_errors = true",
                ]
                if test_name not in jpf_files: # what is the purpose of this one?
                    jpf_files[test_name] = []

                jpf_files[test_name].append((jpf_conf, p))

    return jpf_files