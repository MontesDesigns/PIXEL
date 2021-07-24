#!/bin/bash
SSID=$1
PASS=$2
sudo killall -9 mplayer 2> /dev/null
timestamp() {
 declare -n ts_=$1
 ts_=`date +"%s"` # current time
}
setup_wifi() {
 SsSID=$1
PpASS=$2
sudo killall -9 gsho 2> /dev/null
/home/pi/pixelcade/gsho -platform linuxfb /home/pi/pixelcade/.assets/connectWifi.jpg &
timestamp LAST
echo "Last:$LAST"
nmcli c delete $SSID
timestamp NOW
echo "Now:$NOW"
count=$(( NOW - LAST))
echo "Now:$count"
echo "Delete SSID: $count" >> /home/pi/timerun-$START.txt
timestamp LAST
effort=$(nmcli d wifi connect ${SsSID} password ${PpASS}); echo $effort|grep "successfully activated"
if [ "$?" -ne 0 ]; then
 timestamp NOW
 count=$(( NOW - LAST ))
 echo "Connect Fail SSID: $count" >> /home/pi/timerun-$START.txt
 echo "setup error, retrying"
 killall -9 gsho 2> /dev/null
 /home/pi/pixelcade/gsho -platform linuxfb /home/pi/pixelcade/.assets/connectError.jpg &
 sleep 1
 setup_wifi $SSID $PASS
else
 return 0
fi
}
timestamp START
LAST=$START
NOW=$START
WIFISTART=$START
echo "Starting at $NOW" > /home/pi/timerun-$START.txt
echo "Starting at $NOW"
/home/pi/pixelcade/gsho -platform linuxfb /home/pi/pixelcade/.assets/startSetup.jpg &
sleep 2
killall -9 gsho 2> /dev/null
/home/pi/pixelcade/gsho -platform linuxfb /home/pi/pixelcade/.assets/stopAP.jpg &
downcomm=$(nmcli c down PixelcadeSetup); echo $downcomm|grep "successfully deactivated"
if [ "$?" -ne 0 ]; then
 echo "setup error, retrying"
 killall -9 gsho 2> /dev/null
 /home/pi/pixelcade/gsho -platform linuxfb /home/pi/pixelcade/.assets/setupWifiFail.jpg &
 exit -1
fi
 sleep 2
timestamp NOW
count=$(( NOW - LAST ))
echo "Shutdown AP: $count" >> /home/pi/timerun-$START.txt
sudo modprobe rtl8xxxu; sleep 1
nmcli -f in-use,ssid,bssid,signal,bars dev wifi > /dev/null
killall -9 gsho 2> /dev/null
/home/pi/pixelcade/gsho -platform linuxfb /home/pi/pixelcade/.assets/connectWifi.gif &
setup_wifi $SSID $PASS
if [ "$?" -eq 0 ]; then
timestamp tmestamp
wifiDone=$(( tmestamp - WIFISTART ))
echo "Wifi Setup Completion:$wifiDone" >> /home/pi/timerun-$START.txt
timestamp LAST
 nmcli c mod $SSID autoconnect yes
killall -9 gshow 2> /dev/null
/home/pi/pixelcade/gsho -platform linuxfb /home/pi/pixelcade/.assets/setupWifiOK.jpg &
sleep 2
sudo cp /home/pi/pixelcade/system/modules.conf /etc/modules-load.d/
sudo reboot
else
 killall -9 gsho 2> /dev/null
 /home/pi/pixelcade/gsho -platform linuxfb /home/pi/pixelcade/.assets/wifiFail.jpg &
 return -1
fi
