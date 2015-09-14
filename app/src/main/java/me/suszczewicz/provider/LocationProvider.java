package me.suszczewicz.provider;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import me.suszczewicz.compass.BuildConfig;

public class LocationProvider extends StreamedDataProvider<Location> implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleApiClient googleApiClient;
    private boolean started;

    public LocationProvider(Context context) {
        super(context);

        connectToGoogleApi();
    }

    public void start() {
        started = true;

        if (googleApiClient.isConnected())
            registerForUpdates();
    }

    public void stop() {
        if (started && googleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);

        started = false;
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (started)
            registerForUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(BuildConfig.APPLICATION_ID, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        emitException(new Exception("Unable to connect to Google APi"));
    }

    @Override
    public void onLocationChanged(Location location) {
        emitData(location);
    }

    private void emitLastKnownLocation() {
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (lastLocation != null)
            emitData(lastLocation);
    }

    private void registerForUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, createLocationRequest(), this);
        emitLastKnownLocation();
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return locationRequest;
    }

    private void connectToGoogleApi() {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }
}
