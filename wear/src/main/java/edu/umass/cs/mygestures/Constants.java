package edu.umass.cs.mygestures;

public class Constants {
    public interface ACTION {
        public static String RECORD_LABEL = "edu.umass.cs.mygestures.action.record";
        public static String START_SERVICE = "edu.umass.cs.mygestures.action.start-service";
        public static String STOP_SERVICE = "edu.umass.cs.mygestures.action.stop-service";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}
