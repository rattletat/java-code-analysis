#!/bin/bash
# Uses gzoltar for creating the matrix and spectra file and puts it in the corresponding version folder.
# sh generateSpectraFiles.sh [pathToRunGzoltar] [pathToResourceFolder] [pathToTempFolder]
projects=$(find $2 -maxdepth 1 -type d -not -path '*/\.*' | tail -n +2)
for project in $projects;
do
    project_name=$(basename $project)
    versions=$(find ${2}/$project_name -maxdepth 1 -type d -not -path '*/\.*' | cut -c 3- | tail -n +2)
    for version in $versions;
    do
        version_name=$(basename $version)
        dst=${3}/${project_name}/${version_name}
        $(mkdir -p $dst)
        $(sh ${1} $project_name $version_name $dst developer)
        tar_file=$(find $dst -iname "*.tar.gz")
        $(rm $tar_file)
        spectra_files=$(find $dst/gzoltars -iname "matrix" -o -iname "spectra")
        echo $spectra_files
        $(mv $spectra_files $dst)
        $(rm $dst/gzoltars -rf)
    done
done
exit 0
