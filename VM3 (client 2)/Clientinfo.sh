#!/bin/bash

collect_process_info() {
	# Finding the process tree of all currently running processes:
	ps_tree=$(pstree)

	# Finding the list of all zombie processes:
	zombie_list=$(ps aux | awk '$8=="Z"')
	# Sometimes, there is no zombie processes at all:
	if [ -z "$zombie_list" ]; then
		zombie_list="No zombie processes found."
	fi

	# Finding processes CPU usage:
	cpu_usage_list=$(ps -eo pid,%cpu,comm)

	# Finding processes memory usage:
	memory_usage_list=$(ps -eo pid,%mem,comm)

	# Finding the top cpu-consuming processes and piping the result to the (head) command:
	# 6 is used instead of 5 to display the fields line at the begenning:
	top_cpu_usage=$(ps -eo pid,%cpu,comm --sort=-%cpu | head -n 6)

	# Finding the top memory-consuming processes and pinping the result to the (head) command:
	# 6 is used to display the fields line at the begenning:
	top_memory_usage=$(ps -eo pid,%mem,comm --sort=-%mem | head -n 6)

	# Storing the results in (process_info.log):
	# (First, we will overwrite the file then we will append):
	echo -e "Process tree:\n$ps_tree\n" > $1
	echo -e "List of all zombie processes:\n $zombie_list\n" >> $1
	echo -e "Processes CPU usage:\n $cpu_usage_list\n" >> $1
	echo -e "Processes memory usage:\n $memory_usage_list\n" >> $1
	echo -e "Top 5 cpu-consuming processes:\n $top_cpu_usage\n" >> $1
	echo -e "Top 5 memory-consuming processes:\n $top_memory_usage\n" >> $1
}

while true 
do
	# Calling the function and passing the name of the file where process information will be saved:
	collect_process_info process_info.log

	# To connect to the server, we need two pieces of information: 1- IP address 2- Username:
	# Getting the IP address of the server:
	echo "Enter the IP address of the server: "
	read ip_address
	# Getting the username of the account that we want to connect to in the server:
	echo "Enter the username: "
	read username

	# Securely copying (process_info.log) to the server:
	# (scp already uses SSH for transferring files):
	sudo scp process_info.log "$username@$ip_address:/home/client_02"

	# Making sure that (process_info.log) is stored in the server every hour:
	sleep 3600
done