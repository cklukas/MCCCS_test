#!/bin/bash
(( $# )) || printf '%s\n' 'No arguments given, please type -h for help.'
while getopts ":a :b :h" opt; do
  case $opt in
    a)
      echo "-a Delete results and temporary data?" >&2
			echo
			echo "Press Enter to continue, s for skip or ctrl + c to abort."
			read input
			if [ "$input" != "s" ];
			then
				echo "Clean segmentation_example_1_classification: "
				cd segmentation_example_1_classification
				rm -f **/**/foreground.png
				rm -f **/**/foreground_diff.png
				rm -f **/**/*.csv
				rm -f **/**/*.arff
				rm -f **/*.csv
				rm -f **/*.arff
				rm -f **/*.model
				echo -n "Finish!"
				cd ..
				echo
				echo "Clean hyper_example_1_classification: "
				cd hyper_example_1_classification
				rm -f **/**/channel*
				rm -f **/**/classified.png
				rm -f **/**/*.csv
				rm -f **/**/*.arff
				rm -f **/*.csv
				rm -f **/*.arff
				rm -f **/*.model
				echo -n "Finish!"
				cd ..
				echo
				echo "Clean hyper_example_2_clustering: "
				cd hyper_example_2_clustering
				rm -f **/**/channel*
				rm -f **/**/classified.png
				rm -f **/**/*.csv
				rm -f **/**/*.arff
				rm -f **/*.csv
				echo -n "Finish!"
				cd ..
			fi
		  ;;
		b)
		  echo "-b Delete downloaded data?" >&2
			echo
			echo "Press Enter to continue, s to skip or ctrl + c to abort."
			read input
			if [ "$input" != "s" ];
			then
				rm -r segmentation_example_1_classification
				rm -r hyper_example_1_classification
				rm -r hyper_example_2_clustering
				rm -r Hyperspectral_Project
				rm -r lib
				rm -r lsc_challenge
				rm -f Hyperspectral_Project.zip
				rm -r LSCData.zip
				echo "Finish!"
			fi
      ;;
		h)
			echo "-h  Help." >&2
			echo "-a  Delete results and temporary data."
			echo "-b  Delete downloaded data."
			;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
			echo "Try '-h' for more information."
      ;;
  esac
done
