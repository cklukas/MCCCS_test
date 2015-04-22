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
PRES="bash $(pwd)/preprocess_hyper_cluster.sh"
ADDCLU=$(pwd)/add_cluster.sh
if ! [[ "$(uname)" == CYGWIN* ]]
then
	chmod +x $ADDCLU
fi
if [ "$(uname)" == "Darwin" ]; then
realpath() {
    [[ $1 = /* ]] && echo "$1" || echo "$PWD/${1#./}"
}
fi
CLASSCOUNT=$3
source prepare.sh
echo Java command: $JAVA
echo
echo "Start clustering."
echo
echo "Use model to predict result for data:"
echo "1. Convert image to ARFF."
echo "2. Apply clustering."
echo "3. Create Fcluster image."
echo "4. Quantify areas."
echo "5. Transform result CSV file into column oriented CSV file."
echo -n "."
#ls -1 -d */ | grep -v CVS | $par "$PRES" {}
find * -maxdepth 0 -type d | grep -F -v CVS | $par $PRES {}
for dir in */;
do
	if  [ "$dir" = "CVS" ]; then
		echo
		echo "Ignore CVS directory."
 	else
		echo
	    dir=${dir%*/}
	    #echo -n "[${dir}]"  
	    
	    echo -n "."
	   	rm -f "${dir}/${dir}.arff"
	    $JAVA.ArffFromImageFileGenerator 191 $3 "${dir}"
	    	
	 	echo -n "."
		#resample data for training
		$WEKA weka.filters.unsupervised.instance.Resample -S 1 -Z 0.05 < "${dir}/${dir}.arff" > "${dir}/${dir}_resampled.arff"
		#train clustering on resampled data, ignoring class attribute
		$WEKA weka.clusterers.EM -I 10 -N $CLASSCOUNT -t "${dir}/${dir}_resampled.arff" -T "${dir}/${dir}.arff" -p 0 > "${dir}/${dir}_predict.arff"
#$WEKA weka.filters.unsupervised.attribute.AddCluster -W "weka.clusterers.SimpleKMeans -N 4 -S 5" -I last -i "${dir}/${dir}_resampled.arff" -o "${dir}/${dir}_trained_clustering.arff"
	 	#$WEKA weka.filters.supervised.attribute.AddClassification -i "${dir}/${dir}.arff" -serialized fgbg.model -classification -remove-old-class -o "${dir}/result.arff" -c last
		#$WEKA weka.filters.unsupervised.attribute.AddCluster -W "weka.clusterers.SimpleKMeans -N 4 -S 5" -I last -i "${dir}/${dir}.arff" -o "${dir}/result.arff"
#$WEKA weka.filters.unsupervised.attribute.AddCluster -i "${dir}/${dir}.arff" -serialized fgbg.model -o "${dir}/result.arff" -c last
		$ADDCLU "${dir}/${dir}.arff" "${dir}/${dir}_predict.arff" "${dir}/result.arff"
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
echo "1. Clusterer (EM) has been applied."
echo "2. Pixel-count for the different classes is calculated:"
echo "      'all_results.csv'"
echo "3. A labeled result image has been saved:"
echo "		'clustered.png'"
echo
echo READY
