#!/bin/bash
# Uses gzoltar for creating the matrix and spectra file and puts it in the corresponding version folder.
cd "${0%/*}"
resource_folder=$(realpath ../../resources)
temp_folder=$(realpath ../../../../temp)
gzoltar_sh=$(realpath ../java/gzoltar/run_gzoltar.sh)

projects=$(find $resource_folder -maxdepth 1 -type d -not -path '*/\.*' | tail -n +2)
for project in $projects;
do
    project_name=$(basename $project)
    versions=$(find $resource_folder/$project_name -maxdepth 1 -type d -not -path '*/\.*' | cut -c 3- | tail -n +2)
    for version in $versions;
    do
        version_name=$(basename $version)
        dst=${temp_folder}/$project_name/$version_name
        $(mkdir -p $dst)
        $(sh $gzoltar_sh $project_name $version_name $dst developer)
        tar_file=$(find $dst -iname "*.tar.gz")
        $(rm $tar_file)
        spectra_files=$(find $dst/gzoltars -iname "matrix" -o -iname "spectra")
        echo $spectra_files
        $(mv $spectra_files $dst)
        $(rm $dst/gzoltars -rf)
    done
done
exit 0
