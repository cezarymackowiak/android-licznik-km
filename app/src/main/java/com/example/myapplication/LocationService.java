package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;



import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class LocationService extends Service implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
{

    private static final long INTERVAL = 1000*2;
    private static final long FASTEST_INTERVAL = 1000*1;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation, lStart, lEnd;
    static double distance = 0;
    double speed;

    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        return mBinder;
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return super.onStartCommand(intent, flags,startId);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient,  this);
        distance = 0;
    }

    @Override
    public void onLocationChanged(Location location) {
       MainActivity.locate.dismiss();
        mCurrentLocation = location;
        if (lStart == null) {
            lStart = mCurrentLocation;
            lEnd = mCurrentLocation;
        } else
            lEnd = mCurrentLocation;

        //odwołanie do tej metody powoduje aktualizowanie danych( dystans i prędkość) na żywo do TextViews.
        updateUI();

        //Prędkość z tej metody podawana jest w metrach na sekundę
        speed = location.getSpeed()*18/5;
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,  this);
        }
        catch(SecurityException e){

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    public class LocalBinder extends Binder {

        public LocationService getService() {
            return LocationService.this;
        }
    }

    private void updateUI() {
        if (MainActivity.p == 0) {
            String minutes = " minut";

            distance = distance + (lStart.distanceTo(lEnd)/1000 );

            MainActivity.location.setText(("La : " + lStart.getLatitude()) + "\n" + " Lo : " +(lStart.getLongitude())  );
            MainActivity.endTime = System.currentTimeMillis();
            long diff = MainActivity.endTime - MainActivity.startTime;
            diff = TimeUnit.MILLISECONDS.toMinutes(diff);


            // w zaleznosci od ilosci minut wyswietlana jest odpowiednia wersja słowa minut
            if(diff == 1){
                minutes = " minuta";
            }
            if(diff>1 && diff <5){
                minutes = " minuty";
            }
            if(diff >4){
                minutes = " minut";
            }
            MainActivity.time.setText("Czas: " + diff + minutes);

            if (speed > 0.0)
                MainActivity.speed.setText("Aktualna prędkość: " + new DecimalFormat("#.##").format(speed) + " km/h");
            else
                MainActivity.speed.setText(".......");

         // gdy włączone są km  wynik wyświetlany jest w KM
            if(MainActivity.km){
            MainActivity.dist.setText(new DecimalFormat("#.###").format(distance) + MainActivity.unit);}

         // w przypadku ustawienia metrów wyświetlany jest wynik przemnożony przez 1000.
            if(!MainActivity.km){
            MainActivity.dist.setText(new DecimalFormat("#.###").format(distance*1000) + MainActivity.unit);}

            lStart = lEnd;

        }
    }
    public boolean onUnbind(Intent intent) {
        stopLocationUpdates();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        lStart = null;
        lEnd = null;
        distance = 0;
        return super.onUnbind(intent);
    }
}

