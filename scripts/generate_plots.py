#!/usr/bin/env python3
"""
Read reports/answers.csv (columns: run,answer) and produce simple plots:
- reports/answers_hist.png      (histogram of answers)
- reports/answers_counts.png    (bar chart of value -> count)

Usage:
  python3 scripts/plot_answers.py                # uses reports/answers.csv by default
  python3 scripts/plot_answers.py --csv path/to/answers.csv --outdir reports
"""

import csv
import argparse
import pathlib
from collections import Counter

import matplotlib.pyplot as plt  # pip install matplotlib

ROOT = pathlib.Path(__file__).resolve().parents[1]

def load_answers(csv_path: pathlib.Path):
    vals = []
    with csv_path.open(newline="") as f:
        r = csv.DictReader(f)
        for row in r:
            a = row.get("answer")
            if a is None or a == "" or a.lower() == "none":
                continue
            try:
                vals.append(int(a))
            except ValueError:
                pass
    return vals

def plot_hist(values, out_path: pathlib.Path, bins=None, title="Answer distribution (histogram)"):
    if not values:
        print("[warn] no values to plot for histogram")
        return
    plt.figure()
    plt.hist(values, bins=bins if bins else range(min(values), max(values)+2), edgecolor=None)
    plt.title(title)
    plt.xlabel("answer")
    plt.ylabel("frequency")
    plt.tight_layout()
    plt.savefig(out_path)
    plt.close()
    print(f"[ok] wrote {out_path}")

def plot_counts(values, out_path: pathlib.Path, title="Answer distribution (counts)"):
    if not values:
        print("[warn] no values to plot for counts")
        return
    counts = Counter(values)
    xs = sorted(counts.keys())
    ys = [counts[x] for x in xs]
    plt.figure()
    plt.bar(xs, ys)
    plt.title(title)
    plt.xlabel("answer")
    plt.ylabel("count")
    plt.tight_layout()
    plt.savefig(out_path)
    plt.close()
    print(f"[ok] wrote {out_path}")

def main():
    ap = argparse.ArgumentParser()

    ap.add_argument("--csv", default=str(ROOT / "reports" / "answers.csv"),
                    help="Input CSV with columns: run,answer (default: reports/answers.csv)")
    ap.add_argument("--outdir", default=str(ROOT / "plots"),
                    help="Directory to write plots (default: plots)")
    args = ap.parse_args()

    csv_path = pathlib.Path(args.csv).resolve()
    outdir = pathlib.Path(args.outdir); outdir.mkdir(parents=True, exist_ok=True)

    if not csv_path.exists():
        raise SystemExit(f"[error] CSV not found: {csv_path}")

    values = load_answers(csv_path)

    # Save two plots
    plot_hist(values, outdir / "answers_hist.png")
    plot_counts(values, outdir / "answers_counts.png")

if __name__ == "__main__":
    main()