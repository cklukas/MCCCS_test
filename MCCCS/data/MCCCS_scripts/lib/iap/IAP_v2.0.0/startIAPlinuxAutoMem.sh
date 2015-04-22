#!/bin/bash
cd $(dirname $0)
java -Xmx$(echo $(free -mto | grep Mem: | awk '{ print $2 }')*4/5 | bc)M -cp iap_2_0.jar de.ipk.ag_ba.gui.webstart.IAPmain