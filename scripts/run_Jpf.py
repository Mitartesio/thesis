#!/usr/bin/env python3

import os, pathlib, sys, subprocess

JPF_HOME = os.environ.get("JPF_HOME")

if not JPF_HOME:
    sys.exit("[error] Please set JPF_HOME environment variable")

JPF_CMD = str(pathlib.Path(JPF_HOME) / "bin" / "jpf")
JPF_CP = f"{JPF_HOME}/build/jpf.jar:{JPF_HOME}/build/jpf-classes.jar:."

# COmpile Listener automatisk - Change this so it doesnt compile each run

listener_src = pathlib.Path("Listeners/Random_Scheduler.java")
if listener_src.exists():
    print(f"[info] compiling listener: {listener_src}")
    subprocess.run(
        ["javac", "-cp", JPF_CP, "-d", ".", str(listener_src)],
        check=True
    )



print("Running JPF from:", JPF_CMD)


# Pass argument ind, have SimpleTest2.jpf som standard lige nu
if len(sys.argv) > 1:
    config = pathlib.Path(sys.argv[1])
else:
    config = pathlib.Path("SimpleTest2.jpf")   # default fallback

if not config.exists():
    sys.exit(f"[error] JPF config file not found: {config}")



# --- Build the command ---
cmd = [JPF_CMD, str(config)]

print("[info] running:", " ".join(cmd))

# --- Run the command and stream output ---
proc = subprocess.Popen(
    cmd,
    stdout=subprocess.PIPE,
    stderr=subprocess.STDOUT,
    text=True,
)

for line in proc.stdout:
    sys.stdout.write(line)

rc = proc.wait()
print(f"[info] jpf exited with code {rc}")
sys.exit(rc)