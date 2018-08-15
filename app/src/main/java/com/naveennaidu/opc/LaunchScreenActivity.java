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

    Button changeNameButton;
    String doc;
    EditText doctorName;
    String doctorNameText;
    String hosp;
    EditText hospitalName;
    String hospitalNameText;
    Button newPatient;
    Button oldPatient;
    Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_screen);
        setTitle("Patient");

        newPatient = findViewById(R.id.newPatient);
        oldPatient = findViewById(R.id.oldPatient);
        hospitalName = findViewById(R.id.hospitalEditText);
        doctorName = findViewById(R.id.doctorEditText);
        saveButton = findViewById(R.id.saveButton);
        changeNameButton = findViewById(R.id.changeButton);
        changeNameButton.setVisibility(View.GONE);

        SharedPreferences preferences = getSharedPreferences("LOGIN", 0);
        hosp = preferences.getString("hosp", "");
        doc = preferences.getString("doc", "");

        if (!(hosp.matches("") || doc.matches(""))) {
            hospitalName.setVisibility(View.GONE);
            doctorName.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            changeNameButton.setVisibility(View.VISIBLE);
        }

        newPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hosp.matches("") || doc.matches("")) {
                    Toast.makeText(getApplicationContext(), "Hospital or Doctor name is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent goToNewPatient = new Intent(LaunchScreenActivity.this, CollectInformationActivity.class);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("");
                stringBuilder.append(hosp);
                goToNewPatient.putExtra("hospital", stringBuilder.toString());
                stringBuilder = new StringBuilder();
                stringBuilder.append("");
                stringBuilder.append(doc);
                goToNewPatient.putExtra("doctor", stringBuilder.toString());
                startActivity(goToNewPatient);
            }
        });

        oldPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hosp.matches("") || doc.matches("")) {
                    Toast.makeText(getApplicationContext(), "Hospital or Doctor name is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent goToNewPatient = new Intent(LaunchScreenActivity.this, CollectInformationActivity.class);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("");
                stringBuilder.append(hosp);
                goToNewPatient.putExtra("hospital", stringBuilder.toString());
                stringBuilder = new StringBuilder();
                stringBuilder.append("");
                stringBuilder.append(doc);
                goToNewPatient.putExtra("doctor", stringBuilder.toString());
                startActivity(goToNewPatient);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hospitalNameText = hospitalName.getText().toString();
                doctorNameText = doctorName.getText().toString();
                SharedPreferences.Editor editor = getSharedPreferences("LOGIN", 0).edit();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("");
                stringBuilder.append(hospitalNameText);
                editor.putString("hosp", stringBuilder.toString());
                stringBuilder = new StringBuilder();
                stringBuilder.append("");
                stringBuilder.append(doctorNameText);
                editor.putString("doc", stringBuilder.toString());
                editor.apply();
                hosp = hospitalNameText;
                doc = doctorNameText;
                hospitalName.setVisibility(View.GONE);
                doctorName.setVisibility(View.GONE);
                saveButton.setVisibility(View.GONE);
                changeNameButton.setVisibility(View.VISIBLE);
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(doctorName.getWindowToken(), 0);
            }
        });

        changeNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hospitalName.setVisibility(View.VISIBLE);
                doctorName.setVisibility(View.VISIBLE);
                saveButton.setVisibility(View.VISIBLE);
                changeNameButton.setVisibility(View.GONE);
                hospitalName.setText(hosp);
                doctorName.setText(doc);
                hospitalNameText = hospitalName.getText().toString();
                doctorNameText = doctorName.getText().toString();
                SharedPreferences.Editor editor = getSharedPreferences("LOGIN", 0).edit();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("");
                stringBuilder.append(hospitalNameText);
                editor.putString("hosp", stringBuilder.toString());
                stringBuilder = new StringBuilder();
                stringBuilder.append("");
                stringBuilder.append(doctorNameText);
                editor.putString("doc", stringBuilder.toString());
                editor.apply();
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(doctorName.getWindowToken(), 0);
            }
        });
    }
}
