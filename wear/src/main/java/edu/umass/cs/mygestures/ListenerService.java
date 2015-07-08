package edu.umass.cs.mygestures;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class ListenerService extends WearableListenerService {
    private static final String TAG = ListenerService.class.getName();

    private static final String START_SENSOR_SERVICE = "START_SENSOR_SERVICE"; //TODO: Used in both mobile/wear: make common
    private static final String STOP_SENSOR_SERVICE = "STOP_SENSOR_SERVICE";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        //Log.d(TAG, "Received message: " + messageEvent.getPath());

        if (messageEvent.getPath().equals(START_SENSOR_SERVICE)) {
            startService(new Intent(this, SensorService.class));
        }

        if (messageEvent.getPath().equals(STOP_SENSOR_SERVICE)) {
            stopService(new Intent(this, SensorService.class));
        }
    }
}