package com.example.daniel.sifonride.GoogleDirectionApi;

/**
 * Created by DANIEL on 2/9/2018.
 */

class Step {
    public String travel_mode;
//    public StartLocation start_location;
//    public EndLocation end_location;
//    public Polyline polyline ;
    public Duration duration;
    public String html_instructions;
    public Distance distance;

    public Step() {
    }

    public Step(String travel_mode, Duration duration, String html_instructions, Distance distance) {
        this.travel_mode = travel_mode;
        this.duration = duration;
        this.html_instructions = html_instructions;
        this.distance = distance;
    }

}
