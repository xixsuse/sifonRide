package com.example.daniel.sifonride.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.daniel.sifonride.GoogleDirectionApi.DirectionFinderListener;
import com.example.daniel.sifonride.GoogleDirectionApi.Route;
import com.example.daniel.sifonride.GoogleDirectionApi.RouteFinder;
import com.example.daniel.sifonride.HomeActivity;
import com.example.daniel.sifonride.R;
import com.example.daniel.sifonride.common.Common;
import com.example.daniel.sifonride.model.Util;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import static com.example.daniel.sifonride.common.Common.CHANNEL_ID;
import static com.example.daniel.sifonride.common.Common.DISPLACEMENT;
import static com.example.daniel.sifonride.common.Common.Driver_alert;
import static com.example.daniel.sifonride.common.Common.FASTEST_INTERVAL;
import static com.example.daniel.sifonride.common.Common.SMALLEST_INTERVAL;
import static com.example.daniel.sifonride.common.Common.acceptBooking;
import static com.example.daniel.sifonride.common.Common.drive_info;
import static com.example.daniel.sifonride.common.Common.driverCurrentLocation;
import static com.example.daniel.sifonride.common.Common.driverRatingSum;
import static com.example.daniel.sifonride.common.Common.driverRatingSumB;
import static com.example.daniel.sifonride.common.Common.driverRatingSumC;
import static com.example.daniel.sifonride.common.Common.driverRatingT;
import static com.example.daniel.sifonride.common.Common.driverRatingTotal;
import static com.example.daniel.sifonride.common.Common.driver_details;
import static com.example.daniel.sifonride.common.Common.drivers;
import static com.example.daniel.sifonride.common.Common.mBounceFare;
import static com.example.daniel.sifonride.common.Common.mCarName;
import static com.example.daniel.sifonride.common.Common.mDestinationAddress;
import static com.example.daniel.sifonride.common.Common.mDestinationLatLng;
import static com.example.daniel.sifonride.common.Common.mDistanceFare;
import static com.example.daniel.sifonride.common.Common.mDiverOtherName;
import static com.example.daniel.sifonride.common.Common.mDriverId;
import static com.example.daniel.sifonride.common.Common.mDriverName;
import static com.example.daniel.sifonride.common.Common.mDriverNames;
import static com.example.daniel.sifonride.common.Common.mDriverPhone;
import static com.example.daniel.sifonride.common.Common.mDriverProfile;
import static com.example.daniel.sifonride.common.Common.mDriversKey;
import static com.example.daniel.sifonride.common.Common.mLastLocation;
import static com.example.daniel.sifonride.common.Common.mPlateNum;
import static com.example.daniel.sifonride.common.Common.mPromoFare;
import static com.example.daniel.sifonride.common.Common.mScanningRadius;
import static com.example.daniel.sifonride.common.Common.mTimeFare;
import static com.example.daniel.sifonride.common.Common.mTotalFare;
import static com.example.daniel.sifonride.common.Common.mUsedDriverKeys;
import static com.example.daniel.sifonride.common.Common.myScreenLocation;
import static com.example.daniel.sifonride.common.Common.requestUpdate;
import static com.example.daniel.sifonride.common.Common.riderCurrentLocation;
import static com.example.daniel.sifonride.common.Common.rider_alert;
import static com.example.daniel.sifonride.common.Common.riders;
import static com.example.daniel.sifonride.common.Common.mBaseFare;


public class Booking extends Service implements DirectionFinderListener {

    //TODO: Remember set location api for when gps is off.
    private CountDownTimer bookingCountDown, durationCountDown;
    private FirebaseFirestore mFs = FirebaseFirestore.getInstance();
    private GeoQuery geoQuery;

    private FusedLocationProviderClient mFusedLocation;
    private LocationCallback mLocationCallBack;
    private LocationRequest mLocationRequest;
    public SharedPreferences mActiveBookingDetails;
    private SharedPreferences.Editor mDriverSearchEditor;

    private boolean hasRideStarted, wasDriverLayoutShown;
    private Location mStartLocation , mEndLocation;
    private double totalDistance, totalDuration;
    private long mStartTime, mEndTime, timeDiff;

    private static final int MINUTE = 60;
    private static final int HOUR = 60;
    private static final int DAY = 24;

    public Booking() {
    }

    private static WeakReference<HomeActivity> weakReference;
    public Booking(HomeActivity homeActivity){
        weakReference = new WeakReference<>(homeActivity);
    }

    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (mLastLocation != null)
            collectAllAvailableDriverKey(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        else
            collectAllAvailableDriverKey(new LatLng(myScreenLocation.latitude, myScreenLocation.longitude));

        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);

        locationRequest();

        locationCallBackMethod();

        mActiveBookingDetails = getSharedPreferences("Active booking details", MODE_PRIVATE);

        final HomeActivity activity = weakReference.get();
        if (activity != null && !activity.isFinishing())
            activity.searchCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activity.cancelDriverSearching();
                }
            });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notificationBuilder();
        //searchForAvailableDrivers();
        startLocationUpdate();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdate();

        mDriverSearchEditor = mActiveBookingDetails.edit();
        mDriverSearchEditor.putBoolean("Searching for driver",false);
        mDriverSearchEditor.putString("Driver key",null);
        mDriverSearchEditor.apply();
    }

    private void locationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(SMALLEST_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdate(){
        //noinspection MissingPermission
        if (ActivityCompat.checkSelfPermission(Booking.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(Booking.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

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

    private void locationCallBackMethod(){
        mLocationCallBack = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for(Location location : locationResult.getLocations()){
                    if(getApplicationContext()!=null) {
                        mLastLocation = location;
                        if (hasRideStarted){
                            if (mStartLocation == null) {
                                mStartLocation = location;
                                mEndLocation = location;
                                totalDistance = (mStartLocation.distanceTo(mEndLocation) / 1000);
                                HomeActivity activity = weakReference.get();
                                if (activity != null && !activity.isFinishing()) {
                                    activity.driverDistanceAway.setText(String.valueOf(totalDistance));
                                    activity.driverDistanceAwayD.setText(String.valueOf(totalDistance));
                                }
                            }else {
                                mEndLocation = location;
                                totalDistance = totalDistance+(mStartLocation.distanceTo(mEndLocation) / 1000);

                                HomeActivity activity = weakReference.get();
                                if (activity != null && !activity.isFinishing()) {
                                    activity.driverDistanceAway.setText(String.valueOf(totalDistance));
                                    activity.driverDistanceAwayD.setText(String.valueOf(totalDistance));
                                }
                                mStartLocation = mEndLocation;
                            }
                        }
                    }
                }
            }
        };
    }

    private void stopLocationUpdate(){
        mFusedLocation.removeLocationUpdates(mLocationCallBack);
    }

    public void notificationBuilder(){
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.carone)
                .setContentTitle("Request update")
                .setContentText(getResources().getString(R.string.booking))
                .setContentIntent(pendingIntent)
                .build();

        startForeground(9, notification);
    }

    public void collectAllAvailableDriverKey(LatLng latLng) {

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(Common.DriverLocation);
        GeoFire geoFire = new GeoFire(mRef);

        double lat = latLng.latitude;
        double lng = latLng.longitude;
        Toast.makeText(Booking.this, "DNANNELI", Toast.LENGTH_SHORT).show();

        geoQuery = geoFire.queryAtLocation(new GeoLocation(lat, lng), mScanningRadius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (key != null) {
                    // Add a new marker to the map
                    mDriversKey.put(key, 8.9);

                }

            }

            @Override
            public void onKeyExited(String key) {
                mDriversKey.remove(key);
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                searchForAvailableDrivers();
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
            }

        });


    }

    private void searchForAvailableDrivers(){
        if (Util.Operations.isOnline(this)) {
            Toast.makeText(this, "HHHHH", Toast.LENGTH_SHORT).show();

            if (!mDriversKey.isEmpty()) {
                for (String key:mDriversKey.keySet()){

                    if (!mUsedDriverKeys.containsKey(key)){
                        mDriverId = key;
                        Toast.makeText(this, "ttt", Toast.LENGTH_SHORT).show();
                        mDriverSearchEditor = mActiveBookingDetails.edit();
                        mDriverSearchEditor.putBoolean("Searching for driver",true);
                        mDriverSearchEditor.putString("Driver key",key);
                        mDriverSearchEditor.apply();
                        sendRequestToDriver();
                        //noinspection ConstantConditions
                        mFs.collection(riders).document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .collection(rider_alert).document(requestUpdate).delete();
                    }
                }
                //This means these mean after the loop was completed there was no driver.
//                HomeActivity activity = weakReference.get();
//                if (activity != null && !activity.isFinishing()) {
//                    activity.alertDialog(getResources().getString(R.string.no_driver_text));
//                    activity.homeLayout.setVisibility(View.VISIBLE);
//                    activity.searchLayout.setVisibility(View.GONE);
//                }
//                Toast.makeText(this, "uuuu", Toast.LENGTH_SHORT).show();
//
//                stopSelf();
            } else {

                HomeActivity activity = weakReference.get();
                if (activity != null && !activity.isFinishing()) {
                    activity.alertDialog(getResources().getString(R.string.no_driver_text));
                    activity.homeLayout.setVisibility(View.VISIBLE);
                    activity.searchLayout.setVisibility(View.GONE);

                }
                mDriverSearchEditor = mActiveBookingDetails.edit();
                mDriverSearchEditor.putBoolean("Searching for driver",false);
                mDriverSearchEditor.putString("Driver key",null);
                mDriverSearchEditor.apply();
                Toast.makeText(this, "dddd", Toast.LENGTH_SHORT).show();

                stopSelf();

            }
        }else {
            HomeActivity activity = weakReference.get();
            if (activity != null && !activity.isFinishing()) {
                activity.searchingText.setText(getResources().getString(R.string.no_internet));

                activity.searchCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mLastLocation != null)
                            collectAllAvailableDriverKey(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                        else
                            collectAllAvailableDriverKey(new LatLng(myScreenLocation.latitude, myScreenLocation.longitude));

                    }
                });

            }
            mDriverSearchEditor = mActiveBookingDetails.edit();
            mDriverSearchEditor.putBoolean("Searching for driver",true);
            mDriverSearchEditor.putString("Driver key",null);
            mDriverSearchEditor.apply();

            stopSelf();
        }
    }

    private void sendRequestToDriver(){

        //This used to removed the assign driver for driver available
        DatabaseReference db = FirebaseDatabase.getInstance().getReference(Common.DriverLocation).child(mDriverId);
        db.removeValue();

        Map<String, Object> map = new HashMap<>();
        map.put("Destination", mDestinationAddress);
        if (mDestinationLatLng!=null)
            map.put("DestinationLat", String.valueOf(mDestinationLatLng.latitude));
        if (mDestinationLatLng!=null)
            map.put("DestinationLng", String.valueOf(mDestinationLatLng.longitude));
        map.put("PickUpLat", String.valueOf(myScreenLocation.latitude));
        map.put("PickUpLng", String.valueOf(myScreenLocation.longitude));
        map.put("PayMethod", "Cash");

        //noinspection ConstantConditions
        map.put("CustomerKey",FirebaseAuth.getInstance().getCurrentUser().getUid());
        //noinspection ConstantConditions
        mFs.collection(drivers).document(mActiveBookingDetails.getString("Driver key",null)).collection("Economy")
                .document(Driver_alert).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                bookingCountDownMethod();
                receivedDriverResponds();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //TODO:Add on fail event
                HomeActivity activity = weakReference.get();
                if (activity != null && activity.isFinishing()) {
                    String mss = e.getMessage();
                    activity.searchingText.setText(mss);
                }
            }
        });
    }

    private void bookingCountDownMethod(){

        bookingCountDown = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                Toast.makeText(Booking.this, "finish", Toast.LENGTH_SHORT).show();
                //noinspection ConstantConditions
                mFs.collection(driver_details).document(mActiveBookingDetails.getString("Driver key",null)).collection("Economy")
                        .document(Driver_alert).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        searchForAvailableDrivers();
                    }
                });

            }
        };
        bookingCountDown.start();
    }

    private void receivedDriverResponds(){
        final FirebaseFirestore mFs = FirebaseFirestore.getInstance();

        //noinspection ConstantConditions
        mFs.collection(riders).document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection(rider_alert).document(requestUpdate).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null &&documentSnapshot.exists()){

                    if (Objects.equals(documentSnapshot.getString("Request Update"), "Reject")){

                        bookingCountDown.cancel();

                        searchForAvailableDrivers();

                    }else if (Objects.equals(documentSnapshot.getString("Request Update"), "Cancel")) {
                        HomeActivity activity = weakReference.get();
                        if (activity != null && !activity.isFinishing())
                            activity.cancelAlertDialog();
//                            activity.homeLayout.setVisibility(View.VISIBLE);
//                            activity.driverLayout.setVisibility(View.GONE);
//                        }
//                        stopSelf();
                        searchForAvailableDrivers();

                    }else if (Objects.equals(documentSnapshot.getString("Request Update"), "Accept")) {

                        bookingCountDown.cancel();
                        driverDetails();
                        acceptedRideMethod();
                        driverCurrentLocation();
                        uploadCurrentLocation();
                        acceptBooking = true;
                        mDriverSearchEditor = mActiveBookingDetails.edit();
                        mDriverSearchEditor.putBoolean("Searching for driver",false);
                        mDriverSearchEditor.apply();
                    }else if (Objects.equals(documentSnapshot.getString("Request Update"), "Arrived"))
                    {
                        HomeActivity activity = weakReference.get();
                        if (activity != null && !activity.isFinishing())
                            activity.alertDialog(getString(R.string.driver_arrive_text));
                    }else if (Objects.equals(documentSnapshot.getString("Request Update"), "Start Ride"))
                    {
                        HomeActivity activity = weakReference.get();
                        if (activity != null && !activity.isFinishing())
                            activity.alertDialog(getString(R.string.trip_start_text));
                        hasRideStarted = true;
                        calculateDurationCovered();
                    }
                        else if (Objects.equals(documentSnapshot.getString("Request Update"), "Ride ended"))
                    {
                           //TODO: Next complete this method.
                            // riderHasEnd();
                        mBaseFare = documentSnapshot.getString("BaseFare");
                        mTimeFare = documentSnapshot.getString("TimeFare");
                        mDistanceFare = documentSnapshot.getString("DistanceFare");
                        mTotalFare = documentSnapshot.getString("TotalFare");
                        mPromoFare = documentSnapshot.getString("PromoFare");
                        mBounceFare = documentSnapshot.getString("BounceFare");

                        fareDisplay();
                        hasRideStarted = false;

                        durationCountDown.cancel();
                    }
                            // else if (Objects.equals(documentSnapshot.getString("Request Update"), "Paid"))
//                    {
//                        //
//                    }
                }
            }
        });
    }

    public void uploadCurrentLocation(){
        //TODO: When creating settings add location security.
        DatabaseReference df = FirebaseDatabase.getInstance().getReference(riderCurrentLocation).child(mDriverId);
        GeoFire geoFire = new GeoFire(df);

        if (mLastLocation != null) {

            //noinspection ConstantConditions
            geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                    new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        }
    }

    public void driverDetails(){

        mFs = FirebaseFirestore.getInstance();

        //noinspection ConstantConditions
        mFs.collection(drivers).document(mActiveBookingDetails.getString("Driver key",null))
                .collection(Common.driverRating)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot querySnapshot, FirebaseFirestoreException e) {
                        if (!querySnapshot.isEmpty()){
                            for (DocumentSnapshot document: querySnapshot.getDocuments()){
                                if (document.getString("Rating")!= null){
                                    driverRatingSum += Double.valueOf(document.getString("Rating"));
                                    driverRatingTotal ++;
                                }
                            }
                            driverRatingSumB = driverRatingSum/driverRatingTotal;

                            driverRatingSumC = Double.valueOf(new DecimalFormat("#.##").format(driverRatingSumB));

                            driverRatingT = String.valueOf(driverRatingSumC);
                            HomeActivity activity = weakReference.get();
                             if (activity != null && !activity.isFinishing()) {
                                 activity.driverRating.setText(driverRatingT);
                                 activity.driverRatingD.setText(driverRatingT);
                             }
                        }else {
                             HomeActivity activity = weakReference.get();
                             if (activity != null && !activity.isFinishing()) {
                                 activity.driverRating.setText(String.valueOf("0.0"));
                                 activity.driverRatingD.setText(String.valueOf("0.0"));
                             }
                        }
                    }
                });

        //noinspection ConstantConditions
        mFs.collection(drivers).document(mActiveBookingDetails.getString("Driver key",null))
                .collection(driver_details).document(drive_info).addSnapshotListener(
                new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (documentSnapshot.exists()) {
                    mDriverName = documentSnapshot.getString("Name");
                    mDiverOtherName = documentSnapshot.getString("Other name");
                    mDriverNames = mDriverName + " " + mDiverOtherName;
                    mPlateNum = documentSnapshot.getString("Car plate number");
                    mCarName = documentSnapshot.getString("Car name");
                    mDriverPhone = documentSnapshot.getString("Phone number");
                    mDriverProfile = documentSnapshot.getString("Profile Image");

                    HomeActivity activity = weakReference.get();
                    if (activity != null && !activity.isFinishing()) {
                        if (mDriverProfile != null) {
                            Glide.with(activity).load(mDriverProfile).into(activity.driverProfile);
                        } else {
                            Glide.with(activity).load(R.drawable.profile).into(activity.driverProfile);
                        }

                        if (mDriverNames != null)
                            activity.driverName.setText(mDriverNames); activity.driverNameD.setText(mDriverNames);
                        if (mPlateNum != null)
                            activity.driverPlate.setText(mPlateNum); activity.driverPlateD.setText(mPlateNum);
                        if (mCarName != null)
                            activity.driverCar.setText(mCarName); activity.driverCarD.setText(mCarName);
                    }
                }
            }
        });

    }

    public void acceptedRideMethod(){
        HomeActivity activity = weakReference.get();
        if (activity != null && !activity.isFinishing()){
            activity.searchLayout.setVisibility(View.GONE);
            activity.driverLayout.setVisibility(View.VISIBLE);


            if (!mDestinationAddress.isEmpty())
                activity.destText.setText(mDestinationAddress);

            driverLayoutUiControl();

        }

    }

    public void driverLayoutUiControl(){
        final HomeActivity activity = weakReference.get();
        if (activity != null && !activity.isFinishing()) {

            activity.destText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //startActivity(new Intent());
                    //TODO: Add destination input class.
                    Toast.makeText(activity, "Add destination", Toast.LENGTH_SHORT).show();
                }
            });

            activity.moreInfoBackBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { activity.moreInfoLayout.setVisibility(View.GONE); }
            });

            activity.moreInfoBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { activity.moreInfoLayout.setVisibility(View.VISIBLE); }
            });

            activity.callDriverD.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activity.callDriverMethod();
                }
            });
            activity.callDriver.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activity.callDriverMethod();
                }
            });

            activity.cancelRide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activity.cancelRideDriverLayout();
                }
            });

        }
    }

    public void driverCurrentLocation(){
        //noinspection ConstantConditions
        DatabaseReference df = FirebaseDatabase.getInstance().getReference(driverCurrentLocation)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        GeoFire geoFire = new GeoFire(df);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()),6);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                try {
                    new RouteFinder(Booking.this,
                            new LatLng(location.latitude, location.latitude),
                            new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())).execute();
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }

             Toast.makeText(Booking.this, "Driver working "+location, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                try {
                    new RouteFinder(Booking.this,
                            new LatLng(location.latitude, location.latitude),
                            new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())).execute();
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    //    public void calculateDistanceCovered(){
//
//    }

    public void calculateDurationCovered() {

        durationCountDown = new CountDownTimer(999999999, 1000) {
            @Override
            public void onTick(long l) {
                HomeActivity activity = weakReference.get();
                mEndTime = System.currentTimeMillis();
                timeDiff = mEndTime-mStartTime;
                timeDiff = TimeUnit.MILLISECONDS.toMinutes(timeDiff);
                totalDuration = TimeUnit.MINUTES.toHours(timeDiff);

                if (timeDiff < MINUTE){
                    String min = timeDiff+" "+"mins";
                    if (activity != null && !activity.isFinishing()) {
                        activity.driverTimeAwayD.setText(min);
                        activity.driverTimeAwayD.setText(min);
                    }
                }else if (timeDiff >MINUTE){
                    String min = timeDiff%MINUTE+" "+"mins";
                    String hr = totalDuration+" "+"hr";
                    String time = hr+" "+min;
                    if (activity != null && !activity.isFinishing()) {
                        activity.driverTimeAwayD.setText(time);
                        activity.driverTimeAwayD.setText(time);
                    }
                }else if (totalDuration < DAY){
                    String min = timeDiff%MINUTE+" "+"mins";
                    String hr = totalDuration+" "+"hr";
                    String day = String.valueOf(TimeUnit.MINUTES.toDays(timeDiff))+" "+"day";
                    String time = day+" "+hr+" "+min;
                    if (activity != null && !activity.isFinishing()) {
                        activity.driverTimeAwayD.setText(time);
                        activity.driverTimeAwayD.setText(time);
                    }
                }


            }

            @Override
            public void onFinish() {
                durationCountDown.start();
            }

        };
        durationCountDown.start();
    }

    private void fareDisplay(){
        HomeActivity activity = weakReference.get();
        if (activity != null && !activity.isFinishing()){
            activity.fareLayout.setVisibility(View.VISIBLE);
            if (mBaseFare != null)
                activity.baseFare.setText(String.valueOf(mBaseFare));
            if (mDistanceFare != null)
                activity.distanceFare.setText(mDistanceFare);
            if (mTimeFare != null)
                activity.timeFare.setText(mTimeFare);
            if (mTotalFare != null)
                activity.totalFare.setText(mTotalFare);
            if (mBounceFare != null) {
                activity.bounceLayout.setVisibility(View.VISIBLE);
                activity.bounceFare.setText(mBounceFare);
            }
            if (mPromoFare != null) {
                activity.promoLayout.setVisibility(View.VISIBLE);
                activity.promoFare.setText(mPromoFare);
            }

            activity.fareBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HomeActivity activity = weakReference.get();
                    if (activity != null && !activity.isFinishing())
                        activity.rateDriver();

                        wasDriverLayoutShown = true;


                }
            });

        }
    }


    @Override
    public void onDirectionFinderStart() {

    }

    @Override
    public void onDirectionFinderSuccess(List<Route> route) {
        for(Route routes: route) {
            HomeActivity activity = weakReference.get();
            if (activity != null && !activity.isFinishing()) {
                activity.driverDistanceAway.setText(routes.distance.getText());
                activity.driverTimeAway.setText(routes.duration.getText());

                Toast.makeText(activity, ""+routes.duration.getText(), Toast.LENGTH_SHORT).show();
                if (activity.polylinePaths != null)
                    activity.polylinePaths.clear();
                    //                activity.destinationMarkers.add(activity.mMap.addMarker(new MarkerOptions()
//                        .icon(BitmapDescriptorFactory.defaultMarker())
//                        .title(routes.endAddress)
//                        .position(routes.endLocation)));

                PolylineOptions polylineOptions = new PolylineOptions().
                        geodesic(true).
                        color(Color.BLUE).
                        width(10);

                for (int i = 0; i < routes.points.size(); i++)
                    polylineOptions.add(routes.points.get(i));

                activity.polylinePaths.add(activity.mMap.addPolyline(polylineOptions));
            }
        }
    }

}