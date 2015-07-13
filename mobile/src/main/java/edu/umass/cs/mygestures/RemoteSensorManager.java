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

import edu.umass.cs.shared.SharedConstants;

/**
 * The Remote Sensor Manager is responsible for remotely communicating with the motion sensors
 * on the wearable device. For instance, it can send commands to the wearable device to start/stop
 * sensor data collection.
 *
 * @author Sean Noran
 *
 * @see com.google.android.gms.wearable.DataApi
 * @see GoogleApiClient
 */
public class RemoteSensorManager {
    /** used for debugging purposes */
    private static final String TAG = RemoteSensorManager.class.getName();

    /** the number of milliseconds to wait for a client connection */
    private static final int CLIENT_CONNECTION_TIMEOUT = 5000;

    /** singleton instance of the remote sensor manager */
    private static RemoteSensorManager instance;

    /** used for asynchronous message sending on a non-UI thread */
    private final ExecutorService executorService;

    /** the Google API client is responsible for communicating with the wearable device over the data layer */
    private final GoogleApiClient googleApiClient;

    /** return singleton instance of the remote sensor manager, instantiating if necessary */
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

    /**
     * validate the connection between the handheld and the wearable device.
     * @return true if successful, false if unsuccessful, i.e. no connection after {@link RemoteSensorManager#CLIENT_CONNECTION_TIMEOUT} milliseconds
     */
    private boolean validateConnection() {
        if (googleApiClient.isConnected()) {
            return true;
        }

        ConnectionResult result = googleApiClient.blockingConnect(CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);

        return result.isSuccess();
    }

    /** send a message to the wearable device to start data collection */
    public void startSensorService() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "Start Sensor Service");
                sendMessageInBackground(SharedConstants.COMMANDS.START_SENSOR_SERVICE);
            }
        });
    }

    /** send a message to the wearable device to stop data collection */
    public void stopSensorService() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "Stop Sensor Service");
                sendMessageInBackground(SharedConstants.COMMANDS.STOP_SENSOR_SERVICE);
            }
        });
    }

    /**
     * sends a command/message (referred to as a path in Google API logic) to the wearable application
     * @param path the message sent to the wearable device
     */
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
