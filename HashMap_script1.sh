#!/bin/bash
#SBATCH --job-name=BaseLine
#SBATCH --output=job.%j.out
#SBATCH --nodes=5
#SBATCH --time=15:30:00
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
    export GRADLE_USER_HOME="/tmp/gradle_${SLURMD_NODENAME}_${SLURM_STEP_ID}"
    mkdir -p "$GRADLE_USER_HOME"
    python3 /home/anmv/thesis_code/thesis_code_print/Simple_Example_Thesis/scripts/new_version.py CupTest WrongStripedMap1Bad5 
' &

srun --nodes=1 --ntasks=1 --nodelist=${nodes[1]} bash -c '
    export GRADLE_USER_HOME="/tmp/gradle_${SLURMD_NODENAME}_${SLURM_STEP_ID}"
    mkdir -p "$GRADLE_USER_HOME"
    python3 /home/anmv/thesis_code/thesis_code_print/Simple_Example_Thesis/scripts/new_version.py SctBench WrongStripedMap2Bad5 
' &
srun --nodes=1 --ntasks=1 --nodelist=${nodes[2]} bash -c '
    export GRADLE_USER_HOME="/tmp/gradle_${SLURMD_NODENAME}_${SLURM_STEP_ID}"
    mkdir -p "$GRADLE_USER_HOME"
    python3 /home/anmv/thesis_code/thesis_code_print/Simple_Example_Thesis/scripts/new_version.py SctBench WrongStripedMap3Bad5 
' &
srun --nodes=1 --ntasks=1 --nodelist=${nodes[3]} bash -c '
    export GRADLE_USER_HOME="/tmp/gradle_${SLURMD_NODENAME}_${SLURM_STEP_ID}"
    mkdir -p "$GRADLE_USER_HOME"
    python3 /home/anmv/thesis_code/thesis_code_print/Simple_Example_Thesis/scripts/new_version.py SctBench WrongStripedMap4Bad5 
' &
srun --nodes=1 --ntasks=1 --nodelist=${nodes[4]} bash -c '
    export GRADLE_USER_HOME="/tmp/gradle_${SLURMD_NODENAME}_${SLURM_STEP_ID}"
    mkdir -p "$GRADLE_USER_HOME"
    python3 /home/anmv/thesis_code/thesis_code_print/Simple_Example_Thesis/scripts/new_version.py SctBench WrongStripedMap5Bad5 
' &

wait

#Twostage100Bad100
#TwostageBad2
#WronglockBad8
#Wronglock3Bad2
#Wronglock1Bad4
#Phase01Bad2