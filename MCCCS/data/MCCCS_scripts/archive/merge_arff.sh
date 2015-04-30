#!/bin/bash
# Merge ARFF files
# Output: file "merged"
# V0.1 C. Klukas
FILE=$(ls -1 *.arff|head -1)
cat $FILE | grep @ > merged
cat *.arff | grep -v @ >> merged
echo '%' >> merged
