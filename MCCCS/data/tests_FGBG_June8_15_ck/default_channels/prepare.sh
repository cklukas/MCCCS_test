if [ "$#" -ne 4 ]; then
    echo "Please supply the path to the mcccs.jar as parameter 1, the path to the data-files as parameter 2 and s-single-threaded, m-multi-threaded as parameter 3, description of data set to display progress as parameter 4!"
	exit 1
fi
APPPATH=$(realpath $1)
export MBP="$APPPATH/mcccs.jar:$APPPATH/lib/iap.jar"
if [[ "$(uname)" == CYGWIN* ]]
then
	MBP=$(cygpath -mp $MBP)
fi

cd "$2"
rm -f all_results.csv
rm -f all_fgbg.arff
rm -f all_disease.arff
rm -f fgbgClassifier.data
rm -f diseaseClassifier.data
rm -f fgbg.model
#echo "mcccs jar:" $MBP
if [ "$(uname)" == "Darwin" ]; then
	export JAVA="java -Xmx8g -Dapple.awt.UIElement=true -cp $MBP workflow"
else
	export JAVA="java -Xmx8g -cp $MBP workflow"
fi 
WEKAJAR=$APPPATH/lib/weka.jar
if [[ "$(uname)" == CYGWIN* ]]
then
	WEKAJAR=$(cygpath -wp $WEKAJAR)
fi
export WEKA="java -Xmx8g -cp $WEKAJAR"

NPROCS=1
OS=$(uname -s)

if [ "$OS" == "Linux" ];
then
	NPROCS=$(grep -c ^processor /proc/cpuinfo)
fi
if [ "$OS" == "Darwin" ]; 
then
	NPROCS=$(sysctl -n hw.ncpu)
fi
if [[ "$(uname)" == CYGWIN* ]]
then
	NPROCS=$(wmic cpu get NumberOfLogicalProcessors | sed -n -e '2{p;q}' | sed ':a;N;$!ba;y/\n/ /' | sed 's/ //g')
fi
echo "Requested Operation Mode: $3"
if [ "$3" == "s" ];
then
	NPROCS=1
	echo "Operation Mode: single-threaded"
else
if [ "$3" == "h" ];
then
	NPROCS=$(echo "$NPROCS/2" | bc)
	echo "Operation Mode: utilizing half number of CPUs ($NPROCS)"
else
	echo "Operation Mode: multi-threaded ($NPROCS CPUs detected)"
fi
fi

export SYSTEM_cpu_n=$NPROCS

#if parallel is not installed use xargs instead:
par="parallel --gnu"
#type parallel >/dev/null 2>&1 ||  
par="xargs -n1 -P$NPROCS -I{}" 
echo "Parallel command: $par"
