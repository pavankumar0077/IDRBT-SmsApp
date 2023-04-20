package in.ac.idrbt.ucl.smsapptrig;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.smsapp2.R;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_READ_SMS_PERMISSION = 123;
    private static final int REQUEST_RECEIVE_SMS_PERMISSION = 1;
    private EditText senderNameEditText, kioskUrlEditText;
    private TextView recentMessageTextView;

    public SmsEventListener smsEventListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        senderNameEditText = findViewById(R.id.sender_name_edit_text);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        senderNameEditText.setText(preferences.getString("match text", getResources().getString(R.string.sms_filter)));
        senderNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { /* do nothing */ }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { /* do nothing */ }

            @Override
            public void afterTextChanged(Editable s) {
                String value = senderNameEditText.getText().toString();
                preferences.edit().putString("match text", value).apply();
            }
        });

        kioskUrlEditText = findViewById(R.id.kiosk_url_edit_text);

        kioskUrlEditText.setText(preferences.getString("kiosk url", "http://192.168.138.156:8081/dispense/api/mobile"));
        // kioskUrlEditText.setText(preferences.getString("kiosk url", "https://172.27.10.10:8443/dispense"));
        kioskUrlEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { /* do nothing */ }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { /* do nothing */ }

            @Override
            public void afterTextChanged(Editable s) {
                String value = kioskUrlEditText.getText().toString();
                preferences.edit().putString("kiosk url", value).apply();
            }
        });

        recentMessageTextView = findViewById(R.id.recent_message_text_view);

        Intent intent = new Intent("in.ac.idrbt.ucl.smsapptrigger.SmsEventListener");
        sendBroadcast(intent);

        Button searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener(v -> {
            String senderName = senderNameEditText.getText().toString();
            String recentMessage = getRecentMessage(senderName);
            if (recentMessage != null) {
                recentMessageTextView.setText(recentMessage);
            } else {
                recentMessageTextView.setText("No recent messages found from " + senderName);
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_SMS }, REQUEST_READ_SMS_PERMISSION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.RECEIVE_SMS }, REQUEST_RECEIVE_SMS_PERMISSION);
        }
    }

    private String getRecentMessage(String senderName) {
        String[] projection = new String[] { Telephony.Sms.BODY };
        String selection = Telephony.Sms.ADDRESS + " = ?";
        String[] selectionArgs = new String[] { senderName };
        String sortOrder = Telephony.Sms.DEFAULT_SORT_ORDER + " LIMIT 1";

        try (Cursor cursor = getContentResolver().query(
                Telephony.Sms.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(Telephony.Sms.BODY);
                if (columnIndex >= 0) {
                    return cursor.getString(columnIndex);
                }
            }
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ( REQUEST_READ_SMS_PERMISSION == requestCode || REQUEST_RECEIVE_SMS_PERMISSION == requestCode) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_LONG).show();
            }
        }
    }
}