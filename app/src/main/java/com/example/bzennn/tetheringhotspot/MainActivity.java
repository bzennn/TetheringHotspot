package com.example.bzennn.tetheringhotspot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    private static final int DEFAULT_TTL = 64;

    private boolean isTtlDefault = true;
    private int changableTTLValue;

    private Intent intent;
    private ImageView imageView;
    private TextView textView;
    private Process process;
    private BufferedReader reader;
    //private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSUAccess();

        isTtlDefault = ttlIsDefault();
        changeRoungColorByTTLValue();
        textView = (TextView) findViewById(R.id.textView);
        textView.setText(textViewStringByTTLValue());


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            default:
                return super.onOptionsItemSelected(item);

            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_about:
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        changableTTLValue = loadTTLValuePreference();
    }

    public void onMainButtonClick(View view) {
        if(isTtlDefault) {
            try {
                process = Runtime.getRuntime().exec("su && echo "+changableTTLValue+" > /proc/sys/net/ipv4/ip_default_ttl");
            } catch (IOException e) {
                e.printStackTrace();
            }
            isTtlDefault = !isTtlDefault;
            changeRoundColor("#4aff44");
        } else {
            try {
                process = Runtime.getRuntime().exec("su && echo "+DEFAULT_TTL+" > /proc/sys/net/ipv4/ip_default_ttl");
            } catch (IOException e) {
                e.printStackTrace();
            }
            isTtlDefault = !isTtlDefault;
            changeRoundColor("#ffff4444");
        }

        try {
            Thread.sleep(100);
            textView.setText(textViewStringByTTLValue());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    private void changeRoundColor(String color){
        imageView = (ImageView) findViewById(R.id.mainButtonBackground);
        GradientDrawable drawable = (GradientDrawable) imageView.getBackground();
        drawable.setColor(Color.parseColor(color));
    }

    private void getSUAccess(){
        try {
            process = Runtime.getRuntime().exec("su");
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.superuser_toast), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private boolean ttlIsDefault(){
        if(Integer.parseInt(getTTLValue()) == DEFAULT_TTL){
            return true;
        } else {
            return false;
        }
    }

    private String getTTLValue(){
        String ttl_value = Integer.toString(DEFAULT_TTL);
        try {
            reader = new BufferedReader(new FileReader("/proc/sys/net/ipv4/ip_default_ttl"));
            ttl_value = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ttl_value;
    }

    private void changeRoungColorByTTLValue(){
        if(isTtlDefault){
            changeRoundColor("#ffff4444");
        }else{
            changeRoundColor("#4aff44");
        }
    }

    private String textViewStringByTTLValue(){
        if(isTtlDefault){
            return getString(R.string.ttl_is_not_modified);
        }else{
            return getString(R.string.tts_is_modified) + getTTLValue();
        }
    }

    private int loadTTLValuePreference() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int temp_ttl = 63;

        if(sharedPreferences.getBoolean("check_box_preference_2", false)) {
            if(sharedPreferences.getString("edit_text_preference_1", "63") != "") {
                if(Integer.parseInt(sharedPreferences.getString("edit_text_preference_1", "63")) < 256) {
                    temp_ttl = Integer.parseInt(sharedPreferences.getString("edit_text_preference_1", "63"));
                }
            }
        } else {
            if(sharedPreferences.getBoolean("check_box_preference_1", true)) {
                temp_ttl = 63;
            } else {
                temp_ttl = 127;
            }
        }

        return temp_ttl;
    }
}
