if [ "$#" -ne 3 ]; then
    echo "Please supply the path to the mcccs.jar as parameter 1, the path to the data-files as parameter 2 and the number of disired cluster-classes as parameter 3!"
	exit 1
fi
APPPATH=$(realpath $1)
export MBP="$APPPATH/mcccs.jar:$APPPATH/lib/iap.jar:$APPPATH/lib/bio.jar"
if [[ "$(uname)" == CYGWIN* ]]
then
	MBP=$(cygpath -mp $MBP)
fi

cd "$2"
rm -f all_results.csv
rm -f all_label.arff
rm -f labelClassifier.data
rm -f label.model


echo "MCCCS jar:" $MBP
if [ "$(uname)" == "Darwin" ]; then
	export JAVA="java -Xmx8g -Dapple.awt.UIElement=true -Djava.awt.headless=true -cp $MBP workflow"
else
	export JAVA="java -Xmx8g -classpath $MBP workflow"
fi 
WEKAJAR=$APPPATH/lib/weka.jar
if [[ "$(uname)" == CYGWIN* ]]
then
	WEKAJAR=$(cygpath -mp $WEKAJAR)
fi
if [ "$(uname)" == "Darwin" ]; then
	export WEKA="java -Xmx8g -Dapple.awt.UIElement=true -Djava.awt.headless=true -cp $WEKAJAR"
else
	export WEKA="java -Xmx8g -cp $WEKAJAR"
fi 

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
export SYSTEM_cpu_n=$NPROCS

#if parallel is not installed use xargs instead:
par="parallel --gnu"
#type parallel >/dev/null 2>&1 ||  
par="xargs -n1 -P$NPROCS -I{}" 
echo "CPUS: \"$NPROCS\""
echo "Parallel command: $par"
