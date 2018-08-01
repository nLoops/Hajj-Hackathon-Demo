package com.nloops.lossless;

import android.Manifest;
import android.Manifest.permission;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;
import pub.devrel.easypermissions.EasyPermissions;

public class HajiActivity extends FragmentActivity implements OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
    com.google.android.gms.location.LocationListener, RoutingListener {

  /*Declare TAG to set Logs*/
  private static final String TAG = HajiActivity.class.getSimpleName();
  /*final int code to ensure to get the permission for this specific activity.*/
  private static final int PERMISSION_REQ_CODE = 101;
  private GoogleMap mMap;
  private GoogleApiClient mGoogleClient;
  private Location mLocation;
  private LocationRequest mLocationRequest;
  private boolean isFirstLaunchFlag;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_haji);
    /*first we need to get user permissions to access his location*/
    getPermissions();
    /*Set flag true*/
    isFirstLaunchFlag = true;
    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
  }


  /**
   * This Method will check if we have the required permissions to RECORD and SAVE files, if not we
   * will alert USER to get the permissions.
   */
  @TargetApi(23)
  private void getPermissions() {
    String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
        permission.ACCESS_COARSE_LOCATION, permission.ACCESS_FINE_LOCATION};
    if (!EasyPermissions.hasPermissions(HajiActivity.this, permissions)) {
      EasyPermissions.requestPermissions(this,
          getString(R.string.permissions_required),
          PERMISSION_REQ_CODE, permissions);
    }
  }

  @TargetApi(23)
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;

    if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      return;
    }
    //setLocationsMarker();
    buildGoogleApiClient();
    mMap.setMyLocationEnabled(true);
  }

  /**
   * This method will setup the {@link GoogleApiClient} and to ensure that we have all GoogleAPI
   * features.
   */
  private void buildGoogleApiClient() {
    mGoogleClient = new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(LocationServices.API)
        .build();
    mGoogleClient.connect();
  }


  @Override
  public void onConnected(@Nullable Bundle bundle) {
    mLocationRequest = new LocationRequest();
    mLocationRequest.setInterval(1000/*In Milliseconds*/);
    mLocationRequest.setFastestInterval(1000/*In Milliseconds*/);
    /*if you don't need that don't use it because it ruins battery*/
    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      return;
    }
    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleClient, mLocationRequest
        , HajiActivity.this);
  }


  @Override
  public void onLocationChanged(Location location) {
    /*Set location to current updated location*/
    mLocation = location;
    if (isFirstLaunchFlag) {
      isFirstLaunchFlag = false;
      /*Convert user coordinates into LatLng object.*/
      LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
      mMap.addMarker(new MarkerOptions().position(userLatLng)
          .title(getString(R.string.user_your_location)));
      mMap.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));
      mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
    }
  }

  private void setLocationsMarker() {

  }


  @Override
  public void onRoutingFailure(RouteException e) {

  }

  @Override
  public void onRoutingStart() {

  }

  @Override
  public void onRoutingSuccess(ArrayList<Route> arrayList, int i) {

  }

  @Override
  public void onRoutingCancelled() {

  }


  @Override
  public void onConnectionSuspended(int i) {

  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    Log.i(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
    Toast.makeText(getApplicationContext(),
        getString(R.string.google_api_lost), Toast.LENGTH_LONG).show();
  }

}
