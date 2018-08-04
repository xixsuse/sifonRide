package com.example.daniel.sifonride.GoogleDirectionApi;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Create d by DANIEL on 2/22/2018.
 */

public class FindTime {
    private final static String DIRECTION_URL_API =  "https://maps.googleapis.com/maps/api/directions/json?";
    private final static String GOOGLE_API_KEY = "AIzaSyBqbcuglpiBZeQRgrt9BvFhUbwazQPFpLk" ;
    private LatLng origin, destination;

    public FindTime(LatLng origin, LatLng destination) {
        this.origin = origin;
        this.destination = destination;
    }

    public void execute() throws UnsupportedEncodingException {
        new FindTime.DownloadRawData().execute(createUrl());
    }



    private String createUrl (){
        return  DIRECTION_URL_API + "origin="+origin.latitude +","+origin.longitude+
                "&destination="+destination.latitude+","+destination.longitude + "&key=" + GOOGLE_API_KEY;
    }
    private static class DownloadRawData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String link = params[0];
            try {
                URL url = new URL(link);
                InputStream is = url.openConnection().getInputStream();
                StringBuffer buffer = new StringBuffer();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                parseJSon(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void parseJSon(String res) throws JSONException {
            if (!res.isEmpty()){

                JSONObject jsonData = new JSONObject(res);
                JSONArray jsonRoutes = jsonData.getJSONArray("routes");

                for (int i = 0; i< jsonRoutes.length(); i++){
                    JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
                    Route route = new Route();

                    JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
                    JSONObject jsonLeg = jsonLegs.getJSONObject(0);
                    JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
                    JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
                    JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");
                    JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");

                    route.distance.setText(jsonDistance.getString("text"));
                    route.distance.setValue(jsonDistance.getInt("value"));
                    route.duration.setText(jsonDuration.getString("text"));
                    route.duration.setValue(jsonDuration.getInt("value"));
                    route.setEndAddress(jsonLeg.getString("end_address"));
                    route.setStartAddress(jsonLeg.getString("start_address"));
                    route.setEndLocation( new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng")));
                    route.setStartLocation( new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng")));

                }
            }

        }
    }


}
