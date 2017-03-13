package com.example.bzennn.tetheringhotspot;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity {
    private static final int DEFAULT_TTL = 64;

    private boolean isTtlDefault = true;

    private Intent intent;
    private ImageView imageView;
    private TextView textView;
    private Process process;
    private BufferedReader reader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);

        getSUAccess();
        isTtlDefault = ttlIsDefault();
        changeRoungColorByTTLValue();
        textView.setText(textViewStringByTTLValue());
        //textView.setText(Integer.parseInt(getTTLValue())+"");
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

    public void onMainButtonClick(View view) {
        if(isTtlDefault) {
            try {
                process = Runtime.getRuntime().exec("su && echo "+63+" > /proc/sys/net/ipv4/ip_default_ttl");
            } catch (IOException e) {
                e.printStackTrace();
            }
            isTtlDefault = !isTtlDefault;
            changeRoundColor("#4aff44");
            textView.setText(textViewStringByTTLValue());
        } else {
            try {
                process = Runtime.getRuntime().exec("su && echo "+DEFAULT_TTL+" > /proc/sys/net/ipv4/ip_default_ttl");
            } catch (IOException e) {
                e.printStackTrace();
            }
            isTtlDefault = !isTtlDefault;
            changeRoundColor("#ffff4444");
            textView.setText(textViewStringByTTLValue());
        }
    }



    private void changeRoundColor(String color){
        imageView = (ImageView) findViewById(R.id.imageView);
        GradientDrawable drawable = (GradientDrawable) imageView.getBackground();
        drawable.setColor(Color.parseColor(color));
    }

    private void getSUAccess(){
        try {
            process = Runtime.getRuntime().exec("su");
        } catch (IOException e) {

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
}
