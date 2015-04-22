#!/bin/bash
# stop in case of error:
echo Run:     collect.sh [Main Src Folder] [Target Folder] [File Mask]
echo Example: collect.sh src_folder tgt_folder *.arff
set -e
CURDIR=$(pwd)
cd $1
echo "current directory: " $(pwd)
D2=${2%/}
echo Analyse sub folders in $1
for dir in */;
do
	if  [ "$dir" = "CVS/" ]; then
		echo "[Ignore CVS directory]"
		continue
 	fi
	dir=${dir%*/}
	for dir2 in ${dir}/$3;
	do
		if  [ "$dir2" = "CVS/" ]; then
			echo -n "[Ignore CVS directory]"
			continue
	 	fi
		dir2=${dir2%*/}
		TN=${dir2/\//_}
		cp "${dir2}" "$CURDIR/$D2/$TN"
	done
done
cd -
echo READY
