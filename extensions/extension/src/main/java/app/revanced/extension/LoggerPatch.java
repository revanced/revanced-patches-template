package app.revanced.extension;

import android.util.Log;
import okhttp3.Request;
import okhttp3.HttpUrl;

public class LoggerPatch {
    public static void log() {
        Log.d("LoggerPatch", "Hello from Logger");
    }

    public static Request printAndModifyUrl(Request request, String proxyHost, String proxyCookies) {
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
                .host(proxyHost)
                .encodedPath("/" + originalHost + originalPath)
                .build();
        
        // Start building the modified Request
        Request.Builder modifiedRequestBuilder = request.newBuilder()
                .url(modifiedUrl);

        // Handle cookies
        if (proxyCookies != null && !proxyCookies.isEmpty()) {
            String existingCookies = request.header("Cookie");
            String combinedCookies = existingCookies != null 
                    ? existingCookies + "; " + proxyCookies 
                    : proxyCookies;
            modifiedRequestBuilder.header("Cookie", combinedCookies);
        }

        // Build the modified Request
        Request modifiedRequest = modifiedRequestBuilder.build();
    
        // Log the modified request
        Log.d("LoggerPatch", "Modified request: " + modifiedRequest.toString());
    
        // Return the modified Request
        return modifiedRequest;
    }
}
