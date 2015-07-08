package edu.umass.cs.mygestures;

public class Constants {
    public interface ACTION {
        public static String MAIN_ACTION = "edu.umass.cs.musicapp.action.main";
        public static String PAUSE_ACTION = "edu.umass.cs.musicapp.action.pause";
        public static String PLAY_ACTION = "edu.umass.cs.musicapp.action.play";
        public static String STOP_ACTION = "edu.umass.cs.musicapp.action.stop";
        public static String STARTFOREGROUND_ACTION = "edu.umass.cs.musicapp.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "edu.umass.cs.musicapp.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}
