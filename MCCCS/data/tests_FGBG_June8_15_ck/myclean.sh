#!/bin/bash
echo "Delete temporary files of prediction folder? (Enter=OK, Ctrl+C=Cancel)"
read
	find . -type f -name "foreground*" -delete
	find . -type f -name "channel*" -delete
	find . -type f -name "*arff" -delete
	find . -type f -name "classified" -delete
	find . -type f -name "*csv" -delete
	find . -type f -name "label*" -delete
	find . -type f -name "quantified*" -delete
	find . -type f -name "*model" -delete
echo "READY"

