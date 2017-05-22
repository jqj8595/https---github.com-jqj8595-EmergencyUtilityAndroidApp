package com.example.androidmessage.androidmessage;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainMenu extends AppCompatActivity {
    private Button button1;
    private Button button2;
    private Button button3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        button1 = (Button)findViewById(R.id.button1);
        button2 = (Button)findViewById(R.id.button2);
        button3 = (Button)findViewById(R.id.button3);


        //button for bluetooth
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainMenu.this, Bluetooth.class);
                startActivity(intent);
                finish();
                Toast.makeText(getApplicationContext(), "Opening Bluetooth console",
                        Toast.LENGTH_SHORT).show();
            }
        });
    //button for messaging
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenu.this, MainActivity.class);
                startActivity(intent);
                finish();
                Toast.makeText(getApplicationContext(), "Opening your SMS messaging platform",
                        Toast.LENGTH_SHORT).show();
            }
        });
        //button for map activity and location tracking.
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenu.this, MapsActivity.class);
                startActivity(intent);
                Toast.makeText(getApplicationContext(), "Searching where you are now... One moment please",
                        Toast.LENGTH_SHORT).show();
            }
        });





    }
}
