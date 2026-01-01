import csv
import pathlib
import statistics
from typing import List, Dict, Tuple
import numpy as np
import pandas as pd

#Methods provided for making latex and csv files for time- and correctness tests

SCRIPT_DIR = pathlib.Path(__file__).resolve().parent
PROJECT_ROOT = SCRIPT_DIR.parent
REPORTS_DIR = PROJECT_ROOT / "reports"
FIGURES_DIR = PROJECT_ROOT / "figures"
REPORTS_DIR.mkdir(parents=True, exist_ok=True)
FIGURES_DIR.mkdir(parents=True, exist_ok=True)


def write_latex_tabulars(experi_csv: str, tablename: str):
    '''
    Method with the purpose of converting correctness tests into latex tabulars
    '''
    csv_file = REPORTS_DIR / f"{experi_csv}.csv"
    df = pd.read_csv(csv_file)
    df.columns = df.columns.str.strip()

    df.rename(columns={"k_combined": "k", "violated_mean": "violated"}, inplace=True)
    df = df.fillna(0)
    df["k"] = df["k"].astype(int)

    total = 1000

    mask = ~df["test"].str.contains("Test", case=False, na=False)
    df.loc[mask, "violated"] = df.loc[mask, "violated"] / total

    df_to_write = df.set_index("test")[["k", "P", "violated"]].copy()

    df_to_write["P"] = df_to_write["P"].round(3)
    df_to_write["violated"] = df_to_write["violated"].apply(
        lambda x: "0"
        if x == 0
        else (str(int(x)) if x >= 1 else f"{x:.3f}" if x >= 0.001 else f"{x:.1e}")
    )

    FIGURES_DIR.mkdir(parents=True, exist_ok=True)

    out_file = FIGURES_DIR / f"{tablename}.tex"
    with open(out_file, "w") as f:
        col_align = "l" + "r" * len(df_to_write.columns)
        f.write(r"\begin{tabular}{" + col_align + "}\n")
        f.write("test & " + " & ".join(df_to_write.columns) + r"\\\hline" + "\n")

        for test, row in df_to_write.iterrows():
            f.write(f"{test} & " + " & ".join(map(str, row.values)) + r"\\ " + "\n")

        f.write(r"\end{tabular}" + "\n")


def write_to_csv_and_latex(experi_csv: str, tablename: str):
    '''
    Method with the purpose of converting time tests into latex tabulars
    '''

    data = {}

   
    with open(REPORTS_DIR / f"{experi_csv}.csv", "r", newline='') as csvfile:
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
                t = float(time_val)
                data[test]["numeric_times"].append(t)
                

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

    df_to_write.to_csv(FIGURES_DIR / f"{tablename}.csv", index_label="test")

    out_file = f"{tablename}.tex"
    with open(FIGURES_DIR / out_file, "w") as f:
        col_align = "l" + "r" * len(df_to_write.columns)
        f.write(r"\begin{tabular}{" + col_align + "}\n")
        f.write("& " + " & ".join(df_to_write.columns) + r"\\\hline" + "\n")
        for test, row in df_to_write.iterrows():
            f.write(f"{test} & " + " & ".join(map(str, row.values)) + r"\\ " + "\n")
        f.write(r"\end{tabular}" + "\n")

    return df_to_write


if __name__ == '__main__':
    write_latex_tabulars("SctBench_res", "SctBench_res")
    write_latex_tabulars("baseline_res","baseline_res")
    write_to_csv_and_latex("SctBench_time_res", "SctBench_time_tab")
    write_latex_tabulars("HashMap_res","HashMap_res")

    #Delete???
    # iter_csvs("jvm_experiments", "experiments")
    # iter_csvs("baseline_jvm")
    # write_latex_tabulars(separate_combine("jvm_experiments", "SCT_bench_results", "mainline_experiments"), "mainline_experiments")

    #Delete???
    # df = pd.read_csv("reports/SctBench_time")
    # avg_sd = df["sd_time"].mean()
    # print(avg_sd)
