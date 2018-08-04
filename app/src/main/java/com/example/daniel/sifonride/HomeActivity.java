package com.example.daniel.sifonride;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.daniel.sifonride.GoogleDirectionApi.DirectionFinderListener;
import com.example.daniel.sifonride.GoogleDirectionApi.FindTimeDistance;
import com.example.daniel.sifonride.GoogleDirectionApi.Route;
import com.example.daniel.sifonride.GoogleDirectionApi.TimeDistanceListener;
import com.example.daniel.sifonride.autocomplete.AutocompleteDestination;
import com.example.daniel.sifonride.autocomplete.AutocompletePickUp;
import com.example.daniel.sifonride.common.Common;
import com.example.daniel.sifonride.model.Util;
import com.example.daniel.sifonride.nav.Settings;
import com.example.daniel.sifonride.service.Booking;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.daniel.sifonride.common.Common.DEFAULT_ZOOM;
import static com.example.daniel.sifonride.common.Common.DISPLACEMENT;
import static com.example.daniel.sifonride.common.Common.FASTEST_INTERVAL;
import static com.example.daniel.sifonride.common.Common.KEY_CAMERA_POSITION;
import static com.example.daniel.sifonride.common.Common.LOCATION_KEY;
import static com.example.daniel.sifonride.common.Common.REQUEST_CHECK_SETTINGS;
import static com.example.daniel.sifonride.common.Common.REQUEST_PERMISSION_CODE;
import static com.example.daniel.sifonride.common.Common.REQUEST_PERMISSION_PHONE_CODE;
import static com.example.daniel.sifonride.common.Common.SMALLEST_INTERVAL;
import static com.example.daniel.sifonride.common.Common.bookingBool;
import static com.example.daniel.sifonride.common.Common.driverHistory;
import static com.example.daniel.sifonride.common.Common.drivers;
import static com.example.daniel.sifonride.common.Common.fareDataBase;
import static com.example.daniel.sifonride.common.Common.mBikerBaseFare;
import static com.example.daniel.sifonride.common.Common.mBikerDistanceFare;
import static com.example.daniel.sifonride.common.Common.mBikerMinimumFare;
import static com.example.daniel.sifonride.common.Common.mBikerTimeFare;
import static com.example.daniel.sifonride.common.Common.mCurrentLocation;
import static com.example.daniel.sifonride.common.Common.mDestinationAddress;
import static com.example.daniel.sifonride.common.Common.mDestinationLatLng;
import static com.example.daniel.sifonride.common.Common.mDriverBaseFare;
import static com.example.daniel.sifonride.common.Common.mDriverDistanceFare;
import static com.example.daniel.sifonride.common.Common.mDriverMinimumFare;
import static com.example.daniel.sifonride.common.Common.mDriverNames;
import static com.example.daniel.sifonride.common.Common.mDriverPhone;
import static com.example.daniel.sifonride.common.Common.mDriverTimeFare;
import static com.example.daniel.sifonride.common.Common.mDriversKey;
import static com.example.daniel.sifonride.common.Common.mDriversMarker;
import static com.example.daniel.sifonride.common.Common.mLastLocation;
import static com.example.daniel.sifonride.common.Common.mLimitRadius;
import static com.example.daniel.sifonride.common.Common.mScanningRadius;
import static com.example.daniel.sifonride.common.Common.mStartAddress;
import static com.example.daniel.sifonride.common.Common.mStartLatLng;
import static com.example.daniel.sifonride.common.Common.myScreenLocation;
import static com.example.daniel.sifonride.common.Common.requestUpdate;
import static com.example.daniel.sifonride.common.Common.riderDetails;
import static com.example.daniel.sifonride.common.Common.riderHistory;
import static com.example.daniel.sifonride.common.Common.rider_alert;
import static com.example.daniel.sifonride.common.Common.rider_info;
import static com.example.daniel.sifonride.common.Common.riders;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener,
        OnMapReadyCallback, AddressListener, DirectionFinderListener, TimeDistanceListener
{
    public static final String TAG = "HOME";

    private FusedLocationProviderClient mFusedLocation;
    private SettingsClient mSettingsClient;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationRequest mLocationRequest;
    private GoogleMap.OnCameraIdleListener onCameraIdleListener;

    public GoogleMap mMap = null;
    boolean mLocationPermissionGranted = false;
    SupportMapFragment mapFragment;
    private GeoQuery geoQuery;
    private Marker mDriverMarker;


    //This variable is used to update user profile.
    private TextView riderNames, riderRating;
    private String mRiderNames, mRiderName,
            mRiderOtherName, mRiderProfileUri, riderRatingT;
    private ImageView mProfileImage;
    private double riderRatingSum, riderRatingSumB,riderRatingSumC,
            riderRatingTotal;

    //Home screen display button.
    private ImageButton currentLocation;
    //private AppBarLayout appBarLayout;
    public RelativeLayout homeLayout;
    private TextView homeAddress;
    private Button rideNow;

    //Rider request Layout;
    private RelativeLayout requestLayout;
    private ImageButton requestBackButton;
    @SuppressLint("StaticFieldLeak")
    public static TextView requestDestination, requestLocation;
    private LinearLayout cycleLayout, economyLayout;
    private ImageButton cycleImage, economyImage;
    private View cycleView, economyView;
    private Button sendDriverRequest;
    @SuppressLint("StaticFieldLeak")
    public static ProgressBar progressEstimatePrice;
    @SuppressLint("StaticFieldLeak")
    public static TextView estimatedPrice;
    private TextView estimatedTime, numberOfPassenger;
    private TextView farePaymentMethod;
    static String cycleFare, economyFare;

    //This is used to check type of ride
    private static boolean cycleBool = false;
    private static boolean economyBool = true;

    //This layout is for confirm Layout.
    private ImageButton backToRequest, gotoConfirmLocation;
    private TextView yourAddress;
    private Button confirmBtn;
    private RelativeLayout confirmLayout;

    //This variable are for search layout.
    public TextView searchingText;
    public Button searchCancel;
    public RelativeLayout searchLayout;
    public ProgressBar dotProgressBar;

    public CircleImageView driverProfile, driverProfileD;
    public TextView driverName, driverRating, driverPlate, cancelRide, destText,
            driverNameD, driverRatingD, driverPlateD;
    public TextView driverCar, callDriver, driverTimeAway, driverDistanceAway, driverCarD,
            callDriverD, driverTimeAwayD, driverDistanceAwayD;
    public RelativeLayout driverLayout;
    public LinearLayout driveLayoutDest;
    public RelativeLayout driverInfoLayout;
    public ScrollView moreInfoLayout;
    public ImageButton moreInfoBackBtn;
    public ImageView moreInfoBtn;

    public SharedPreferences mActiveBookingDetails;
    private Booking booking;

    public List<Marker> originMarkers = new ArrayList<>();
    public List<Marker> destinationMarkers = new ArrayList<>();
    public List<Polyline> polylinePaths = new ArrayList<>();

    public TextView baseFare, timeFare, distanceFare, totalFare, promoFare, bounceFare;
    public Button fareBtn;
    public LinearLayout fareLayout, promoLayout, bounceLayout;

    //Variable for the rating tab
    public RelativeLayout ratingLayout;
    public CircleImageView ratingImage;
    public TextView ratingDriverName;
    public EditText ratingComment;
    public RatingBar ratingBar;
    public Button submitRateBtn;

    private void uiInitializer(){

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        riderNames = header.findViewById(R.id.profileName);
        riderRating = header.findViewById(R.id.profileRating);
        mProfileImage = header.findViewById(R.id.imageViewP);

        //home screen views.
        //appBarLayout = findViewById(R.id.appbar);
        currentLocation = findViewById(R.id.loc);
        rideNow = findViewById(R.id.rideNow);
        //Button rideLater = findViewById(R.id.ride_later);
        homeAddress = findViewById(R.id.address);
        homeLayout = findViewById(R.id.home_layout);

        currentLocation.setOnClickListener(this);
        homeAddress = findViewById(R.id.address);
        //rideLater.setOnClickListener(this);
        rideNow.setOnClickListener(this);
        homeAddress.setOnClickListener(this);

        //request layout.
        requestLayout = findViewById(R.id.request_layout);
        requestBackButton = findViewById(R.id.requestBackButton);
        requestDestination = findViewById(R.id.requestDestination);
        requestLocation = findViewById(R.id.requestLocation);
        //this is choose the method o travel
        cycleImage = findViewById(R.id.cycleImage);
        cycleLayout = findViewById(R.id.cycleLayout);
        cycleView = findViewById(R.id.cycleView);
        economyImage = findViewById(R.id.economyImage);
        economyLayout = findViewById(R.id.economy);
        economyView = findViewById(R.id.economyView);

        cycleLayout.setOnClickListener(this);
        cycleImage.setOnClickListener(this);
        economyLayout.setOnClickListener(this);
        economyImage.setOnClickListener(this);

        progressEstimatePrice = findViewById(R.id.estimatePriceProgressBar);
        estimatedTime = findViewById(R.id.timeOfArrival);
        estimatedPrice = findViewById(R.id.estimatePrice);
        sendDriverRequest = findViewById(R.id.gettingDriver);
        farePaymentMethod = findViewById(R.id.requestPaymentMethod);
        numberOfPassenger = findViewById(R.id.numOfpassenger);

        confirmLayout = findViewById(R.id.confirmLayout);
        confirmBtn = findViewById(R.id.confirmLocationBtn);
        backToRequest = findViewById(R.id.backButtonToRequest);
        yourAddress = findViewById(R.id.confirmCurrentText);
        gotoConfirmLocation = findViewById(R.id.locB);
        gotoConfirmLocation.setOnClickListener(this);

        searchLayout = findViewById(R.id.findingDriverLayout);
        searchCancel = findViewById(R.id.searchCancel);
        searchingText = findViewById(R.id.searchingText);
        dotProgressBar = findViewById(R.id.net);
        new Booking(HomeActivity.this);

        //driver detail layout
        driverTimeAway = findViewById(R.id.driverTimeAway);
        driverDistanceAway = findViewById(R.id.driverKmAway);
        driverCar = findViewById(R.id.carName);
        driverName = findViewById(R.id.driverName);
        driverRating = findViewById(R.id.driverRating);
        driverPlate = findViewById(R.id.carPlateNum);
        cancelRide = findViewById(R.id.cancel_request);
        callDriver = findViewById(R.id.call_driver);
        driverProfile = findViewById(R.id.driver_image);
        driverLayout = findViewById(R.id.driverLayout);
        destText = findViewById(R.id.riderDestination);
        driveLayoutDest = findViewById(R.id.destinationLayout);
        driverInfoLayout = findViewById(R.id.driverInfoLayout);

        driverTimeAwayD = findViewById(R.id.driverTimeAwayD);
        driverDistanceAwayD = findViewById(R.id.driverKmAwayD);
        driverCarD = findViewById(R.id.carNameD);
        driverNameD = findViewById(R.id.driverNameD);
        driverRatingD = findViewById(R.id.driverRatingD);
        driverPlateD = findViewById(R.id.plateNumD);
        callDriverD = findViewById(R.id.phoneNumberD);
        driverProfileD = findViewById(R.id.driverProfileD);
        moreInfoLayout = findViewById(R.id.moreInfo);
        moreInfoBackBtn = findViewById(R.id.moreInfoBackBtn);
        moreInfoBtn = findViewById(R.id.moreDetailsBtn);

        baseFare = findViewById(R.id.baseFare);
        distanceFare = findViewById(R.id.distanceFare);
        timeFare =  findViewById(R.id.timeFare);
        totalFare = findViewById(R.id.totalFare);
        promoFare = findViewById(R.id.promoDis);
        bounceFare =  findViewById(R.id.bounceDis);
        fareBtn = findViewById(R.id.fareBtn);
        fareLayout = findViewById(R.id.fareLayout);
        promoLayout =  findViewById(R.id.promoLayout);
        bounceLayout = findViewById(R.id.bounceLayout);

        //rating info
        ratingLayout = findViewById(R.id.ratingTable);
        ratingImage = findViewById(R.id.driverRatingImage);
        ratingDriverName = findViewById(R.id.rateDriverName);
        ratingBar = findViewById(R.id.driverRatingBar);
        ratingComment = findViewById(R.id.ratingCommentBox);
        submitRateBtn = findViewById(R.id.rateBtn);
    }

    private void headerInfo() {

        FirebaseFirestoreSettings firestoreSettings = new  FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();

        FirebaseFirestore fs = FirebaseFirestore.getInstance();

        //noinspection ConstantConditions
        fs.collection(riders).document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection(Common.riderRating)
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot querySnapshot, FirebaseFirestoreException e) {
                        if (!querySnapshot.isEmpty()){
                            for (DocumentSnapshot document: querySnapshot.getDocuments()){
                                if (document.getString("Rating")!= null){
                                    riderRatingSum += Double.valueOf(document.getString("Rating"));
                                    riderRatingTotal ++;
                                }
                            }
                            riderRatingSumB = riderRatingSum/riderRatingTotal;

                            riderRatingSumC = Double.valueOf(new DecimalFormat("#.##").format(riderRatingSumB));

                            riderRatingT = String.valueOf(riderRatingSumC);
                            riderRating.setText(String.valueOf("0.0"));
                            riderRating.setText(riderRatingT);
                        }else {
                            riderRating.setText(String.valueOf("0.0"));
                        }
                    }
                });

        //noinspection ConstantConditions
        DocumentReference doc = fs.collection(riders).document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection(riderDetails).document(rider_info);
        doc.addSnapshotListener(HomeActivity.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (documentSnapshot.exists()) {
                    mRiderName = documentSnapshot.getString("Name");
                    mRiderOtherName = documentSnapshot.getString("Other name");
                    mRiderNames = mRiderName + " " + mRiderOtherName;

                    mRiderProfileUri = documentSnapshot.getString("Profile Image");

                    if(mRiderProfileUri != null) {
                                Glide.with(HomeActivity.this).load(mRiderProfileUri).into(mProfileImage);
                    }else {
                        Glide.with(HomeActivity.this).load(R.drawable.profile).into(mProfileImage);
                    }

                    if (mRiderNames != null) {
                        riderNames.setText(mRiderNames);
                    }
                }
            }
        });
        fs.setFirestoreSettings(firestoreSettings);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        uiInitializer();

        headerInfo();

        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);

        getLocationPermission();

        getDeviceLocation();

        locationRequest();

        buildLocationSettingsRequest();

        locationSettingsRequest();

        configureCameraIdle();

        fareUpdate();

        mActiveBookingDetails = getSharedPreferences("Active booking details", MODE_PRIVATE);

        booking = new Booking();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (requestLayout.isShown()) {
            homeLayout.setVisibility(View.VISIBLE);
            requestLayout.setVisibility(View.GONE);
        }else if(confirmLayout.isShown()){
            requestLayout.setVisibility(View.VISIBLE);
            confirmLayout.setVisibility(View.GONE);
        }else if (moreInfoLayout.isShown()){
            moreInfoLayout.setVisibility(View.GONE);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        if (id == R.id.settings) {
            // Handle the camera action
            startActivity(new Intent(HomeActivity.this, Settings.class));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSION_CODE:
                if (grantResults.length <= 0) {
                    //noinspection ConstantConditions
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                    android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_CODE

                            );
                        }
                    }
                } else {
                    mLocationPermissionGranted = true;
                }
                break;

            case REQUEST_PERMISSION_PHONE_CODE:
                //noinspection SingleStatementInBlock
                if (grantResults.length <= 0) {
                    //noinspection ConstantConditions
                    for (int grantresult : grantResults) {
                        if (grantresult != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this,
                                    new String[]{android.Manifest.permission.CALL_PHONE}
                                    , REQUEST_PERMISSION_PHONE_CODE);
                        }
                    }
                }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startLocationUpdate();
//        if (mActiveBookingDetails.getBoolean("Searching for driver", false)){
//            searchLayout.setVisibility(View.VISIBLE);
//            searchCancel.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) { cancelDriverSearching();
//                }
//            });
//        }

        //uploadCurrentBookingStatus();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //startLocationUpdate();
        displayCarOnScreen();
//        if (mActiveBookingDetails.getBoolean("Driver search", false)){
//            searchLayout.setVisibility(View.VISIBLE);
//            searchCancel.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                }
//            });
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //this line of code is used to clear all geo query listeners.
        if (geoQuery != null)
            geoQuery.removeAllListeners();
        stopLocationUpdate();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //this line of code is used to clear all geo query listeners.
        if (geoQuery != null)
            geoQuery.removeAllListeners();
        stopLocationUpdate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //This line of code is used to stop location from requesting location when the app is destory.
        stopLocationUpdate();

//        FirebaseFirestore fa = FirebaseFirestore.getInstance();
//        fa.collection(riders).document(userID).collection(rider_alert)
//                .document(requestUpdate).delete();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        //this method is to create google api client when connected

        mMap.setOnCameraIdleListener(onCameraIdleListener);
        //mMap.setOnCameraMoveListener(onCameraMoveListener);
        getDeviceLocation();

        uploadLastLocation();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(LOCATION_KEY, mLastLocation);
        }
        super.onSaveInstanceState(outState);
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_PERMISSION_CODE);
        }
    }

    private void moveCameraToPosition(LatLng latLng){

        CameraPosition position = new CameraPosition.Builder()
                .zoom(DEFAULT_ZOOM)
                .target(latLng)
                .tilt(10)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
    }

    private void getDeviceLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
            try{
                Task<Location> mLocation = mFusedLocation.getLastLocation();
                mLocation.addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()){
                            mLastLocation = task.getResult();

                            if (mLastLocation != null) {
                                double lat = mLastLocation.getLatitude();
                                double lng = mLastLocation.getLongitude();
                                displayCarOnScreen();
                                //displayBikerOnScreen();
                                LatLng latLng = new LatLng(lat, lng);
                                moveCameraToPosition(latLng);
                                saveLastLocation(latLng.latitude, latLng.longitude);
                                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), DEFAULT_ZOOM));
                            }
                        }else{
                            if (mCurrentLocation != null)
                                moveCameraToPosition(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                        }
                    }
                });
            } catch (SecurityException e) {
                Log.e(TAG, " Failed to get your Location" + e.getMessage());
            }
        }

    }

    private void moveToCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

            try{
                Task<Location> mLocation = mFusedLocation.getLastLocation();
                mLocation.addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()){
                            mLastLocation = task.getResult();

                            if (mLastLocation != null) {
                                double lat = mLastLocation.getLatitude();
                                double lng = mLastLocation.getLongitude();
                                mMap.setMyLocationEnabled(true);
                                moveCameraToPosition(new LatLng(lat, lng));
                            }
                        }else{
                            if (mCurrentLocation != null)
                                moveCameraToPosition(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                        }
                    }
                });
            } catch (SecurityException e) {
                Log.e(TAG, " Failed to get your Location" + e.getMessage());
            }
        }else {
            getLocationPermission();
        }
    }

    private void locationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(SMALLEST_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildLocationSettingsRequest(){
        LocationSettingsRequest.Builder mBuilder  = new LocationSettingsRequest.Builder();
        mBuilder.addLocationRequest(mLocationRequest);

        mSettingsClient = LocationServices.getSettingsClient(this);
        mLocationSettingsRequest = mBuilder.build();
    }

    private void locationSettingsRequest(){
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        getDeviceLocation();

                    }
                }).addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(HomeActivity.this, REQUEST_CHECK_SETTINGS);

                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(HomeActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }

    private void startLocationUpdate(){
        //noinspection MissingPermission
        if (ActivityCompat.checkSelfPermission(HomeActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(HomeActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocation.requestLocationUpdates(mLocationRequest,
                mLocationCallBack, Looper.myLooper());

    }

    private void stopLocationUpdate(){
        mFusedLocation.removeLocationUpdates(mLocationCallBack);
    }

    private void saveLastLocation(double lat, double lng){
        String mLat = String.valueOf(lat);
        String mLng = String.valueOf(lng);

        SharedPreferences mSharePre = getSharedPreferences("lastLocation", Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharePre.edit();
        mEditor.putFloat("lastLat", Float.valueOf(mLat));
        mEditor.putFloat("lastLng", Float.valueOf(mLng));
        mEditor.apply();

    }

    private void uploadLastLocation(){
        SharedPreferences mSharePre = getSharedPreferences("lastLocation", Context.MODE_PRIVATE);
        mSharePre.getFloat("lastLat", 0);
        mSharePre.getFloat("lastLng", 0);
        if (mSharePre.getFloat("lastLat", 0) != 0 && mSharePre.getFloat("lastLng", 0) != 0){
            moveCameraToPosition(new LatLng(mSharePre.getFloat("lastLat", 0),
                    mSharePre.getFloat("lastLng", 0)));
        }

    }

    private LocationCallback mLocationCallBack = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                if(getApplicationContext()!=null) {
                    mLastLocation = location;

                    //        if (isStarted){
//            if (IStart != null){
//                IStart = mLastLocation;
//                IEnd = mLastLocation;
//            }else {
//
//            }
//        }
                    //double lat = mLastLocation.getLatitude();
                    //double lng = mLastLocation.getLongitude();

                    //This method is used to position camera to current location.
                    //moveCamera(new LatLng(lat, lng), DEFAULT_ZOOM);

                    //show driver any time the location c
                    //displayDeviceLocationo();
                    //findAvailableDrivers();
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    if (mLastLocation!= null) {
                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        //mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
                        moveCameraToPosition(latLng);
                    }
                }
            }
        }
    };

    @Override
    public void onClick(View v) {
        if (v == currentLocation) {
            moveToCurrentLocation();
            uploadLastLocation();
        }else if (v == homeAddress) {
            startActivity(new Intent(HomeActivity.this, AutocompletePickUp.class));
        }else if (v == rideNow) {
            setRequestRide();
        }
        //else if (v == rideLater) {
            // This used to add upcoming trips
            //startActivity(new Intent(HomeActivity.this, AddTrips.class));
            //stopReading();
       // }
    }

    private void displayCarOnScreen(){
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(Common.DriverLocation);
        GeoFire geoFire = new GeoFire(mRef);

        if (!bookingBool) {
            // This if statement is used to verify
            if (mLastLocation != null) {

                geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude())
                        , mScanningRadius);
                geoQuery.removeAllListeners();
                geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(String key, GeoLocation location) {
                        if (key != null) {
                            // Add a new marker to the map
                            mDriverMarker = mMap.addMarker(new MarkerOptions()
                                    .icon(BitmapDescriptorFactory.defaultMarker()).flat(true)
                                    .position(new LatLng(location.latitude, location.longitude)));
                            mDriversMarker.put(key, mDriverMarker);
                            mDriversKey.put(key, 8.9);


                            if (mLastLocation != null) {
                                if (economyBool) {
                                    LatLng origin = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                                    LatLng des = new LatLng(location.latitude, location.longitude);
                                    try {
                                        new FindTimeDistance(HomeActivity.this, origin, des).execute();
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }


                        }

                    }

                    @Override
                    public void onKeyExited(String key) {

                        mDriverMarker = mDriversMarker.get(key);
                        if (mDriverMarker != null) {
                            Toast.makeText(HomeActivity.this, "Exit", Toast.LENGTH_SHORT).show();
                            mDriverMarker.remove();
                            mDriversMarker.remove(key);
                        }
                    }

                    @Override
                    public void onKeyMoved(String key, GeoLocation location) {
                        Marker marker = mDriversMarker.get(key);
                        if (marker != null) {
                            animateMarkerTo(marker, location.latitude, location.longitude);
                        }

                    }

                    @Override
                    public void onGeoQueryReady() {
                        if (mScanningRadius <= mLimitRadius) {
                            ++mScanningRadius;
                            displayCarOnScreen();
                        }
                    }

                    @Override
                    public void onGeoQueryError(DatabaseError error) {
                    }

                });
            }
        }
    }

    // Animation handler for old APIs without animation support
    private void animateMarkerTo(final Marker marker, final double lat, final double lng) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long DURATION_MS = 3000;
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final LatLng startPosition = marker.getPosition();
        handler.post(new Runnable() {
            @Override
            public void run() {
                float elapsed = SystemClock.uptimeMillis() - start;
                float t = elapsed/DURATION_MS;
                float v = interpolator.getInterpolation(t);

                double currentLat = (lat - startPosition.latitude) * v + startPosition.latitude;
                double currentLng = (lng - startPosition.longitude) * v + startPosition.longitude;
                marker.setPosition(new LatLng(currentLat, currentLng));

                // if animation is not finished yet, repeat
                if (t < 1) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    private void fareUpdate() {
        FirebaseFirestore mFireStore = FirebaseFirestore.getInstance();


        mFireStore.collection(fareDataBase).document("gh").collection("cycle").document("Accra")
                .addSnapshotListener(HomeActivity.this, new EventListener<DocumentSnapshot>() {

                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if (documentSnapshot.exists()) {
                            if (documentSnapshot.getDouble("Duration Fare") != null)
                                mBikerTimeFare = documentSnapshot.getDouble("Duration Fare");
                            //if (documentSnapshot.getDouble("Base fare") != null)
                              //  mBikerBaseFare = documentSnapshot.getDouble("Base fare");
//                            if (documentSnapshot.getDouble("Distance fare") != null)
//                                mBikerDistanceFare = documentSnapshot.getDouble("Distance fare");
//                            if (documentSnapshot.getDouble("Minimum fare") != null)
//                                mBikerMinimumFare = documentSnapshot.getDouble("Minimum fare");

                            //TODO: Find why it is crashing.
                        }
                    }
                });
        mFireStore.collection(fareDataBase).document("gh").collection("economy").document("Accra")
                .addSnapshotListener(HomeActivity.this, new EventListener<DocumentSnapshot>() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        //if (documentSnapshot.exists()) {
//                            if (documentSnapshot.getDouble("Duration fare") != null)
//                                mDriverTimeFare = documentSnapshot.getDouble("Duration fare");
//
//                            if (documentSnapshot.getDouble("Base fare") != null)
//                                mDriverBaseFare = documentSnapshot.getDouble("Base fare");
//
//                            if (documentSnapshot.getDouble("Distance fare") != null)
//                                mDriverDistanceFare = documentSnapshot.getDouble("Distance fare");
//
//                            if (documentSnapshot.getDouble("Minimum fare") != null)
//                                mDriverMinimumFare = documentSnapshot.getDouble("Minimum fare");

                        //}
                    }
                });
        //mFireStore.setFirestoreSettings(settings);

    }

    public void fareCalculationUpdate(String time, String km){
        String dist = null ,dur, durHr, durMin;
        Double durT = 0.0, totalPriceB, totalPriceE;
        String[] durArray;

        if (km.contains("km") && km.contains(",")){
            String dis = km.replace("km", "");
            dist = dis.replace(",", "");
        } else if (km.contains("km")) {
            dist = km.replace("km", "");
        } else if (km.contains("m")) {
            dist = km.replace("m", "");
        }

        if (time.contains("hr") && time.contains("mins")) {
            dur = time.replace("mins", "");
            durArray = dur.split("hr");
            durHr = durArray[0];
            durMin = durArray[1];
            durT = (Double.valueOf(durHr) * 60) + Double.valueOf(durMin);
        }else if (time.contains("hours") && time.contains("mins")) {
            dur = time.replace("mins", "");
            durArray = dur.split("hours");
            durHr = durArray[0];
            durMin = durArray[1];
            durT = (Double.valueOf(durHr) * 60) + Double.valueOf(durMin);
        } else if (time.contains("hr")) {
            dur = time.replace("hr", "");
            durT = Double.valueOf(dur);
        } else if (time.contains("mins")) {
            dur = time.replace("mins", "");
            durT = Double.valueOf(dur);
        }

        if (dist != null) {

            totalPriceB = (durT * mBikerTimeFare)+(Double.valueOf(dist) * mBikerDistanceFare)+mBikerBaseFare;
            if (totalPriceB < mBikerMinimumFare){
                cycleFare = "GHC" + " " + String.valueOf(new DecimalFormat("#.##").format(mBikerMinimumFare));
            }else {
                cycleFare = "GHC" + " " + String.valueOf(new DecimalFormat("#.##").format(totalPriceB));
            }

            totalPriceE = (durT * mDriverTimeFare)+(Double.valueOf(dist) * mDriverDistanceFare)+mDriverBaseFare;
            if (totalPriceE <mDriverMinimumFare) {
                economyFare = "GHC" + " " + String.valueOf(new DecimalFormat("#.##").format(mDriverMinimumFare));
            }else {
                economyFare = "GHC" + " " + String.valueOf(new DecimalFormat("#.##").format(totalPriceE));

            }

            if (cycleBool) {
                estimatedPrice.setText(cycleFare);

            } else {
                estimatedPrice.setText(economyFare);
            }

        }else {
            Toast.makeText(this, "Km null", Toast.LENGTH_SHORT).show();
        }
    }

    private void setRequestRide() {
        //this is used to remove home screen view and replace with request layout.
        //appBarLayout.setVisibility(View.GONE);
        homeLayout.setVisibility(View.GONE);
        requestLayout.setVisibility(View.VISIBLE);

        //this block of code is used to verify payment method selected
        SharedPreferences mPreference = getSharedPreferences("payMethod", Context.MODE_PRIVATE);
        String defaultValue =  "Cash";
        String pay = mPreference.getString("Payment",defaultValue);

        if (Objects.equals(pay, "Cash")){
            farePaymentMethod.setText(R.string.cash);
        }else if (Objects.equals(pay, "Card")){
            farePaymentMethod.setText(R.string.credit_card);
        }

        //To go back to home view.
        requestBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //appBarLayout.setVisibility(View.VISIBLE);
                homeLayout.setVisibility(View.VISIBLE);
                requestLayout.setVisibility(View.GONE);
            }
        });


        //search for your location.
        requestLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, AutocompletePickUp.class));
                }
        });

        //search for destination.
        requestDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, AutocompleteDestination.class));
            }});

        //this is to select ride
        selectTypeOfRide();

        estimatedPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFareCalculation();
            }
        });


        sendDriverRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "Clicking", Toast.LENGTH_LONG).show();
                confirmYourLocation();
            }
        });

        farePaymentMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectPaymentMethod();
            }
        });





        //for estimate fare
        estimatedPrice.setText("");

        //for set location and destination.
        mStartAddress = "";
        mStartLatLng = null;
        mDestinationLatLng = null;
        mDestinationAddress ="";
        requestDestination.setText(R.string.destination);
        requestLocation.setText(R.string.your_location);


        //rideCancel();
//        if (mDestinationAddress != null && mDestinationLatLng != null){
//                requestDestination.setText(mDestinationAddress);
//        }
//        if (mStartLatLng != null && mStartAddress != null){
//            requestLocation.setText(mStartAddress);
//        }

    }

    private void showFareCalculation() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);

        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.fare_form_home, null);
        mBuilder.setView(view);
        ImageView alertImage = view.findViewById(R.id.typeOfRideImage);
        TextView alertTypeOfRide = view.findViewById(R.id.alertRideChoose);
        final TextView alertBaseFare = view.findViewById(R.id.alertBaseFare);
        final TextView alertTimeFare = view.findViewById(R.id.alertCostPerMin);
        final TextView alertDistanceFare = view.findViewById(R.id.alertCostPerKm);
        final TextView alertMinimumFare = view.findViewById(R.id.alertCostPerMinimum);
        TextView alertFareCancel = view.findViewById(R.id.doneForFare);

        if (cycleBool) {
            if (mBikerTimeFare != 0)
                alertTimeFare.setText(String.valueOf(mBikerTimeFare));
            if (mBikerBaseFare != 0)
                alertBaseFare.setText(String.valueOf(mBikerBaseFare));
            if (mBikerDistanceFare != 0)
                alertDistanceFare.setText(String.valueOf(mBikerDistanceFare));
            if (mBikerMinimumFare != 0)
                alertMinimumFare.setText(String.valueOf(mBikerMinimumFare));
            alertTypeOfRide.setText(getResources().getText(R.string.motor_cycle));
            alertImage.setImageDrawable(getResources().getDrawable(R.drawable.deliveryimage));
        }else {
            if (mDriverTimeFare != 0)
                alertTimeFare.setText(String.valueOf(mDriverTimeFare));
            if (mDriverBaseFare != 0)
                alertBaseFare.setText(String.valueOf(mDriverBaseFare));
            if (mDriverDistanceFare != 0)
                alertDistanceFare.setText(String.valueOf(mDriverDistanceFare));
            if (mDriverMinimumFare != 0)
                alertMinimumFare.setText(String.valueOf(mDriverMinimumFare));
            alertTypeOfRide.setText(getResources().getText(R.string.economy));
            alertImage.setImageDrawable(getResources().getDrawable(R.drawable.carone));

        }

        final AlertDialog dialog = mBuilder.create();
        dialog.setCancelable(false);
        dialog.show();
        alertFareCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }

    private void selectTypeOfRide(){

        cycleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cycleBool = true;
                economyBool = false;
                cycleView.setVisibility(View.VISIBLE);
                economyView.setVisibility(View.GONE);
                numberOfPassenger.setText("1");
                if (cycleFare !=null)
                    estimatedPrice.setText(cycleFare);



            }
        });
        cycleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cycleBool = true;
                economyBool = false;
                cycleView.setVisibility(View.VISIBLE);
                economyView.setVisibility(View.GONE);
                numberOfPassenger.setText("1");
                if (cycleFare !=null)
                    estimatedPrice.setText(cycleFare);

            }});

        economyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cycleBool = false;
                economyBool = true;
                economyView.setVisibility(View.VISIBLE);
                cycleView.setVisibility(View.GONE);
                numberOfPassenger.setText("1-4");
                if (economyFare !=null)
                    estimatedPrice.setText(economyFare);
            }
        });

        economyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cycleBool = false;
                economyBool = true;
                economyView.setVisibility(View.VISIBLE);
                cycleView.setVisibility(View.GONE);
                numberOfPassenger.setText("1-4");
                if (economyFare !=null)
                    estimatedPrice.setText(economyFare);
            }
        });
    }

    private void selectPaymentMethod(){
        final Dialog mBuilder = new Dialog(this, R.style.Dialog_No_Border);
        mBuilder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.payment_dialog_layout, null);
        mBuilder.setContentView(view);

        LinearLayout payLayout = view.findViewById(R.id.payLayout);
        payLayout.setBackgroundResource(R.drawable.round_edge_dialog);
        TextView card = view.findViewById(R.id.card);
        TextView cash= view.findViewById(R.id.cash);
        Button payDoneBtn = view.findViewById(R.id.payCancel);

        mBuilder.setCancelable(false);
        mBuilder.show();

        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBuilder.dismiss();
            }
        });

        cash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBuilder.dismiss();

            }
        });

        payDoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBuilder.dismiss();

            }
        });

        //TODO: Make the payment methos change when it is clicked.
    }

    private void confirmYourLocation() {
        // Toast.makeText(getApplicationContext(), "Send request", Toast.LENGTH_LONG).show();

        requestLayout.setVisibility(View.GONE);
        confirmLayout.setVisibility(View.VISIBLE);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestVerification();
            }
        });

        backToRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestLayout.setVisibility(View.VISIBLE);
                confirmLayout.setVisibility(View.GONE);
            }
        });

        gotoConfirmLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToCurrentLocation();
                uploadLastLocation();
            }
        });

    }

    private void requestVerification(){
        searchLayout.setVisibility(View.VISIBLE);
        confirmLayout.setVisibility(View.GONE);

        if (Util.Operations.isOnline(this)) {
            if (cycleBool) {
                bookingDetails();
                Toast.makeText(this, "No available yet", Toast.LENGTH_SHORT).show();
            } else if (economyBool) {
                bookingDetails();
                new Booking(HomeActivity.this);
                startService(new Intent(HomeActivity.this, Booking.class));
            }
        }else {
            searchingText.setText(R.string.no_internet);
        }
        //TODO: Start the booking process from net time.
    }

    private void bookingDetails(){
        if (cycleBool) {
            SharedPreferences.Editor editor = mActiveBookingDetails.edit();
            editor.putBoolean("cycleBool", true);
            editor.putBoolean("economyBool", false);
            editor.putString("PickLat",String.valueOf(myScreenLocation.latitude));
            editor.putString("PickLng",String.valueOf(myScreenLocation.longitude));
            editor.putString("DestinationLat",String.valueOf(mDestinationLatLng.latitude));
            editor.putString("DestinationLng", String.valueOf(mDestinationLatLng.longitude));
            editor.putString("DestinationAddress",mDestinationAddress);
            editor.putString("BaseFare", String.valueOf(mBikerBaseFare));
            editor.putString("DistanceFare", String.valueOf(mBikerDistanceFare));
            editor.putString("DurationFare", String.valueOf(mBikerTimeFare));
            editor.apply();

        } else if (economyBool) {
            SharedPreferences.Editor editor = mActiveBookingDetails.edit();
            editor.putBoolean("economyBool", true);
            editor.putBoolean("cycleBool", false);
            editor.putString("PickUpLat",String.valueOf(myScreenLocation.latitude));
            editor.putString("PickUpLng",String.valueOf(myScreenLocation.longitude));
            //editor.putString("PickUpAddress",mDestinationAddress);
            if (mDestinationLatLng != null)
                editor.putString("DestinationLat",String.valueOf(mDestinationLatLng.latitude));
            if (mDestinationLatLng != null)
                editor.putString("DestinationLng", String.valueOf(mDestinationLatLng.longitude));
            if (mDestinationAddress != null)
                editor.putString("DestinationAddress",mDestinationAddress);
            editor.putString("BaseFare", String.valueOf(mDriverBaseFare));
            editor.putString("DistanceFare", String.valueOf(mDriverDistanceFare));
            editor.putString("DurationFare", String.valueOf(mDriverTimeFare));
            editor.apply();


        }
    }

    private void configureCameraIdle(){

        onCameraIdleListener = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                myScreenLocation = mMap.getCameraPosition().target;
                new AddressUpdate(HomeActivity.this, HomeActivity.this)
                        .execute(myScreenLocation);
            }
        };
    }

    @Override
    public void onAddressSuccessful(String address) {

        homeAddress.setText(address);
        yourAddress.setText(address);

        if(mStartAddress == null)
            requestLocation.setText(address);

    }

    @Override
    public void onDirectionFinderStart() {

    }

    @Override
    public void onDirectionFinderSuccess(List<Route> route) {

    }

    @Override
    public void onTimeDistanceStart() {

    }

    @Override
    public void onTimeDistanceSuccess(List<Route> routes) {

        for (Route route : routes) {
            if(cycleBool){
                estimatedTime.setText(route.duration.text);
            }
            if (economyBool){
                estimatedTime.setText(route.duration.text);

            }
        }
    }

    public void cancelDriverSearching(){
        searchingText.setText(R.string.cancel_alert_text);
        if (mActiveBookingDetails.getBoolean("economyBool", false)) {
            if (mActiveBookingDetails.getString("Driver key", null) != null) {
                final FirebaseFirestore mFs = FirebaseFirestore.getInstance();

                mFs.collection(drivers).document(String.valueOf(mActiveBookingDetails.getString("Driver key", null)))
                        .collection(Common.Driver_alert)
                        .document("Economy").delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        //noinspection ConstantConditions
                        mFs.collection(riders).document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .collection(rider_alert).document(requestUpdate).delete();
                        stopService(new Intent(HomeActivity.this, Booking.class));
                        searchLayout.setVisibility(View.GONE);
                        homeLayout.setVisibility(View.VISIBLE);
                    }
                });
                uploadCancelDetails("Economy");
            }else {
                searchLayout.setVisibility(View.GONE);
                homeLayout.setVisibility(View.VISIBLE);
                stopService(new Intent(HomeActivity.this, Booking.class));
            }
        }else if (mActiveBookingDetails.getBoolean("cycleBool", false)){
            if (mActiveBookingDetails.getString("Biker key", null) != null) {
                Toast.makeText(this, "dd", Toast.LENGTH_SHORT).show();
            }else {
                searchLayout.setVisibility(View.GONE);
                homeLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    public void alertDialog(String message){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(HomeActivity.this).inflate(R.layout.booking_dialog_layout,
                null, false);
        alertDialog.setView(view);
        alertDialog.setCancelable(false);

        //TextView alertTitle = view.findViewById(R.id.bookingAlertTitle);
        TextView alertText = view.findViewById(R.id.bookingAlertText);
        Button alertBtn = view.findViewById(R.id.bookingAlertBtn);

        final Dialog dialog = alertDialog.create();
        dialog.show();

        alertText.setText(message);
        alertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

    }

    public Dialog mDialog;
    public void cancelAlertDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(HomeActivity.this).inflate(R.layout.cancel_ride_dialog, null, false);
        alertDialog.setView(view);
        alertDialog.setCancelable(false);

        //TextView alertTitle = view.findViewById(R.id.bookingAlertTitle);
        //TextView alertText = view.findViewById(R.id.bookingAlertText);
        Button alertBtn = view.findViewById(R.id.doneAlertBtn);

        mDialog = alertDialog.create();
        mDialog.show();

        alertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });

    }

    public void cancelRideDriverLayout(){

        if (mActiveBookingDetails.getBoolean("economyBool", false)) {
            if (mActiveBookingDetails.getString("Driver key", null) != null) {
                FirebaseFirestore mFs = FirebaseFirestore.getInstance();

                mFs.collection(drivers).document(String.valueOf(mActiveBookingDetails.getString("Driver key", null)))
                        .collection(Common.Driver_alert)
                        .document("Economy").delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        driverLayout.setVisibility(View.GONE);
                        homeLayout.setVisibility(View.VISIBLE);
                        stopService(new Intent(HomeActivity.this, Booking.class));
                    }
                });
                uploadCancelDetails("Economy");
            }else {
                driverLayout.setVisibility(View.GONE);
                homeLayout.setVisibility(View.VISIBLE);
                stopService(new Intent(HomeActivity.this, Booking.class));
            }
        }else if (mActiveBookingDetails.getBoolean("cycleBool", false)){
            if (mActiveBookingDetails.getString("Biker key", null) != null) {
                Toast.makeText(this, "dd", Toast.LENGTH_SHORT).show();
            }else {
                driverLayout.setVisibility(View.GONE);
                homeLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    public void uploadCancelDetails(String cancelType){
        if (cancelType.equals("Economy")){
            Calendar c = Calendar.getInstance();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm");
            Map<String, Object> map = new HashMap<>();
            map.put("StartTime",  df.format(c.getTime()));
            map.put("PickUpAddress", mActiveBookingDetails.getString("PickUpAddress",null));
            map.put("DestinationAddress", mActiveBookingDetails.getString("PickUpAddress",null));
            map.put("PickUpLat", mActiveBookingDetails.getString("PickUpLat",null));
            map.put("PickUpLng", mActiveBookingDetails.getString("PickUpLng",null));
            map.put("DestinationLat", mActiveBookingDetails.getString("PickUpLat",null));
            map.put("DestinationLng", mActiveBookingDetails.getString("PickUpLng",null));
            //TODO: Update cash to method of payment when payment options is completed
            map.put("PaymentMethod", "cash");
            map.put("RideType", "Economy");

            FirebaseFirestore mFs = FirebaseFirestore.getInstance();

            //noinspection ConstantConditions
            mFs.collection(riders).document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection(riderHistory).document().set(map);

        }else {
            Calendar c = Calendar.getInstance();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm");
            Map<String, Object> map = new HashMap<>();
            map.put("StartTime",  df.format(c.getTime()));
            map.put("PickUpAddress", mActiveBookingDetails.getString("PickUpAddress",null));
            map.put("DestinationAddress", mActiveBookingDetails.getString("PickUpAddress",null));
            //TODO: Update cash to method of payment when payment options is completed
            map.put("PaymentMethod", "cash");
            map.put("RideType", "Cycle");


            FirebaseFirestore mFs = FirebaseFirestore.getInstance();

            //noinspection ConstantConditions
            mFs.collection(riders).document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection(riderHistory).document().set(map);
        }
    }

    public void callDriverMethod(){
        if (mDriverPhone != null) {
            Intent i = new Intent(Intent.ACTION_DIAL);
            i.setData(Uri.parse("tel:" + mDriverPhone));
            startActivity(i);
        } else {
            Toast.makeText(getApplicationContext(), "Numbe not available", Toast.LENGTH_SHORT).show();
        }
    }

    public void uploadCurrentBookingStatus(){

        booking = new Booking(HomeActivity.this);
        FirebaseFirestore mFs = FirebaseFirestore.getInstance();

        //noinspection ConstantConditions
        mFs.collection(riders).document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection(rider_alert).document(requestUpdate).addSnapshotListener(HomeActivity.this,
                new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null &&documentSnapshot.exists()){

                   if (Objects.equals(documentSnapshot.getString("Request Update"), "Accept")) {

                        booking.driverDetails();
                        booking.acceptedRideMethod();
                        booking.driverCurrentLocation();
                        booking.uploadCurrentLocation();

                   }else if (Objects.equals(documentSnapshot.getString("Request Update"), "Arrived"))
                   {
                        alertDialog(getString(R.string.driver_arrive_text));
                   }else if (Objects.equals(documentSnapshot.getString("Request Update"), "Start Ride"))
                   {
                        alertDialog(getString(R.string.trip_start_text));
                   }
                    //else if (Objects.equals(documentSnapshot.getString("Request Update"), "Ride ended"))
//                    {
//                        //TODO: Next complete this method.
//                        // riderHasEnd();
//                    }else if (Objects.equals(documentSnapshot.getString("Request Update"), "Paid"))
//                    {
//                        //
//                    }
                }
            }
        });

    }

    String ratings;
    public void rateDriver() {

        fareLayout.setVisibility(View.GONE);
        ratingLayout.setVisibility(View.VISIBLE);

        ratingDriverName.setText(mDriverNames);
        String comment = ratingDriverName.getText().toString();


        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {

                ratings = String.valueOf(v);
            }
        });

        final Map<String, Object> map = new HashMap<>();
        map.put("Comment", comment);
        map.put("Rating", ratings);
        final FirebaseFirestore mFs = FirebaseFirestore.getInstance();

        submitRateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFs.collection(drivers).document()
                        .collection(driverHistory).document().set(map);
            }
        });

    }

}