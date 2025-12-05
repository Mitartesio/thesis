import subprocess
import sys
from typing import List, Iterator, Tuple, Dict

import os
import subprocess
import tempfile

numberOfRuns = 5

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
            "+search_with_reset.probabilities = 0.9999 0.0001",
            "+search_with_reset.eps = 0.1", #We should do some logic here with inserting eps and probabilities
            "+numberOfThreads = " + str(threads),
            "search.multiple_errors = false",
            "jpf.report.console.property_violation = error",
            "report.console.finished = result,statistics,error",
            "report.unique_errors = true"
        ]
        jpf_files[test] = jpf_conf
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

            if "error" in stdout:
                results[name] += 1

                
                
    for test, successes in results.items():
        print(f"This test: {test} had this many sucesses: {successes}")
    return results

#I assume we already have a method for this but basically we just need to run all tests in the map and write to csv
def run_all(map: Dict):
    for test_name, spec in map.items():
        print(f"Running test: {test_name}")
        # subprocess.run(spec)


if __name__ == '__main__':
    run_jpf()



