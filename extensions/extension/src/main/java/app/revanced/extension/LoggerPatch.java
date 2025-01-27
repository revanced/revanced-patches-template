package app.revanced.extension;

import android.util.Log;
import okhttp3.Request;
import okhttp3.HttpUrl;

public class LoggerPatch {
    public static void log() {
        Log.d("LoggerPatch", "Hello from Logger");
    }

    public static Request printAndModifyUrl(Request request) {
        // Log the original request
        Log.d("LoggerPatch", "Original request: " + request.toString());
    
        // Extract the original URL
        HttpUrl originalUrl = request.url();
        
        // Extract the host and path from the original URL
        String originalHost = originalUrl.host();
        String originalPath = originalUrl.encodedPath();
    
        // Construct the new URL
        HttpUrl modifiedUrl = new HttpUrl.Builder()
                .scheme("https")
                .host("proxy.lol")
                .encodedPath("/" + originalHost + originalPath)
                .build();
    
        // Create a new Request with the modified URL
        Request modifiedRequest = request.newBuilder()
                .url(modifiedUrl)
                .build();
    
        // Log the modified request
        Log.d("LoggerPatch", "Modified request: " + modifiedRequest.toString());
    
        // Return the modified Request
        return modifiedRequest;
    }
}
