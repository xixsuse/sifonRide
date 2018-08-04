package com.example.daniel.sifonride.common;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Map;

public class Common {
    public static String methodOfTrans;

    //driver variable
    public static final String DriverLocation = "Driver Location";
    public static final String drive_info = "Driver Information";
    public static final String drivers = "Drivers";
    public static final String driver_details = "Driver Details";
    public static final String driverRating = "Driver Rating";
    public static final String historyUpdate  = "Driver History";
    public static final String driverNumCancel = "Driver number of cancel";

    public static final String bikers = "Bikers";
    public static final String bikersInfo = "Biker Information";
    public static final String bikerNumCancel = "Biker number of cancel";
    public static final String bikersDetails = "Biker Details";
    public static final String bikers_alert = "Biker Alert]";

    public static final String bikersRatings = "Biker Ratings";
    public static final String bikerLocation = "Biker Location";

    //rider variable
    public static final String riders  = "Riders";
    public static final String rider_info  = "Rider Information";
    public static final String riderDetails  = "Rider Details";
    public static final String economy = "Economy";
    static final String cycle = "Cycle";
    public static final String riderHistory = "Rider History";
    public static final String riderRating = "Rider Rating";


    //variable for ride request
    public static final String token_table = "Tokens";
    public static final String Driver_alert =  "Driver Alert";
    public static final String rider_alert =  "Rider Alert";
    public static String riderCurrentLocation = "Rider current location";
    public static String driverCurrentLocation = "Driver current location";
    public static String bikeWorking = "Biker Working";


    public static final String driverHistory = "Rider History";
    public static final String requestUpdate = "Request Update";
    public static final String requestUpdateR = "Rider request update";

    public static final String driverUpdate = "Driver update";
    public static final String driveRequestUpdate = "Driver request update";
    public static final String rideHasEnded = "Ride ended";

    public static final String fareDataBase = "Fare Price";

    //for Location request
    public static final int SMALLEST_INTERVAL = 1000;
    public static final int FASTEST_INTERVAL = 900;
    public static final int DISPLACEMENT = 1000;

    //for permissions
    public static final int REQUEST_PERMISSION_CODE = 1000;

    public static final float DEFAULT_ZOOM = 16.3f;
    public static final int REQUEST_PERMISSION_PHONE_CODE = 10;

    //Key for storing location when activity is paused.
    public static final String KEY_CAMERA_POSITION = "camera_position";
    public static final String LOCATION_KEY = "location";
    public static final int REQUEST_CHECK_SETTINGS = 5;

    //This data are for place complete
    public static String mStartAddress;
    public static String mDestinationAddress;
    public static LatLng mStartLatLng;
    public static LatLng mDestinationLatLng;
    public static String mHomeAddress;
    public static LatLng mHomeLatLng;
    public static String mWorkAddress;
    public static LatLng mWorkLatLng;
    public static String mSavePlaceAddress;
    public static LatLng mSavePlaceLatLng;
    //this is to get distance from time;
    public static String fareDuration;
    public static String fareDistance;

    public static Location mCurrentLocation;
    public static Location mLastLocation;
    public static String countryCode;

    public static double mDriverBaseFare = 2.3;
    public static double mDriverTimeFare= 0.5;
    public static double mDriverDistanceFare= 0.8;
    public static double mDriverMinimumFare= 6;

    public static double mBikerBaseFare= 1.3;
    public static double mBikerTimeFare= 0.3;
    public static double mBikerDistanceFare= 0.5;
    public static double mBikerMinimumFare= 5;


    public static int mScanningRadius =3;
    public static int mLimitRadius = 5;
    public static LatLng myScreenLocation, mRequestLocation;
    public static Map<String, Double> mDriversKey = new HashMap<>();
    public static Map<String, Marker> mDriversMarker = new HashMap<>();
    public static Map<String, String> mUsedDriverKeys = new HashMap<>();

    //booking variable
    public static boolean bookingBool =  false;
    public static boolean acceptBooking;
    public static String mDriverProfile, mDriverName, mDiverOtherName,
            driverRatingT,mDriverNames, mPlateNum, mDriverPhone, mCarName;
    public static double driverRatingSum, driverRatingSumB,driverRatingSumC,
            driverRatingTotal;
    public static String mDriverId;

    public static final String CHANNEL_ID =  "4";

    public static String mBaseFare;
    public static String mTimeFare;
    public static String mDistanceFare;
    public static String mTotalFare;
    public static String mPromoFare;
    public static String mBounceFare;

}
