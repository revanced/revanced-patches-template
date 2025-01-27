package app.revanced.extension;

import android.util.Log;
import okhttp3.Request;

public class LoggerPatch {
    public static void log() {
        Log.d("LoggerPatch", "Hello from Logger");
    }

    public static void printUrl(Request request) {
        Log.d("LoggerPatch", "Request: " + request.toString());
    }
}
