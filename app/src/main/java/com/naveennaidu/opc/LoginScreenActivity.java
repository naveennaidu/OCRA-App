package com.naveennaidu.opc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class LoginScreenActivity extends AppCompatActivity {

    ImageView appLogo;
    EditText capturerName;
    EditText capturerOrg;
    Button loginButton;

    String savedCapturerName;
    String SavedCapturerOrg;

    String cName;
    String cOrg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_login);

        capturerName = findViewById(R.id.capturerId);
        capturerOrg = findViewById(R.id.capturerOrg);
        loginButton = findViewById(R.id.loginButton);

        SharedPreferences preferences = getSharedPreferences("LOGIN", 0);
        cName = preferences.getString("savedCapturerName", "");
        cOrg = preferences.getString("SavedCapturerOrg", "");

        if (!(cName.matches("") || cOrg.matches(""))) {
            Intent goToHomeScreen = new Intent(LoginScreenActivity.this, LaunchScreenActivity.class);

            goToHomeScreen.putExtra("capturerName", savedCapturerName);
            goToHomeScreen.putExtra("capturerOrg", SavedCapturerOrg);
            startActivity(goToHomeScreen);
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savedCapturerName = capturerName.getText().toString();
                SavedCapturerOrg = capturerOrg.getText().toString();

                SharedPreferences.Editor editor = getSharedPreferences("LOGIN", 0).edit();

                editor.putString("savedCapturerName", savedCapturerName);
                editor.putString("SavedCapturerOrg", SavedCapturerOrg);
                editor.apply();

                if (savedCapturerName.matches("") || SavedCapturerOrg.matches("")) {
                    Toast.makeText(getApplicationContext(), "Name and Organization is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent goToHomeScreen = new Intent(LoginScreenActivity.this, LaunchScreenActivity.class);

                goToHomeScreen.putExtra("capturerName", savedCapturerName);
                goToHomeScreen.putExtra("capturerOrg", SavedCapturerOrg);
                startActivity(goToHomeScreen);
            }
        });
    }
}
