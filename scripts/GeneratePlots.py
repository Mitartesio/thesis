#!/usr/bin/env python3
from collections import defaultdict
import csv
import matplotlib.pyplot as plt

import csv
from collections import defaultdict
import matplotlib.pyplot as plt
from path_setup import ROOT

def makePlot(filename, include_tests=None, csv_name = "test"):
    """
    Plot P vs violated for each test in the CSV.

    Parameters:
    - filename: CSV file to read.
    - include_tests: Optional list of test names to include. 
                     If None or empty, include all tests.
    """
    # Dictionary: test_value → list of (P, violated)
    groups = defaultdict(lambda: {"P": [], "violated": [], "k": []})

    # Read CSV
    with open(filename, newline='') as f:
        reader = csv.DictReader(f)
        for row in reader:
            test_value = row["test"]
            groups[test_value]["P"].append(float(row["P"]))
            groups[test_value]["violated"].append(float(row["violated"]))
            groups[test_value]["k"].append(int(row["k"]))

    # Plot each test as its own line
    for test_value, data in groups.items():
        if include_tests and test_value not in include_tests:
            continue  # skip tests not in the include list
        P_sorted, violated_sorted = zip(*sorted(zip(data["P"], data["violated"])))
        k_sorted, violated_sorted = zip(*sorted(zip(data["k"], data["violated"])))
        # plt.plot(P_sorted, violated_sorted, label=f"test = {test_value}", linewidth=2)
        plt.plot(k_sorted, violated_sorted, label=f"test = {test_value}", linewidth=2)

    plt.xlabel("k")
    plt.ylabel("violated")
    plt.title("k vs violated for each test")
    plt.legend()
    plt.grid(True)

    # Save plot to file
    outpath = ROOT / "plots" / f"{csv_name}.png"
    #out_name = csv_name + ".png"
    plt.savefig(outpath, dpi=300, bbox_inches="tight")
    print(f"Saved plot as f{outpath.stem}")

    plt.show()


if __name__ == "__main__":
    # makePlot("Baseline.csv")
    #makePlot(ROOT/"reports"/"experiments"/"SCT_bench_results.csv", ["WronglockBad" , "Wronglock3Bad", "TwostageBad"], "SCT_bench_res1")
    makePlot(ROOT/"reports"/"experiments"/"SCT_bench_results.csv", ["StackBad" , "Wronglock1Bad", "Wronglock3Bad", "WronglockBad", "TwostageBad", "StackBad"], "SCT_bench_res2")
