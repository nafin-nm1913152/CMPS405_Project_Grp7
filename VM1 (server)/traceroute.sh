#!/bin/bash

# Check if a target IP was provided

if [ "$#" -eq 0 ]; then
    echo "No ip was provided"
    exit 1
fi

target_IP=$1
log="network.log"

# Log the start of the traceroute process
echo "Running traceroute for $target_IP" | tee -a "$log"

# Display routing table
echo "Routing table:" | tee -a "$log"
netstat -nr | tee -a "$log"

# Display hostname
echo "Hostname: $(hostname)" | tee -a "$log"

# Testing local DNS server connection
echo "Testing local DNS server connection" | tee -a "$log"
nslookup google.com | tee -a "$log"

# Tracing route to google
echo "Tracing route to google.com" | tee -a "$log"
traceroute google.com | tee -a "$log"

# Tracing route to the target IP
echo "Tracing route to $target_IP" | tee -a "$log"
traceroute "$target_IP" | tee -a "$log"

# Ping google.com
echo "Pinging google.com" | tee -a "$log"
ping -c 3 -W 5 google.com | tee -a "$log" 

# Check connectivity to target IP
if ! ping -c 3 -W 5 "$target_IP" &> /dev/null; then
    echo "Cannot reach target $target_IP, rebooting now" | tee -a "$log"
    sudo reboot
else
    echo "Reconnected to $target_IP" | tee -a "$log"
fi
