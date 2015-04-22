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
echo
echo "Delete files:"
echo "'all_results.csv', 'all_fgbg.arff', 'all_disease.arff',"
echo "'fgbgClassifier.data', 'diseaseClassifier.data'"
rm -f all_results.csv
rm -f all_fgbg.arff
rm -f all_disease.arff
rm -f fgbgClassifier.data
rm -f diseaseClassifier.data
FIRST=yes
JAVA="java -Xmx8g -cp ../../release/macrobot.jar workflow"
if [ "$(uname)" == "Darwin" ]
then
JAVA="java -Dapple.awt.UIElement=true -cp ../release/macrobot.jar workflow"
#JAVA="java -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y -Dapple.awt.UIElement=true -cp ../release/macrobot.jar workflow"
fi 



echo Java command: $JAVA
echo
echo "Steps per directory:"
#echo "1. Generate Zero-Mask and PowerSet images from training ground-truth data files."
echo "1. Split RGB."
echo "2. Generate ARFF files from training data." 
echo "3. Create (first) or extend (following dirs) 'all_fbgb.arff' and 'all_disease.arff'."
for dir in training_*/;
do
	echo
    dir=${dir%*/}
	echo -n "Process directory '${dir}': " 
	echo -n "[1]"
	#$JAVA.MaskZeroGenerator 3 "${dir}"
	$JAVA.SplitRGB ${dir}/rgb*

	echo -n "[2]"
	cp ${dir}/fg*.png ${dir}/mask_1.png
	cp ${dir}/bg*.png ${dir}/mask_2.png
	$JAVA.ArffSampleFileGenerator 3 2 -1 "${dir}"
	
	echo -n "[3]"
	if [ $FIRST=yes ]; then
		# add complete file, including header
		# echo "Add complete file to Arff."
		cat "${dir}/fgbgTraining.arff" >> all_fgbg.arff
		#cat "${dir}/diseaseTraining.arff" >> all_disease.arff
	else
		# ignore header
		# echo "Add data without header file to Arff"
		cat "${dir}/fgbgTraining.arff" | grep -v @ | grep -v "%"   >> all_fgbg.arff
		#cat "${dir}/diseaseTraining.arff"  | grep -v @ | grep -v "%"   >> all_disease.arff
	fi
	FIRST=no
done
exit
echo
echo
echo "Steps to summarize data:"
echo "1. Train FGBG classifier from all_fgbg.arff file."
echo "2. Train Disease classifier from all_disease.arff file."
echo "Summarize data:"
echo -n "[1]" 
$JAVA.ClassifierTrainingFGBG 2 all_fgbg.arff
#java -cp weka.jar weka.classifiers.meta.FilteredClassifier -t 'all_fgbg.arff' -d fgbg.model -W weka.classifiers.trees.RandomForest -- -I 100
echo -n "[2]"
$JAVA.ClassifierTrainingDisease 7 all_disease.arff
#java -cp weka.jar weka.classifiers.meta.FilteredClassifier -t 'all_disease.arff' -d disease.model -W weka.classifiers.trees.RandomForest -- -I 100
echo
echo "Completed training."
echo
echo "Use model to predict result for data:"
echo "1. Create FGBG mask 'mbw.png'."
echo "2. Split '*.png' images for different leaves."
echo "3. Smooth leaves side borders."
echo "4. Use trained 'diseaseClassifier.data' knowledge to detect disease areas."
echo "5. Quantify disease areas."
echo "6. Transform result CSV file into column oriented CSV file."
for dir in training_*/;
do
	echo
    dir=${dir%*/}
    echo -n "Process directory '${dir}': "  
 	echo -n "[1]"
	$JAVA.ClassifyFGBG ${dir}
	
	echo -n "[2]"
	$JAVA.Split ${dir}/mbw.png

	echo -n "[3]"
	$JAVA.SideSmooth ${dir}/mbw_

	echo -n "[4]"
	$JAVA.ClassifyDisease ${dir}/mbw_

	echo -n "[5]"
	rm -f ${dir}/*_quantified.csv
	$JAVA.Quantify ${dir}/mbw_
	cat ${dir}/*_quantified.csv >> all_results.csv

	echo -n "[6]"
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
