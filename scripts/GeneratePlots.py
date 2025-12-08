#!/usr/bin/env python3
from collections import defaultdict
import csv
import matplotlib.pyplot as plt

def makePlot(filename):
    # Dictionary: test_value → list of (P, successes)
    groups = defaultdict(lambda: {"P": [], "successes": []})

    # Read CSV
    with open(filename, newline='') as f:
        reader = csv.DictReader(f)
        for row in reader:
            test_value = row["test"]
            groups[test_value]["P"].append(float(row["P"]))
            groups[test_value]["successes"].append(float(row["successes"]))

    # Plot each test as its own line
    for test_value, data in groups.items():
        P_sorted, successes_sorted = zip(*sorted(zip(data["P"], data["successes"])))
        plt.plot(P_sorted, successes_sorted, label=f"test = {test_value}", linewidth=2)

    plt.xlabel("P")
    plt.ylabel("successes")
    plt.title("P vs successes for each test")
    plt.legend()
    plt.grid(True)

    # ---- Save plot to file ----
    out_name = filename.replace(".csv", ".png")
    plt.savefig(out_name, dpi=300, bbox_inches="tight")
    print(f"Saved plot as {out_name}")

    plt.show()

if __name__ == "__main__":
    makePlot("Baseline.csv")
