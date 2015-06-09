$JAVA.RGB2ALL ${dir}/*rgb_r.tif ${dir}/*rgb_g.tif ${dir}/*rgb_b.tif 8 -1 4
#$JAVA.RGB2HSV ${dir}/*rgb_r.tif ${dir}/*rgb_g.tif ${dir}/*rgb_b.tif 8
#$JAVA.RGB2XYZ ${dir}/*rgb_r.tif ${dir}/*rgb_g.tif ${dir}/*rgb_b.tif 8
#$JAVA.RGB2LAB ${dir}/*rgb_r.tif ${dir}/*rgb_g.tif ${dir}/*rgb_b.tif 8

#echo -n "c"
#for img in ${dir}/channel*;
#do
	#$JAVA.FILTER ${img} ${img} 3 3 BLUR
	#$JAVA.FILTER ${img} ${img} 4 4 MEDIAN
#done