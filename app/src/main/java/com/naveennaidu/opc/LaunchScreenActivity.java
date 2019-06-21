package com.naveennaidu.opc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LaunchScreenActivity extends AppCompatActivity {

    Button newPatient;
    Button oldPatient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_screen);
        setTitle("HOME");

        newPatient = findViewById(R.id.newPatient);
        oldPatient = findViewById(R.id.oldPatient);


        newPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Intent goToNewPatient = new Intent(LaunchScreenActivity.this, CollectInformationActivity.class);
            goToNewPatient.putExtra("hospital", getIntent().getStringExtra("capturerOrg"));
            goToNewPatient.putExtra("doctor", getIntent().getStringExtra("capturerName"));
            startActivity(goToNewPatient);
            }
        });

        oldPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToOldPatient = new Intent(LaunchScreenActivity.this, PatientDatabaseActivity.class);
                startActivity(goToOldPatient);
            }
        });
    }
}
