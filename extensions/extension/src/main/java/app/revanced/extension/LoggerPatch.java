package app.revanced.extension;

import android.util.Log;

public class LoggerPatch {
    public static void log() {
        Log.d("LoggerPatch", "Hello from Logger");
    }
}
