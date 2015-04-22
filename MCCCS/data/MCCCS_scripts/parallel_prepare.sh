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

#if parallel is not installed use xargs instead:
par="parallel --gnu"
type parallel >/dev/null 2>&1 ||  par="xargs -n1 -P$NPROCS -I{}" 
echo "CPUS: $NPROCS"
echo "Parallel command: $par"
