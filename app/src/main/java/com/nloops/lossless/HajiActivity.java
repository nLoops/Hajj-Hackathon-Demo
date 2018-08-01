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
import android.view.View;
import android.widget.Toast;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.nloops.lossless.utilis.Constants;
import java.util.ArrayList;
import java.util.List;
import pub.devrel.easypermissions.EasyPermissions;

public class HajiActivity extends FragmentActivity implements OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
    com.google.android.gms.location.LocationListener, RoutingListener {

  /*Declare TAG to set Logs*/
  private static final String TAG = HajiActivity.class.getSimpleName();
  /*final int code to ensure to get the permission for this specific activity.*/
  private static final int PERMISSION_REQ_CODE = 101;
  /*this colors array will draw the routes*/
  private static final int[] COLORS = new int[]{R.color.colorPrimary, R.color.colorAccent,
      R.color.primary_dark_material_light};
  /*ref of GoogleMap object*/
  private GoogleMap mMap;
  /*ref of GoogleApiClient*/
  private GoogleApiClient mGoogleClient;
  /*ref of Updated Location*/
  private Location mLocation;
  /*ref of GuideLocation*/
  private LatLng mGuideLocation;
  /*ref of GoogleMap Location Request*/
  private LocationRequest mLocationRequest;
  /*this flag to set first run Location*/
  private boolean isFirstLaunchFlag;
  /*ref of Geofire to update guide current location in the Firebase DB*/
  private GeoFire mGeoFire;
  /*When Haji click on Marker this location will hold the Coordinates of guide location*/
  private LatLng mRouteLocation;
  /*Global Marker ref*/
  private Marker guideMarker;
  /*this arrayList will draw the route between two points*/
  private List<Polyline> polylines;
  /*Test location for testing routes*/
  private LatLng testLocation;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_haji);
    /*first we need to get user permissions to access his location*/
    getPermissions();
    /*init Poly ArrayList*/
    polylines = new ArrayList<>();
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
    testLocation = new LatLng(21.618585, 39.158023);
    mMap.addMarker(new MarkerOptions().position(testLocation).title("Test Position"));
    buildGoogleApiClient();
    mMap.setMyLocationEnabled(true);

    mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
      @Override
      public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        mRouteLocation = marker.getPosition();
        return true;
      }
    });
  }

  public void setLostRequest(View view) {
    LatLng myLocation = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
    Routing routing = new Routing.Builder()
        .travelMode(AbstractRouting.TravelMode.DRIVING)
        .withListener(this)
        .alternativeRoutes(true)
        .waypoints(myLocation, testLocation)
        .build();
    Log.i(TAG, "setLostRequest: " + myLocation.toString() + " " + testLocation.toString());
    routing.execute();
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


  /**
   * When Connecting with {@link #mGoogleClient} this method will setup our LocationRequest.
   *
   * @param bundle {@link Bundle}
   */
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

  /**
   * This method will called every time location updates.
   *
   * @param location {@link Location}
   */
  @Override
  public void onLocationChanged(Location location) {
    /*Set location to current updated location*/
    mLocation = location;
    if (isFirstLaunchFlag) {
      isFirstLaunchFlag = false;
      /*Convert user coordinates into LatLng object.*/
      LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
      Marker userMarker = mMap.addMarker(new MarkerOptions().position(userLatLng)
          .title(getString(R.string.user_your_location)));
      userMarker.showInfoWindow();
      mMap.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));
      mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
    }

    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    mGeoFire = new GeoFire(mDatabase.getReference(Constants.GUIDE_GENERAL_REF));
    mGeoFire.getLocation(Constants.GUIDE_GEO_FIRE_KEY, new LocationCallback() {
      @Override
      public void onLocationResult(String key, GeoLocation location) {
        if (guideMarker != null) {
          guideMarker.remove();
        }
        mGuideLocation = new LatLng(location.latitude, location.longitude);
        guideMarker = mMap.addMarker(new MarkerOptions()
            .position(mGuideLocation).title("Your Guide"));

      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    });
  }

  @Override
  public void onRoutingFailure(RouteException e) {
    if (e != null) {
      Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
    } else {
      Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public void onRoutingStart() {

  }

  @Override
  public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
    LatLng latLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
    CameraUpdate center = CameraUpdateFactory.newLatLng(latLng);
    CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);

    mMap.moveCamera(center);

    if (polylines.size() > 0) {
      for (Polyline poly : polylines) {
        poly.remove();
      }
    }

    polylines = new ArrayList<>();
    //add route(s) to the map.
    for (int i = 0; i < route.size(); i++) {

      //In case of more than 5 alternative routes
      int colorIndex = i % COLORS.length;

      PolylineOptions polyOptions = new PolylineOptions();
      polyOptions.color(getResources().getColor(COLORS[colorIndex]));
      polyOptions.width(10 + i * 3);
      polyOptions.addAll(route.get(i).getPoints());
      Polyline polyline = mMap.addPolyline(polyOptions);
      polylines.add(polyline);

      Toast.makeText(getApplicationContext(),
          "Route " + (i + 1) + ": distance - " + route.get(i).getDistanceValue() + ": duration - "
              + route.get(i).getDurationValue(), Toast.LENGTH_SHORT).show();
    }

    // Start marker
    MarkerOptions options = new MarkerOptions();
    options.position(latLng);
    mMap.addMarker(options);

    // End marker
    options = new MarkerOptions();
    options.position(mGuideLocation);
    //options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map));
    mMap.addMarker(options);
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
