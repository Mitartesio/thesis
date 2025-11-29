import subprocess
from typing import List, Iterator, Tuple, Dict

#We put in all of our tests here with the number of threads
all_tests = {"Test1": 1, "Test2": 2}

#We generate a dictionary with the of the test as key and list of .jpf instructions as value
def convert_to_jpf(tests: Dict):
    jpf_files = {}
    for test, threads in tests.items():
        jpf_conf = [
            "target = sut." + test,
            "classpath = CupTest/app/build/classes/java/main",
            "native_classpath = out",
            "vm.args = -ea",
            "listener = Listeners.Listener_Uniform_Adapts,Listeners.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
            "search.class = SearchAlgorithms.Reset_Search",
            "+search_with_reset.probabilities = 0.999 0.001"
            "+search_with_reset.eps = 0.1", #We should do some logic here with inserting eps and probabilities
            "+numberOfThreads = " + str(threads),
            "search.multiple_errors = false",
            "jpf.report.console.property_violation = error",
            "report.console.finished = result,statistics,error",
            "report.unique_errors = true"
        ]
        jpf_files[test] = jpf_conf
    return jpf_files

#I assume we already have a method for this but basically we just need to run all tests in the map and write to csv
def run_all(map_of_tests: Dict):
    for test_name, spec in map_of_tests.items():
        print(f"Running test: {test_name}")
        # subprocess.run(spec)


if __name__ == '__main__':
    run_all(convert_to_jpf(all_tests))

