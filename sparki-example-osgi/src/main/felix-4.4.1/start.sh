#!/bin/sh

#
# Copyright (C) 2014 InventIt Inc.
# 
# See https://github.com/inventit/moat-iot-sparki
#

ARCH=64 # or 32
APP_HOME=../home

CP=bin:bin/felix.jar:$APP_HOME/config
MAIN=org.apache.felix.main.Main
APP_HOME_PATH="-Durn:inventit:dmc:java:home-path=$APP_HOME"
LD_LIB_PATH="-Durn:inventit:dmc:java:library-path=lib$ARCH"

CMD="java -cp $CP $APP_HOME_PATH $LD_LIB_PATH $MAIN"

echo $CMD
$CMD
