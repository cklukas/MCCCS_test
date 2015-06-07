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
$JAVA.RGB2ALL ${dir}/*rgb_r.tif ${dir}/*rgb_g.tif ${dir}/*rgb_b.tif 8 -1 4
#$JAVA.RGB2HSV ${dir}/*rgb_r.tif ${dir}/*rgb_g.tif ${dir}/*rgb_b.tif 8
#$JAVA.RGB2XYZ ${dir}/*rgb_r.tif ${dir}/*rgb_g.tif ${dir}/*rgb_b.tif 8
#$JAVA.RGB2LAB ${dir}/*rgb_r.tif ${dir}/*rgb_g.tif ${dir}/*rgb_b.tif 8

#echo -n "c"
#for img in ${dir}/channel*;
#do
#	$JAVA.FILTER ${img} ${img} 3 3 BLUR
#	$JAVA.FILTER ${img} ${img} 4 4 MEDIAN
#done
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
