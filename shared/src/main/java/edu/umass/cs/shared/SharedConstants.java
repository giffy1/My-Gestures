package edu.umass.cs.shared;

/**
 * This file contains constants that are shared between the handheld and wearable applications,
 * such as the voice commands and data layer communications tags
 */
public class SharedConstants {
    //voice commands 'before' and 'after' were chosen because they are easily distinguishable from most words, unlike 'start'/'stop'
    //TODO: Maybe put in shared settings file? A little different than constants
    public interface VOICE_COMMANDS {
        String START_COMMAND = "before";
        String STOP_COMMAND = "after";
    }

    public interface DATA_LAYER_CONSTANTS {
        String SENSOR_PATH = "/sensors/";
        String LABEL_PATH = "/label";
    }

    public interface VALUES {
        String TIMESTAMPS = "edu.umass.cs.shared.values.timestamps";
        String SENSOR_VALUES = "edu.umass.cs.shared.values.sensor-values";
        String LABEL_TIMESTAMP = "edu.umass.cs.shared.values.label-timestamp";
        String ACTIVITY = "edu.umass.cs.shared.values.activity";
        String COMMAND = "edu.umass.cs.shared.values.command";
    }

    public interface COMMANDS {
        String START_SENSOR_SERVICE = "edu.umass.cs.shared.commands.start-sensor-service";
        String STOP_SENSOR_SERVICE = "edu.umass.cs.shared.commands.stop-sensor-service";
    }
}
