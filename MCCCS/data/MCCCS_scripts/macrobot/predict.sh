#!/bin/bash
echo "°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°"
echo "°                                                                   °"
echo "°                          Welcome to the                           °"
echo "°       'Multi Channel Classification and Clustering System'        °"
echo "°                                                                   °"
echo "°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°"
echo "°                                                                   °"
echo "°          V1.0 developed in January till April 2015                °"
echo "°          by the following members of the Research Group           °"
echo "°                                                                   °"
echo "°          - IMAGE ANALYSIS at IPK -                                °"
echo "°                                                                   °"
echo "°          Jean-Michel Pape and                                     °"
echo "°          Dr. Christian Klukas (Head of group)                     °"
echo "°                                                                   °"
echo "°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°"
echo "°                                                                   °"
echo "°          Pipeline for for disease classification.                 °"
echo "°          Implemented by Jean-Michel Pape                          °"
echo "°                                                                   °"
echo "°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°"
echo "°                                                                   °"
echo "°              !! Script will stop in case of error. !!             °"
echo "°           !!  Last output is READY in case of no error !!         °"
echo "°                                                                   °"
echo "°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°"
# parameter:
# 1: path to the mcccs.jar
# 2: path to the data-files
# 3: number of procs
# 4: path to the model files
if [ "$#" -ne 4 ]; then
    echo "Please supply the path to the mcccs.jar as parameter 1, the path to the data-files as parameter 2 and s-single-threaded, m-multi-threaded as parameter 3!"
	exit 1
fi
# stop in case of error:
set -e
echo "prepare"
source prepare.sh
FIRST=yes
echo Java command: $JAVA
MODELPATH="../$4"
echo "Path to model files:"
echo "$MODELPATH"
echo
echo "Use model to predict result for data:"
echo "1. Create .arff file for fgbg segmentation."
echo "2. Classify fgbg.arff."
echo "3. Split leaves."
echo "4. Reconstruct leaf shape."
echo "5. Create .arff for disease classification."
echo "6. Classify disease.arff."
echo "7. Quantify disease areas."
echo "8. Transform result CSV file into column oriented CSV file."
for dir in */;
do
	echo
    dir=${dir%*/}
    	echo -n "Process directory '${dir}': "

	cp -n "${dir}"/*red.tif "${dir}/channel_0.tif"
	cp -n "${dir}"/*green.tif "${dir}/channel_1.tif"
	cp -n "${dir}"/*blue.tif "${dir}/channel_2.tif"
	cp -n "${dir}"/*uv.tif "${dir}/channel_3.tif"

	echo -n "[0]"
	$JAVA.ArffFromImageFileGenerator 4 2 "${dir}"

 	echo -n "[1]"
	$WEKA weka.filters.supervised.attribute.AddClassification -i "${dir}/${dir}_2.arff" -serialized $MODELPATH/fgbg.model -classification -remove-old-class -o "${dir}/fgbgresult.arff" -c last

	#create foreground png
	cp "${dir}/channel_0.tif" "${dir}/fgbgresult.tif"
	$JAVA.ApplyClass0ToImage "${dir}/fgbgresult.tif"
	rm "${dir}/fgbgresult.tif"
	
	echo -n "[2]"
	$JAVA.ApplyMask ${dir}/foreground.png ${dir}/roi.png 
	$JAVA.Split ${dir}/foreground_roi.png

	echo -n "[3]"
	$JAVA.SideSmooth ${dir}/foreground_roi_

	echo -n "[4]"
	$JAVA.ArffFromImageFileGenerator 4 11 "${dir}"

	echo -n "[5]"
	#$JAVA.ClassifyDisease ${dir}/foreground_
	$WEKA weka.filters.supervised.attribute.AddClassification -i "${dir}/${dir}_11.arff" -serialized $MODELPATH/label.model -classification -remove-old-class -o "${dir}/labelresult.arff" -c last

	echo -n "[6]"
	cp ${dir}/foreground_roi_smooth_all.png "${dir}/labelresult.png"
	$JAVA.ArffToImageFileGenerator 11 "${dir}/labelresult.png"
	rm "${dir}/labelresult.png"

	echo -n "[7]"
	rm -f ${dir}/*_quantified.csv
	$JAVA.Quantify ${dir}/classified.png
	cat ${dir}/*_quantified.csv >> all_results.csv

	echo -n "[8]"
	rm -f all_results.csv.transformed
	$JAVA.TransformCSV all_results.csv
	mv all_results.csv.transformed all_results.csv
done
echo
echo "Processing finished:"
echo "1. FGBG model is trained: "
echo "      'fgbgClassifier.data'"
echo "2. Disease identification model is trained:"
echo "      'diseaseClassifier.data'"
echo "3. Model has been applied to training data. Disease infection rate is calculated:"
echo "      'all_results.csv'"
echo
echo READY
