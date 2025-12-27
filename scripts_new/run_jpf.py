import csv
import os, pathlib, sys, subprocess
from pathlib import Path
from datetime import datetime
import time
from typing import Dict, List, Tuple
import tempfile
import run_experiments

def read_jpf():
    tests = {}
    with open("all_tests.csv", "w", newline="") as f:
        reader = csv.reader(f)
        for row in reader:
            test_name = row["test"]
            number_of_threads = row["threads"]
            package = row["package"]

            if row["time_exp"] == "yes":
                run_experiments.run_time(test_name, number_of_threads, package)
            
            run_experiments.run_correctness(test_name, number_of_threads, package)

