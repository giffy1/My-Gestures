package edu.umass.cs.mygestures;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The Main Activity is the application's entry point on the handheld device. It is where the user
 * interacts with the background data collection service. Here the user can start/stop the service
 * and delete the existing data if necessary.
 *
 * @see Activity
 * @see RemoteSensorManager
 *
 * @author Sean Noran 6/8/18
 */
public class MainActivity extends Activity {
    //TODO: Add options to change sampling rate, choose storage location/filename, edit falsely entered labels, etc.
    // The UI is currently very sparse... we may not need much, but it might be good to add some of these options
    //TODO: Look into lambda expressions (Java 1.7 and later) to "fix" warnings - might be more appropriate syntax?

    /** used for debugging purposes */
    private static final String TAG = MainActivity.class.getName();

    /** The sensor manager which handles sensors on the wearable device remotely */
    private RemoteSensorManager remoteSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        remoteSensorManager = RemoteSensorManager.getInstance(this);

        Button startButton = (Button) findViewById(R.id.start_button);
        Button stopButton = (Button) findViewById(R.id.stop_button);
        Button deleteButton = (Button) findViewById(R.id.delete_button);
        final TextView startTimeText = (TextView) findViewById(R.id.start_time_text);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(MainActivity.this, DataWriterService.class);
                startIntent.setAction(Constants.ACTION.START_FOREGROUND);
                startService(startIntent);

                remoteSensorManager.startSensorService();
                startTimeText.setText(String.valueOf(System.currentTimeMillis()));
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent stopIntent = new Intent(MainActivity.this, DataWriterService.class);
                stopIntent.setAction(Constants.ACTION.STOP_FOREGROUND);
                startService(stopIntent);

                remoteSensorManager.stopSensorService();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Are you sure you want to delete the gestures data?")
                        .setCancelable(false)
                        .setPositiveButton("Yes, Delete Data", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                if (FileUtil.deleteData()) {
                                    Toast.makeText(getApplicationContext(), "Data successfully deleted.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Error: Directory may not have been deleted!", Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton("No, I misclicked", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id){} //do nothing
                        })
                        .show();
            }
        });
    }
}
