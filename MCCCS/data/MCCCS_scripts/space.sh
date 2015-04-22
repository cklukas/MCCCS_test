SOURCE="${BASH_SOURCE[0]}"
SOURCE=$(realpath $SOURCE)
case $SOURCE in  
     *\ * )
		   echo "Problematic script location: "$SOURCE
		   echo
		   echo "Currently, the MCCCS system needs to be placed and saved in"
		   echo "a directory structure without any SPACE character in the name."
		   echo
		   echo "Script execution needs to be stopped - EXIT 1"
	   exit 1
          ;;
       *)
          ;;
esac