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
import java.util.ArrayList;
import java.util.List;

/**
 * Create d by DANIEL on 2/17/2018.
 */

public class FindTimeDistance {
    private final static String DIRECTION_URL_API =  "https://maps.googleapis.com/maps/api/directions/json?";
    private final static String GOOGLE_API_KEY = "AIzaSyBqbcuglpiBZeQRgrt9BvFhUbwazQPFpLk" ;
    private LatLng origin, destination;
    private TimeDistanceListener listener;


    public FindTimeDistance(TimeDistanceListener listener, LatLng origin, LatLng destination) {
        this.listener = listener;
        this.origin = origin;
        this.destination = destination;
    }


    public void execute() throws UnsupportedEncodingException {
        listener.onTimeDistanceStart();
        new FindTimeDistance.DownloadRawData().execute(createUrl());
    }

//    private String createUrl() throws UnsupportedEncodingException {
//        String urlOrigin = URLEncoder.encode(origin, "utf-8");
//        String urlDestination = URLEncoder.encode(destination, "utf-8");
//
//        return DIRECTION_URL_API + "origin=" + urlOrigin + "&destination=" + urlDestination + "&key=" + GOOGLE_API_KEY;
//    }

    private String createUrl (){
        return  DIRECTION_URL_API + "origin="+origin.latitude +","+origin.longitude+
                "&destination="+destination.latitude+","+destination.longitude + "&key=" + GOOGLE_API_KEY;
    }

    protected class DownloadRawData extends AsyncTask<String, Void, String> {

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
    }

    private void parseJSon(String data) throws JSONException {
        if (data == null)
            return;

        List<Route> routes = new ArrayList<Route>();
        JSONObject jsonData = new JSONObject(data);
        JSONArray jsonRoutes = jsonData.getJSONArray("routes");
        for (int i = 0; i < jsonRoutes.length(); i++) {
            JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
            Route route = new Route();

            //JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
            JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
            JSONObject jsonLeg = jsonLegs.getJSONObject(0);
            JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
            JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
            JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");
            JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");

            route.distance = new Distance(jsonDistance.getString("text"), jsonDistance.getInt("value"));
            route.duration = new Duration(jsonDuration.getString("text"), jsonDuration.getInt("value"));
            route.endAddress = jsonLeg.getString("end_address");
            route.startAddress = jsonLeg.getString("start_address");
            route.startLocation = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
            route.endLocation = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));


            routes.add(route);
        }

        listener.onTimeDistanceSuccess(routes);
    }


    //    public static class TimeDistance extends AsyncTask<String, Void, String>{
//
//        @Override
//        protected String doInBackground(String... params) {
//            String link = params[0];
//            try {
//                URL url = new URL(link);
//                InputStream is = url.openConnection().getInputStream();
//                StringBuffer buffer = new StringBuffer();
//                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    buffer.append(line + "\n");
//                }
//
//                return buffer.toString();
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            timeDistance(s);
//            super.onPostExecute(s);
//        }
//
//        private void timeDistance(String res){
//
//            List<Route> routess = new ArrayList<Route>();
//            Route routes = new Route();
//
//            try {
//                JSONObject jsonObject = new JSONObject(res);
//                JSONArray jsonArray = jsonObject.getJSONArray("routes");
//                for (int i=0; i< jsonArray.length(); i++){
//                    JSONObject route = jsonArray.getJSONObject(i);
//                    JSONArray legs = route.getJSONArray("legs");
//                    JSONObject steps = legs.getJSONObject(0);
//                    JSONObject distance = steps.getJSONObject("distance");
//                    JSONObject duration = distance.getJSONObject("duration");
//
//                    routes.distance = new Distance(distance.getString("text"), distance.getInt("value"));
//                    routes.duration = new Duration(duration.getString("text"), duration.getInt("value"));
//                }
//                listener.onTimeDistanceSuccess(routess);
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//        }
//    }

}
