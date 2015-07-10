package edu.umass.cs.mygestures;

public class Constants {
    public interface ACTION {
        public static String MAIN_ACTION = "edu.umass.cs.mygestures.action.main";
        public static String PAUSE_ACTION = "edu.umass.cs.mygestures.action.pause";
        public static String PLAY_ACTION = "edu.umass.cs.mygestures.action.play";
        public static String STOP_ACTION = "edu.umass.cs.mygestures.action.stop";
        public static String STARTFOREGROUND_ACTION = "edu.umass.cs.mygestures.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "edu.umass.cs.mygestures.action.stopforeground";

        public static String SEND_ACCELEROMETER_ACTION = "edu.umass.cs.mygestures.action.sendaccelerometer";
        public static String SEND_GYROSCOPE_ACTION = "edu.umass.cs.mygestures.action.sendgyroscope";
        public static String SEND_LABEL_ACTION = "edu.umass.cs.mygestures.action.label";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}
