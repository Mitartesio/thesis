import csv
from typing import List, Dict
import numpy as np

def read_results(filename: str): # make it ino dictionaries? -> Dict[str, Dict[int, int]] where last int is int version of boolean
    results: Dict[str, Dict[int, int]] = dict()
    with open(filename, 'r') as f:
        reader=csv.DictReader(f) #reader(f) if not dictionary
        for row in reader:
            problem: str = row.get('problem', 'default_problem')
            k = 'k' if 'k' in row else 'run'
            runs: int = int(row[k])
            violated: int = int((row['violated'])) # type: ignore
            if problem not in results:
                results[problem] = dict()
            if runs not in results[problem]:
                results[problem][runs] = list()
            results[problem][runs].append(violated)
    return results

# F's in the chat for np.ndarray approach
    # def dict_to_array(problemname: str,resultsdict: dict): # np.ndarray
    rows = []
    for k, violations in resultsdict.items():
        for v in violations:
            rows.append((problemname, k, v))

    dtype = [('prolbem', 'U20'), ('k', 'i4'), ('violated', 'i4')]
    return np.array(rows, dtype=dtype) # add dtype if necessary

# Have to convert to pandas reading if we wanna specify specific columns and rows in write mode.
def write_latex_tabulars(results: dict, filename: str, problem: str = "default_problem"):
    if problem not in results:
        raise ValueError(
            f"Assertion violation for'{problem}' not found. Available: {list(results.keys())}")

    problem_data = results[problem]

    with open(filename, "w") as f:
        f.write(r"\begin{tabular}{rr}" + "\n")
        f.write(r"$k$ & violation\\\hline" + "\n")
        for k in sorted(problem_data.keys()):
            for v in problem_data[k]:
                f.write(f"{k} & {v}\\\\\n")
        f.write(r"\end{tabular}" + "\n")


if __name__ == '__main__':
    raw_res = read_results('reports/min_uniform.csv')
    write_latex_tabulars(raw_res, 'plots/test_table.tex') #utilize raw_res[<scheduler>]
