#!/bin/bash
#author Jean-Michel Pape & Christian Klukas
if [[ ! -f space.sh ]] ; then
    echo 'File "space.sh" is not there, aborting.'
    exit
fi
source space.sh
echo "Continue download of the following datasets and libraries?"
echo "1. https://engineering.purdue.edu/~biehl/Hyperspectral_Project.zip"
echo "2. http://www.plant-phenotyping.org/lw_resource/cvppp_dataset/LSCData.zip"
echo "3. http://sourceforge.net/projects/iapg2p/files/v2.0/IAP_v2.0.0.zip/download"
echo "4. http://prdownloads.sourceforge.net/weka/weka-3-6-12.zip"
echo "5. http://downloads.openmicroscopy.org/bio-formats/5.0.8/artifacts/bioformats_package.jar"
echo "If the download fails (proxy/firewall), please use your webbrowser for download or modify this script."
echo "Press Enter to continue or ctrl + c to abort."
read

echo "download example data"
wget -N "http://engineering.purdue.edu/~biehl/Hyperspectral_Project.zip"
echo "unzip downloaded zip file 1 ..."
unzip -o Hyperspectral_Project.zip
echo "create working directory 'hyper_example_1_classification' ..."
mkdir -p hyper_example_1_classification
cp Hyperspectral_Project/dc.tif hyper_example_1_classification/dc.tif
echo 
echo "create working directory 'hyper_example_2_clustering' ..."
mkdir -p hyper_example_2_clustering
cp Hyperspectral_Project/dc.tif hyper_example_2_clustering/dc.tif
wget -N "http://www.plant-phenotyping.org/lw_resource/cvppp_dataset/LSCData.zip"
echo "create working directory 'lsc_challenge' ..."
mkdir -p lsc_challenge
cp LSCData.zip lsc_challenge/LSCData.zip
cd lsc_challenge
unzip -o -q LSCData.zip
rm LSCData.zip
cd ..
echo "prepare A1"
mkdir -p segmentation_example_1_classification/A1_training
mkdir -p segmentation_example_1_classification/A1_prediction
cp lsc_challenge/A1/*00* segmentation_example_1_classification/A1_training
cp lsc_challenge/A1/* segmentation_example_1_classification/A1_prediction

echo "prepare A2"
mkdir -p segmentation_example_1_classification/A2_training
mkdir -p segmentation_example_1_classification/A2_prediction
cp lsc_challenge/A2/*00* segmentation_example_1_classification/A2_training
cp lsc_challenge/A2/* segmentation_example_1_classification/A2_prediction

echo "prepare A3"
mkdir -p segmentation_example_1_classification/A3_training
mkdir -p segmentation_example_1_classification/A3_prediction
cp lsc_challenge/A3/*00* segmentation_example_1_classification/A3_training
cp lsc_challenge/A3/* segmentation_example_1_classification/A3_prediction

echo "Download libs: IAP, WEKA, Bio-Formats ..."

mkdir -p lib/iap
mkdir -p lib/weka

cd lib/iap/
wget -N "http://sourceforge.net/projects/iapg2p/files/v2.0/IAP_v2.0.0.zip/download" 
cp download iap.zip
cd ../..
cd lib/weka/
wget -N "http://prdownloads.sourceforge.net/weka/weka-3-6-12.zip" 
cp weka-3-6-12.zip weka.zip
cd ../..
cd lib/
wget -N "http://downloads.openmicroscopy.org/bio-formats/5.0.8/artifacts/bioformats_package.jar" 
cp bioformats_package.jar bio.jar
cd ..
cd lib/iap
unzip -o -q iap.zip
cp IAP_v2.0.0/iap_2_0.jar ../iap.jar

cd ..

cd weka
unzip -o -q weka.zip
cd weka-3-6-12
cp weka.jar ../../weka.jar

cd ../../..
pwd
echo "copy scripts into example folders"
cp scripts/hyperspec_classification/* hyper_example_1_classification
cp scripts/hyperspec_clustering/* hyper_example_2_clustering
cp scripts/example_RGB_segmentation/* segmentation_example_1_classification

echo "Move all images and ground-truth images into subfolders for training and prediction."
#segmentation exp
cd segmentation_example_1_classification/A1_training
../move_all_to_subdir.sh _label.png
../move_all_to_subdir.sh _rgb.png
cd ..

cd A1_prediction
rm -f *label.png
../move_all_to_subdir.sh _rgb.png
cd ..

cd A2_training
../move_all_to_subdir.sh _label.png
../move_all_to_subdir.sh _rgb.png
cd ..

cd A2_prediction
rm -f *label.png
../move_all_to_subdir.sh _rgb.png
cd ..

cd A3_training
../move_all_to_subdir.sh _label.png
../move_all_to_subdir.sh _rgb.png
cd ..

cd A3_prediction
rm -f *label.png
../move_all_to_subdir.sh _rgb.png
cd ../..

#classification hyper
cd hyper_example_2_clustering
mkdir -p stack_images
cp dc.tif stack_images/dc.tif
cd stack_images
../move_all_to_subdir.sh tif
cd ../..

#classification hyper
cd hyper_example_1_classification
mkdir -p stack_images
cp dc.tif stack_images/dc.tif
cd stack_images
../move_all_to_subdir.sh tif
cd ../..

echo "Finish dataset preparation!"
