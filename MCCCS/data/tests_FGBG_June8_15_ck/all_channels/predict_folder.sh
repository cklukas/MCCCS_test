#!/bin/bash
dir=$2
dir=${dir%*/}
WORKDIR=$3
START=$(date +%s)
echo -n "[${dir}]"   
echo -n "a"
rm -f ${dir}/channel_*
rm -f ${dir}/*.arff
rm -f ${dir}/*_quantified.csv
rm -f ${dir}/foreground.png
$JAVA.SplitRGB ${dir}/*rgb*

#apply filter
echo -n "b"
source $WORKDIR/createChannelImages.sh

echo -n "d"
classes=2
$JAVA.ArffFromImageFileGenerator $classes "${dir}"

echo -n "e"
$WEKA weka.filters.supervised.attribute.AddClassification -i "${dir}/${dir}_$classes.arff" -serialized "$MODEL" -classification -remove-old-class -o "${dir}/result.arff" -c last -distribution

echo -n "f"
#create foreground png
cp "${dir}/channel_rgb_r.tif" "${dir}/result.tif"
$JAVA.ApplyClass0ToImage "${dir}/result.tif"
rm "${dir}/result.tif"

#create foreground png
#cp "${dir}/channel_rgb_r.tif" "${dir}/result.tif"
#$JAVA.ArffToProbabilityImageFileGenerator $classes "${dir}/result.tif"
#rm "${dir}/result.tif"

echo -n "g"
#quantify prediction errors based on colored 'diff' image
cp "${dir}/foreground.png" "${dir}/foreground_cluster.png"
$JAVA.Quantify ${dir}/foreground
rm "${dir}/foreground_cluster.png"
cat ${dir}/*_quantified.csv >> all_prediction_results.csv

rm -f ${dir}/channel_*
#rm -f ${dir}/*.arff

END=$(date +%s)
DIFF=$(echo "$END - $START" | bc)
echo -n " (finished ${dir} in ${DIFF} sec.) "
