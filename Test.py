import paramiko
import getpass
import time
import os
from scp import SCPClient

password = getpass.getpass('Enter your password: ')

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('hpc.itu.dk', username="anmv", password=password)

with SCPClient(ssh.get_transport()) as scp:
    
    # job_id = 41279

    # job_id = 41532 #Important

    job_id = 42181
    
    stdin, stdout, stderr = ssh.exec_command(f'sacct -j {job_id} --format=State --noheader')
    output = stdout.read().decode().strip().split('\n')
    print(output)

    stdin, stdout, stderr = ssh.exec_command('squeue -u anmv')
    print(stdout.read().decode())

    stdin, stdout, stderr = ssh.exec_command('sinfo -s')
    output = stdout.read().decode()
    error = stderr.read().decode()

    if error:
        print(f"Error: {error}")
    else:
        print("Partition summary:\n")
        print(output)

    stdin, stdout, stderr = ssh.exec_command(f'scancel {job_id}')

    # Optional: read the output or errors
    err = stderr.read().decode()
    if err:
        print(f'Error cancelling job: {err}')
    else:
        print(f'Job {job_id} has been cancelled.')