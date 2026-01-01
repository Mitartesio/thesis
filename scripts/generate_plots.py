#!/usr/bin/env python3
from collections import defaultdict
import csv
import pathlib
import matplotlib.pyplot as plt

import csv
from collections import defaultdict
import matplotlib.pyplot as plt

SCRIPT_DIR = pathlib.Path(__file__).resolve().parent
PROJECT_ROOT = SCRIPT_DIR.parent
REPORTS_DIR = PROJECT_ROOT / "reports"
FIGURES_DIR = PROJECT_ROOT / "figures"
REPORTS_DIR.mkdir(parents=True, exist_ok=True)
FIGURES_DIR.mkdir(parents=True, exist_ok=True)

#Simple script for making the plots needed for the overleaf report

def makePlot(filename, include_tests=None, csv_name="test"):
    """
    Plot (P_min) vs violated with 0.5 on the left and 0.0 on the right.
    """

    groups = defaultdict(lambda: {"P_min": [], "violated": []})

    # Read CSV
    with open(REPORTS_DIR / filename, "r", newline="") as f:
        reader = csv.DictReader(f)
        for row in reader:
            test_value = row["test"]
            P = float(row["P"])

            groups[test_value]["P_min"].append(1.0 - P)
            groups[test_value]["violated"].append(float(row["violated"]))

    for test_value, data in groups.items():
        if include_tests and test_value not in include_tests:
            continue

        # Sort normally (ascending)
        x_sorted, violated_sorted = zip(
            *sorted(zip(data["P_min"], data["violated"]))
        )

        plt.plot(
            x_sorted,
            violated_sorted,
            label=f"test = {test_value}",
            linewidth=2,
            marker="o"
        )

    plt.xlabel("P_min")
    plt.ylabel("violated")
    plt.title("p_min vs violated")
    plt.legend()
    plt.grid(True)

    plt.gca().invert_xaxis()

    plt.savefig(FIGURES_DIR / f"{csv_name}.png", dpi=300, bbox_inches="tight")

    plt.show()



#I think this is solved in the run_experiments by now, lets discuss tomorrow
# def separate_combine(csv1: str, csv2: str, combined_name: str):
#     csv1_path = ROOT / "reports" / f"{csv1}.csv"
#     csv2_path = ROOT / "reports" / f"{csv2}.csv"
#     output_path = ROOT / "reports"/ f"{combined_name}.csv"
#     df1 = pd.read_csv(csv1_path)
#     df2 = pd.read_csv(csv2_path)
#     combined_df = pd.concat([df1, df2], ignore_index=True)
#     combined_df = combined_df.fillna(0)
#     combined_df.to_csv(output_path, index=False)j
#     return output_path.stem

if __name__ == "__main__":
    makePlot("SctBench_res.csv", ["WronglockBad" , "Wronglock3Bad", "TwostageBad"], "SCT_bench_res1")
    makePlot("SctBench_res.csv", ["StackBad" , "Wronglock1Bad", "Wronglock3Bad", "WronglockBad", "TwostageBad", "StackBad"], "SCT_bench_res2")