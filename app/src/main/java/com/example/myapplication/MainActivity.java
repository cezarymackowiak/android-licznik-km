package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;

import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    LocationService myService;
    static boolean status;
    LocationManager locationManager;
    static TextView dist, time, speed, location;
    Button start, pause, stop;
    static long startTime, endTime;
    Switch simpleSwitch;
    static String unit = "km";
    static boolean km = true;

    static ProgressDialog locate;
    static int p = 0;


    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            myService = binder.getService();
            status = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            status = false;
        }
    };


    void bindService() {
        if (status == true)
            return;
        Intent i = new Intent(getApplicationContext(), LocationService.class);
        bindService(i, sc, BIND_AUTO_CREATE);
        status = true;
        startTime = System.currentTimeMillis();
    }

    void unbindService() {
        if (status == false)
            return;
        Intent i = new Intent(getApplicationContext(), LocationService.class);
        unbindService(sc);
        status = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (status == true)
            unbindService();
    }

    @Override
    public void onBackPressed() {
        if (status == false)
            super.onBackPressed();
        else
            moveTaskToBack(true);
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case 1000: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(myService, "GRANTED", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(myService, "DENIED", Toast.LENGTH_SHORT).show();

            }
            return;

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Prośba o pozwolenie na używanie usług
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1000);
        }

//Inicjacja przycisków i pól tekstowych
        dist = (TextView) findViewById(R.id.distancetext);
        time = (TextView) findViewById(R.id.timetext);
        speed = (TextView) findViewById(R.id.speedtext);
        location = (TextView) findViewById(R.id.locationText);

        pause = (Button) findViewById(R.id.pause);
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);

        simpleSwitch = (Switch) findViewById(R.id.simpleSwitch);


        simpleSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (simpleSwitch.isChecked()) {
                    km = false;
                    simpleSwitch.setText("m");
                    unit = "m";
                } else {
                    km = true;
                    simpleSwitch.setText("km");
                    unit = "km";
                }

            }
        });


        start.setOnClickListener(new View.OnClickListener() {
                                     @Override
                                     public void onClick(View v) {

                                         checkGps();
                                         locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                                         if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                                             return;
                                         if (status == false) {
                                             bindService();
                                             locate = new ProgressDialog(MainActivity.this);
                                             locate.setIndeterminate(true);
                                             locate.setCancelable(false);
                                             locate.setMessage("Pobieranie lokalizacji...");
                                             locate.show();

                                             start.setVisibility(View.GONE);
                                             pause.setVisibility(View.VISIBLE);
                                             pause.setText("Pauza");
                                             stop.setVisibility(View.VISIBLE);
                                         }
                                     }

                                 }
        );
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pause.getText().toString().equalsIgnoreCase("Pauza")) {
                    pause.setText("Wznów");
                    p = 1;
                } else if (pause.getText().toString().equalsIgnoreCase("Wznów")) {
                    checkGps();
                    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                        return;
                    pause.setText("Pauza");
                    p = 0;
                }
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status == true)
                    unbindService();
                start.setVisibility(View.VISIBLE);
                pause.setText("Pauza");
                pause.setVisibility(View.GONE);
                stop.setVisibility(View.GONE);


            }
        });

    }


    //metoda z powiadomieniem o koniecznośći włączenia GPS.
    void checkGps() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            showGPSDisabledAlertToUser();
        }
    }

    //Konfiguracja alertDialog.
    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Włącz GPS aby używać aplikacji")
                .setCancelable(false)
                .setPositiveButton("Włącz GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("wróć",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

}
