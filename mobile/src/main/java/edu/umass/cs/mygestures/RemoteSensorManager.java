package edu.umass.cs.mygestures;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * TODO: Create RemoteSensorService instead, which extends Service: When the
 * service starts, connect to the Google API client and send the start sensor
 * service command to the wearable; when the remote sensor service ends, tell
 * the wearable to stop the sensor service. This will allow us to build a
 * custom notification to inform the user that the wearable is collecting sensor data.
 */
public class RemoteSensorManager {
    private static final String TAG = RemoteSensorManager.class.getName();
    private static final int CLIENT_CONNECTION_TIMEOUT = 5000; //wait 5 secs

    private static RemoteSensorManager instance;

    private ExecutorService executorService;
    private GoogleApiClient googleApiClient;

    //TODO: Used in both mobile/wear: make common
    private static final String START_SENSOR_SERVICE = "START_SENSOR_SERVICE";
    private static final String STOP_SENSOR_SERVICE = "STOP_SENSOR_SERVICE";

    public static synchronized RemoteSensorManager getInstance(Context context) {
        if (instance == null) {
            instance = new RemoteSensorManager(context.getApplicationContext());
        }

        return instance;
    }

    private RemoteSensorManager(Context context) {

        this.googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();

        this.executorService = Executors.newCachedThreadPool();
    }

    private boolean validateConnection() {
        if (googleApiClient.isConnected()) {
            return true;
        }

        ConnectionResult result = googleApiClient.blockingConnect(CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);

        return result.isSuccess();
    }

    public void startSensorService() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "Start Sensor Service");
                sendMessageInBackground(START_SENSOR_SERVICE);
            }
        });
    }

    public void stopSensorService() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "Stop Sensor Service");
                sendMessageInBackground(STOP_SENSOR_SERVICE);
            }
        });
    }

    private void sendMessageInBackground(final String path) {
        if (validateConnection()) {
            List<Node> nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await().getNodes();

            for (Node node : nodes) {
                Log.i(TAG, "add node " + node.getDisplayName());
                Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), path, null).
                        setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                            @Override
                            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                Log.d(TAG, "startOrStopInBackground(" + path + "): " + sendMessageResult.getStatus().isSuccess());
                            }
                        });
            }
        } else {
            Log.w(TAG, "No connections available");
        }
    }
}
