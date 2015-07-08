package edu.umass.cs.mygestures;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class handles file input/output operations, such as saving the accelerometer/gyroscope
 * data and labels, opening/closing file readers/writers, and deleting the data storage location.
 * TODO: This is all done using static operations, but because reading/writing inherently introduces
 * race conditions, this is not a recommended approach. This needs to be refactored!
 */
public class FileUtil {

    /**
     * Returns a root directory where the logging takes place
     * @return
     */
    private static File getStorageLocation(){
        File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"motionData");
        if(!root.exists())
            root.mkdir();
        return root;
    }

    /**
     * Returns a file writer for a device
     * @param tag file name
     * @return
     */
    public static BufferedWriter getFileWriter(String tag){
        File rootDir = getStorageLocation();
        java.util.Date date= new java.util.Date();
        long time = date.getTime();
        String fileName = tag+".csv";

        BufferedWriter out = null;
        try{
            out = new BufferedWriter(new FileWriter(new File(rootDir,fileName)));
        }catch(IOException e){
            e.printStackTrace();
        }
        return out;
    }

    /**
     * Write the log for a device
     * @param s log to write
     * @param out file writer
     */
    public static void writeToFile(String s, BufferedWriter out) {
        synchronized(out) {
            try{
                out.write(s+"\n");
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Close the log writer for a device
     * @param out log writer
     */
    public static void closeWriter(BufferedWriter out) {
        try{
            out.flush();
            out.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Deletes all the data from the log directory
     * @return
     */
    public static boolean deleteData(){
        boolean deleted = false;
        File root = getStorageLocation();
        if(root!=null){
            File files[] = root.listFiles();
            if(files!=null){
                for(int i=0;i<files.length;i++)
                    files[i].delete();
            }
            deleted = root.delete();
        }
        return deleted;
    }
}
