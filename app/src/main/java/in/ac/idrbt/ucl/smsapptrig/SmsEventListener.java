package in.ac.idrbt.ucl.smsapptrig;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.widget.Toast;
import com.example.smsapp2.R;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;

public class SmsEventListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                for (int i = 0; i < pdus.length; i++) {
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    String sender = sms.getOriginatingAddress();
                    String message = sms.getMessageBody();
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    String altText = context.getResources().getString(R.string.sms_filter);
                    String matchText = preferences.getString("match text", altText);
                    if(message.contains(matchText)) {
                        //Hit URL to dispense Cash
                        String urlStr = preferences.getString("kiosk url", "https://172.27.10.10:8443/dispense");
                        Map<String, String> postData = new HashMap<>();
                        postData.put("sender", sender);
                        postData.put("message", message);
                        AsyncURLCall task = new AsyncURLCall(postData, context);
                        task.execute(urlStr);
                        Toast.makeText(context, "SMS processed from " + sender + ": " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}