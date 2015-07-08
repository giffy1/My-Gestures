---
title: MyGestures
description: Accelerometer and Gyroscope Data Collection Application for Android Wear
author: Sean Noran
tags: accelerometer, gyroscope, Android, Android Wear, Java, gesture recognition, Machine Learning

---

Master
======

## Description

MyGestures is a data collection application for Android Wear. Its purpose is to collect easily labelled (via voice commands) motion data (accelerometer & gyroscope) for training classifiers.

## Debugging

To run the application through Android Studio, you must first set up your Android Wear device with your handheld device. Instructions on how to do this are typically available upon starting your watch for the first time. If you encounter any problems, refer to https://support.google.com/androidwear/answer/6056630?hl=en for troubleshooting.

Next, make sure you have the appropriate APIs installed. In Android Studio, click on Tools -> Android -> SDK Manager to run the SDK Manager and install the necessary SDK packages. The target SDK version is specified in both the mobile and wear build.gradle files, next to 'targetSdkVersion'. This project targets API level 22. The mobile application has a minimum SDK version 9 and the wear application has a minimum SDK version 20.

To put that into perspective, I am using a Samsung Galaxy S3 with API level 19 and a Sony Smartwatch 3 with API level 22.
Although the application is intended to work for any Android Wear device (currently only ones with a square watch face, though), it has not only been tested on a Sony Smartwatch 3.


You should by now have the Android Wear companion application installed on your handheld device. Make sure that you're wearable is connected: It will say Connected under 'Device Settings'. Then click on the cog icon in the top-right to navigate to the settings view and check 'Debugging over Bluetooth'. You should see something like

"Host: disconnected
Target: connected".

The Host refers to your computer and the target refers to your wearable. When debugging over Bluetooth, the application will be installed and all debugging communication will be established through your handheld. Now you must open a port for your computer to communicate to your wearable. This can be done by opening the command prompt in the folder where your adb.exe application resides. For me, it was in Local AppData:

"C:\Users\Sean\AppData\Local\Android\sdk\platform-tools"

But that's likely not the same for everyone! (very likely unless you are also named Sean). In the Command Prompt, enter

adb forward tcp:4444 localabstract:/adb-hub
adb connect localhost:4444 

And you should see in the companion application that both the host and target are listed as connected. Every time you disconnect your handheld device, you need to repeat these commands, and since this can get a bit annoying, I would recommend making a .bat file which simply contain these commands.

Finally to run the application, just hit run (either click the green triangle or go to Run -> Run 'wear' or Run 'mobile'. In the run panel you can select whether to run the mobile or wear application, or go to Run -> Edit Configurations to configure your run parameters.

Having trouble?

## Using the Application

This application is intended to collect motion data from a wrist-worn Android device. Run the application on BOTH the mobile and wearable devices. Currently the mobile application is an empty activity which starts a listening service that receives buffered accelerometer/gyroscope data from the wearable. Both sensors are sampled at approximately 100 Hz (although this can be adjusted through the Android sensor API). Upon receiving the buffer, the mobile application saves the data in a .csv file, which can be opened in Excel.

The first column contains the timestamps (in nanoseconds since the sensor service started on the wearable) and the remaining three columns are the x-, y- and z-values respectively. Each sensor has its own corresponding .csv file, i.e. ACCEL14362354667.csv or GYRO143574200256.csv. The numbers following the ACCEl/GYRO represent a UTC timestamp in milliseconds given by System.currentTimeMillis() at the time the file was created. This is either when the application begins or more likely upon receiving the first accelerometer/gyroscope buffer.

Lastly, there is a REPORT file, also with a corresponding timestamp in its filename. This file contains labels and corresponding timestamps. The first column is the timestamp in milliseconds when the label is recorded (when the user presses the label button, not sure if that is the best choice?); the second column is the activity; and the third column says whether the user started or stopped the activity.

## Additional

...

