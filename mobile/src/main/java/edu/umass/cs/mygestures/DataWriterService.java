package edu.umass.cs.mygestures;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileInputStream;

/**
 * Created by Sean on 7/7/2015.
 */
public class DataWriterService extends Service {

    private static final String TAG = DataWriterService.class.getName();

    private static final String ACCEL_TAG = "ACCEL";
    private static final String GYRO_TAG = "GYRO";
    private static final String LABEL_TAG = "REPORT";

    private BufferedWriter accelWriter;
    private BufferedWriter gyroWriter;
    private BufferedWriter labelWriter;

    private void init(){
        if (accelWriter == null)
            accelWriter = FileUtil.getFileWriter(ACCEL_TAG);
        if (gyroWriter == null)
            gyroWriter = FileUtil.getFileWriter(GYRO_TAG);
        if (labelWriter == null)
            labelWriter = FileUtil.getFileWriter(LABEL_TAG);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                if (intent.getAction().equals("ACCEL_DATA")) {
                    String line = intent.getStringExtra("sensor_data");
                    FileUtil.writeToFile(line, accelWriter);
                }else if (intent.getAction().equals("GYRO_DATA")){
                    String line = intent.getStringExtra("sensor_data");
                    FileUtil.writeToFile(line, gyroWriter);
                }else if (intent.getAction().equals("LABEL")) {
                    String line = intent.getStringExtra("label");
                    FileUtil.writeToFile(line, labelWriter);
                }
            }
        }
    };

    @Override
    public void onCreate(){
        IntentFilter filter = new IntentFilter();
        filter.addAction("ACCEL_DATA");
        filter.addAction("GYRO_DATA");
        filter.addAction("LABEL");

        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy(){
        Log.v(TAG, "onDestroy()");
        unregisterReceiver(receiver);
        if (accelWriter != null)
            FileUtil.closeWriter(accelWriter);
        if (gyroWriter != null)
            FileUtil.closeWriter(gyroWriter);
        if (labelWriter != null)
            FileUtil.closeWriter(labelWriter);
        //stopForeground(true);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d(TAG, "START COMMAND");
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            Log.i(TAG, "Received Start Foreground Intent ");
            Intent notificationIntent = new Intent(this, MainActivity.class); //open main activity when user clicks on notification
            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            Intent playIntent = new Intent(this, DataReceiverService.class);
            playIntent.setAction(Constants.ACTION.PLAY_ACTION);
            PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playIntent, 0);

            Intent pauseIntent = new Intent(this, DataReceiverService.class);
            pauseIntent.setAction(Constants.ACTION.PAUSE_ACTION);
            PendingIntent pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, 0);

            Intent stopIntent = new Intent(this, DataReceiverService.class);
            stopIntent.setAction(Constants.ACTION.STOP_ACTION);
            PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, 0);

            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("My Gestures")
                    .setTicker("My Gestures")
                    .setContentText("Collecting Accelerometer/Gyroscope Data")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .setVibrate(new long[]{0, 250, 200, 250, 150, 150, 75, 150, 75, 150}) //I LOVE THIS!!!
                    .setPriority(Notification.PRIORITY_MAX) //otherwise buttons will not show up!
                    .addAction(android.R.drawable.ic_media_play,
                            "Start", pausePendingIntent)
                    .addAction(android.R.drawable.ic_media_pause, "Pause",
                            playPendingIntent)
                    .addAction(android.R.drawable.ic_delete, "Stop",
                            stopPendingIntent).build(); //TODO: No ic_media_stop, but find appropriate icons...

            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
            init();
        } else if (intent.getAction().equals(Constants.ACTION.PAUSE_ACTION)) {
            Log.i(TAG, "Clicked Previous");
        } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
            Log.i(TAG, "Clicked Play");
        } else if (intent.getAction().equals(Constants.ACTION.STOP_ACTION)) {
            Log.i(TAG, "Clicked Next");
        } else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(TAG, "Received Stop Foreground Intent");
            stopForeground(true);
            stopSelf();
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void saveLabel(String label){
        if (label != null) {
            FileUtil.writeToFile(label, labelWriter);
        }
    }

}
