import pandas as pd
import os, pathlib, sys, subprocess
from pathlib import Path
from dynamic_run_jpf import ROOT
from pathlib import Path

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


def populate_csv(csv_name: str, answers: List[int]):
    if csv_name is None:
        csv_name = "results"

    out_file = ROOT / "reports" / f"{csv_name}.csv"
    out_file.parent.mkdir(exist_ok=True)

    if not out_file.exists():
        with out_file.open("w", newline="") as f:
            writer = csv.writer(f)
            writer.writerow(["problem", "k", "violated"])  # <-- header
            problem = csv_name
            k, viol = answers[0]
            writer.writerow([problem, k, viol])
    else:
        with out_file.open("a", newline="") as f:
            writer = csv.writer(f)
            problem = csv_name
            k, viol = answers[0]
            writer.writerow([problem, k, viol])

    print(f" answers -> {out_file.stem}.csv")


def resolve_config(arg: str | None) -> pathlib.Path:
    if arg:
        p = pathlib.Path(arg)
        if not p.is_absolute():
            p = ROOT / p
        return p
    # Default config
    return CONFIGS_DIR / "SimpleTest2.jpf"


def setup():
    """Check whether script is run with correct version of java (only checks if its java 11)"""
    result = subprocess.run(["java", "-version"], stderr=subprocess.PIPE, text=True)

    output = result.stderr
    # print(output)
    if "version" in output:
        versionLine = output.splitlines()[0]
        java_version = versionLine.split('"')[1]
        if java_version.split(".")[0] == "11":
            print("Correctly using java 11.xx.xx")
        else:
            print("WARNING: Using wrong version of java. please use java 11")
            sys.exit(1)

    """Here we're building the jars needed for jpf _ NOT CONFIRMED WORKING YET"""

    if JPF_JAR.exists():
        print("JPF JARs already exist, skipping JAR generation")
    else:
        try:
            print("generating JPF jars...")
            subprocess.run(
                ["./jpf-core/gradlew", "-p", "./jpf-core", "buildJars"],
                check=True,
                cwd=ROOT,
            )
            print("Finished JPF JAR building")
        except subprocess.CalledProcessError as e:
            sys.exit(f"[error] JAR generation failed: {e}")

    """ here we compile with gradle, which should ensure we've compiled with java 11, as its a demand in the gradle build"""

    try:
        print("Compiling with Gradle...")
        subprocess.run(
            ["./CupTest/gradlew", "-p", "./CupTest", "build", "-x", "test"],
            check=True,
            cwd=ROOT,
        )
        print("Finished Gradle compilation")
    except subprocess.CalledProcessError as e:
        sys.exit(f"[error] Gradle build failed: {e}")


def run_gradle_tests(gradletestfile: str):  # making it more modular
    log_file = ROOT / "reports" / f"{gradletestfile}.log"
    log_file.parent.mkdir(parents=True, exist_ok=True)

    gradle_cmd = ["./gradlew", "test", "--tests", f"sut.{gradletestfile}"]

    print("Running Gradle tests...")
    with open(log_file, "w") as f:
        result = subprocess.run(
            gradle_cmd,
            cwd=str(CUPTEST),
            stdout=f,
            stderr=subprocess.STDOUT,
            text=True,
            # check=True makes it so it doesn't create the csv if the build fails
        )

    print(f"Gradle test finished with return code {result.returncode}")
    print(f"Gradle test log saved to {log_file}")
    return log_file


def parse_console_log(
    log_file: Path, output_csv: Path
):  # need to make it so it takes str name instead
    output_csv.parent.mkdir(parents=True, exist_ok=True)

    rows = []
    current_rep = None
    current_result = None
    repetition_count = 0
    problem_name = log_file.stem
    is_repetition_test = False

    with open(log_file, "r") as f:
        for line in f:
            line = line.strip()
            #   "MinimizationTesting > repetition 123 of 10000"
            if "repetition" in line and "of" in line:
                try:
                    parts = line.split()
                    rep_index = parts.index("repetition") + 1
                    current_rep = int(parts[rep_index])
                    # rep_number = int(parts[3]) # x of n
                except Exception:
                    current_rep = None

            # Repeated tests
            elif line.startswith("RESULT"):
                current_result = (
                    0 if line.split(":", 1)[1].strip().lower() == "true" else 1
                )
                # current_result = line.split(":", 1)[1].strip()

            # for singular test run
            if "repetition" not in line:
                if line.endswith("PASSED"):
                    repetition_count += 1
                    rows.append(
                        [problem_name, repetition_count, 0]
                    )  # didnt find for instance deadlock.

                elif line.endswith("FAILED"):
                    repetition_count += 1
                    rows.append([problem_name, repetition_count, 1])

            if current_rep is not None and current_result is not None:
                rows.append([problem_name, current_rep, current_result])
                current_rep = None
                current_result = None

    with open(output_csv, "w", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["problem", "k", "violated"])
        writer.writerows(rows)

    print(f"Parsing done. output -> {output_csv}")


if __name__ == '__main__':
    #combine_and_convert_csv("MinimizationTest", "DeadlockExample", "test123")
    #combine_and_convert_csv("MinimizationTestRand", "DeadlockExampleRand", "test123rand")
    #combine_and_convert_csv("test123", "test123rand", "combinedUniRand")
    combine_and_convert_csv("MinimizationTesting", "DeadlockTesting", "base_tests_jvm")
    combine_and_convert_csv('combinedUniRand', 'base_tests_jvm', "base_total")
