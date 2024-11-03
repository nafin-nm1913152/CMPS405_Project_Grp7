#!/bin/bash

# Locate the files with 777 permissions and save to a temporary log
find / -type f -perm 777 > permission_log.log 2>/dev/null

# Display and update permissions for each file found
while read -r file_entry; do
    echo "File with 777 permissions: $file_entry"  # Display file with 777 permission
    chmod 700 "$file_entry"  # Change permissions to 700
done < permission_log.log

echo "Permission changes complete for files with 777 permissions."
