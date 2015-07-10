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

public class DataReceiverService extends WearableListenerService{

    private static final String TAG = DataReceiverService.class.getName();

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);

        Log.i(TAG, "Connected: " + peer.getDisplayName() + " (" + peer.getId() + ")");
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);

        Log.i(TAG, "Disconnected: " + peer.getDisplayName() + " (" + peer.getId() + ")");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        //Log.d(TAG, "onDataChanged()");

        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = dataEvent.getDataItem();
                Uri uri = dataItem.getUri();
                String path = uri.getPath();

                //1 = Accelerometer, 4 = Gyroscope
                if (path.startsWith("/sensors/")) {
                    int sensorType = Integer.parseInt(uri.getLastPathSegment());
                    unpackSensorData(sensorType, DataMapItem.fromDataItem(dataItem).getDataMap());
                }else if (path.equals("/label")) {
                    unpackLabel(DataMapItem.fromDataItem(dataItem).getDataMap());
                }
            }
        }
    }

    /**
     * @see <a href=https://github.com/pocmo/SensorDashboard/blob/master/mobile/src/main/java/com/github/pocmo/sensordashboard/SensorReceiverService.java>this</a>
     * @param sensorType
     * @param dataMap
     */
    private void unpackSensorData(int sensorType, DataMap dataMap) {
        //TODO: Don't use strings "timestamps", etc., specify a string in a shared (mobile + wear) library constants file
        long[] timestamps = dataMap.getLongArray("timestamps");
        float[] values = dataMap.getFloatArray("values");

        for (int i = 0; i < timestamps.length; i++){
            long timestamp = timestamps[i];
            float x = values[3*i];
            float y = values[3*i+1];
            float z = values[3*i+2];

            String line = timestamp + "," + x + "," + y + "," + z;
            Intent intent = new Intent();
            if (sensorType == Sensor.TYPE_ACCELEROMETER) {
                //FileUtil.writeToFile(line, accelWriter);
                intent.putExtra("sensor_data", line);
                intent.setAction(Constants.ACTION.SEND_ACCELEROMETER_ACTION);
                sendBroadcast(intent);
            }else if (sensorType == Sensor.TYPE_GYROSCOPE) {
                //FileUtil.writeToFile(line, gyroWriter);
                intent.putExtra("sensor_data", line);
                intent.setAction(Constants.ACTION.SEND_GYROSCOPE_ACTION);
                sendBroadcast(intent);
            }

            //also log the first value just for debugging purposes
            if (sensorType == Sensor.TYPE_ACCELEROMETER && i == 1)
                Log.d(TAG, line);
        }
    }

    private void unpackLabel(DataMap dataMap){
        String activity = dataMap.getString("activity");
        int tag = dataMap.getInt("tag");
        long timestamp = dataMap.getLong("timestamp");

        String line = timestamp + "," + activity + "," + (tag == 1 ? "Before" : "After"); //TODO: Don't use tag == 1, use common constant instead of 1

        Intent intent = new Intent();
        intent.putExtra("label", line);
        intent.setAction(Constants.ACTION.SEND_LABEL_ACTION);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy(){
        Log.v(TAG, "onDestroy()");
        super.onDestroy();
    }
}