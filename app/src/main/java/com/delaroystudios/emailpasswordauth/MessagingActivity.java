package com.delaroystudios.emailpasswordauth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by delaroy on 4/4/18.
 */

public class MessagingActivity extends AppCompatActivity {
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        Intent intent = getIntent();
        String phoneNumber = intent.getExtras().getString("phone");

        textView = (TextView) findViewById(R.id.myTextView);

        textView.setText("This is the authenticated phone number " + phoneNumber);
    }
}
