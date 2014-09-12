/*
 * Copyright (C) 2014 InventIt Inc.
 * 
 * See https://github.com/inventit/moat-iot-sparki
 */

#include <Sparki.h>

#define VERSION "1.0.0"

const int BAUDRATE = 9600;//115200;
const int MAXBUFFLEN = 5;
char buff[MAXBUFFLEN];

unsigned long lastUpdateTime = 0;
int updateInterval = 1000;

boolean cmdComplete = false;
int cmdLen = 0;

void setup() {
  Serial.begin(BAUDRATE);
  sparki.RGB(RGB_GREEN);
  sparki.servo(SERVO_CENTER);
  sparki.clearLCD();
  sparki.println("Inventit ServiceSync!");
  sparki.print("Version: ");
  sparki.println(VERSION);
  sparki.updateLCD();
}

void loop() {
  if (cmdComplete) {
    String cmd = buff;
    
    sparki.print("Control: ");
    sparki.println(cmd);
    sparki.updateLCD();

    if (cmd.equalsIgnoreCase("p")) {
        sparki.beep();
    } else if (cmd.equalsIgnoreCase("f")) {
        sparki.moveForward();
    } else if (cmd.equalsIgnoreCase("b")) {
        sparki.moveBackward();
    } else if (cmd.equalsIgnoreCase("r")) {
        sparki.moveRight(30);
    } else if (cmd.equalsIgnoreCase("l")) {
        sparki.moveLeft(30);
    } else if (cmd.equalsIgnoreCase("s")) {
        sparki.moveStop();
        sparki.gripperStop();
    } else if (cmd.startsWith("si:")) {
      // sampling interval in seconds
      updateInterval = cmd.substring(3).toInt();
    }
    // ready for the next control command
    cmdComplete = false;
    cmdLen = 0;
  }
  
  int updateDiff = millis() - lastUpdateTime;
  if ((updateInterval > 0) && (updateDiff >= updateInterval)) {
    lastUpdateTime = updateDiff + lastUpdateTime;
    int val = 0;
    // 0 ... RangeInCentimeter
    val = sparki.ping();
    if (val != -1) {
      Serial.print(val);
    }
    Serial.print(",");
    // 1 ... LightCenter
    val = sparki.lightCenter();
    Serial.print(val);
    Serial.print(",");
    // 2 ... LightLeft
    val = sparki.lightLeft();
    Serial.print(val);
    Serial.print(",");
    // 3 ... LightRight
    val = sparki.lightRight();
    Serial.print(val);
    
    // terminator
    Serial.println();
  }
}

// Fixes an issue where serialEventRun() seems not to be implemented.
// http://forum.arduino.cc/index.php?PHPSESSID=7vs9f9p8ur9956s0aciaslgma4&topic=135011.msg1017291#msg1017291
void serialEventRun() {
    int num;
    while (num = Serial.available()) {
        // get the new byte:
        char inChar = (char) Serial.read(); 
        // add it to the inputString:
        buff[cmdLen++] = inChar;
        // if the incoming character is a newline, set a flag
        // so the main loop can do something about it:
        if (inChar == '\n' || cmdLen >= MAXBUFFLEN) {
            cmdLen--;
            buff[cmdLen] = '\0'; // set a terminator
            cmdComplete = true;
        }
        sparki.print("IN:");
        sparki.println(inChar);
        sparki.updateLCD();
    } 
}
