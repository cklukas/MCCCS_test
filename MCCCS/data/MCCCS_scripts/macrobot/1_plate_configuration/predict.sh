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
    echo "Please supply the path to the mcccs.jar as parameter 1, the path to the data-files as parameter 2, s-single-threaded, m-multi-threaded as parameter 3 and path to the model files as parameter 4!"
	exit 1
fi
# stop in case of error:
set -e
#set path to preprocess (used for parallel processing)
PREDICT="$(pwd)/predict_folder.sh"
echo "$PREDICT"
if ! [[ "$(uname)" == CYGWIN* ]]
then
	chmod +x $PREDICT
	chmod +x "prepare.sh"
fi
if [ "$(uname)" == "Darwin" ]; then
realpath() {
    [[ $1 = /* ]] && echo "$1" || echo "$PWD/${1#./}"
}
fi
echo "prepare"
source prepare.sh
echo Java command: $JAVA
export MODELPATH="../$4"
echo "Path to model files:"
echo "$MODELPATH"
echo
echo "Use model to predict result for data:"
echo "(a) Create .arff file for fgbg segmentation."
echo "(b) Classify foreground (fgbg.arff)."
echo "(c) Create foreground image."
echo "(d) Split leaves."
echo "(e) Reconstruct leaf shape (side smooth)."
echo "(f) Create .arff for disease classification."
echo "(g) Classify disease.arff."
echo "(h) Create classification image."
echo "(i) Quantify disease areas."
echo
find * -maxdepth 0 -type d | grep -F -v CVS | $par $PREDICT $2 {}
echo -n "[i]"
#for dir in */;
#do
#	rm -f ${dir}/*_quantified.csv
#	$JAVA.Quantify ${dir}/classified.png
#	cat ${dir}/*_quantified.csv >> all_results.csv
#done
	echo "Transform result CSV file into column oriented CSV file."
	rm -f all_results.csv.transformed
	$JAVA.TransformCSV all_results.csv
	mv all_results.csv.transformed all_results.csv
echo
echo "Processing finished:"
echo "Result images are stored in the individual experiment folder,"
echo "numeric results are stored in 'all_results.csv'."
echo
echo READY
