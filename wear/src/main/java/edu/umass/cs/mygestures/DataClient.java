package edu.umass.cs.mygestures;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import edu.umass.cs.shared.SharedConstants;

/**
 * The DataClient is responsible for sending data from the wearable device to the handheld application.
 *
 * See <a href=https://github.com/pocmo/SensorDashboard/blob/master/wear/src/main/java/com/github/pocmo/sensordashboard/DeviceClient.java>this link</a>
 * for the basis of the structure for the DataClient.
 *
 * @see GoogleApiClient
 * @see DataApi
 */
public class DataClient {

    /** used for debugging purposes */
    private static final String TAG = DataClient.class.getName();

    /** Google API client used to communicate between devices over data layer */
    private GoogleApiClient googleApiClient;

    /** static singleton - we should only have one data client! */
    private static DataClient instance;

    /** timeout when connecting to the handheld device: If not connected after 5 seconds, return failure */
    private static final int CLIENT_CONNECTION_TIMEOUT = 5000;

    /** used to send data on a separate non-UI thread */
    private ExecutorService executorService;

    /** returns the singleton instance of the class, instantiating if necessary */
    public static DataClient getInstance(Context context) {
        if (instance == null){
            instance = new DataClient(context.getApplicationContext());
        }
        return instance;
    }

    private DataClient(Context context){
        googleApiClient = new GoogleApiClient.Builder(context).addApi(Wearable.API).build();

        executorService = Executors.newCachedThreadPool();
    }

    public void sendSensorData(final int sensorType, final long[] timestamps, final float[] values) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                sendSensorDataInBackground(sensorType, timestamps, values);
            }
        });
    }

    /**
     * Sends the sensor data via the data layer to the handheld application
     * @param sensorType the (3-axis) sensor from which we are collecting data, i.e. Sensor.TYPE_ACCELEROMETER, TYPE_GYROSCOPE
     * @param timestamps a sequence of timestamps corresponding to when the values were measured (uptime in nanoseconds)
     * @param values a list of the x-, y-, and z-values of the sensor reading
     */
    private void sendSensorDataInBackground(int sensorType, long[] timestamps, float[] values) {
        PutDataMapRequest dataMap = PutDataMapRequest.create(SharedConstants.DATA_LAYER_CONSTANTS.SENSOR_PATH + sensorType);

        dataMap.getDataMap().putLongArray(SharedConstants.VALUES.TIMESTAMPS, timestamps);
        dataMap.getDataMap().putFloatArray(SharedConstants.VALUES.SENSOR_VALUES, values);

        PutDataRequest putDataRequest = dataMap.asPutDataRequest();
        send(putDataRequest);
    }

    private boolean validateConnection() {
        if (googleApiClient.isConnected()) {
            return true;
        }

        ConnectionResult result = googleApiClient.blockingConnect(CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);

        return result.isSuccess();
    }

    private void send(PutDataRequest putDataRequest) {
        if (validateConnection()) {
            Wearable.DataApi.putDataItem(googleApiClient, putDataRequest).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(DataApi.DataItemResult dataItemResult) {
                    //use dataItemResult.getStatus().isSuccess() to see if successful
                }
            });
        }
    }

    public void sendLabel(final long timestamp, final String activity, final String command) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                sendLabelInBackground(timestamp, activity, command);
            }
        });
    }

    /**
     * sends the label via the data layer to the handheld application
     * @param timestamp the moment at which the label occurred (uptime in nanoseconds)
     * @param activity the activity the user performed
     * @param command a command describing the action: either starting the action or stopping the action
     */
    private void sendLabelInBackground(long timestamp, String activity, String command) {
        PutDataMapRequest dataMap = PutDataMapRequest.create(SharedConstants.DATA_LAYER_CONSTANTS.LABEL_PATH);

        dataMap.getDataMap().putLong(SharedConstants.VALUES.LABEL_TIMESTAMP, timestamp);
        dataMap.getDataMap().putString(SharedConstants.VALUES.ACTIVITY, activity);
        dataMap.getDataMap().putString(SharedConstants.VALUES.COMMAND, command);

        PutDataRequest putDataRequest = dataMap.asPutDataRequest();
        send(putDataRequest);
    }
}
