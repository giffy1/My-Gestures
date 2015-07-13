package edu.umass.cs.mygestures;

/**
 * Constants used for communication between components of the handheld application. For example,
 * the Data Writer Service can be sure it is receiving the data from the Data Receiver Service.
 *
 * @see DataWriterService
 * @see DataReceiverService
 */
public class Constants {
    public interface ACTION {
        public static String NAVIGATE_TO_APP = "edu.umass.cs.mygestures.action.navigate-to-app";
        public static String RECORD_LABEL = "edu.umass.cs.mygestures.action.record-label";
        public static String START_FOREGROUND = "edu.umass.cs.mygestures.action.start-foreground";
        public static String STOP_FOREGROUND = "edu.umass.cs.mygestures.action.stop-foreground";

        public static String SEND_ACCELEROMETER = "edu.umass.cs.mygestures.action.send-accelerometer";
        public static String SEND_GYROSCOPE = "edu.umass.cs.mygestures.action.send-gyroscope";
        public static String SEND_LABEL = "edu.umass.cs.mygestures.action.send-label";
    }

    public interface VALUES {
        public static String SENSOR_DATA = "edu.umass.cs.mygestures.values.sensor-data";
        public static String LABEL = "edu.umass.cs.mygestures.values.label";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}
