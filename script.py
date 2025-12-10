import paramiko
import getpass
import time
import os
from scp import SCPClient

password = getpass.getpass('Enter your password: ')

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('hpc.itu.dk', username="frek", password=password)

# Upload the .job file
local_job_file = "SCT_bench_scrip1.sh"
remote_job_file = 'SCT_bench_scrip1.sh'
with SCPClient(ssh.get_transport()) as scp:
    scp.put(local_job_file, remote_job_file)

# Submit the job
stdin, stdout, stderr = ssh.exec_command(f'sbatch {remote_job_file}')
job_id = int(stdout.read().decode().split()[-1])
print(f'Submitted job with ID {job_id}')

job_finished = False
while not job_finished:
    time.sleep(10)
    stdin, stdout, stderr = ssh.exec_command(f'sacct -j {job_id} --format=State --noheader')
    output = stdout.read().decode().strip().split('\n')
    for line in output:
        if 'COMPLETED' in line:
            job_finished = True
            print(f'Job {job_id} has completed')
            break
        elif 'RUNNING' in line:
            print(f'Job {job_id} is still running...')
            break

job_output_filename = f'job.{job_id}.out'

current_directory = os.getcwd()
local_output_filename = os.path.join(current_directory, job_output_filename)

with SCPClient(ssh.get_transport()) as scp:
    scp.get(job_output_filename, local_output_filename)

print(f'Downloaded .out file to {local_output_filename}')

with open(local_output_filename) as file:
    for line in file:
        print(line)

ssh.close()
