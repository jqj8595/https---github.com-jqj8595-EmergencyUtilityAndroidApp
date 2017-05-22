package com.example.androidmessage.androidmessage;
import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.util.ArrayList;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    ProgressBar progressBar;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;
    private static final int READ_SMS_PERMISSIONS_REQUEST =1;
    EditText phoneEditText, smsEditText;
    Button sendButton;
    Button locatbt;
    Button refreshBT;
    String phoneNumber, messageText;
    ArrayList<String> smsMessageList = new ArrayList<>();
    ListView messages;
    ArrayAdapter arrayAdapter;
    EditText input;
    SmsManager smsManger = SmsManager.getDefault();
    private static MainActivity inst;


    public static MainActivity instance(){
        return inst;
    }

    @Override
    public void onStart(){
        super.onStart();
        inst = this;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messages = (ListView) findViewById(R.id.messages);
        input = (EditText) findViewById(R.id.input);
        arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, smsMessageList);
        messages.setAdapter(arrayAdapter);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            getPermissionToReadSMS();
        } else {
            refreshingInbox();
        }
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        phoneEditText = (EditText) findViewById(R.id.phoneEditText);
        smsEditText = (EditText) findViewById(R.id.input);
        sendButton = (Button) findViewById(R.id.send);
        refreshBT = (Button) findViewById(R.id.refreshbt);
        locatbt = (Button)findViewById(R.id.locate);


        locatbt.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });


    }

    public void updateInbox(final String smsMessage){
        arrayAdapter.insert(smsMessage, 0);
        arrayAdapter.notifyDataSetChanged();
    }
    /**
     * gain permission to read sms from user input.
     */

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getPermissionToReadSMS(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
            != PackageManager.PERMISSION_GRANTED){
            if(shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_SMS )) {
                Toast.makeText(this, "Please give permission!!", Toast.LENGTH_SHORT).show();
            }

            requestPermissions(new String[] {Manifest.permission.READ_SMS},
            READ_SMS_PERMISSIONS_REQUEST);
        }
    }

    /**
     * void function refreshes the list of messages retrieved from sms in the arraylist.
     */
    public void refreshingInbox() {
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse
                ("content://sms/inbox"), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
        arrayAdapter.clear();
        do {
            String str = "SMS From: " + smsInboxCursor.getString(indexAddress) +
                    "\n" + smsInboxCursor.getString(indexBody) + "\n";
            arrayAdapter.add(str);
        } while (smsInboxCursor.moveToNext());
    }

    /**
     * Refresh the inbox at a given time when button is pushed.
     * @param view
     */
    public void refreshInbox(View view){
        refreshingInbox();
        Toast.makeText(getApplicationContext(),
                "Refreshing your inbox...", Toast.LENGTH_SHORT).show();
    }

    /**
     * sendMessage using values in edit text view. Checks to see if permission granted
     * in manifest.xml.
     * @param view
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void sendMessage(View view) {

        phoneNumber = phoneEditText.getText().toString();
        messageText = smsEditText.getText().toString();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            getPermissionToReadSMS();
        } else {

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, messageText, null, null);
            Toast.makeText(getApplicationContext(), "Your message has been sent.",
                    Toast.LENGTH_LONG).show();
            refreshingInbox();


        }

    }
    /**
     * On request of permission result, lets the user know when message has been sent
     * or message has failed. Depending on the swtich case of the request code.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[]
            , int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length ==0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(getApplicationContext(),
                            "ERROR: Could not send message", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            case READ_SMS_PERMISSIONS_REQUEST:{
                if (grantResults.length == 1 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Reading SMS permission allowed",
                            Toast.LENGTH_SHORT).show();
                    refreshingInbox();
                } else {
                    Toast.makeText(this, "Reading SMS permission not allowed",
                            Toast.LENGTH_SHORT).show();
                }

            }
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
    }



}
