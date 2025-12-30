import csv
import statistics
from typing import List, Dict, Tuple
# from click import File
import numpy as np
import pandas as pd
from pathlib import Path
# from utilities import combine_all_csvs, separate_combine
# from path_setup import ROOT

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

def compute_mean_median(combined_df: pd.DataFrame):
    final_df = (
        combined_df.groupby("test")
        .agg(
            k_max=("k", "max"),
            violated_mean=("violated", "mean"),
            violated_median=("violated", "median"),
        )
        .reset_index()
    )
    return final_df


def check_format(df: pd.DataFrame):
    # csv_file = ROOT/ "reports" / f"{csv_name}.csv"
    # df = pd.read_csv(f"{csv_file}")

    if "problem" in df.columns:
        df.rename(columns={"problem": "test"}, inplace=True)

    if "k_max" in df.columns:
        df.rename(columns={"k_max": "k", "violated_mean": "violated"}, inplace=True)

    if 'k' not in df.columns:
        if "P" not in df.columns:
            raise ValueError(f"'P' column not found in {df}")
        df["k"] = df["P"].apply(lambda p: mini_ccp([p, 1.0 - p])).astype(int)
    
    if "successes" in df.columns:
        df = df.rename(columns={"successes": "violated_mean"})
        total = 1000  # adjust if needed
        df["violated_mean"] = df["violated_mean"] / total
        print(df.head())
    if "P" in df.columns:
        df = df.sort_values(by=["test", "P"], ascending=[True, True]).reset_index(
            drop=True)
    return df

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

# def iter_csvs(experiment_name=None , folder_name=None):
#     """
#     Docstring for iter_csvs:
#     experiment_name is a str,
#     folder_name is a str
#     """
#     combined_df = None
#     if folder_name == "experiments":    
#         paths = [p for p in (ROOT / "reports" / "experiments").rglob("*.csv")
#                 if not p.name.endswith("_mean.csv") and "combined" not in p.name]
#         combined_df = combine_all_csvs(paths)
#     else:
#         csv_file = ROOT / "reports" / f"{experiment_name}.csv"
#         not_experi_df = pd.read_csv(csv_file)
#         not_experi_df = compute_mean_median(not_experi_df)
#         not_experi_df = check_format(not_experi_df)
#         final_path = ROOT / "reports" / f"{experiment_name}.csv"
#         not_experi_df.to_csv(final_path, index=False)
#         return
#     final_df = compute_mean_median(combined_df)
#     final_df = check_format(final_df)
#     final_path = ROOT / "reports" / f"{experiment_name}.csv"
#     final_df.to_csv(final_path, index=False)


# Have to convert to pandas reading if we wanna specify specific columns and rows in write mode.
def write_latex_tabulars(experi_csv: str, tablename: str):
    csv_file =  f"reports/{experi_csv}.csv"
    df = pd.read_csv(csv_file)
    df.columns = df.columns.str.strip()

    df.rename(columns={"k_combined": "k", "violated_mean": "violated"}, inplace=True)
    df = df.fillna(0)
    df["k"] = df["k"].astype(int)

    total = 1000
    # utilizing masking to check str
    mask = ~df["test"].str.contains("Test", case=False, na=False)
    df.loc[mask, "violated"] = df.loc[mask, "violated"] / total
    df_to_write = df.set_index("test")[["k", "P", "violated"]].copy()

    df_to_write["P"] = df_to_write["P"].round(3)
    df_to_write["violated"] = df_to_write["violated"].apply(
        #lambda x: f"{x:.3f}" if x >= 0.001 else f"{x:.1e}")
    lambda x: "0" if x == 0 else (str(int(x)) if x >= 1 else f"{x:.3f}" if x >= 0.001 else f"{x:.1e}")
        )

    df_to_write.columns = df_to_write.columns.astype(str)


def write_to_csv_and_latex(experi_csv: str, tablename: str):
    data = {}

    # Read input CSV
    with open(f"reports/{experi_csv}.csv", newline='') as csvfile:
        reader = csv.DictReader(csvfile)
        for row in reader:
            test = row["test"]
            time_val = row["time"].strip()

            if test not in data:
                data[test] = {
                    "numeric_times": [],
                    "count_time_<30": 0
                }

            if time_val == ">30":
                data[test]["count_time_<30"] += 1
            else:
                try:
                    t = float(time_val)
                    data[test]["numeric_times"].append(t)
                except ValueError:
                    pass  # skip malformed entries

    # Build a DataFrame
    df_to_write = pd.DataFrame(columns=[
        "avg_time",
        "median_time",
        "min_time",
        "max_time",
        "std_time",
        "count_time_<30"
    ])

    for test, values in data.items():
        times = values["numeric_times"]

        if times:
            avg_time = sum(times) / len(times)
            median_time = statistics.median(times)
            min_time = min(times)
            max_time = max(times)
            std_time = statistics.stdev(times) if len(times) > 1 else 0.0
        else:
            avg_time = median_time = min_time = max_time = std_time = ""

        df_to_write.loc[test] = [
            avg_time,
            median_time,
            min_time,
            max_time,
            std_time,
            values["count_time_<30"]
        ]

    # Write summary CSV
    df_to_write.to_csv(f"reports/{tablename}.csv", index_label="test")

    # Write LaTeX table
    out_file = f"figures/{tablename}.tex"
    with open(out_file, "w") as f:
        # Column alignment: left for test + right for each metric
        col_align = "l" + "r" * len(df_to_write.columns)
        f.write(r"\begin{tabular}{" + col_align + "}\n")
        f.write("& " + " & ".join(df_to_write.columns) + r"\\\hline" + "\n")
        for test, row in df_to_write.iterrows():
            f.write(f"{test} & " + " & ".join(map(str, row.values)) + r"\\ " + "\n")
        f.write(r"\end{tabular}" + "\n")

    return df_to_write


if __name__ == '__main__':

    write_latex_tabulars("SctBench_res", "Baseline_experiments")
    # iter_csvs("jvm_experiments", "experiments")
    # iter_csvs("baseline_jvm")
    # write_latex_tabulars(separate_combine("jvm_experiments", "SCT_bench_results", "mainline_experiments"), "mainline_experiments")
    write_latex_tabulars("baseline_res","baseline_res")
    write_to_csv_and_latex("SctBench_time", "SctBench_time_overview")
    write_latex_tabulars("HashMap_res_test","HashMap_res_test")
    # df = pd.read_csv("reports/SctBench_time")
    # avg_sd = df["sd_time"].mean()
    # print(avg_sd)
