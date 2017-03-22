package com.example.bzennn.tetheringhotspot;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSUAccess();

        isTtlDefault = ttlIsDefault();
        changeRoungColorByTTLValue();
        textView = (TextView) findViewById(R.id.textView);
        textView.setText(textViewStringByTTLValue());

        setDefaultTtlOnNotificationClick();
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

    /*
        Main button handler
     */

    public void onMainButtonClick(View view) {
        if(isTtlDefault) {
            changeTTL(changableTTLValue);
            isTtlDefault = !isTtlDefault;
            changeRoundColor("#4aff44");
        } else {
            changeTTL(DEFAULT_TTL);
            isTtlDefault = !isTtlDefault;
            changeRoundColor("#ffff4444");
        }

        delay(100);
        textView.setText(textViewStringByTTLValue());
    }



    private void changeRoundColor(String color) {
        imageView = (ImageView) findViewById(R.id.mainButtonBackground);
        GradientDrawable drawable = (GradientDrawable) imageView.getBackground();
        drawable.setColor(Color.parseColor(color));
    }

    private boolean ttlIsDefault() {
        if(Integer.parseInt(getTTLValue()) == DEFAULT_TTL) {
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

    private void changeRoungColorByTTLValue() {
        if(isTtlDefault){
            changeRoundColor("#ffff4444");
        } else {
            changeRoundColor("#4aff44");
        }
    }

    private String textViewStringByTTLValue() {
        if(isTtlDefault){
            return getString(R.string.ttl_is_modified) + DEFAULT_TTL;
        } else {
            return getString(R.string.ttl_is_modified) + getTTLValue();
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

    private void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void changeTTL(int ttlValue) {
        try {
            process = Runtime.getRuntime().exec("su && echo "+ttlValue+" > /proc/sys/net/ipv4/ip_default_ttl");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(ttlValue != DEFAULT_TTL) {
            notifyByPreference();
        }
    }

    /*
        Notifications
     */

    private boolean loadNotificationsPreference() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(sharedPreferences.getBoolean("check_box_preference_4", true)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean loadNotificationsDefaultOnClickPreference() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(sharedPreferences.getBoolean("check_box_preference_5", true)) {
            return true;
        } else {
            return false;
        }
    }

    private void notifyByPreference() {
        if(loadNotificationsPreference()) {
            buildNotification();
        }
    }

    private void setDefaultTtlOnNotificationClick() {
        if(getIntent().hasExtra("callDefault")) {
            changeTTL(DEFAULT_TTL);
            delay(100);
            changeRoundColor("#ffff4444");
            textView.setText(textViewStringByTTLValue());
            isTtlDefault = !isTtlDefault;
        }
    }

    private void buildNotification() {
        NotificationCompat.Builder nBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_text)+" "+changableTTLValue);

        nBuilder.setAutoCancel(true);

        Intent resultIntent = new Intent(this, MainActivity.class);
        if(loadNotificationsDefaultOnClickPreference()) {
            resultIntent.putExtra("callDefault", true);
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        nBuilder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, nBuilder.build());
    }

    private void getSUAccess() {
        try {
            process = Runtime.getRuntime().exec("su");
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.superuser_toast), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
