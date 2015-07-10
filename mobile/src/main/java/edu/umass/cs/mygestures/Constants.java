package edu.umass.cs.mygestures;

/**
 * Constants used for communication between services on the handheld. This way,
 * the Data Writer Service can be sure it is receiving the data from the DataReceiverService.
 *
 * @see DataWriterService
 * @see DataReceiverService
 */
public class Constants {
    public interface ACTION {
        public static String MAIN_ACTION = "edu.umass.cs.mygestures.action.main";
        public static String RECORD_LABEL_ACTION = "edu.umass.cs.mygestures.action.record-label";
        public static String START_FOREGROUND_ACTION = "edu.umass.cs.mygestures.action.start-foreground";
        public static String STOP_FOREGROUND_ACTION = "edu.umass.cs.mygestures.action.stop-foreground";

        public static String SEND_ACCELEROMETER_ACTION = "edu.umass.cs.mygestures.action.send-accelerometer";
        public static String SEND_GYROSCOPE_ACTION = "edu.umass.cs.mygestures.action.send-gyroscope";
        public static String SEND_LABEL_ACTION = "edu.umass.cs.mygestures.action.label";
    }

    public interface VALUES {
        public static String SENSOR_DATA = "edu.umass.cs.mygestures.values.sensor-data";
        public static String LABEL = "edu.umass.cs.mygestures.values.label";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}
