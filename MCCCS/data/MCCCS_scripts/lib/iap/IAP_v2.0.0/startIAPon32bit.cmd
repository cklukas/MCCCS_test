@echo off
cd /d %~dp0
echo.
echo WARNING: Using low memory for compatibiliy with 32-bit
echo operating systems. Not all functions (e.g. data analysis)
echo may work correctly.
echo.
pause
echo.
echo About to start IAP with 1100m memory utilization...
java -Xmx1100m -cp iap_2_0.jar de.ipk.ag_ba.gui.webstart.IAPmain 
echo.
echo IAP execution has finished.
pause