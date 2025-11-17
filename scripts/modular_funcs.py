import pandas as pd
from pathlib import Path
from dynamic_run_jpf import ROOT

def combine_and_convert_csv(csv1: str, csv2: str, combinedname: str):
    csv1_path = ROOT / "reports" / f"{csv1}.csv"
    csv2_path = ROOT / "reports" / f"{csv2}.csv"

    output_path = ROOT / "reports" / f"{combinedname}.csv"
    output_path.parent.mkdir(parents=True, exist_ok=True)
    csv1 = pd.read_csv(csv1_path)
    csv2 = pd.read_csv(csv2_path)
    csv1_df = pd.DataFrame(csv1)
    csv2_df = pd.DataFrame(csv2)
    combined_csv = pd.concat([csv1_df, csv2_df], ignore_index=True)
    return combined_csv.to_csv(output_path, index=False, float_format="%.0f")


if __name__ == '__main__':
    #combine_and_convert_csv("MinimizationTest", "DeadlockExample", "test123")
    #combine_and_convert_csv("MinimizationTestRand", "DeadlockExampleRand", "test123rand")
    #combine_and_convert_csv("test123", "test123rand", "combinedUniRand")
    combine_and_convert_csv("MinimizationTesting", "DeadlockTesting", "base_tests_jvm")
    combine_and_convert_csv('combinedUniRand', 'base_tests_jvm', "base_total")