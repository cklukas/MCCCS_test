#!/bin/bash
dir=$2
dir=${dir%*/}
echo -n "[${dir}]" 

START=$(date +%s)

#echo
#echo "delete previous results: foreground*, *.arff, classified.png"
rm -f ${dir}/foreground*
rm -f ${dir}/*.arff
rm -f ${dir}/classified*
rm -f ${dir}/channel_*
rm -f ${dir}/foreground_*
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

#rotate
$JAVA.Rotate l "${dir}/foreground.png"

#start loop for subplates
echo -n "[c]"
for i in {1..4}
do
	echo -n "[$i]"
	$JAVA.ApplyMask ${dir}/foreground_rot_l.tif ${dir}/roi_$i.png

	$JAVA.Split ${dir}/foreground_rot_l_roi_$i.tif

	$JAVA.SideSmooth ${dir}/foreground_rot_l_roi_$i

	$JAVA.Erode ${dir}/foreground_rot_l_roi_$i'smooth_all.png'

	$JAVA.Rotate r ${dir}/foreground_rot_l_roi_$i'smooth_all.png'
done

#do classification
echo -n "[d]"
$JAVA.ArffFromImageFileGenerator 4 3 "${dir}"

echo -n "[e]"
$WEKA weka.filters.supervised.attribute.AddClassification -i "${dir}/${dir}_3.arff" -serialized $MODELPATH/label.model -classification -remove-old-class -o "${dir}/labelresult.arff" -c last

#start loop for subplates again
echo -n "[f]"
for i in {1..4}
do
	echo -n "[$i]"
	cp ${dir}/foreground_rot_l_roi_$i'smooth_all_rot_r.tif' "${dir}/labelresult.tif"
	$JAVA.ArffToImageFileGenerator 3 "${dir}/labelresult.tif"
	rm "${dir}/labelresult.tif"
	
	mv ${dir}/classified.png ${dir}/classified_$i.png
	$JAVA.Rotate l ${dir}/classified_$i.png

	$JAVA.Split ${dir}/classified_$i'_rot_l.tif'

	#echo -n "[g]"
	#rm -f ${dir}/*_quantified.csv
	$JAVA.Quantify ${dir}/classified_$i'_rot_l_'*
	cat ${dir}/*_quantified.csv >> all_results.csv

	sleep 10s
done

echo "delete temporary results"
rm -f ${dir}/foreground*
rm -f ${dir}/*.arff
rm -f ${dir}/*.csv
rm -f ${dir}/channel_*
rm -f ${dir}/foreground_*

END=$(date +%s)
DIFF=$(echo "$END - $START" | bc)
if [ "$NPROCS" == "1" ];
then
	echo " ($DIFF seconds)"
else
	echo -n " (finished ${dir} in ${DIFF} sec.) "
fi
