package edu.umass.cs.mygestures;

/**
 * Constants used for communication between components of the handheld application. For example,
 * the Data Writer Service can be sure it is receiving the data from the Data Receiver Service.
 *
 * @see DataWriterService
 * @see DataReceiverService
 */
class Constants {
    public interface ACTION {
        String NAVIGATE_TO_APP = "edu.umass.cs.mygestures.action.navigate-to-app";
        String RECORD_LABEL = "edu.umass.cs.mygestures.action.record-label";
        String START_FOREGROUND = "edu.umass.cs.mygestures.action.start-foreground";
        String STOP_FOREGROUND = "edu.umass.cs.mygestures.action.stop-foreground";

        String SEND_ACCELEROMETER = "edu.umass.cs.mygestures.action.send-accelerometer";
        String SEND_GYROSCOPE = "edu.umass.cs.mygestures.action.send-gyroscope";
        String SEND_LABEL = "edu.umass.cs.mygestures.action.send-label";
    }

    public interface VALUES {
        String SENSOR_DATA = "edu.umass.cs.mygestures.values.sensor-data";
        String LABEL = "edu.umass.cs.mygestures.values.label";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
    }
}
