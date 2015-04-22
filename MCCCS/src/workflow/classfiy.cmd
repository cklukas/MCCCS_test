echo Delete existing CSV file (if existing)
rm all_results.csv

while read -r dir
do
	echo Process directory '${dir}'.
	echo.
	echo Generate ARFF files from training data....
	java -cp iap.jar workflow.ArffSampleFileGenerator ${dir}

	echo Split images for different leaves...
	java -cp iap.jar workflow.Split ${dir}/XYZ.png

	echo Smooth leaves.side borders...
	java -cp iap.jar workflow.SideSmooth ${dir}/XYZ_*.png

	echo Quantify disease areas...
	java -cp iap.jar workflow.Quantify ${dir}/XYZ_*_smooth.png

	echo Merge result CSV files...
	cat  ${dir}/*.csv >> all_results.cvs
    something
done < <(find . -type d)