package edu.umass.cs.mygestures;

import android.content.Intent;
import android.hardware.Sensor;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import edu.umass.cs.shared.SharedConstants;

/**
 * The Data Receiver Service listens for data sent from the wearable to the handheld device. In
 * particular, the service is notified when the accelerometer and gyroscope buffers are updated
 * in the data layer and when a voice label is recorded and sent to the data layer.
 *
 * @see com.google.android.gms.common.api.GoogleApiClient
 * @see WearableListenerService
 */
public class DataReceiverService extends WearableListenerService{

    private static final String TAG = DataReceiverService.class.getName();

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);

        Log.i(TAG, "Connected to: " + peer.getDisplayName() + " [" + peer.getId() + "]");
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);

        Log.i(TAG, "Disconnected from: " + peer.getDisplayName() + " [" + peer.getId() + "]");
    }

    //Note: This is only called when the data is actually changed!! Since we have a timestamp in the data event, that is no problem
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = dataEvent.getDataItem();
                Uri uri = dataItem.getUri(); //easy way to manipulate path we receive
                String path = uri.getPath();

                if (path.startsWith(SharedConstants.DATA_LAYER_CONSTANTS.SENSOR_PATH)) {
                    int sensorType = Integer.parseInt(uri.getLastPathSegment());
                    onReceiveSensorData(sensorType, DataMapItem.fromDataItem(dataItem).getDataMap());
                }else if (path.equals(SharedConstants.DATA_LAYER_CONSTANTS.LABEL_PATH)) {
                    onReceiveLabel(DataMapItem.fromDataItem(dataItem).getDataMap());
                }
            }
        }
    }

    /**
     * Called upon receiving accelerometer or gyroscope sensor data from the wearable device
     * @param sensorType the type of sensor, corresponding to the Sensor class constants, i.e. Sensor.TYPE_ACCELEROMETER
     * @param dataMap the map containing the key-value pairs for the sensor data (timestamps and xyz values)
     */
    private void onReceiveSensorData(int sensorType, DataMap dataMap) {
        long[] timestamps = dataMap.getLongArray(SharedConstants.VALUES.TIMESTAMPS);
        float[] values = dataMap.getFloatArray(SharedConstants.VALUES.SENSOR_VALUES);

        for (int i = 0; i < timestamps.length; i++){
            long timestamp = timestamps[i];
            float x = values[3*i];
            float y = values[3*i+1];
            float z = values[3*i+2];

            //TODO: probably better to create multiline string and save that instead of writing each line separately
            String line = timestamp + "," + x + "," + y + "," + z;

            //broadcast line to data writer service
            Intent intent = new Intent();
            if (sensorType == Sensor.TYPE_ACCELEROMETER) {
                intent.putExtra(Constants.VALUES.SENSOR_DATA, line);
                intent.setAction(Constants.ACTION.SEND_ACCELEROMETER);
                sendBroadcast(intent);
            }else if (sensorType == Sensor.TYPE_GYROSCOPE) {
                intent.putExtra(Constants.VALUES.SENSOR_DATA, line);
                intent.setAction(Constants.ACTION.SEND_GYROSCOPE);
                sendBroadcast(intent);
            }

            //also log the first value just for debugging purposes
            if (sensorType == Sensor.TYPE_ACCELEROMETER && i == 1)
                Log.d(TAG, line);
        }
    }

    /**
     * Called upon receiving a label from the wearable device
     * @param dataMap a map containing the key-value pairs describing the label (activity, before or after, and a timestamp)
     */
    private void onReceiveLabel(DataMap dataMap){
        String activity = dataMap.getString(SharedConstants.VALUES.ACTIVITY);
        String command = dataMap.getString(SharedConstants.VALUES.COMMAND);
        long timestamp = dataMap.getLong(SharedConstants.VALUES.LABEL_TIMESTAMP);

        String line = timestamp + "," + activity + "," + command;

        //broadcast label to main handheld service
        Intent intent = new Intent();
        intent.putExtra(Constants.VALUES.LABEL, line);
        intent.setAction(Constants.ACTION.SEND_LABEL);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy(){
        Log.v(TAG, "onDestroy()");
        super.onDestroy();
    }
}