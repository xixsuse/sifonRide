package com.example.daniel.sifonride.GoogleDirectionApi;

import java.util.List;

/**
 * Create d by DANIEL on 2/17/2018.
 */

public interface TimeDistanceListener {
   void onTimeDistanceStart();
   void onTimeDistanceSuccess(List<Route> routes);

}
