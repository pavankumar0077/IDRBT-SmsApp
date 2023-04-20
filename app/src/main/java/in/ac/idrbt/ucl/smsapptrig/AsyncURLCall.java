package in.ac.idrbt.ucl.smsapptrig;


/* Ref https://medium.com/@lewisjkl/android-httpurlconnection-with-asynctask-tutorial-7ce5bf0245cd */

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class AsyncURLCall extends AsyncTask<String, Void, Void> {
    // This is the JSON body of the post
    JSONObject postData;
    @SuppressLint("StaticFieldLeak")
    Context appContext;

    // This is a constructor that allows you to pass in the JSON body
    public AsyncURLCall(Map<String, String> postData, Context ctx) {
        if (postData != null) {
            this.postData = new JSONObject(postData);
            appContext = ctx;
        }
    }


    @Override
    protected Void doInBackground(String... params) {
        String responseString = null;
        try {
            URL url = new URL(params[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestMethod("POST");


            // OPTIONAL - Sets an authorization header
            urlConnection.setRequestProperty("Authorization", "someAuthString");

            // Send the post body
            if (this.postData != null) {
                OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                writer.write(postData.toString());
                writer.flush();
            }

            int statusCode = urlConnection.getResponseCode();

            if (statusCode ==  200) {
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                String response = convertInputStreamToString(inputStream);

            } else {
                Toast.makeText(appContext, "URL Invoke failed " + params[0], Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Log.d("ASYNC CALL", e.getLocalizedMessage());
        }
        return null;
    }
    private String convertInputStreamToString(InputStream inputStream) {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

}

