package com.example.bzennn.tetheringhotspot;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
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
    private TextView textView, infTextView;
    private Process process;
    private BufferedReader reader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSUAccess();

        isTtlDefault = ttlIsDefault();

        textView = (TextView) findViewById(R.id.textView);
        infTextView = (TextView) findViewById(R.id.infTextView);

        setDefaultTtlOnNotificationClick();

        /*MobileAds.initialize(getApplicationContext(), "ca-app-pub-4687335904240792~8907355463");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);*/
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
        textView.setText(textViewStringByTTLValue());
        infTextView.setText("Changable TTL = "+changableTTLValue+"\nFor return to default value, tap again."+"\nFor change changable value go to settings.");
        changeRoungColorByTTLValue();
    }

    /*
        Main button handler
     */

    public void onMainButtonClick(View view) {
        if(isTtlDefault) {
            changeTTL(changableTTLValue);
            isTtlDefault = !isTtlDefault;
            changeRoundColor("#4aff44");
            openHotspotSettingsByPreference();
        } else {
            changeTTL(DEFAULT_TTL);
            isTtlDefault = !isTtlDefault;
            changeRoundColor("#ffff4444");
        }

        delay(100);
        textView.setText(textViewStringByTTLValue());
        reconnectNetwork();
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

    private void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void changeTTL(int ttlValue) {
        try {
            process = Runtime.getRuntime().exec("su && echo "+ttlValue+" > /proc/sys/net/ipv4/ip_default_ttl");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(ttlValue != DEFAULT_TTL) {
            notifyByPreference();
        }
    }

    private void reconnectNetwork() {
        toggleFlightMode(true);
        delay(1000);
        toggleFlightMode(false);
        //delay(100);
        //toggleFlightMode(false);
    }


    private void toggleFlightMode(boolean state) {
        if(loadReconnectPreference()) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if(state) {
                    try {
                        process = Runtime.getRuntime().exec("su && settings put global airplane_mode_on 1 && am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        process = Runtime.getRuntime().exec("su && settings put global airplane_mode_on 0 && am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Settings.System.putInt(this.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, state ? 0 : 1);
            }
        }
    }

    private void openHotspotSettingsByPreference() {
        if(loadHotspotSettingsOpenPreference()) {
            Intent hotspotSettingsIntent = new Intent();
            hotspotSettingsIntent.setClassName("com.android.settings", "com.android.settings.TetherSettings");
            startActivityForResult(hotspotSettingsIntent, 0);
        }
    }

    /*
        Notifications
     */

    public void notifyByPreference() {
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
            reconnectNetwork();
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

    /*
        Load preferences
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

    private boolean loadReconnectPreference() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(sharedPreferences.getBoolean("reconnect_preference", true)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean loadHotspotSettingsOpenPreference() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(sharedPreferences.getBoolean("open_hotspot_preferences", true)) {
            return true;
        } else {
            return false;
        }
    }

    /*
        Utilities
     */
    private void getSUAccess() {
        try {
            process = Runtime.getRuntime().exec("su");
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.superuser_toast), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
