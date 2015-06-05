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
if [ "$#" -ne 3 ]; then
    echo "Please supply the path to the mcccs.jar as parameter 1, the path to the data-files as parameter 2 and s-single-threaded, m-multi-threaded as parameter 3!"
	exit 1
fi
# stop in case of error:
set -e
#set path to preprocess (used for parallel processing)
PRES="$(pwd)/preprocess.sh"
echo "$PRES"
if ! [[ "$(uname)" == CYGWIN* ]]
then
	chmod +x $PRES
	chmod +x "prepare.sh"
fi
if [ "$(uname)" == "Darwin" ]; then
realpath() {
    [[ $1 = /* ]] && echo "$1" || echo "$PWD/${1#./}"
}
fi
echo "prepare"
source prepare.sh
FIRST=yes
echo Java command: $JAVA
echo
echo "Steps per directory:"
echo "(a) Generate Zero-Mask and PowerSet images from training ground-truth data files."
echo "(b) Generate ARFF files from training data." 
echo "(c) Create (first) or extend (following dirs) 'all_fbgb.arff' and 'all_disease.arff'."
echo
find * -maxdepth 0 -type d | grep -F -v CVS | $par $PRES $2 {}
echo
for dir in */;
do
	echo -n "[c]"
	if [ $FIRST == "yes" ];
	then
		# add complete file, including header
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
echo "Steps for classifier training:"
echo "(a) Train FGBG classifier using all_fgbg.arff file."
echo "(b) Train disease classifier using all_disease.arff file."
echo "Summarize data:"
echo -n "[a]"
$WEKA weka.classifiers.meta.FilteredClassifier -t 'all_fgbg.arff' -d fgbg.model -W weka.classifiers.trees.RandomForest -- -I 200
echo -n "[b]"
$WEKA weka.classifiers.meta.FilteredClassifier -t 'all_label.arff' -d label.model -W weka.classifiers.trees.RandomForest -- -I 100
echo
echo "Completed training."
echo
echo "Use model to predict result for data:"
echo "(a) Create .arff file for fgbg segmentation."
echo "(b) Classify foreground (fgbg.arff)."
echo "(c) Create foreground image (and composite if possible)."
echo "(d) Split leaves."
echo "(e) Reconstruct leaf shape (side smooth)."
echo "(f) Create .arff for disease classification."
echo "(g) Classify disease.arff."
echo "(h) Create classification image."
echo "(i) Quantify disease areas."
for dir in */;
do
	echo
    dir=${dir%*/}
    	echo -n "Process directory '${dir}': "

	echo -n "[a]"
	$JAVA.ArffFromImageFileGenerator 4 2 "${dir}"

 	echo -n "[b]"
	$WEKA weka.filters.supervised.attribute.AddClassification -i "${dir}/${dir}_2.arff" -serialized fgbg.model -classification -remove-old-class -o "${dir}/fgbgresult.arff" -c last

	#create foreground png
	cp "${dir}/channel_0.tif" "${dir}/fgbgresult.tif"
	$JAVA.ApplyClass0ToImage "${dir}/fgbgresult.tif"
	rm "${dir}/fgbgresult.tif"
	
	echo -n "[c]"
	$JAVA.ApplyMask ${dir}/foreground.png ${dir}/roi.png

	echo -n "[d]"
	$JAVA.Split ${dir}/foreground_roi.png
	$JAVA.MakeRGBComposite ${dir}

	echo -n "[e]"
	$JAVA.SideSmooth ${dir}/foreground_roi_

	echo -n "[f]"
	$JAVA.ArffFromImageFileGenerator 4 3 "${dir}"

	echo -n "[g]"
	$WEKA weka.filters.supervised.attribute.AddClassification -i "${dir}/${dir}_3.arff" -serialized label.model -classification -remove-old-class -o "${dir}/labelresult.arff" -c last

	echo -n "[h]"
	cp ${dir}/foreground_roi_smooth_all.png "${dir}/labelresult.png"
	$JAVA.ArffToImageFileGenerator 3 "${dir}/labelresult.png"
	rm "${dir}/labelresult.png"

	echo -n "[i]"
	rm -f ${dir}/*_quantified.csv
	$JAVA.Quantify ${dir}/classified.png
	cat ${dir}/*_quantified.csv >> all_results.csv
done
	echo
	echo "Transform result CSV file into column oriented CSV file."
	rm -f all_results.csv.transformed
	$JAVA.TransformCSV all_results.csv
	mv all_results.csv.transformed all_results.csv
echo
echo "Processing finished:"
echo
echo READY
