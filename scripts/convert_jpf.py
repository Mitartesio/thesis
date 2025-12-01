import subprocess
import sys
from typing import List, Iterator, Tuple, Dict

import os
import subprocess
import tempfile

#We put in all of our tests here with the number of threads
all_tests = {"MinimizationTest": 2, "DeadlockExample": 2, "DifficultTest" : 4}

tests_to_run = {}

#We generate a dictionary with the of the test as key and list of .jpf instructions as value
def convert_to_jpf(tests: Dict):
    jpf_files = {}
    for test, threads in tests.items():
        jpf_conf = [
            "target = sut." + test,
            "classpath = CupTest/app/build/classes/java/main",
            "native_classpath = CupTest/app/build/resources/main",
            # "native_classpath = out",
            "vm.args = -ea",
            "listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
            # "search.class = SearchAlgorithms.Reset_Search",
            "search.class = gov.nasa.jpf.search.Reset_Search",
            "+search_with_reset.probabilities = 0.999 0.001",
            "+search_with_reset.eps = 0.1", #We should do some logic here with inserting eps and probabilities
            "+numberOfThreads = " + str(threads),
            "search.multiple_errors = false",
            "jpf.report.console.property_violation = error",
            "report.console.finished = result,statistics,error",
            "report.unique_errors = true"
        ]
        jpf_files[test] = jpf_conf
    return jpf_files

def run_jpf(map_of_tests: Dict):

    results = {}
    jpf_jar = "jpf-core/build/RunJPF.jar"

    for name, test in map_of_tests.items():
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

        results[name] = {
            "exit_code": process.returncode,
            "stdout": stdout,
            "stderr": stderr,
            "jpf_file": jpf_path
        }

    return results

#I assume we already have a method for this but basically we just need to run all tests in the map and write to csv
def run_all(map: Dict):
    for test_name, spec in map.items():
        print(f"Running test: {test_name}")
        # subprocess.run(spec)


if __name__ == '__main__':
    if len(sys.argv) > 1:
        for x in range(1, len(sys.argv)):
            print("1")
            if sys.argv[x] in all_tests:
                tests_to_run[sys.argv[x]] = all_tests[sys.argv[x]]
            else:
                print("Error")
    myMap = run_jpf(convert_to_jpf(tests_to_run))
    for name, test in myMap.items():
        if 'error' in test["stdout"]:
            print(f"Success for {name}")
        else:
            print(test)


