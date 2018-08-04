package com.example.daniel.sifonride;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import com.example.daniel.sifonride.model.Util;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

public class AddressUpdate extends AsyncTask<LatLng, Void, String>{
    @SuppressLint("StaticFieldLeak")
    private Context mContext;

    private AddressListener addressListener;

    public AddressUpdate(Context mContext, AddressListener listener) {
        this.mContext = mContext;
        this.addressListener = listener;

    }

    @Override
    protected void onPostExecute(String s) {
//        mapHomeAddress.setText(s);
//        homeMapAddress = s;\
        addressListener.onAddressSuccessful(s);
        super.onPostExecute(s);

    }

    @Override
    protected String doInBackground(LatLng... latLngs) {

        LatLng latLng = latLngs[0];
        if (Util.Operations.isOnline(mContext)) {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            List<Address> addresses = null;
            String addres = null;

            try {
                addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addresses != null && addresses.size() > 0) {
                    addres = addresses.get(0).getAddressLine(0).trim();
                }
            } catch (IOException e) {
                e.printStackTrace();
                //addressOnMap.setText("Loading Address....");
            }


            return addres;
        }else {
            return "No internet connection";
        }
    }

}