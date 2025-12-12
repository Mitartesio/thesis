#!/bin/bash
#SBATCH --job-name=BaseLine
#SBATCH --output=job.%j.out
#SBATCH --nodes=4
#SBATCH --time=14:00:00
#SBATCH --partition=scavenge
#SBATCH --export=ALL

mkdir -p /home/anmv/SOFT/java
mkdir -p /home/anmv/tmp
export TMPDIR=/home/anmv/tmp

# export GRADLE_USER_HOME="/home/anmv/tmp/gradle_$SLURM_NODEID"
# mkdir -p "$GRADLE_USER_HOME"

cd /home/anmv/SOFT/java

wget https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.20+8/OpenJDK11U-jdk_x64_linux_hotspot_11.0.20_8.tar.gz

tar -xzf OpenJDK11U-jdk_x64_linux_hotspot_11.0.20_8.tar.gz

export JAVA_HOME=/home/anmv/SOFT/java/jdk-11.0.20+8
export PATH=$JAVA_HOME/bin:$PATH


module purge
module load Python/3.11.3-GCCcore-12.3.0

nodes=($(scontrol show hostnames $SLURM_JOB_NODELIST))


srun --nodes=1 --ntasks=1 --nodelist=${nodes[0]} bash -c '
    # Set a unique Gradle home per node/step
    export GRADLE_USER_HOME="/home/anmv/tmp/gradle_${SLURMD_NODENAME}_${SLURM_STEP_ID}"
    mkdir -p "$GRADLE_USER_HOME"

    # Copy the whole project to a node-local folder
    PROJECT_COPY="/home/anmv/tmp/SctBench_${SLURMD_NODENAME}_${SLURM_STEP_ID}"
    rm -rf "/home/anmv/tmp/SctBench_${SLURMD_NODENAME}_${SLURM_STEP_ID}"
    cp -r /home/anmv/thesis_code/thesis_code_print/Simple_Example_Thesis "$PROJECT_COPY"

    # Move to the project copy and run Python
    cd "$PROJECT_COPY"
    python3 scripts/new_version.py SctBench StackBad2
' &

wait

#Twostage100Bad100
#TwostageBad2
#WronglockBad8
#Wronglock3Bad2
#Wronglock1Bad4
# Phase01Bad2


