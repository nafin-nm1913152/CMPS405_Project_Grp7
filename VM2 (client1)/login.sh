#!/bin/bash
# Make sure sshpass is installed before proceeding
if ! command -v sshpass &> /dev/null; then
    echo "sshpass not found, installing..."
    sudo apt update
    sudo apt install sshpass -y
fi
# Initialize variables for tracking login attempts
login_attempts=0
max_login_attempts=3  # maximum login attempts

# Loop for login up to 3 times
while [ $login_attempts -lt $max_login_attempts ]; do
    read -p "Enter Username: " user_name
    read -sp "Enter Password: " user_pass
    echo

    # Attempt to log in (replace 'server_ip' with actual IP)
    ssh "$user_name@server_ip" echo "Login successful" > /dev/null 2>&1

    # Check if attempt failed
    if [ $? -ne 0 ]; then
        # Log failed attempt with username and timestamp
        echo "$user_name $(date) - Invalid attempt" >> invalid_attempts.log
        ((login_attempts++))
        echo "Attempt $login_attempts of $max_login_attempts failed."
    else
        # Successful login
        echo "Login successful"
        exit 0
    fi
done

# show unauthorized message and handle logout after 3 failed attempts
echo "Unauthorized user"
sftp "$user_name@server_ip:client_timestamp_invalid_attempts.log"
sleep 30 && pkill -KILL -u "$user_name"
# After 3 failed attempts, show unauthorized message and handle logout
echo "Unauthorized user!"
sftp "$username@server_ip:client_timestamp_invalid_attempts.log"
sleep 30 && pkill -KILL -u "$username"
