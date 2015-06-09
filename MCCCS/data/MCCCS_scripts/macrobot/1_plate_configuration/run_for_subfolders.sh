#!/bin/bash
for dir in $1/*
do
	echo "analyze $dir"
	if [ $dir == 'prediction/6dai' ]
	then
		bash predict.sh .. $dir/ m ../training/
	fi
done 
