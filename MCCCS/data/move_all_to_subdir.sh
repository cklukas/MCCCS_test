#!/bin/bash
if [ "$#" -ne 1 ]; then
    echo "Illegal number of parameters"
	exit 1
fi
find *.$1 -maxdepth 0 -type f -exec basename -s .$1 '{}' \; | xargs mkdir
find *.$1 -maxdepth 0 -type f -exec basename -s .$1 '{}' \; | xargs -i mv {}.$1 {}

