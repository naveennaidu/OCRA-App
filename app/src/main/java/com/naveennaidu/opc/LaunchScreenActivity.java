package com.naveennaidu.opc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LaunchScreenActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    Button newPatient;
    Button oldPatient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_screen);
        setTitle("HOME");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.nav_home) {
            Intent current = new Intent(this,this.getClass());
            Intent login = new Intent(this,LaunchScreenActivity.class);
            if(!current.filterEquals(login)) {
                startActivity(login);
            }
        }

        if (id == R.id.nav_logout) {
            Intent logout = new Intent(this, LoginScreenActivity.class);
            startActivity(logout);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
