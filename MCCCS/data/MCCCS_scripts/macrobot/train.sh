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
# parameter:
# 1: path to the mcccs.jar
# 2: path to the data-files
# 3: number of procs
if [ "$#" -ne 3 ]; then
    echo "Please supply the path to the mcccs.jar as parameter 1, the path to the data-files as parameter 2 and s-single-threaded, m-multi-threaded as parameter 3!"
	exit 1
fi
# stop in case of error:
set -e
echo "prepare"
source prepare.sh
FIRST=yes
echo Java command: $JAVA
echo
echo "Steps per directory:"
echo "1. Generate Zero-Mask and PowerSet images from training ground-truth data files."
echo "2. Generate ARFF files from training data." 
echo "3. Create (first) or extend (following dirs) 'all_fbgb.arff' and 'all_disease.arff'."
for dir in */;
do
	echo
    dir=${dir%*/}
	echo -n "Process directory '${dir}': "

	echo "Delete files ..."
	echo "rm -f ${dir}/*.arff"
	rm -f ${dir}/*.arff
	echo "rm -f ${dir}/foreground*"
	rm -f ${dir}/foreground*
	echo "rm -f ${dir}/label*"	
	rm -f ${dir}/label*
	echo "rm -f ${dir}/classified*"	
	rm -f ${dir}/classified*
	echo "rm -f ${dir}/quantified*"	
	rm -f ${dir}/quantified*
	echo "Finish deletion!"

	echo -n "[1]"
	#echo $JAVA.PowerSetGenerator 3 "${dir}"
	$JAVA.PowerSetGenerator 3 "${dir}"
	
	echo -n "[2]"
	$JAVA.ArffSampleFileGenerator 4 -2 1000 "${dir}"
	$JAVA.ArffSampleFileGenerator 4 7 5000 "${dir}"
		
	echo -n "[3]"
	if [ $FIRST=yes ]; then
		# add complete file, including header
		# echo "Add complete file to Arff."
		cat "${dir}/fgbgTraining.arff" >> all_fgbg.arff
		cat "${dir}/labelTraining.arff" >> all_label.arff
	else
		# ignore header
		# echo "Add data without header file to Arff"
		cat "${dir}/fgbgTraining.arff" | grep -v @ | grep -v "%"   >> all_fgbg.arff
		cat "${dir}/labelTraining.arff"  | grep -v @ | grep -v "%"   >> all_label.arff
	fi
	FIRST=no
done 
echo
echo
echo "Steps to summarize data:"
echo "1. Train FGBG classifier from all_fgbg.arff file."
echo "2. Train Disease classifier from all_disease.arff file."
echo "Summarize data:"
echo -n "[1]"
$WEKA weka.classifiers.meta.FilteredClassifier -t 'all_fgbg.arff' -d fgbg.model -W weka.classifiers.trees.RandomForest -- -I 100
echo -n "[2]"
$WEKA weka.classifiers.meta.FilteredClassifier -t 'all_label.arff' -d label.model -W weka.classifiers.trees.RandomForest -- -I 100
echo
echo "Completed training."
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

	echo -n "[0]"
	$JAVA.ArffFromImageFileGenerator 4 2 "${dir}"

 	echo -n "[1]"
	$WEKA weka.filters.supervised.attribute.AddClassification -i "${dir}/${dir}_2.arff" -serialized fgbg.model -classification -remove-old-class -o "${dir}/fgbgresult.arff" -c last

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
	$JAVA.ArffFromImageFileGenerator 4 7 "${dir}"

	echo -n "[5]"
	#$JAVA.ClassifyDisease ${dir}/foreground_
	$WEKA weka.filters.supervised.attribute.AddClassification -i "${dir}/${dir}_7.arff" -serialized label.model -classification -remove-old-class -o "${dir}/labelresult.arff" -c last

	echo -n "[6]"
	cp ${dir}/foreground_roi_1_smooth.png "${dir}/labelresult.png"
	$JAVA.ArffToImageFileGenerator 7 "${dir}/labelresult.png"
	rm "${dir}/labelresult.png"

	echo -n "[7]"
	#rm -f ${dir}/*_quantified.csv
	$JAVA.Quantify ${dir}/foreground_
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
