#!/bin/bash
# Loads a specified number of versions into the project folder
# sh loadVersions.sh [projectName] [maxVersion] [resourceFolder]
for number in $(seq 1 $2);
do
    $(mkdir -p ${3}/${1})
    $(defects4j checkout -p $1 -v ${number}b -w ${3}/${1}/$number)
done
exit 0
