import csv
from typing import List, Dict, Tuple
import numpy as np
import pandas as pd
from utilities import combine_and_convert_csv
from path_setup import ROOT

def read_results(filename: str):
    results: Dict[str, Dict[int, int]] = {}

    with open(filename, 'r', newline='') as f:
        reader = csv.DictReader(f)
        for row in reader:
            problem = row['problem'] or row['test']
            k = row['k']
            p = row['P']
            violated = row['violated']

            if problem not in results:
                results[problem] = {}
            results[problem][k] = violated
    return results


def get_mean_median(csv_name: str):
    csv_file = ROOT / "reports" / f"{csv_name}.csv"

    df = pd.read_csv(csv_file)

    if "k" not in df.columns:
        raise ValueError(
            f"CSV '{csv_name}.csv' has no 'k' column. "
            f"Run format_csv('{csv_name}') first."
        )

    if "test" not in df.columns:
        raise ValueError(f"CSV '{csv_name}.csv' must contain a 'test' column.")

    df["k"] = df["k"].astype("Int64")
    df["violated"] = df["violated"].astype("Int64")

    result_rows = []

    for test_name, group in df.groupby("test"):
        k_max = int(group["k"].max())
        result_rows.append(
            {
                "test": test_name,
                #"k": k_value,
                "k_max": k_max,
                "violated_mean": float(group["violated"].mean()),
                "violated_median": int(group["violated"].median()),
            }
        )

    result_df = pd.DataFrame(result_rows)

    out_path = csv_file.with_name(f"{csv_name}_mean.csv")

    result_df.to_csv(f"{out_path}", index=False) # idx false to overwrite csv

    return result_df


def format_csv(csv_name: str):
    csv_file = ROOT/ "reports" / f"{csv_name}.csv"
    df = pd.read_csv(f"{csv_file}")
    if "problem" in df.columns:
        df.rename(columns={"problem": "test"}, inplace=True)
    if 'k' not in df.columns:
        if "P" not in df.columns:
            raise ValueError(f"'P' column not found in {csv_file}")
        for i, row in df.iterrows():
            p = row["P"]
            df.at[i, "k"] = mini_ccp([p, 1.0 - p])
        df["k"] = df["k"].astype(int)
    if "successes" in df.columns:
        df.rename(columns={"successes": "violated_mean"}, inplace=True)
        print(df.head())
        for i, row in df.iterrows():
            total = 1000
            val = df.at[i, "violated_mean"]
            df.at[i, "violated_mean"] = val / total
        print(df.head())

    out_path = csv_file.with_name(f"{csv_name}2.csv")
    df.to_csv(f"{out_path}", index=False)

def mini_ccp(P: Tuple[float, float], N=2, eps=0.1):
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


# Have to convert to pandas reading if we wanna specify specific columns and rows in write mode.
def write_latex_tabulars(experi_csv: str, tablename: str):
    csv_file = ROOT / "reports" / f"{experi_csv}.csv"
    df = pd.read_csv(csv_file)

    df.rename(columns={"k_combined": "k", "violated_mean": "violated"}, inplace=True)
    df = df.fillna(0)
    df["k"] = df["k"].astype(int)

    #df_to_write = df[["k", "P", "violated"]]
    df_to_write = df.set_index("test")[["k", "P", "violated"]].copy()
    df_to_write["P"] = df_to_write["P"].round(3)
    df_to_write["violated"] = df_to_write["violated"].round(3)

    # df.rename(columns={"k_max": "k", "violated_mean": "violated"}, inplace=True)
    # df_to_write = df[["k", "P", "violated"]].round(3)  # df.loc[:,['k', 'violated']].round(3)

    # df_to_write = df_to_write.T
    df_to_write.columns = df_to_write.columns.astype(str)

    out_file = ROOT / "plots" / f"{tablename}.tex"
    with out_file.open("w") as f:
        # Column alignment: left for metric + right for each test
        col_align = "l" + "r" * len(df_to_write.columns)
        f.write(r"\begin{tabular}{" + col_align + "}\n")
        f.write("& " + " & ".join(df_to_write.columns) + r"\\\hline" + "\n")
        for metric, row in df_to_write.iterrows():
            f.write(f"{metric} & " + " & ".join(map(str, row.values)) + r"\\ " + "\n")
        f.write(r"\end{tabular}" + "\n")


if __name__ == '__main__':
    # write_latex_tabulars(combined, "plots/combined_table2.tex")
    # format_csv(ROOT / "reports"/ "Baseline")
    #format_csv("DeadlockTesting")
    #format_csv("MinimizationTesting")
    #format_csv("Baseline")
    #test = get_mean_median("DeadlockTesting2")
    #mini = get_mean_median("MinimizationTesting2")
    #combine_and_convert_csv("MinimizationTesting2_mean", "DeadlockTesting2_mean", "MiniDeadTesting")
    #combine_and_convert_csv("MiniDeadTesting", "Baseline2", "Baseline_experiments")
    write_latex_tabulars("Baseline_experiments", "Baseline_experiments")
