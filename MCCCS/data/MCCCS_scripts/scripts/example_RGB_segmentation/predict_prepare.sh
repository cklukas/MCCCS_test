#!/bin/bash
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

if [ "$4" == "s" ];
then
	NPROCS=1
	echo "Operation Mode: single-threaded"
else
if [ "$4" == "h" ];
then
	NPROCS=$(echo "$NPROCS/2" | bc)
	echo "Operation Mode: utilizing half number of CPUs ($NPROCS)"
else
	echo "Operation Mode: multi-threaded ($NPROCS CPUs detected)"
fi
fi

#if parallel is not installed use xargs instead:
par="parallel --gnu"
#type parallel >/dev/null 2>&1 ||  
par="xargs -n1 -P$NPROCS -I {}"
echo "Parallel command: $par"
