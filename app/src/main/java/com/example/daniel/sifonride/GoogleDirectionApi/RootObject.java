package com.example.daniel.sifonride.GoogleDirectionApi;

import java.util.List;

/**
 * Created by DANIEL on 2/9/2018.
 */


public class RootObject {
    public String status;
    public List<GeocodedWaypoint> geocoded_waypoints;
    public List<Route> routes ;

    public RootObject() {
    }

    public RootObject(String status, List<GeocodedWaypoint> geocoded_waypoints, List<Route> routes) {
        this.status = status;
        this.geocoded_waypoints = geocoded_waypoints;
        this.routes = routes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<GeocodedWaypoint> getGeocoded_waypoints() {
        return geocoded_waypoints;
    }

    public void setGeocoded_waypoints(List<GeocodedWaypoint> geocoded_waypoints) {
        this.geocoded_waypoints = geocoded_waypoints;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }
}


