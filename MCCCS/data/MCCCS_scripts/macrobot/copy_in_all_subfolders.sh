#!/bin/bash
for dir in **/**/
do
	echo "copy into $dir"
	cp $1 $dir/$1
done
