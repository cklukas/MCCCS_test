#!/bin/bash	
dir=$1
dir=${dir%*/}
echo -n "[${dir}]" 

echo -n "."
rm -f ${dir}/channel_*
P=${dir}/${dir}.tif
if [[ "$(uname)" == CYGWIN* ]]
then
	P="$(cygpath -wp $P)"
fi
$JAVA.SplitTiffStackToImages $P > /dev/null
#echo rm -f ${dir}/${dir}.tif
