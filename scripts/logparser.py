import os
import csv
import xml.etree.ElementTree as ET
from pathlib import Path
from dynamic_run_jpf import ROOT, CUPTEST

# Correct paths based on your structure
REPORT_DIR = CUPTEST / "app/build/test-results/test"
OUTPUT_DIR = ROOT / "reports/console-output-MinimizationTesting.csv"


def extract_from_xml(xml_file):
    """Extract console logs from each testcase in a JUnit XML file."""
    tree = ET.parse(xml_file)
    root = tree.getroot()
    data = []

    for case in root.findall(".//testcase"):
        classname = case.attrib.get("classname", "")
        name = case.attrib.get("name", "")
        time = case.attrib.get("time", "")

        # System out and err (empty if not printed)
        system_out = case.findtext("system-out", "").strip()
        system_err = case.findtext("system-err", "").strip()

        data.append([classname, name, time, system_out, system_err])

    return data


def main():
    rows = []

    print(f"Scanning XML reports in: {REPORT_DIR}")

    if not REPORT_DIR.exists():
        print(f"❌ ERROR: Directory does not exist: {REPORT_DIR}")
        return

    # Read every XML report file
    for xml_file in REPORT_DIR.glob("*.xml"):
        rows.extend(extract_from_xml(xml_file))

    # Ensure parent folder exists
    OUTPUT_DIR.parent.mkdir(parents=True, exist_ok=True)

    # Write CSV
    with open(OUTPUT_DIR, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(["Class", "Test Method", "Time", "System Out", "System Err"])
        writer.writerows(rows)

    print(f"\n Extracted console output written to:\n   {OUTPUT_DIR}\n")


if __name__ == "__main__":
    main()
