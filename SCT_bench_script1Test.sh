#!/bin/bash
#SBATCH --job-name=BaseLine
#SBATCH --output=job.%j.out
#SBATCH --partition=scavenge
#SBATCH --nodes=6
#SBATCH --time=06:30:00
#SBATCH --export=ALL


### -------------------------------
### PREPARE JAVA + TEMP DIRECTORIES
### -------------------------------
mkdir -p /home/anmv/SOFT/java
mkdir -p /home/anmv/tmp
export TMPDIR=/home/anmv/tmp

cd /home/anmv/SOFT/java

# Download Java if not already present
if [ ! -d "jdk-11.0.20+8" ]; then
    wget -q https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.20+8/OpenJDK11U-jdk_x64_linux_hotspot_11.0.20_8.tar.gz
    tar -xzf OpenJDK11U-jdk_x64_linux_hotspot_11.0.20_8.tar.gz
fi

export JAVA_HOME=/home/anmv/SOFT/java/jdk-11.0.20+8
export PATH=$JAVA_HOME/bin:$PATH
java -version

### -------------------------------
### LIST OF EXPERIMENTS
### -------------------------------
EXPERIMENTS=(
    "AccountBad3"
    "Carter01Bad4"
    "FsbenchBad27"
    "Phase01Bad2"
    "StackBad2"
    "TokenRingBad4"
)

### -------------------------------
### GET NODE LIST
### -------------------------------
nodes=($(scontrol show hostnames $SLURM_JOB_NODELIST))

for i in "${!EXPERIMENTS[@]}"; do
    EXP=${EXPERIMENTS[$i]}
    NODE=${nodes[$i]}

    srun --exclusive -N1 --nodelist=$NODE bash -c "
    
    module purge
    module load Python/3.11.3-GCCcore-12.3.0

    # Set Java environment
    export JAVA_HOME=/home/anmv/SOFT/java/jdk-11.0.20+8
    export PATH=\$JAVA_HOME/bin:\$PATH

    export GRADLE_USER_HOME=\"\$TMP_BUILD_DIR/gradle_cache\"
    mkdir -p \"\$GRADLE_USER_HOME\"

    echo \"Running experiment: $EXP on node \$(hostname)\"
    python scripts/new_version.py SctBench $EXP
    " &
done

wait
echo "All experiments completed."
