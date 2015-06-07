#!/bin/bash
echo
echo "°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°"
echo "°                                                                   °"
echo "°             Prediction for un-classified data of $5               °"
echo "°                                                                   °"
echo "°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°"

# stop in case of error:
set -e
PF="$(pwd)/predict_folder.sh"
if ! [[ "$(uname)" == CYGWIN* ]]
then
	chmod +x $PF
fi
if [ "$(uname)" == "Darwin" ]; then
realpath() {
    [[ $1 = /* ]] && echo "$1" || echo "$PWD/${1#./}"
}
fi
if [ "$#" -ne 5 ]; then
    	echo "Illegal number of parameters: 1 - path to mcccs.jar, 2 - path to testing data folder, 3 - model file and s-single-threaded, m-multi-threaded as parameter 4, description of data set to improve progress output as parameter 5"
	exit 1
fi
APPPATH=$(realpath $1)
export MODEL=$(realpath $3)
WEKAJAR=$APPPATH/lib/weka.jar
if [[ "$(uname)" == CYGWIN* ]]
then
	WEKAJAR=$(cygpath -mp $WEKAJAR)
	export MODEL=$(cygpath ../$3)
fi
source predict_prepare.sh
#echo $WEKAJAR
export MBP="$APPPATH/mcccs.jar:$APPPATH/lib/iap.jar"
if [[ "$(uname)" == CYGWIN* ]]
then
	MBP=$(cygpath -mp $MBP)
fi
#echo "MCCCS jar & IAP jar:" $MBP
export JAVA="java -Xmx8g -cp $MBP workflow"
export WEKA="java -Xmx8g -cp $WEKAJAR"
if [ "$(uname)" == "Darwin" ]; then
	JAVA="java -Xmx8g -Dapple.awt.UIElement=true -cp $MBP workflow"
	WEKA="java -Xmx8g -Dapple.awt.UIElement=true -cp $WEKAJAR"
fi 
echo "Path to model file: $MODEL"
WORKDIR=$(realpath $2)
cd "$WORKDIR"
echo
echo "Delete files:"
echo "'all_prediction_results.csv'"
rm -f all_prediction_results.csv
FIRST=yes
echo Java command: $JAVA
echo Weka command: $WEKA
echo
echo "Use model to predict result for data:"
echo "(a) Split RGB channels."
echo "(b) Convert RGB to HSV, XYZ, LAB to extend channel list."
echo "(c) Apply filter (blur, median) to extend channel list."
echo "(d) Create ARFF files from images."
echo "(e) Use trained 'fgbg.model' knowledge to detect fgbg areas."
echo "(f) Apply mask to result image."
echo "(g) Quantify predicted areas."
echo
echo "[Depending on the number of images, the prediction phase may take a longer time]"
echo
#ls -1 -d */ | grep -v CVS | $par $PF $2 {}
find * -maxdepth 0 -type d | grep -F -v CVS | $par $PF $WORKDIR {}
echo
echo "Transform result CSV file into column oriented CSV file..."
rm -f all_prediction_results.csv.transformed
$JAVA.TransformCSV all_prediction_results.csv
mv all_prediction_results.csv.transformed all_prediction_results.csv
echo
echo "Processing finished:"
echo "Model has been applied to training data. Disease infection rate is calculated:"
echo "      'all_prediction_results.csv'"
echo
echo READY
