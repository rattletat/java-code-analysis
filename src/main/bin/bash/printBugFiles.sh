#!/bin/bash
# Extracts the faulty files of a specified project and version.
# sh printBugLines.sh [projectName] [projectVersion]
name=$number'_faultyfiles'
info=$(defects4j info -p $1 -b $2)
echo "$info" | sed  \
    -e '1,/sources/ d' \
    -e '/-----/,$ d' \
    -re 's/[^a-zA-Z]*(.*)/\1/' >> $name
    exit 0
