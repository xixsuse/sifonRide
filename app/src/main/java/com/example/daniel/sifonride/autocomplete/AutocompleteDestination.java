package com.example.daniel.sifonride.autocomplete;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.daniel.sifonride.AddressListener;
import com.example.daniel.sifonride.AddressUpdate;
import com.example.daniel.sifonride.GoogleDirectionApi.DirectionFinderListener;
import com.example.daniel.sifonride.GoogleDirectionApi.FareCalculating;
import com.example.daniel.sifonride.GoogleDirectionApi.Route;
import com.example.daniel.sifonride.HomeActivity;
import com.example.daniel.sifonride.R;
import com.example.daniel.sifonride.common.Common;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static com.example.daniel.sifonride.common.Common.mDestinationAddress;
import static com.example.daniel.sifonride.common.Common.mDestinationLatLng;
import static com.example.daniel.sifonride.common.Common.mHomeAddress;
import static com.example.daniel.sifonride.common.Common.mHomeLatLng;
import static com.example.daniel.sifonride.common.Common.mLastLocation;
import static com.example.daniel.sifonride.common.Common.mStartLatLng;
import static com.example.daniel.sifonride.common.Common.mWorkAddress;

public class AutocompleteDestination extends AppCompatActivity implements OnMapReadyCallback,
        View.OnClickListener, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, AddressListener, DirectionFinderListener
{

    private static final int REQUEST_PERMISSION_CODE = 1000;
    public static final float DEFAULT_ZOOM = 14.3f;

    private EditText dest;
    private ImageView clearEditText;
    private RecyclerView recyclerView;
    private TextView homeBtn, workBtn, workAddress, homeAddress,
            mapSelectBtn, addressOnMap;
    private ImageView homeImageBtn, workImageBtn;
    private LinearLayout locationSearchLayout, hwLayout;
    private RelativeLayout locationMapLayout;

    private ImageView gotoCurrentLocation;
    private Button selectLocation;

    private static final String LOG_TAG = "Autocomplete dest";
    protected GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    boolean mLocationPermissionGranted = false;
    private GoogleMap.OnCameraIdleListener onCameraIdleListener;

    private PlaceAutoCompleteAdapters mPlaceAdapter;

    protected LatLng startMapLatLng;
    private String startMapAddress;
    private LatLngBounds BOUNDS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autocomplete_destination);

        //noinspection ConstantConditions
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.destination);

        dest = findViewById(R.id.end);
        clearEditText = findViewById(R.id.clearTextD);
        recyclerView = findViewById(R.id.addressListingD);
        homeBtn = findViewById(R.id.homeD);
        homeAddress = findViewById(R.id.homeDD);
        workBtn = findViewById(R.id.workD);
        workAddress = findViewById(R.id.workDD);
        homeImageBtn = findViewById(R.id.homeImageD);
        workImageBtn = findViewById(R.id.workImageD);
        mapSelectBtn = findViewById(R.id.mapChoiceD);

        locationSearchLayout = findViewById(R.id.chooseLocD);
        locationMapLayout = findViewById(R.id.autoMapLayoutD);
        hwLayout = findViewById(R.id.addHwD);

        addressOnMap = findViewById(R.id.autoConfirmCurrentD);
        gotoCurrentLocation = findViewById(R.id.gotoLocD);
        selectLocation = findViewById(R.id.doneBtnD);

        clearEditText.setOnClickListener(this);
        homeBtn.setOnClickListener(this);
        homeAddress.setOnClickListener(this);
        homeImageBtn.setOnClickListener(this);
        workBtn.setOnClickListener(this);
        workAddress.setOnClickListener(this);
        workImageBtn.setOnClickListener(this);
        mapSelectBtn.setOnClickListener(this);
        gotoCurrentLocation.setOnClickListener(this);
        selectLocation.setOnClickListener(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapD);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        if (mLastLocation != null)
            BOUNDS =  new LatLngBounds(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                    new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

        getHomeAndWork();

        BuildGoogleApiClient();

        buildRecyclerView();

        displayDeviceLocation();

        dest.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                //                if (count<0){
//                    recyclerView.setVisibility(View.GONE);
//                }else {
//                    recyclerView.setVisibility(View.VISIBLE);
//                }
            }

            @Override
            public void onTextChanged(CharSequence s, int startNum, int before, int count) {
                if (!s.toString().isEmpty() || mGoogleApiClient.isConnected()){
                    mPlaceAdapter.getFilter().filter(s.toString());
                    recyclerView.setVisibility(View.VISIBLE);

                }else if (s.toString().isEmpty()){
                    recyclerView.clearFocus();
                }
                if (s.length() < 1){
                    recyclerView.setVisibility(View.GONE);
                    hwLayout.setVisibility(View.VISIBLE);
                    mapSelectBtn.setVisibility(View.VISIBLE);
                    clearEditText.setVisibility(View.GONE);

                }else {
                    hwLayout.setVisibility(View.GONE);
                    mapSelectBtn.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    clearEditText.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });

        configureCameraIdle();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnCameraIdleListener(onCameraIdleListener);

        uploadLastLocation();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mPlaceAdapter.setGoogleApiClient(mGoogleApiClient);
        Log.i(LOG_TAG, "Google Places API connected.");
    }

    @Override
    public void onConnectionSuspended(int i) {
        mPlaceAdapter.setGoogleApiClient(null);
        Log.e(LOG_TAG, "Google Places API connection suspended.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onBackPressed() {
        if (locationMapLayout.isShown()){
            locationSearchLayout.setVisibility(View.VISIBLE);
            locationMapLayout.setVisibility(View.GONE);
            //noinspection ConstantConditions
            getSupportActionBar().setSubtitle("");
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                if (locationMapLayout.isShown()) {
                    locationSearchLayout.setVisibility(View.VISIBLE);
                    locationMapLayout.setVisibility(View.GONE);
                    //noinspection ConstantConditions
                    getSupportActionBar().setSubtitle("");
                } else {
                    finish();
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (view == clearEditText){
            dest.setText("");

        }else if (view == homeBtn){
            homeMethod();
        }else if (view == homeAddress){
            homeMethod();
        }else if (view == homeImageBtn){
            homeMethod();
        }else if (view == workBtn){
            workMethod();
        }else if (view == workAddress){
            workMethod();
        }else if (view == workImageBtn){
            workMethod();
        }else if (view == mapSelectBtn){
            closeKeyBoard();
            locationSearchLayout.setVisibility(View.GONE);
            locationMapLayout.setVisibility(View.VISIBLE);
            //noinspection ConstantConditions
            getSupportActionBar().setSubtitle(R.string.auto_drag_text);
        }else if (view == gotoCurrentLocation){
            displayDeviceLocation();
        }else if (view == selectLocation){
            if (startMapAddress == null){
                Toast.makeText(this, "Please wait for address to load", Toast.LENGTH_SHORT).show();
            }else if (startMapAddress.equals("No internet connection")){
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            }else {
                mDestinationLatLng = new LatLng(startMapLatLng.latitude, startMapLatLng.longitude);
                mDestinationAddress = startMapAddress;
                HomeActivity.requestDestination.setText(mDestinationAddress);
                destFareMain();
                // addWork(workMapAddress, new LatLng(workMapLatLng.latitude, workMapLatLng.longitude));
            }
        }

    }

    private void homeMethod(){
        if (mHomeAddress != null) {
            Common.mDestinationAddress = mHomeAddress;
            mDestinationLatLng = mHomeLatLng;
            HomeActivity.requestDestination.setText(Common.mDestinationAddress);
            destFareMain();
        } else {
            startActivity(new Intent(AutocompleteDestination.this, AutocompleteHome.class));
        }
    }

    private void workMethod(){
        if (Common.mWorkAddress != null){
            Common.mDestinationLatLng = Common.mWorkLatLng;
            Common.mDestinationAddress = mWorkAddress;
            HomeActivity.requestDestination.setText(Common.mDestinationAddress);
            destFareMain();
        }else {
            startActivity(new Intent(AutocompleteDestination.this, AutocompleteWork.class));
        }
    }

    private void destFareMain(){
        if (mStartLatLng != null){
            fareCalculator(new LatLng(mStartLatLng.latitude, mStartLatLng.longitude),
                    new LatLng(Common.mDestinationLatLng.latitude, Common.mDestinationLatLng.longitude));
            finish();
        }else{
            fareCalculator(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                    new LatLng(Common.mDestinationLatLng.latitude, Common.mDestinationLatLng.longitude));
            finish();

        }
    }

    @Override
    public void onAddressSuccessful(String address) {
        addressOnMap.setText(address);
        startMapAddress=address;
    }

    private void buildRecyclerView() {

        recyclerView = findViewById(R.id.addressListingD);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        mPlaceAdapter = new PlaceAutoCompleteAdapters(this, R.layout.auto_complete_item_apdater, //mGoogleApiClient,
                BOUNDS, null );

        recyclerView.setAdapter(mPlaceAdapter);

        mPlaceAdapter.setOnItemClickListener(new PlaceAutoCompleteAdapters.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

                final PlaceAutoCompleteAdapters.PlaceAutocomplete item = mPlaceAdapter.getItem(position);
                assert item != null;
                final String placeId = String.valueOf(item.placeId);
                Log.i(LOG_TAG, "Autocomplete item selected: " + item.description);
                       /*
                 Issue a request to the Places Geo Data API to retrieve a Place object with additional
                details about the place.
                  */
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, placeId);
                placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {
                        if (!places.getStatus().isSuccess()) {
                            // Request did not complete successfully
                            Log.e(LOG_TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                            places.release();
                            return;
                        }
                        // Get the Place object from the buffer.
                        final Place place = places.get(0);

                        Common.mDestinationLatLng = place.getLatLng();
                        //noinspection ConstantConditions
                        Common.mDestinationAddress = place.getAddress().toString();
                        HomeActivity.requestDestination.setText(mDestinationAddress);
                        dest.setText(Common.mDestinationAddress);

                        destFareMain();

                    }
                });
            }
        });
    }

    private void getHomeAndWork(){
        FirebaseFirestore fs = FirebaseFirestore.getInstance();

        //noinspection ConstantConditions
        fs.collection(Common.riders).document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection(Common.riderDetails).document(Common.rider_info)
                .addSnapshotListener(AutocompleteDestination.this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                        if (e != null){
                            Log.e(LOG_TAG, "Error"+e.getMessage());
                        }
                        String source = documentSnapshot.getMetadata().isFromCache() ?
                                "local cache" : "server";
                        Log.d(LOG_TAG, "Data fetched from " + source);

                        if (documentSnapshot.exists()){
                            mHomeAddress = documentSnapshot.getString("Home Address");
                            String homeLat = documentSnapshot.getString("Home Lat");
                            String homeLng = documentSnapshot.getString("Home Lng");
                            mWorkAddress = documentSnapshot.getString("Work Address");
                            String workLat = documentSnapshot.getString("Work Lat");
                            String workLng = documentSnapshot.getString("Work Lng");

                            if (homeLat != null && homeLng != null) {
                                mHomeLatLng = new LatLng(Double.parseDouble(homeLat), Double.parseDouble(homeLng));
                                homeAddress.setVisibility(View.VISIBLE);
                                homeAddress.setText(mHomeAddress);
                                homeBtn.setText(R.string.home);
                            }else {
                                homeAddress.setVisibility(View.GONE);
                                homeBtn.setText(R.string.add_home);
                            }
                            if (workLat != null && workLng != null ) {
                                Common.mWorkLatLng = new LatLng(Double.parseDouble(workLat), Double.parseDouble(workLng));

                                workAddress.setVisibility(View.VISIBLE);
                                workAddress.setText(mWorkAddress);
                                workBtn.setText(R.string.work);
                            }else {
                                workAddress.setVisibility(View.GONE);
                                workBtn.setText(R.string.add_work);
                            }
                        }

                    }
                });
    }

    private synchronized void BuildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Places.GEO_DATA_API)
                .build();
        mGoogleApiClient.connect();
    }

    private void moveCamera(LatLng latLng, float zoom) {

        if (latLng != null) {
            CameraPosition position = new CameraPosition.Builder()
                    .target(latLng) // Sets the new camera position
                    .zoom(zoom) // Sets the zoom
                    .tilt(10) // Set the camera tilt
                    .build(); // Creates a CameraPosition from the builder

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            //.bearing(180) // Rotate the camera
        }
    }

    private void displayDeviceLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            try{

                Task<Location> location = mFusedLocationClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        mLastLocation = task.getResult();
                        if (mLastLocation !=null) {
                            double lat = mLastLocation.getLatitude();
                            double lng = mLastLocation.getLongitude();
                            moveCamera(new LatLng(lat, lng), DEFAULT_ZOOM);

                        }else {
                            getLocationPermission();
                        }
                    }
                });

            }catch (SecurityException e){
                Log.e(LOG_TAG, " Failed to get your Location" + e.getMessage());
            }
        }else {
            getLocationPermission();
        }

    }

    private void uploadLastLocation(){
        SharedPreferences mSharePre = getSharedPreferences("lastLocation", Context.MODE_PRIVATE);
        mSharePre.getFloat("lastLat", 0);
        mSharePre.getFloat("lastLng", 0);
        if (mSharePre.getFloat("lastLat", 0) != 0 && mSharePre.getFloat("lastLng", 0) != 0){
            moveCamera(new LatLng(mSharePre.getFloat("lastLat", 0),
                    mSharePre.getFloat("lastLng", 0)), DEFAULT_ZOOM);

            //mLastLocation.setLatitude(mSharePre.getFloat("lastLat", 0));
            //mLastLocation.setLongitude(mSharePre.getFloat("lastLng", 0));
        }

    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_PERMISSION_CODE);
        }
    }

    private void closeKeyBoard(){
        View view = this.getCurrentFocus();
        if (view != null){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void configureCameraIdle(){
        onCameraIdleListener = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                startMapLatLng = mMap.getCameraPosition().target;
                new AddressUpdate(AutocompleteDestination.this, AutocompleteDestination.this)
                        .execute(startMapLatLng);

            }
        };
    }

    private void fareCalculator(LatLng origin, LatLng des){
        try {
            new FareCalculating(AutocompleteDestination.this, origin, des).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDirectionFinderStart() {
        HomeActivity.progressEstimatePrice.setVisibility(View.VISIBLE  );
        HomeActivity.estimatedPrice.setVisibility(View.GONE);

    }

    @Override
    public void onDirectionFinderSuccess(List<Route> route) {
        HomeActivity homeActivity = new HomeActivity();
        HomeActivity.progressEstimatePrice.setVisibility(View.GONE);
        HomeActivity.estimatedPrice.setVisibility(View.VISIBLE);
        for (Route route1: route){
            homeActivity.fareCalculationUpdate(route1.duration.text, route1.distance.getText());
            //Toast.makeText(homeActivity, ""+route1.duration.text, Toast.LENGTH_SHORT).show();

        }

    }

}