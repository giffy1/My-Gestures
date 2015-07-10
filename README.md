---
title: MyGestures
description: Accelerometer and Gyroscope Data Collection Application for Android Wear
author: Sean Noran
tags: accelerometer, gyroscope, Android, Android Wear, Java, gesture recognition, Machine Learning

---

Master
======

## Description

MyGestures is a data collection application for Android Wear. Its purpose is to collect easily labelled motion data (raw accelerometer & gyroscope) for training classifiers for gesture recognition. The data is labelled using voice commands.

## Getting Started

Make sure the application is installed on both your wearable and handheld devices. If installed through the Play Store (not yet available!), then you must sync your applications using the Android Wear companion application. If installing through Android Studio, you must run the code on both the handheld and wearable. Detailed instructions on how to debug the application over Bluetooth are provided in the 'Debugging' section.

Open the application on your mobile phone. Press the start button to begin collecting data. Your wearable device will receive an ongoing notification as long as the sensors are running. On your handheld device, you will also receive an ongoing notification indicating that data is being collected and stored locally. The accelerometer and gyroscope data are stored in the ACCEL.csv and GYRO.csv files respectively, both located in the motionData folder in the Downloads directory.

A third file REPORT.csv contains the spoken labels. To report a label, swipe the wearable notification left and press the microphone icon. You must label your activity as either "before <activity>" or "after <activity>", where the activity can be anything from "eating", "drinking" or "sleeping" to "watering plants", "doing jumping jacks" or "washing my hands". If the perceived spoken text does not begin with the word "before" or the word "after", then it will be disregarded and no label will be recorded.

Ending the handheld application will NOT stop the data logging service, because the data logging service runs as a foreground service, independent from the UI. This allows users to close the application and use their phone as they normally would with little effect on its battery life. To end the service, open the application (this can also be done by clicking on the ongoing notification); then press the stop button.

Warning: There is another button to delete the data. This button will not ask for a confirmation if you click on it (I'll add one in eventually). It will delete the motionData folder and all files contained in that folder. So be careful!!

## Sampling Rate

The sensors are sampled at a rate of 60 Hz (see android.hardware.Sensor.SENSOR_DELAY_GAME). However, there is no guarantee that the sampling rate will be the requested sampling rate. This specifies the minimum sampling rate. The true sampling rate is the maximum among all processes which have registered a sensor listener to the sensor.

Using a Sony Smartwatch3 with standard configuration, the accelerometer samples at around 60 Hz, and the gyroscope samples at around 100 Hz.

See http://developer.android.com/guide/topics/sensors/sensors_overview.html

## File Format

The data is written into .csv (comma separated values) files. The accelerometer and gyroscope files are formatted in the same way: The first column indicates the timestamp (uptime in nanoseconds*). The remaining 3 columns indicate the x-, y-, and z-values.

The REPORT.csv file contains 3 columns, a timestamp (also uptime in nanoseconds), the activity and whether or not the user started or stopped the activity.

*On some devices, the timestamp does match the documentation. See http://stackoverflow.com/questions/7773954/android-sensor-timestamp-reference-time and https://code.google.com/p/android/issues/detail?id=78858

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

Finally to run the application, just hit run (either click the green triangle or go to Run -> Run 'wear' or Run 'mobile'. In the run panel you can select whether to run the mobile or wear application, or go to Run -> Edit Configurations to configure your run parameters. Since the wear module contains no Activity, make sure to select "Do not launch Activity".

Having trouble?

## Troubleshooting

Sometimes uninstalling the application from the wearable can fix some issues. To uninstall the .apk, navigate to the folder containing adb.exe in the command prompt and, assuming the android wear is connected to your computer over port 4444, enter:

adb -s localhost:4444 uninstall edu.umass.cs.mygestures

## Known Issues

The data is stored in CSV files. When a CSV file is saved manually, all long-precision fields in scientific notation are truncated. This problem has been confirmed by Microsoft at https://support.microsoft.com/en-us/kb/216023.

A workaround is to make sure to change each column containing values displayed in scientific notation is changed to be 'General Number' format.

If you don't plan on making changes to any of the files, i.e. to correct a mislabelled activity, then do not save the file, even if prompted!

## Future Work

Before the data collection application can be widely used for research, there are a few issues that should be resolved:

1. Allow user to correct mislabels (probably on handheld)
2. Add functionality to adjust sampling rate from handheld device
3. Handle low-battery:
	Should the wearable disable the sensors if low on battery?
	If so the user should receive a notification before this occurs.
	Can the wearable reduce sampling rate if low on battery?
	If the wearable turns off, the handheld service should terminate
	If the handheld turns off, the sensors should be disabled
4. Add functionality to notification button on handheld
5. Allow wearable to store data locally temporarily, when not near phone
6. UI improvements on handheld
7. Continuous speech recognition only if battery life doesn't suffer too much
8. GNU GPL?

## Additional

...

