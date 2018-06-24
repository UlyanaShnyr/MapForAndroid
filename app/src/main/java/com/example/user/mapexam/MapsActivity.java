
package com.example.user.mapexam;

import android.Manifest;
import android.bluetooth.BluetoothClass;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.service.media.MediaBrowserService;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.user.mapexam.Model.Devices;
import com.example.user.mapexam.Model.MyPlaces;
import com.example.user.mapexam.Remote.Common;
import com.example.user.mapexam.Remote.IGoogleAPIService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener
{
    private static final int MY_PERMISION_CODE =1000 ;
    private GoogleMap mMap;
    private  GoogleApiClient mGoogleApiClient;

    private double latitude,longitude;
    private Location mLastLocation;
    private Marker mMarker;
    private LocationRequest mLocationRequest;

    IGoogleAPIService mServices;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mServices= Common.getGoogleApiService();

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            checkLocationPermission();
        }
        BottomNavigationView bottomNavigationView= findViewById(R.id.bottom_navigation);
       bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
           @Override
           public boolean onNavigationItemSelected(@NonNull MenuItem item) {
             switch (item.getItemId()){
                 case R.id.action_atm:
                     nearByPlace("ATM");
                     break;
                     default:break;
             }
               return true;
           }
       });
    }

    private void nearByPlace(final String type) {
    mMap.clear();
    String url=getUrl(latitude,longitude,type);

    mServices.getNearByPlaces(url)
            .enqueue(new Callback<MyPlaces>() {
                @Override
                public void onResponse(Call<MyPlaces> call, Response<MyPlaces> response) {
                    if (response.isSuccessful()) {

                        for(int i=0;i<response.body().toString().length();i++){
                            MarkerOptions markerOptions=new MarkerOptions();
                            Devices googlePlace = response.body().getDevices()[i];
                            double lat = Double.parseDouble(googlePlace.getLatitude());
                            double lng=Double.parseDouble(googlePlace.getLongitude());
                            String placeName=googlePlace.getPlaceUa();
                            String fullAdress=googlePlace.getFullAddressUa();
                            LatLng latLng=new LatLng(lat,lng);
                            markerOptions.position(latLng);
                            markerOptions.title(placeName);

                            if(type.equals("ATM"))
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_atm));
                            else
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                            mMap.addMarker(markerOptions);
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

                        }
                    }
                }

                @Override
                public void onFailure(Call<MyPlaces> call, Throwable t) {

                }
            });

    }

    private String getUrl(double latitude, double longitude, String type) {
        StringBuilder googlePlacesUrl=new StringBuilder("https://api.privatbank.ua/p24api/infrastructure?json&atm&address=&city=Lviv");
        googlePlacesUrl.append("location="+latitude+","+longitude);
        googlePlacesUrl.append("&radius="+10000);
        googlePlacesUrl.append("&type="+type);
        googlePlacesUrl.append("&sensor=true");
        Log.d("getUrl",googlePlacesUrl.toString());
        return  googlePlacesUrl.toString();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISION_CODE:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                        if(mGoogleApiClient==null){
                            buildGoogleApiClien();
                            mMap.setMyLocationEnabled(true);
                        }
                    }else
                        Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show();
                }break;
        }
    }

    private boolean checkLocationPermission()
    {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, MY_PERMISION_CODE);
            }
            else
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, MY_PERMISION_CODE);

            return false;
        }
        else
            return true;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
    {
        buildGoogleApiClien();
        mMap.setMyLocationEnabled(true);
    }
    
}else{
    buildGoogleApiClien();
    mMap.setMyLocationEnabled(true);
}
    }

    private synchronized void buildGoogleApiClien() {
        mGoogleApiClient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest=new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
        {
LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation=location;
        if(mMarker!=null){
            mMarker.remove();


            latitude=location.getLatitude();
            longitude=location.getLongitude();

            LatLng latLng=new LatLng(latitude,longitude);
            MarkerOptions markerOptions=new MarkerOptions()
                    .position(latLng)
                    .title("Your position")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            mMarker=mMap.addMarker(markerOptions);

            //Move camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

            if(mGoogleApiClient!=null)
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);


        }

    }
}



























