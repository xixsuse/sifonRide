package com.example.daniel.sifonride.autocomplete;

import android.Manifest;
import android.content.Context;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.daniel.sifonride.AddressListener;
import com.example.daniel.sifonride.AddressUpdate;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static com.example.daniel.sifonride.common.Common.mLastLocation;

public class AutocompleteHome extends AppCompatActivity implements OnMapReadyCallback,
        View.OnClickListener, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, AddressListener
{


    private static final int REQUEST_PERMISSION_CODE = 1000;
    public static final float DEFAULT_ZOOM = 13.3f;
    boolean mLocationPermissionGranted = false;

    private static final String LOG_TAG = "AutocompleteHome";
    private EditText homeInput;
    private RecyclerView homeRecycler;
    private ImageView clearHomeInputText;
    private TextView chooseOnMap, autoConfirmText;
    private ImageButton moveToCurrentLocation;
    private LinearLayout homeSearchLayout;
    private RelativeLayout homeMapSearchLayout;
    private Button selectFromMap;

    private PlaceAutoCompleteAdapters mPlaceAdapter;
    protected GoogleApiClient mGoogleApiClient;

    private static LatLngBounds BOUNDS;
    private GoogleMap mMap;
    private GoogleMap.OnCameraIdleListener onCameraIdleListener;
    private String homeMapAddress;
    private LatLng homeMapLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autocomplete_home);

        //noinspection ConstantConditions
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.home_address);

        homeInput = findViewById(R.id.homeAddressInput);
        clearHomeInputText = findViewById(R.id.clearTextH);
        homeRecycler = findViewById(R.id.homeAddressList);
        chooseOnMap = findViewById(R.id.mapPickerH);
        homeSearchLayout = findViewById(R.id.homeSearchLayout);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapH);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        autoConfirmText = findViewById(R.id.autoConfirmCurrentH);
        moveToCurrentLocation = findViewById(R.id.gotoLocH);
        selectFromMap = findViewById(R.id.doneBtnH);
        homeMapSearchLayout = findViewById(R.id.autoMapLayoutH);

        clearHomeInputText.setOnClickListener(this);
        chooseOnMap.setOnClickListener(this);

        moveToCurrentLocation.setOnClickListener(this);
        selectFromMap.setOnClickListener(this);

        if (mLastLocation != null)
            BOUNDS =  new LatLngBounds(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                    new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));


        buildRecyclerView();

        BuildGoogleApiClient();

        homeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int startNum, int before, int count) {
                if (!s.toString().isEmpty() || mGoogleApiClient.isConnected()){
                    //recycleCard.setVisibility(View.VISIBLE);
                    homeRecycler.setVisibility(View.VISIBLE);
                    mPlaceAdapter.getFilter().filter(s.toString());
                }else if (!mGoogleApiClient.isConnected()){
                    Toast.makeText(getApplicationContext(), "Google Api not connected", Toast.LENGTH_SHORT).show();
                }

                if (count < 1){
                    clearHomeInputText.setVisibility(View.GONE);
                    homeRecycler.setVisibility(View.GONE);
                }else{
                    homeRecycler.setVisibility(View.VISIBLE);
                    clearHomeInputText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        configureCameraIdle();
    }

    private void buildRecyclerView(){
        homeRecycler.setHasFixedSize(true);
        homeRecycler.setLayoutManager(new LinearLayoutManager(this));

        mPlaceAdapter = new PlaceAutoCompleteAdapters(this, R.layout.auto_complete_item_apdater, //mGoogleApiClient,
                BOUNDS, null );

        homeRecycler.setAdapter(mPlaceAdapter);

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

                        Common.mHomeLatLng = place.getLatLng();
                        //noinspection ConstantConditions
                        Common.mHomeAddress = place.getAddress().toString();
                        homeInput.setText(Common.mHomeAddress);
                        addHome(place.getAddress().toString(), place.getLatLng());
                        finish();
                    }
                });
            }
        });
    }

    private void addHome(String address, LatLng latLng) {

        String lat = String.valueOf(latLng.latitude);
        String lng = String.valueOf(latLng.longitude);

        FirebaseFirestore fs = FirebaseFirestore.getInstance();

        Map<String, Object> map = new HashMap<>();
        map.put("Home Address", address);
        map.put("Home Lat", lat);
        map.put("Home Lng", lng);

        //noinspection ConstantConditions
        fs.collection(Common.riders).document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection(Common.riderDetails).document(Common.rider_info).update(map);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraIdleListener(onCameraIdleListener);
        uploadLastLocation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                if (homeMapSearchLayout.isShown()) {
                    //noinspection ConstantConditions
                    getSupportActionBar().setSubtitle(R.string.auto_drag_text);
                    homeMapSearchLayout.setVisibility(View.GONE);
                    homeSearchLayout.setVisibility(View.VISIBLE);
                } else {
                    finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (homeMapSearchLayout.isShown()){
            //noinspection ConstantConditions
            getSupportActionBar().setSubtitle("");
            homeMapSearchLayout.setVisibility(View.GONE);
            homeSearchLayout.setVisibility(View.VISIBLE);
        }else {
            super.onBackPressed();
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        if (view == clearHomeInputText){
            homeInput.setText("");
        }else if (view == chooseOnMap){
            closeKeyBoard();
            //noinspection ConstantConditions
            getSupportActionBar().setSubtitle(R.string.auto_drag_text);
            homeSearchLayout.setVisibility(View.GONE);
            homeMapSearchLayout.setVisibility(View.VISIBLE);

        }else if (view == moveToCurrentLocation){
            displayDeviceLocation();
        }else if (view == selectFromMap){
            if (homeMapAddress == null){
                Toast.makeText(this, "Please wait for address to load", Toast.LENGTH_SHORT).show();
            }else if (homeMapAddress.equals("No internet connection")){
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            }else {
                homeMapLatLng = new LatLng(homeMapLatLng.latitude, homeMapLatLng.longitude);
                addHome(homeMapAddress, new LatLng(homeMapLatLng.latitude, homeMapLatLng.longitude));
                finish();
            }

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

    private void moveCamera(LatLng latLng, float zoom) {

        if (latLng != null) {
            CameraPosition position = new CameraPosition.Builder()
                    .target(latLng) // Sets the new camera position
                    .zoom(zoom) // Sets the zoom
                    .tilt(10) // Set the camera tilt
                    .build(); // Creates a CameraPosition from the builder

            if (mMap != null){
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
            }
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            //.bearing(180) // Rotate the camera
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

    private synchronized void BuildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Places.GEO_DATA_API)
                .build();
        mGoogleApiClient.connect();
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
    public void onAddressSuccessful(String address) {
        autoConfirmText.setText(address);
        homeMapAddress = address;
    }

    private void configureCameraIdle(){
        onCameraIdleListener = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                homeMapLatLng = mMap.getCameraPosition().target;
                new AddressUpdate(AutocompleteHome.this, AutocompleteHome.this)
                        .execute(homeMapLatLng);

            }
        };
    }

    private void closeKeyBoard(){
        View view = this.getCurrentFocus();
        if (view != null){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
