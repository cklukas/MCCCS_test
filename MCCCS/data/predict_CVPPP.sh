#!/bin/bash
echo "°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°"
echo "°                                                                   °"
echo "°        Welcome to the 'Leaf Disease Classification System'        °"
echo "°                                                                   °"
echo "°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°"
echo "°                                                                   °"
echo "°          V1.0 developed in January and February 2015              °"
echo "°          by the following members of the Research Group           °"
echo "°                                                                   °"
echo "°          - IMAGE ANALYSIS -                                       °"
echo "°                                                                   °"
echo "°               Head of group:                                      °"
echo "°                  Dr. Christian Klukas                             °"
echo "°                                                                   °"
echo "°               Scientific Assistant:                               °"
echo "°                  Jean-Michel Pape                                 °"
echo "°                                                                   °"
echo "°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°"
echo "°                                                                   °"
echo "°              !! Script will stop in case of error. !!             °"
echo "°           !!  Last output is READY in case of no error !!         °"
echo "°                                                                   °"
echo "°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°"
# stop in case of error:
set -e
if [ "$#" -ne 2 ]; then
    	echo "Illegal number of parameters: 1 - path to testing data folder, 2 - model file"
	exit 1
fi
MODEL=$(realpath $2)
WEKAJAR=$(realpath weka.jar)
echo $WEKAJAR
cd "$1"
echo
echo "Delete files:"
echo "'all_prediction_results.csv'"
rm -f all_prediction_results.csv
FIRST=yes
MBP=../../release/macrobot.jar
echo "MacroBot jar:" $MBP
JAVA="java -cp $MBP workflow"
WEKA="java -cp $WEKAJAR"
if [ "$(uname)" == "Darwin" ]
then
JAVA="java -Dapple.awt.UIElement=true -cp ../release/macrobot.jar workflow"
fi 
echo Java command: $JAVA
echo
echo "Use model to predict result for data:"
echo "0. Split RGB channels."
echo "1. Create ARFF files from images."
echo "2. Use trained 'fgbg.model' knowledge to detect fgbg areas."
echo "3. Quantify disease areas."
echo "4. Transform result CSV file into column oriented CSV file."
for dir in */;
do
	echo
    dir=${dir%*/}
    	echo -n "Process directory '${dir}': "  
 	
 	echo -n "[0]"
	rm -f ${dir}/channel_*
	rm -f ${dir}/*.arff
	$JAVA.SplitRGB ${dir}/*
	
	echo -n "[1]" 
	$JAVA.ArffFromImageFileGenerator 3 "${dir}"
	
 	echo -n "[2]"
	$WEKA weka.filters.supervised.attribute.AddClassification -i "${dir}/${dir}.arff" -serialized "$MODEL" -classification -remove-old-class -o "${dir}/foreground.arff" -c last

	cp "${dir}/channel_0_r.tif" "${dir}/foreground.tif"
	$JAVA.ApplyClass0ToImage "${dir}/foreground.tif"
	rm "${dir}/foreground.tif"
	
	echo -n "[5]"
	cp "${dir}/foreground.png" "${dir}/foreground_cluster.png"
	rm -f ${dir}/*_quantified.csv
	$JAVA.Quantify ${dir}/foreground
	rm "${dir}/foreground_cluster.png"
	cat ${dir}/*_quantified.csv >> all_prediction_results.csv
done
echo -n "[6]"
rm -f all_prediction_results.csv.transformed
$JAVA.TransformCSV all_prediction_results.csv
mv all_prediction_results.csv.transformed all_prediction_results.csv
echo
echo "Processing finished:"
echo "Model has been applied to training data. Disease infection rate is calculated:"
echo "      'all_prediction_results.csv'"
echo
echo READY
