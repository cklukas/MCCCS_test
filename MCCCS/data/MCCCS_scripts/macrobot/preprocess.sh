#!/bin/bash
dir=$2
dir=${dir%*/}
echo -n "[${dir}]" 

START=$(date +%s)

#echo
    #dir=${dir%*/}
#echo -n "'${dir}'"

echo
#echo -n "Delete files "
#echo "rm -f ${dir}/*.arff"
rm -f ${dir}/*.arff
#echo -n "."
#echo "rm -f ${dir}/foreground*"
rm -f ${dir}/foreground*
#echo -n "."
#echo "rm -f ${dir}/label*"
rm -f ${dir}/label*
#echo -n "."
#echo "rm -f ${dir}/classified*"
rm -f ${dir}/classified*
#echo -n "."
#echo "rm -f ${dir}/quantified*"
rm -f ${dir}/quantified*
#echo -n ". "
#echo "Finish deletion!"

#rename and copy
cp -n "${dir}"/*red.tif "${dir}/channel_0.tif"
cp -n "${dir}"/*green.tif "${dir}/channel_1.tif"
cp -n "${dir}"/*blue.tif "${dir}/channel_2.tif"
cp -n "${dir}"/*uv.tif "${dir}/channel_3.tif"

echo -n "[a]"
$JAVA.PowerSetGenerator 3 "${dir}"

rm -f ${dir}/label_2*
rm -f ${dir}/label_4*
rm -f ${dir}/label_5*
rm -f ${dir}/label_6*
rm -f ${dir}/label_7*
rm -f ${dir}/label_8*
rm -f ${dir}/label_9*
rm -f ${dir}/label_10*
mv ${dir}/label_3.png ${dir}/label_2.png

echo -n "[b]"
$JAVA.ArffSampleFileGenerator 4 -2 5000 "${dir}"
$JAVA.ArffSampleFileGenerator 4 3 2000 "${dir}"

END=$(date +%s)
DIFF=$(echo "$END - $START" | bc)
if [ "$NPROCS" == "1" ];
then
	echo " ($DIFF seconds)"
else
	echo -n " (finished ${dir} in ${DIFF} sec.) "
fi
