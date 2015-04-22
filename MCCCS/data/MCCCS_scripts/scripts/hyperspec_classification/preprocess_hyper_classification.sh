#!/bin/bash	
dir=$1
dir=${dir%*/}
#echo -n "[${dir}]" 
set -e
echo -n "."
rm -f ${dir}/channel_*
P=${dir}/${dir}.tif
if [[ "$(uname)" == CYGWIN* ]]
then
	P="$(cygpath -wp $P)"
fi
$JAVA.SplitTiffStackToImages $P > /dev/null
#echo rm -f ${dir}/${dir}.tif
echo -n "."
cp ../fg* ${dir}/fg.png
cp ../bg* ${dir}/bg.png
cp ../label* ${dir}/
rm -f "${dir}/Training.arff"
rm -f "${dir}/mask_1.png"
rm -f "${dir}/mask_2.png"
cat ${dir}/fg* > ${dir}/mask_1.png
cat ${dir}/bg* > ${dir}/mask_2.png  

$JAVA.ArffSampleFileGenerator 191 $CLASSCOUNT 100 "${dir}"

