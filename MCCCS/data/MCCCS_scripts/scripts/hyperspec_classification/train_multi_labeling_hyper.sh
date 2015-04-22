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
echo "°              !! Script will stop in case of error. !!             °"
echo "°           !!  Last output is READY in case of no error !!         °"
echo "°                                                                   °"
echo "°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°"
# stop in case of error:
set -e
PRES="$(pwd)/preprocess_hyper_classification.sh"
if ! [[ "$(uname)" == CYGWIN* ]]
then
	chmod +x $PRES
fi
if [ "$(uname)" == "Darwin" ]; then
realpath() {
    [[ $1 = /* ]] && echo "$1" || echo "$PWD/${1#./}"
}
fi
export CLASSCOUNT=$3
source prepare.sh
echo Java command: $JAVA
echo
echo "Steps per directory:"
echo "1. Split tiff-stack into separate images."
echo "2. Generate ARFF files from training data." 
echo "3. Create (first) or extend (following dirs) 'all_fbgb.arff'."
echo -n "."
#ls -1 -d */ | grep -v CVS | $par "$PRES" {}
find * -maxdepth 0 -type d | grep -F -v CVS | $par $PRES {}
echo -n "."
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
		cat "${dir}/labelTraining.arff" >> all_label.arff
	else
		# ignore header
		cat "${dir}/labelTraining.arff" | grep -v @ | grep -v "%"   >> all_label.arff
	fi
	FIRST="no"
done
echo
echo
echo "Steps to summarize data:"
echo "1. Train FGBG classifier from all_label.arff file."
echo
echo "Summarize data:"
echo -n "."
$WEKA weka.classifiers.meta.FilteredClassifier -t 'all_label.arff' -d 	label.model -W weka.classifiers.trees.RandomForest -- -I 100 -K 0 -S 1
echo
echo "Completed training."
echo
echo "Use model to predict result for data:"
echo "0. Convert image to ARFF."
echo "1. Apply model."
echo "2. Create FGBG mask."
echo "3. Quantify areas."
echo "4. Transform result CSV file into column oriented CSV file."
for dir in */;
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
		echo "${dir}/${dir}.arff"
	    $JAVA.ArffFromImageFileGenerator 191 $CLASSCOUNT "${dir}"
	    	
	 	echo -n "."
	 	$WEKA weka.filters.supervised.attribute.AddClassification -i "${dir}/${dir}.arff" -serialized label.model -classification -remove-old-class -o "${dir}/result.arff" -c last
		
		echo -n "."
		#create foreground png
		cp "${dir}/channel_001.tif" "${dir}/result.tif"
		$JAVA.ArffToImageFileGenerator $CLASSCOUNT "${dir}/result.tif"
		rm "${dir}/result.tif"
		
		echo -n "."
		cp "${dir}/classified.png" "${dir}/foreground_cluster.png"
		rm -f ${dir}/*_quantified.csv
		$JAVA.Quantify ${dir}/foreground
		rm "${dir}/foreground_cluster.png"
	fi
done
echo
echo "Transform CSV"
for dir in */;
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
echo "1. WEKA Model (Random Forest) is trained: "
echo "      'fgbg.model'"
echo "2. Model has been applied to training data. Pixel-count for the different classes is calculated:"
echo "		'all_results.csv'"
echo "3. A labeled result image has been saved:"
echo "		'classfied.png'"
echo
echo READY
