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
echo "'all_prediction_results.csv'"
rm -f all_prediction_results.csv
FIRST=yes
JAVA="java -cp ../release/macrobot.jar workflow"
if [ "$(uname)" == "Darwin" ]
then
JAVA="java -Dapple.awt.UIElement=true -cp ../release/macrobot.jar workflow"
fi 
echo Java command: $JAVA
echo
echo "Use model to predict result for data:"
echo "1. Create FGBG mask 'mbw.png'."
echo "2. Split '*.png' images for different leaves."
echo "3. Smooth leaves side borders."
echo "4. Use trained 'diseaseClassifier.data' knowledge to detect disease areas."
echo "5. Quantify disease areas."
echo "6. Transform result CSV file into column oriented CSV file."
for dir in testing_*/;
do
	echo
    dir=${dir%*/}
    	echo -n "Process directory '${dir}': "  
 	echo -n "[1]"
	$JAVA.ClassifyFGBG ${dir}
	#java -cp weka.jar weka.filters.supervised.attribute.AddClassification -i 'input1.arff' -serialized fgbg.model -classification -remove-old-class -o output_fgbg.arff -c last
	
	echo -n "[2]"
	$JAVA.Split ${dir}/mbw.png

	echo -n "[3]"
	$JAVA.SideSmooth ${dir}/mbw_

	echo -n "[4]"
	$JAVA.ClassifyDisease ${dir}/mbw_
	#java -cp weka.jar weka.filters.supervised.attribute.AddClassification -i 'input2.arff' -serialized disease.model -classification -remove-old-class -o output_d.arff -c last

	echo -n "[5]"
	rm -f ${dir}/*_quantified.csv
	$JAVA.Quantify ${dir}/mbw_
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
