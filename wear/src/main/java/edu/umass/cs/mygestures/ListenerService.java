package edu.umass.cs.mygestures;

import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import edu.umass.cs.shared.SharedConstants;

/**
 * The Listener Service is responsible for handling messages received from the handheld device.
 * Currently, this includes only commands to start and stop the sensor service, but it could
 * also include commands to change the sampling rate, or provide some sort of notification on
 * the wearable device.
 */
public class ListenerService extends WearableListenerService {
    /** used for debugging purposes */
    private static final String TAG = ListenerService.class.getName();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(SharedConstants.COMMANDS.START_SENSOR_SERVICE)) {
            Intent startServiceIntent = new Intent(this, SensorService.class);
            startServiceIntent.setAction(Constants.ACTION.START_SERVICE);
            startService(startServiceIntent);
        }

        if (messageEvent.getPath().equals(SharedConstants.COMMANDS.STOP_SENSOR_SERVICE)) {
            Intent stopServiceIntent = new Intent(this, SensorService.class);
            stopServiceIntent.setAction(Constants.ACTION.STOP_SERVICE);
            startService(stopServiceIntent);

            //note: we call startService() instead of stopService() and pass in an intent with the stop service action,
            //so that the service can unregister the sensors and do anything else it needs to do and then call stopSelf()
        }
    }
}