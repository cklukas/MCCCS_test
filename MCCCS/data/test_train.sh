#!/bin/bash
echo "***********************************************************************"
echo "***********************************************************************"
echo "**                                                                   **"
echo "**         Welcome to the 'Leaf Disease Classifier System'           **"
echo "**                                                                   **"
echo "***********************************************************************"
echo "***********************************************************************"
echo "**                                                                   **"
echo "**            V1.0 developed in January and February 2015            **"
echo "**          by the following members of the Research Group           **"
echo "**                                                                   **"
echo "**            ++++++++++++  Image Analysis  ++++++++++++             **"
echo "**            +                                        +             **"
echo "**            +  Head of group:                        +             **"
echo "**            +     Dr. Christian Klukas               +             **"
echo "**            +                                        +             **"
echo "**            +  Scientific Assistant:                 +             **"
echo "**            +     Jean-Michel Pape                   +             **"
echo "**            +                                        +             **"
echo "**            ++++++++++++++++++++++++++++++++++++++++++             **"
echo "**                                                                   **"
echo "***********************************************************************"
echo	
rm -f all_results.csv
for dir in training_*/;
do
    dir=${dir%*/}
 	echo Create FGBG mask 'mbw.png'.
	java -cp ../release/macrobot.jar workflow.ClassifyFGBG ${dir}
	
	echo Split '*.png' images for different leaves.
	java -cp ../release/macrobot.jar workflow.Split ${dir}/mbw.png

	echo Smooth leaves.side borders of '*_*.png' images.
	java -cp ../release/macrobot.jar workflow.SideSmooth ${dir}/mbw_

	echo Use trained 'disease.data' knowledge to detect disease areas.
	java -cp ../release/macrobot.jar workflow.ClassifyDisease ${dir}/mbw_

	echo Quantify disease areas.
	java -cp ../release/macrobot.jar workflow.Quantify ${dir}/mbw_
	cat "${dir}/quantified.csv" >> all_results.csv
done

echo "Processing finished!"