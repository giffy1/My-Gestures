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
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * The Data Writer Service is the main service on the handheld application. It is responsible for writing
 * the sensor data along with the labels to their respective files. It is a foreground service, allowing
 * the user to close the main UI while still collecting data.
 *
 * @author Sean Noran 7/7/15.
 * @see DataReceiverService
 * @see FileUtil
 * @see Service
 */
public class DataWriterService extends Service {

    private static final String TAG = DataWriterService.class.getName();

    /** Name of the file to which to write the accelerometer data */
    private static final String ACCEL_TAG = "ACCEL";

    /** Name of the file to which to write the gyroscope data */
    private static final String GYRO_TAG = "GYRO";

    /** Name of the file to which to write the reported labels */
    private static final String LABEL_TAG = "REPORT";

    /** Buffered writer used to log the accelerometer data */
    private final BufferedWriter accelWriter = FileUtil.getFileWriter(ACCEL_TAG);

    /** Buffered writer used to log the gyroscope data */
    private final BufferedWriter gyroWriter = FileUtil.getFileWriter(GYRO_TAG);

    /** Buffered writer used to log the reported labels */
    private final BufferedWriter labelWriter = FileUtil.getFileWriter(LABEL_TAG);

    /** used to receive messages from other components of the handheld app through intents, i.e. receive labels **/
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                if (intent.getAction().equals(Constants.ACTION.SEND_ACCELEROMETER_ACTION)) {
                    String line = intent.getStringExtra(Constants.VALUES.SENSOR_DATA);
                    synchronized (accelWriter) {
                        FileUtil.writeToFile(line, accelWriter);
                    }
                }else if (intent.getAction().equals(Constants.ACTION.SEND_GYROSCOPE_ACTION)){
                    String line = intent.getStringExtra(Constants.VALUES.SENSOR_DATA);
                    synchronized (gyroWriter) {
                        FileUtil.writeToFile(line, gyroWriter);
                    }
                }else if (intent.getAction().equals(Constants.ACTION.SEND_LABEL_ACTION)) {
                    String line = intent.getStringExtra(Constants.VALUES.LABEL);
                    synchronized (labelWriter) {
                        FileUtil.writeToFile(line, labelWriter);
                    }
                    try {
                        //to make sure the labels appear readily, flush the writer. We can do this
                        //with the label writer right away because labels are infrequent enough. We
                        //do not need to do this with the gyro/accel writers because the data is so
                        //frequent that the buffer fills and is flushed automatically
                        labelWriter.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    @Override
    public void onCreate(){
        //the intent filter specifies the messages we are interested in receiving
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION.SEND_ACCELEROMETER_ACTION);
        filter.addAction(Constants.ACTION.SEND_GYROSCOPE_ACTION);
        filter.addAction(Constants.ACTION.SEND_LABEL_ACTION);

        registerReceiver(receiver, filter);
    }

    //TODO: According to the Android Documentation there is no guarantee that onDestroy() will ever
    //be called... especially if the process is killed by the OS due to low memory. So we may want
    //to flush and close the file writers somewhere else, otherwise we may lose recent accel/gyro
    //data since it is not being flushed regularly (only when buffer is full)

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

        RemoteSensorManager.getInstance(this).stopSensorService();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if (intent.getAction().equals(Constants.ACTION.START_FOREGROUND_ACTION)) {
            Log.i(TAG, "Received Start Service Intent ");

            Intent notificationIntent = new Intent(this, MainActivity.class); //open main activity when user clicks on notification
            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            Intent recordIntent = new Intent(this, DataReceiverService.class);
            recordIntent.setAction(Constants.ACTION.RECORD_LABEL_ACTION);
            PendingIntent recordPendingIntent = PendingIntent.getService(this, 0, recordIntent, 0);

            Intent stopIntent = new Intent(this, DataReceiverService.class);
            stopIntent.setAction(Constants.ACTION.STOP_FOREGROUND_ACTION);
            PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, 0);

            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("My Gestures")
                    .setTicker("My Gestures")
                    .setContentText("Collecting Accelerometer/Gyroscope Data")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setPriority(Notification.PRIORITY_MAX) //otherwise buttons will not show up!
                    .setDeleteIntent(stopPendingIntent) //user can swipe away notification to end service
                    .addAction(android.R.drawable.ic_btn_speak_now,
                            "Record Label", recordPendingIntent).build(); //got rid of notification actions, could use them later?

            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
        } else if (intent.getAction().equals(Constants.ACTION.RECORD_LABEL_ACTION)) {
            Log.i(TAG, "Clicked Record Label");
            Toast.makeText(getApplicationContext(), "Please record labels using the wearable device!", Toast.LENGTH_LONG).show();
        } else if (intent.getAction().equals(Constants.ACTION.STOP_FOREGROUND_ACTION)) {
            Log.i(TAG, "Received Stop Service Intent");

            //make sure wearable sensor service stops when the service ends
            RemoteSensorManager.getInstance(this).stopSensorService();

            stopForeground(true);
            stopSelf();
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}