#!/bin/bash
#arguments: 1 - unclustered arff-file, 2 - cluster prediction result, 3 - output file
cat $1 | sed -e '/@data/,$d' > $3
echo @data >> $3

cat $1 | sed -e '1,/@data/d' > $1.tmp

cat $2 | cut -d' ' -f 2 -s > $2.tmp

paste -d, $1.tmp $2.tmp >> $3
rm $1.tmp
rm $2.tmp
cat $3 | sed 's|?||g' > $3.tmp
cat $3.tmp | sed 's|,,|,|g' > $3
rm $3.tmp
