package com.naveennaidu.opc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

public class LoginScreenActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    ImageView appLogo;
    EditText capturerName;
    EditText capturerOrg;
    Button loginButton;

    String savedCapturerName;
    String SavedCapturerOrg;

    String cName;
    String cOrg;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();

        if (v != null &&
                (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) &&
                v instanceof EditText &&
                !v.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            v.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + v.getLeft() - scrcoords[0];
            float y = ev.getRawY() + v.getTop() - scrcoords[1];

            if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom())
                hideKeyboard(this);
        }
        return super.dispatchTouchEvent(ev);
    }

    public static void hideKeyboard(Activity activity) {
        if (activity != null && activity.getWindow() != null && activity.getWindow().getDecorView() != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_login);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        capturerName = findViewById(R.id.capturerId);
        capturerOrg = findViewById(R.id.capturerOrg);
        loginButton = findViewById(R.id.loginButton);

        SharedPreferences preferences = getSharedPreferences("LOGIN", 0);
        cName = preferences.getString("savedCapturerName", "");
        cOrg = preferences.getString("SavedCapturerOrg", "");


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

        if (id == R.id.nav_login) {
            Intent current = new Intent(this,this.getClass());
            Intent login = new Intent(this,LoginScreenActivity.class);
            if(!current.filterEquals(login)) {
                startActivity(login);
            }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
