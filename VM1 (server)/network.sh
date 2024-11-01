#!/bin/bash

# Check if at least one IP is provided

if [ "$#" -eq 0 ]; then
    echo "No ip was provided"
    exit 1
fi

# Check whether ping and traceroute are installed. Otherwise, install all network tools
if ! command -v ping &> /dev/null || ! command -v traceroute &> /dev/null; then
    echo "Installing ping and traceroute"
    sudo apt-get update -y &> /dev/null
    sudo apt-get install -y iputils-ping traceroute net-tools &> /dev/null
fi  

# Creating log file for network
log="network.log"
echo "Testing Connection on $(date)" > $log

# Checking connectivity
check() {
    # Taking one IP at a time
    local target_IP=$1
    echo "Testing connection for $target_IP"

    # Test with ping
    if ping -c 3 -W 5 "$target_IP" &> /dev/null; then
        # If good connection, then log result
        echo "$(date '+%Y-%m-%d %H:%M:%S') - Testing connection with $target_IP is successful" | tee -a "$log"
    else
        # If ping fails, then call traceroute
        echo "$(date '+%Y-%m-%d %H:%M:%S') - Testing connection with $target_IP has failed, calling traceroute now" | tee -a "$log"
        ./traceroute.sh "$target_IP"
    fi
}

# Check connection for each IP three times
for i in {1..3}; do
    for target_IP in "$@"; do 
        check "$target_IP"
    done
    echo "Pass no.$i is done"
done
