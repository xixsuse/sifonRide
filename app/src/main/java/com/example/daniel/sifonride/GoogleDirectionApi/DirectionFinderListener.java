package com.example.daniel.sifonride.GoogleDirectionApi;

import java.util.List;

/**
 * Created by DANIEL on 2/12/2018.
 */

public interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);
}
