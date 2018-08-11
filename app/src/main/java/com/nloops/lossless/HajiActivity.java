package com.nloops.lossless;

import android.Manifest;
import android.Manifest.permission;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.clustering.ClusterManager;
import com.nloops.lossless.models.MyItem;
import com.nloops.lossless.utilis.Constants;
import com.nloops.lossless.utilis.MyItemReader;
import com.nloops.lossless.utilis.Utilis;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.json.JSONException;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Fetch Data from Backend to track camping Guide and allow to Draw route and using
 * Google Maps Routing we can start routing
 */
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
  /*Bind Activity Views using ButterKnife*/
  @BindView(R.id.edit_map_search)
  EditText mSearchEditText;
  @BindView(R.id.btn_edit_search)
  ImageButton mSearchButton;
  /*ref of Geofire to update guide current location in the Firebase DB*/
  private GeoFire mGeoFire;
  /*When Haji click on Marker this location will hold the Coordinates of guide location*/
  private LatLng mRouteLocation;
  /*Global Marker ref*/
  private Marker guideMarker;
  @BindView(R.id.search_bar_container)
  CardView mSearchBarCardView;
  /*this arrayList will draw the route between two points*/
  private List<Polyline> polylines;
  @BindView(R.id.dest_views_container)
  CardView mDestCardView;
  @BindView(R.id.tv_dest_name)
  TextView mDestName;
  @BindView(R.id.tv_dest_duration)
  TextView mDestDuration;
  @BindView(R.id.cancel_route)
  Button mCancelRoute;
  @BindView(R.id.dest_start_container)
  CardView mStartCardView;
  @BindView(R.id.tv_start_name)
  TextView mStartName;
  @BindView(R.id.tv_start_duration)
  TextView mStartDuration;
  @BindView(R.id.start_route)
  Button mStartRoute;
  /*Create ArrayList Whichs holds mock locations data*/
  ArrayList<LatLng> locations;
  /*index to loop on Mock Guide Locations*/
  int index = 0;
  private boolean isFirstLoop;
  private boolean isRouting;
  /*Global Marker for User*/
  private Marker userMarker;
  /*Current Mock Location*/
  private Location currentLocation;
  /*Draw a panch of locations*/
  private ClusterManager<MyItem> mClusterManager;

  public static void hideKeyboardFrom(Context context, View view) {
    InputMethodManager imm = (InputMethodManager) context
        .getSystemService(Activity.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_haji);
    /*Init Views*/
    ButterKnife.bind(this);
    /*first we need to get user permissions to access his location*/
    getPermissions();
    /*init Poly ArrayList*/
    polylines = new ArrayList<>();
    /*Set flag true*/
    isFirstLaunchFlag = true;
    isFirstLoop = true;
    currentLocation = new Location("");
    /*Get Mock Locations*/
    locations = Utilis.createLatLngTable();
    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
    /*Search On Marker in the Map*/
    mSearchButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mSearchEditText.length() <= 0 ||
            mClusterManager.getMarkerCollection().getMarkers() == null) {
          return;
        }
        Collection<Marker> mapMarkers = mClusterManager.getMarkerCollection().getMarkers();
        for (Marker marker : mapMarkers) {
          if (marker.getTitle().equals(mSearchEditText.getText().toString())) {
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_camping_marker));
            marker.showInfoWindow();
            mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
          }
        }
      }
    });

    mSearchEditText.setOnEditorActionListener(new OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          if (mSearchEditText.length() <= 0 ||
              mClusterManager.getMarkerCollection().getMarkers() == null) {
            return false;
          }
          Collection<Marker> mapMarkers = mClusterManager.getMarkerCollection().getMarkers();
          for (Marker marker : mapMarkers) {
            if (marker.getTitle().equals(mSearchEditText.getText().toString())) {
              marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_camping_marker));
              marker.showInfoWindow();
              mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
              mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
              hideKeyboardFrom(HajiActivity.this, mSearchEditText);
            }
          }
          return true;
        }
        return false;
      }
    });

    mStartRoute.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mStartCardView.setVisibility(View.INVISIBLE);
        LatLng myLocation = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        mDestCardView.setVisibility(View.VISIBLE);
        mSearchBarCardView.setVisibility(View.INVISIBLE);

        if (mRouteLocation != null) {
          setUpRouting(myLocation, mRouteLocation, false);
          FirebaseDatabase database = FirebaseDatabase.getInstance();
          DatabaseReference databaseReference = database.getReference(Constants.DATABASE_ROOT_NODE)
              .child(Constants.DATABASE_CURRENT_CAMPING)
              .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
          LatLng latLng = new LatLng(myLocation.latitude, myLocation.longitude);
          databaseReference.setValue(latLng);
        }

      }
    });

    mCancelRoute.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mDestCardView.setVisibility(View.INVISIBLE);
        mSearchBarCardView.setVisibility(View.VISIBLE);
        erasePolys();
      }
    });

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

    buildGoogleApiClient();
    mMap.setMyLocationEnabled(true);

    mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
      @Override
      public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        mRouteLocation = marker.getPosition();
        mStartCardView.setVisibility(View.VISIBLE);
        mSearchBarCardView.setVisibility(View.INVISIBLE);
        mStartName.setText(marker.getTitle());
        return true;
      }
    });

    mMap.setOnMapClickListener(new OnMapClickListener() {
      @Override
      public void onMapClick(LatLng latLng) {
        if (mStartCardView.getVisibility() == View.VISIBLE) {
          mStartCardView.setVisibility(View.INVISIBLE);
          mSearchBarCardView.setVisibility(View.VISIBLE);
        }
      }
    });
    mClusterManager = new ClusterManager<MyItem>(this, mMap);
    mMap.setOnCameraIdleListener(mClusterManager);
    try {
      readItems();
    } catch (JSONException e) {
      Log.i(TAG, "onMapReady: " + e.getMessage());
      Toast.makeText(this, "Problem reading list of markers.", Toast.LENGTH_LONG).show();
    }

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

  private void setUpRouting(LatLng startPoint, LatLng endPoint, boolean shouldUpdate) {
    erasePolys();
    Routing routing = new Routing.Builder()
        .travelMode(AbstractRouting.TravelMode.DRIVING)
        .withListener(this)
        .alternativeRoutes(true)
        .waypoints(startPoint, endPoint)
        .build();
    routing.execute();
    if (shouldUpdate) {
      isRouting = true;
    }
  }

  @Override
  public void onRoutingFailure(RouteException e) {
    if (e != null) {
      Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
    } else {
      Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * This method will called every time location updates.
   *
   * @param location {@link Location}
   */
  @Override
  public void onLocationChanged(Location location) {
    /*if user moves this method will smoothly moves user marker on map*/
    if (userMarker != null) {
      moveHajiOnMap(userMarker, location);
    }
    /*Set location to current updated location*/
    mLocation = location;
    if (isFirstLaunchFlag) {
      isFirstLaunchFlag = false;
      /*Convert user coordinates into LatLng object.*/
      LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
      userMarker = mMap.addMarker(new MarkerOptions().position(userLatLng)
          .title(getString(R.string.user_your_location))
          .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_user_marker)));
      userMarker.showInfoWindow();
      mMap.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));
      mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }
    /*Launch the Guide Mock Scenario*/
    LatLng currentLat = locations.get(index);
    currentLocation.setLatitude(currentLat.latitude);
    currentLocation.setLongitude(currentLat.longitude);
    if (guideMarker != null) {
      moveHajiOnMap(guideMarker, currentLocation);
    }
    if (isFirstLoop) {
      isFirstLoop = false;
      guideMarker = mMap.addMarker(new MarkerOptions()
          .position(currentLat).title(getString(R.string.user_your_guide_location))
          .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_guide_marker)));
      guideMarker.showInfoWindow();
    }
    if (index + 1 == 5) {
      index = 0;
    } else {
      index++;
    }
    if (isRouting) {
      isRouting = false;
      LatLng userLatLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
      setUpRouting(userLatLng, currentLat, true);
    }

  }

  @Override
  public void onRoutingStart() {
    /*To implemented in the future*/
  }

  @Override
  public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
    LatLng latLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
    CameraUpdate center = CameraUpdateFactory.newLatLng(latLng);
    CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

    mMap.moveCamera(center);
    mMap.animateCamera(zoom);

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

      /*Toast.makeText(getApplicationContext(),
          "Route " + (i + 1) + ": distance - " + route.get(i).getDistanceValue() + ": duration - "
              + route.get(i).getDurationValue(), Toast.LENGTH_SHORT).show();*/
      mDestName.setText(route.get(i).getDistanceText());
      mDestDuration.setText(route.get(i).getDurationText());
    }

  }

  @Override
  public void onRoutingCancelled() {
    /*To implemented in the future*/
  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    Log.i(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
    Toast.makeText(getApplicationContext(),
        getString(R.string.google_api_lost), Toast.LENGTH_LONG).show();
  }

  @Override
  public void onConnectionSuspended(int i) {
    /*To implemented in the future*/
  }

  /**
   * This Method will read attached raw JSONfile to add collection of static markers on map
   */
  private void readItems() throws JSONException {
    InputStream inputStream = getResources().openRawResource(R.raw.arrafat_coordinate);
    List<MyItem> items = new MyItemReader().read(inputStream);
    mClusterManager.addItems(items);
  }

  public void moveHajiOnMap(final Marker myMarker, final Location finalPosition) {

    final LatLng startPosition = myMarker.getPosition();

    final Handler handler = new Handler();
    final long start = SystemClock.uptimeMillis();
    final Interpolator interpolator = new AccelerateDecelerateInterpolator();
    final float durationInMs = 3000;
    final boolean hideMarker = false;

    handler.post(new Runnable() {
      long elapsed;
      float t;
      float v;

      @Override
      public void run() {
        // Calculate progress using interpolator
        elapsed = SystemClock.uptimeMillis() - start;
        t = elapsed / durationInMs;
        v = interpolator.getInterpolation(t);

        LatLng currentPosition = new LatLng(
            startPosition.latitude * (1 - t) + (finalPosition.getLatitude()) * t,
            startPosition.longitude * (1 - t) + (finalPosition.getLongitude()) * t);
        myMarker.setPosition(currentPosition);

        // Repeat till progress is completeelse
        if (t < 1) {
          // Post again 16ms later.
          handler.postDelayed(this, 16);
        } else {
          if (hideMarker) {
            myMarker.setVisible(false);
          } else {
            myMarker.setVisible(true);
          }
        }
      }
    });


  }

  private void erasePolys() {
    isRouting = false;
    for (Polyline line : polylines) {
      line.remove();
    }
    polylines.clear();
  }


}
