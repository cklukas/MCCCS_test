#!/bin/bash	
dir=$1
dir=${dir%*/}
echo -n "[${dir}]" 

echo -n "."
rm -f ${dir}/channel_*
$JAVA.SplitRGB ${dir}/rgb*

echo -n "."
$JAVA.RGB2HSV ${dir}/*rgb_r.tif ${dir}/*rgb_g.tif ${dir}/*rgb_b.tif 8
$JAVA.RGB2XYZ ${dir}/*rgb_r.tif ${dir}/*rgb_g.tif ${dir}/*rgb_b.tif 8
$JAVA.RGB2LAB ${dir}/*rgb_r.tif ${dir}/*rgb_g.tif ${dir}/*rgb_b.tif 8

echo -n "."
for img in ${dir}/channel*;
do
	$JAVA.FILTER ${img} ${img} 3 3 BLUR
	$JAVA.FILTER ${img} ${img} 4 4 MEDIAN
done
echo -n "."
cat ${dir}/fg* > ${dir}/mask_1.png
cat ${dir}/bg* > ${dir}/mask_2.png  
$JAVA.ArffSampleFileGenerator 3 2 2500 "${dir}"
