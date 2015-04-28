#!/bin/bash
dir=$2
dir=${dir%*/}
echo -n "[${dir}]" 

START=$(date +%s)

#echo
#echo "delete previous results: foreground*, *.arff, classified.png"
rm -f ${dir}/foreground*
rm -f ${dir}/*.arff
rm -f ${dir}/classified.png
#echo

#rename and copy
cp -n "${dir}"/*red.tif "${dir}/channel_0.tif"
cp -n "${dir}"/*green.tif "${dir}/channel_1.tif"
cp -n "${dir}"/*blue.tif "${dir}/channel_2.tif"
cp -n "${dir}"/*uv.tif "${dir}/channel_3.tif"

echo -n "[a]"
$JAVA.ArffFromImageFileGenerator 4 2 "${dir}"

 echo -n "[b]" 
# ${dir}/${dir}_2.arff""
$WEKA weka.filters.supervised.attribute.AddClassification -i "${dir}/${dir}_2.arff" -serialized $MODELPATH/fgbg.model -classification -remove-old-class -o "${dir}/fgbgresult.arff" -c last

#create foreground png
cp "${dir}/channel_0.tif" "${dir}/fgbgresult.tif"
$JAVA.ApplyClass0ToImage "${dir}/fgbgresult.tif"
rm "${dir}/fgbgresult.tif"

echo -n "[c]"
$JAVA.ApplyMask ${dir}/foreground.png ${dir}/roi.png

echo -n "[d]"
$JAVA.Split ${dir}/foreground_roi.png

echo -n "[e]"
$JAVA.SideSmooth ${dir}/foreground_roi_

echo -n "[f]"
$JAVA.ArffFromImageFileGenerator 4 11 "${dir}"

echo -n "[g]"
$WEKA weka.filters.supervised.attribute.AddClassification -i "${dir}/${dir}_11.arff" -serialized $MODELPATH/label.model -classification -remove-old-class -o "${dir}/labelresult.arff" -c last

echo -n "[h]"
cp ${dir}/foreground_roi_smooth_all.png "${dir}/labelresult.png"
$JAVA.ArffToImageFileGenerator 11 "${dir}/labelresult.png"
rm "${dir}/labelresult.png"

echo -n "[i]"
rm -f ${dir}/*_quantified.csv
$JAVA.Quantify ${dir}/classified.png
cat ${dir}/*_quantified.csv >> all_results.csv

END=$(date +%s)
DIFF=$(echo "$END - $START" | bc)
if [ "$NPROCS" == "1" ];
then
	echo " ($DIFF seconds)"
else
	echo -n " (finished ${dir} in ${DIFF} sec.) "
fi
