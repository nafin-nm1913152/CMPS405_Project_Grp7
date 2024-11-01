#!/bin/bash
# System.sh script

# LOGs
DISK_LOG="disk_info.log"
SYS_LOG="mem_cpu_info.log"

# Disk usage for HOME directory, including subdirectories
echo "Disk usage for HOME directory:" > "$DISK_LOG"
du -h $HOME/* >> "$DISK_LOG"

# Total disk space for the file system containing the home directory
echo "Total Disk Space:" >> "$DISK_LOG"
df -h $HOME | awk 'NR==2{print "Total: "$2", Used: "$3", Available: "$4", Usage: "$5}' >> "$DISK_LOG"

# Memory and CPU information
echo "Memory and CPU Information:" > "$SYS_LOG"
echo "---------------------------------" >> "$SYS_LOG"

# Free and used memory as percentage
free -m | awk 'NR==2{printf "Memory Usage: Used: %.2f%%, Free: %.2f%%\n", $3*100/$2, $4*100/$2 }' >> "$SYS_LOG"

# CPU model and cores
echo "CPU Model:" >> "$SYS_LOG"
lscpu | grep 'Model name' | awk -F: '{print $2}' >> "$SYS_LOG"
echo "CPU Cores:" $(nproc) >> "$SYS_LOG"

# Save information to logs and display the output
cat "$DISK_LOG"
cat "$SYS_LOG"
