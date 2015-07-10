package edu.umass.cs.mygestures;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * The wearable sensor service is responsible for collecting the accelerometer and gyroscope data on
 * the wearable device. It is an ongoing service that is initiated and terminated through the handheld
 * UI. All interactions on the wearable are through the notification, primarily voice labeling.
 * <br><br>
 * See {@link DataClient} for sending the sensor data and labels to the handheld client
 * <br><br>
 * See <a href=https://github.com/pocmo/SensorDashboard/blob/master/wear/src/main/java/com/github/pocmo/sensordashboard/SensorService.java>SensorDashboard</a>
 * for similar work. This application collects all possible sensor streams from the wearable device and visualizes them on the handheld.
 *
 * @author Sean Noran 6/18/15
 * @see DataClient
 *
 */
public class SensorService extends Service implements SensorEventListener {

    /** Sensor Manager object for registering and unregistering system sensors */
    private SensorManager mSensorManager;

    /** device accelerometer sensor */
    private Sensor accelSensor;

    /** device gyroscope sensor */
    private Sensor gyroSensor;

    private static final String TAG = SensorService.class.getName();

    private DataClient client;

    /** Start Gesture Voice Command */
    private static final String START_COMMAND = "before";

    /** Stop Gesture Voice Command */
    private static final String STOP_COMMAND = "after";

    /** Speech Recognizer object to transcribe speech */
    protected SpeechRecognizer mSpeechRecognizer;

    /** Used to start/configure the Speech Recognition service */
    protected Intent mIntent;

    /** Buffer of timestamps for gyroscope data */
    private long[] gyroTimestamps;

    /** Buffer of timestamps for accel data */
    private long[] accelTimestamps;

    /** Buffer of values for gyro data */
    private float[] gyroValues;

    /** Buffer of values for accel data */
    private float[] accelValues;

    /** Index into gyroscope data */
    private int gyroIndex;

    /** Index into accelerometer data */
    private int accelIndex;

    /** Buffer size */
    private static final int BUFFER_SIZE = 512;

    /** When recording audio labels, should the timestamp be recorded when the
     * label is recognized or when the button is pressed? Because the action
     * typically starts AFTER user completes the start command, the timestamp
     * should be recorded when the start command is recognized. However, for
     * end commands, we use the timestamp when the button is pressed: The
     * labelTimestamp is that value. */

    private long labelTimestamp;

    @Override
    public void onCreate() {
        super.onCreate();

        client = DataClient.getInstance(this);

        Intent intent = new Intent(this, SensorService.class);
        intent.setAction(Constants.ACTION.RECORD_LABEL_ACTION);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("My Gestures")
                .setTicker("My Gestures")
                .setContentText("Collecting sensor data...")
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setOngoing(true)
                .setVibrate(new long[]{0,50,100,50,100,50,100,400,100,300,100,350,50,200,100,100,50,600}) //I LOVE THIS!!!
                .setPriority(Notification.PRIORITY_MAX) //otherwise buttons will not show up!
                .addAction(android.R.drawable.ic_btn_speak_now,
                        "Record Label", pendingIntent).build();

        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification); //id is arbitrary, so we choose id=1

        registerSensors();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getAction() != null) {
                if (intent.getAction().equals(Constants.ACTION.RECORD_LABEL_ACTION)) {
                    labelTimestamp = SystemClock.elapsedRealtimeNanos();
                    startListening();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        unregisterSensors();
        super.onDestroy();
    }

    /**
     * register accelerometer and gyroscope sensor listeners and initialize respective buffers
     */
    private void registerSensors(){
        //initialize buffers:
        gyroTimestamps = new long[BUFFER_SIZE];
        gyroValues = new float[BUFFER_SIZE*3];

        accelTimestamps = new long[BUFFER_SIZE];
        accelValues = new float[BUFFER_SIZE*3];

        gyroIndex = 0;
        accelIndex = 0;

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if (mSensorManager == null){
            Log.e(TAG, "ERROR: Could not retrieve sensor manager...");
            return;
        }

        gyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelSensor != null) {
            mSensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_GAME);
        } else {
            Log.w(TAG, "No Accelerometer found");
        }

        if (gyroSensor != null) {
            mSensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_GAME);
        } else {
            Log.w(TAG, "No gyroscope found");
        }
    }

    private void unregisterSensors() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this, accelSensor);
            mSensorManager.unregisterListener(this, gyroSensor);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized(this){
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                accelTimestamps[accelIndex] = event.timestamp;
                accelValues[3*accelIndex] = event.values[0];
                accelValues[3*accelIndex + 1] = event.values[1];
                accelValues[3*accelIndex + 2] = event.values[2];
                accelIndex++;
                if (accelIndex >= BUFFER_SIZE){
                    client.sendSensorData(Sensor.TYPE_ACCELEROMETER, accelTimestamps, accelValues);
                    accelIndex = 0;
                }
            }else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                gyroTimestamps[gyroIndex] = event.timestamp;
                gyroValues[3*gyroIndex] = event.values[0];
                gyroValues[3*gyroIndex + 1] = event.values[1];
                gyroValues[3*gyroIndex + 2] = event.values[2];
                gyroIndex++;
                if (gyroIndex >= BUFFER_SIZE){
                    client.sendSensorData(Sensor.TYPE_GYROSCOPE, gyroTimestamps, gyroValues);
                    gyroIndex = 0;
                }
            }else{
                Log.w(TAG, "Sensor Not Supported!");
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startListening(){
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
        mIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());

        //cancel any outgoing request:
        mSpeechRecognizer.cancel();
        mSpeechRecognizer.startListening(mIntent);
    }

    /** The Listener handles the events raised by the Speech Recognizer such as errors and results */
    protected class SpeechRecognitionListener implements RecognitionListener
    {
        private final String TAG = SpeechRecognitionListener.class.getName();

        @Override
        public void onBeginningOfSpeech() {}

        @Override
        public void onBufferReceived(byte[] buffer) {}

        @Override
        public void onEndOfSpeech() {
            Log.v(TAG, "End of Speech");
        }

        @Override
        public void onError(int error)
        {
            Toast.makeText(getApplicationContext(), "Error " + error, Toast.LENGTH_LONG).show();
            //TODO: Respond appropriately to error
        }

        @Override
        public void onEvent(int eventType, Bundle params) {}

        //TODO: Using onResults for handling speech recognition will work sufficiently well, but
        //there is often a delay since the onResults method is called only once it is clear that
        //the user has stopped speaking. We could use onPartialResults to retrieve labels more
        //quickly and more naturally ... but note that onPartialResults is not guaranteed to be
        //called!
        @Override
        public void onPartialResults(Bundle partialResults) {}

        @Override
        public void onReadyForSpeech(Bundle params) {}

        @Override
        public void onResults(Bundle results)
        {
            Log.v(TAG, "onResults: " + results);

            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String activity = "";
            int tag = -1;
            String[] words = null;

            //look for "start" or "stop" command
            for (int i = 0; i < data.size(); i++)
            {
                String result = data.get(i).toString();
                Log.d(TAG, "result " + result);
                words = result.split(" ");
                if (words[0].equalsIgnoreCase(START_COMMAND)){
                    tag = 1; //start
                    break;
                }else if (words[0].equalsIgnoreCase(STOP_COMMAND)){
                    tag = 0; //stop
                    break;
                }
            }
            if (tag == -1)
                return;

            //store the activity
            for (int j = 1; j < words.length; j++) {
                activity += words[j] + " ";
            }
            activity = activity.trim();

            if (activity.length() > 0) { //could have an empty activity...
                Toast.makeText(getApplicationContext(), activity + ", " + tag, Toast.LENGTH_LONG).show();
                //if we are starting the activity (tag = 1), use the timestamp after recognizing the command:
                long timestamp = (tag == 1 ? SystemClock.elapsedRealtimeNanos() : labelTimestamp);
                client.sendLabel(timestamp, activity, tag);
            }
        }

        @Override
        public void onRmsChanged(float rmsdB) {}

    }
}
