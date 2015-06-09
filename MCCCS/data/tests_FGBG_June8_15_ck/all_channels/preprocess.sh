#!/bin/bash	
dir=$2
dir=${dir%*/}
echo -n "[${dir}]" 

START=$(date +%s)

echo -n "a"
rm -f ${dir}/channel_*
rm -f ${dir}/mask_1.png
rm -f ${dir}/mask_2.png
$JAVA.SplitRGB ${dir}/*rgb*

echo -n "b"
source ../createChannelImages.sh

echo -n "d"
$JAVA.ThresholdGTforFGBG ${dir}/*label*
echo -n "e"
$JAVA.ArffSampleFileGenerator -2 2500 "${dir}"
END=$(date +%s)
DIFF=$(echo "$END - $START" | bc)
if [ "$NPROCS" == "1" ];
then
	echo " ($DIFF seconds)"
else
	echo -n " (finished ${dir} in ${DIFF} sec.) "
fi