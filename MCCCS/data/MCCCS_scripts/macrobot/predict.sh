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
echo "prepare"
source prepare.sh
echo Java command: $JAVA
MODELPATH="../$4"
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
for dir in */;
do
	echo
    dir=${dir%*/}
    	echo -n "Process directory '${dir}': "

	echo
	echo "delete previous results: foreground*, *.arff, classified.png"
	rm -f ${dir}/foreground*
	rm -f ${dir}/*.arff
	rm -f ${dir}/classified.png
	echo

	#rename and copy
	cp -n "${dir}"/*red.tif "${dir}/channel_0.tif"
	cp -n "${dir}"/*green.tif "${dir}/channel_1.tif"
	cp -n "${dir}"/*blue.tif "${dir}/channel_2.tif"
	cp -n "${dir}"/*uv.tif "${dir}/channel_3.tif"

	echo -n "[a]"
	$JAVA.ArffFromImageFileGenerator 4 2 "${dir}"

 	echo -n "[b] "${dir}/${dir}_2.arff""
	$WEKA weka.filters.supervised.attribute.AddClassification -i "${dir}/${dir}_2.arff" -serialized $MODELPATH/fgbg.model -classification -remove-old-class -o "${dir}/fgbgresult.arff" -c last

	#create foreground png
	cp "${dir}/channel_0.tif" "${dir}/fgbgresult.tif"
	$JAVA.ApplyClass0ToImage "${dir}/fgbgresult.tif"
	rm "${dir}/fgbgresult.tif"
	
	echo -n "[c]"
	$JAVA.ApplyMask ${dir}/foreground.png ${dir}/roi.png

	echo -n "[d]"
	$JAVA.Split ${dir}/foreground_roi.png

	echo -n "[e]"
	$JAVA.SideSmooth ${dir}/foreground_roi_

	echo -n "[f]"
	$JAVA.ArffFromImageFileGenerator 4 11 "${dir}"

	echo -n "[g]"
	$WEKA weka.filters.supervised.attribute.AddClassification -i "${dir}/${dir}_11.arff" -serialized $MODELPATH/label.model -classification -remove-old-class -o "${dir}/labelresult.arff" -c last

	echo -n "[h]"
	cp ${dir}/foreground_roi_smooth_all.png "${dir}/labelresult.png"
	$JAVA.ArffToImageFileGenerator 11 "${dir}/labelresult.png"
	rm "${dir}/labelresult.png"

	echo -n "[i]"
	rm -f ${dir}/*_quantified.csv
	$JAVA.Quantify ${dir}/classified.png
	cat ${dir}/*_quantified.csv >> all_results.csv
done
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
