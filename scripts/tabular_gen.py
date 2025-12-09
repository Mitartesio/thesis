import csv
from typing import List, Dict
import numpy as np
import pandas as pd

def read_results(filename: str):
    results: Dict[str, Dict[int, int]] = {}

    with open(filename, 'r', newline='') as f:
        reader = csv.DictReader(f)
        for row in reader:
            problem = row['problem']
            k = row['k']
            violated = row['violated']

            if problem not in results:
                results[problem] = {}
            results[problem][k] = violated
    return results


def get_mean_median(csv_file: str):
    df = pd.read_csv(csv_file)

    df['k'] = df['k'].astype(int)
    df['violated'] = df['violated'].astype(int)

    result = {}

    # grp by problem
    for prolbem, group in df.groupby('problem'):
        result[prolbem] = {
            "k_mean": group["k"].mean(),
            "k_median": group["k"].median(),
            "violated_mean": group["violated"].mean(),
            "violated_median": group["violated"].median(),
        }
    return result


# Have to convert to pandas reading if we wanna specify specific columns and rows in write mode.
def write_latex_tabulars(results: dict, tablename: str):

    df = pd.DataFrame(results)
    df.rename(index={"k_mean": "k", "violated_mean": "violated"}, inplace=True)
    df_to_write = df.loc[['k', 'violated']].round(2)

    df_to_write = df_to_write.T

    with open(tablename, "w") as f:
    # Column alignment: left for metric + right for each problem
        col_align = "l" + "r" * len(df_to_write.columns)
        f.write(r"\begin{tabular}{" + col_align + "}\n")
        f.write("& " + " & ".join(df_to_write.columns) + r"\\\hline" + "\n")
        for metric, row in df_to_write.iterrows():
            f.write(f"{metric} & " + " & ".join(map(str, row.values)) + r"\\ " + "\n")
        f.write(r"\end{tabular}" + "\n")


if __name__ == '__main__':
    # raw_res = read_results('reports/test123.csv')
    # med_mea = get_mean_median('reports/test123.csv')
    med_mea2 = get_mean_median("reports/combinedUniRand.csv")
    combined = get_mean_median('reports/base_total.csv')
    #write_latex_tabulars(med_mea, 'plots/test_table.tex')
    write_latex_tabulars(combined, "plots/combined_table2.tex")
