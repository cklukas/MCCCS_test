#!/bin/bash
echo "Delete temporary files of prediction folder."
cd prediction
echo "delete previous results for each subfolder: foreground*, *.arff, classified.png, *.csv"
rm -f *.csv
for dir in */;
do
	echo -n "[${dir}]" 
	rm -f ${dir}/foreground*
	rm -f ${dir}/*.arff
	rm -f ${dir}/classified.png
	rm -f ${dir}/*.csv
done
cd ..
cd training
rm -f *.csv
rm -f *.model
echo "Delete temporary files of training folder."
for dir in */;
do
	dir=${dir%*/}
	echo -n "[${dir}]" 
	#echo
		#dir=${dir%*/}
	#echo -n "'${dir}'"

	#echo
	#echo -n "Delete files "
	#echo "rm -f ${dir}/*.arff"
	rm -f ${dir}/*.arff
	echo -n "."
	#echo "rm -f ${dir}/foreground*"
	rm -f ${dir}/foreground*
	echo -n "."
	#echo "rm -f ${dir}/label*"
	rm -f ${dir}/label*
	echo -n "."
	#echo "rm -f ${dir}/classified*"
	rm -f ${dir}/classified*
	echo -n "."
	#echo "rm -f ${dir}/quantified*"
	rm -f ${dir}/quantified*
	echo -n ". "
	rm -f ${dir}/*.model
	echo -n ". "
	rm -f ${dir}/*.csv
	echo -n ". "
done
echo "Finish deletion!"

