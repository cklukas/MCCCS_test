cd "$1"
rm -f all_results.csv
rm -f all_fgbg.arff
rm -f all_disease.arff
rm -f fgbgClassifier.data
rm -f diseaseClassifier.data
rm -f fgbg.model
#$(dirname $0)/
export MBP=../../release/macrobot.jar
echo "MacroBot jar:" $MBP
if [ "$(uname)" == "Darwin" ]
	export JAVA="java -Dapple.awt.UIElement=true -cp $MBP workflow"
then
	export JAVA="java -cp $MBP workflow"
fi 
export WEKA="java -Xmx20g -cp ../weka.jar"

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

#if parallel is not installed use xargs instead:
par="parallel --gnu"
type parallel >/dev/null 2>&1 ||  par="xargs -n1 -P$NPROCS -I{}" 
echo "CPUS: $NPROCS"
echo "Parallel command: $par"
