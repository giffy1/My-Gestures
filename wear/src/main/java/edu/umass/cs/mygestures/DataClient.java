package edu.umass.cs.mygestures;

import android.content.Context;
import android.hardware.Sensor;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @See https://github.com/pocmo/SensorDashboard/blob/master/wear/src/main/java/com/github/pocmo/sensordashboard/DeviceClient.java
 */
public class DataClient {
    private static final String TAG = DataClient.class.getName();

    private GoogleApiClient googleApiClient;

    private static DataClient instance;

    private static final int CLIENT_CONNECTION_TIMEOUT = 5000;

    private ExecutorService executorService;

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

    private void sendSensorDataInBackground(int sensorType, long[] timestamps, float[] values) {
        PutDataMapRequest dataMap = PutDataMapRequest.create("/sensors/" + sensorType);

        dataMap.getDataMap().putLongArray("timestamps", timestamps);
        dataMap.getDataMap().putFloatArray("values", values);

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
                    //Log.v(TAG, "Sending sensor data: " + dataItemResult.getStatus().isSuccess());
                }
            });
        }
    }

    public void sendLabel(final long timestamp, final String activity, final int tag) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                sendLabelInBackground(timestamp, activity, tag);
            }
        });
    }

    private void sendLabelInBackground(long timestamp, String activity, int tag) {
        PutDataMapRequest dataMap = PutDataMapRequest.create("/label");

        //TODO: Use common file with constants for identifiers:
        dataMap.getDataMap().putLong("timestamp", timestamp);
        dataMap.getDataMap().putString("activity", activity);
        dataMap.getDataMap().putInt("tag", tag);

        PutDataRequest putDataRequest = dataMap.asPutDataRequest();
        send(putDataRequest);
    }
}
