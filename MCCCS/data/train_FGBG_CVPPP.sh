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
if [ "$(uname)" == "Darwin" ]
then
	PRES=$(pwd)/preprocess.sh
else
	PRES=$(pwd)/preprocess.sh
fi
source prepare.sh
echo Java command: $JAVA
echo
echo "Steps per directory:"
echo "0. Split RGB to channel images."
echo "1. Convert images to HSV, XYZ, LAB."
echo "2. Perform image operations to extend the channel list."
echo "3. Generate ARFF files from training data." 
echo "4. Create (first) or extend (following dirs) 'all_fbgb.arff'."
ls -1 -d */ | grep -v CVS | $par "$PRES" {}
FIRST="yes"
for dir in */;
do
	if  [ "$dir" = "CVS/" ]; then
		echo -n "[Ignore CVS directory]"
		continue
 	fi
	if  [ "$FIRST" = "yes" ]; then
		# add complete file, including header
		# echo "Add complete file to Arff."
		cat "${dir}/fgbgTraining.arff" >> all_fgbg.arff
	else
		# ignore header
		cat "${dir}/fgbgTraining.arff" | grep -v @ | grep -v "%"   >> all_fgbg.arff
	fi
	FIRST="no"
done
echo
echo
echo "Steps to summarize data:"
echo "1. Train FGBG classifier from all_fgbg.arff file."
echo
echo "Summarize data:"
echo -n "."
#$WEKA weka.classifiers.meta.FilteredClassifier -t 'all_fgbg.arff' -d fgbg.model -W weka.classifiers.trees.J48 -- -C 0.25 -M 2
$WEKA weka.classifiers.meta.FilteredClassifier -t 'all_fgbg.arff' -d fgbg.model -W weka.classifiers.bayes.NaiveBayes
#$WEKA weka.classifiers.meta.FilteredClassifier -t 'all_fgbg.arff' -d fgbg.model -W weka.classifiers.trees.RandomForest -- -I 100 -K 0 -S 1
#$WEKA weka.classifiers.meta.FilteredClassifier -t 'all_fgbg.arff' -d fgbg.model -W weka.classifiers.bayes.BayesNet -- -D -Q weka.classifiers.bayes.net.search.local.K2 -- -S BAYES -E weka.classifiers.bayes.net.estimate.SimpleEstimator -- -A 0.5
echo
echo "Completed training."
echo
echo "Use model to predict result for data:"
echo "0. Convert image to ARFF."
echo "1. Apply model."
echo "2. Create FGBG mask."
echo "3. Create Diff Image"
echo "4. Quantify areas."
echo "5. Transform result CSV file into column oriented CSV file."
for dir in training_*/;
do
	if  [ "$dir" = "CVS" ]; then
		echo
		echo "Ignore CVS directory."
 	else
		echo
	    dir=${dir%*/}
	    echo -n "[${dir}]"  
	    
	    echo -n "."
	   	rm -f "${dir}/${dir}.arff"
	    $JAVA.ArffFromImageFileGenerator 3 "${dir}"
	    	
	 	echo -n "."
	 	$WEKA weka.filters.supervised.attribute.AddClassification -i "${dir}/${dir}.arff" -serialized fgbg.model -classification -remove-old-class -o "${dir}/result.arff" -c last
		
		echo -n "."
		#create foreground png
		cp "${dir}/channel_rgb_r.tif" "${dir}/result.tif"
		$JAVA.ApplyClass0ToImage "${dir}/result.tif"
		rm "${dir}/result.tif"
		
		echo -n "."
		rm -f ${dir}/foreground_diff.png
		$JAVA.CreateDiffImage "${dir}/mask_2.png" "${dir}/foreground.png" "${dir}/foreground_diff.png"
		
		echo -n "."
		cp "${dir}/foreground.png" "${dir}/foreground_cluster.png"
		rm -f ${dir}/*_quantified.csv
		$JAVA.Quantify ${dir}/foreground
		rm "${dir}/foreground_cluster.png"
	fi
done
echo
echo "Transform CSV"
for dir in training_*/;
do
	if  [ "$dir" = "CVS" ]; then
		echo "Ignore CVS directory."
	else
	cat ${dir}/*_quantified.csv >> all_results.csv
	fi
done
rm -f all_results.csv.transformed
$JAVA.TransformCSV all_results.csv
mv all_results.csv.transformed all_results.csv
echo
echo "Processing finished:"
echo "1. FGBG model is trained: "
echo "      'fgbg.model'"
echo "2. Model has been applied to training data. Disease infection rate is calculated:"
echo "      'all_results.csv'"
echo
echo READY
