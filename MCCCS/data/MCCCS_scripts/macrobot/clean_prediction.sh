#!/bin/bash
echo "Delete temporary files of prediction folder."
read
cd prediction
echo "delete previous results for each subfolder: foreground*, *.arff, classified.png, *.csv"
#rm -f *.csv
for dir in */;
do
	echo -n "[${dir}]"
	
	for dir_sub in ${dir}*/
	do
			echo -n "[${dir_sub}]"
		rm -f ${dir_sub}/foreground_*
		rm -f ${dir_sub}/*.arff
		rm -f ${dir_sub}/channel*
		rm -f ${dir_sub}/classified*
		rm -f ${dir_sub}/composite.png
		#rm -f ${dir_sub}/*.csv
		rm -f ${dir_sub}/foreground*
	done
done
cd ..
exit 0
echo "Delete training results?"
read
cd training
rm -f *.csv
rm -f *.model
echo
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

