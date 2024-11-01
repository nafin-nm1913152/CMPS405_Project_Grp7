#!/bin/bash

search_files() {
    # Finding the files that are larger than 1 Megabyte in client 2 account:
    # ((2> /dev/null) is used to redirect error messages to /dev/null to discard them):
    files=$(find ~ -type f -size +1M 2> /dev/null)

    # Piping (files) to (wc -l) to find the number of files larger than 1 Megabyte:
    number_of_files=$(echo "$files" | wc -l)

    # Search date:
    search_date=$(date)

    # Storing the results in (bigfile.log):
    # (First, we will overwrite the file then we will append the number of files and search date):
    echo -e "$files\n" > bigfile.log
    echo -e "Number of files: $number_of_files\n" >> bigfile.log
    echo -e "Search date: $search_date" >> bigfile.log

    # Checking if (bigfile.log) is not empty:
    if [ -s "bigfile.log" ]; then
        # Sending an email using the (curl) command:
        # Note: 2FA has to be enabled in the email account and an app password must be used:
        curl -s --url 'smtps://smtp.gmail.com:465' --ssl-reqd \
        --mail-from "mriyadh1500@gmail.com" \
        --mail-rcpt "mriyadh1500@gmail.com"\
        --user "mriyadh1500@gmail.com":omgbkpfxhqxknfle \
        -T <(echo -e "$(cat "bigfile.log")")
    fi
}

search_files



