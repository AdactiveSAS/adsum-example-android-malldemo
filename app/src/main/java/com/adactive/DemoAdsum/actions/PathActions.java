package com.adactive.DemoAdsum.actions;

import com.adactive.nativeapi.MapView;

/**
 * Created by Ambroise on 30/09/2016.
 */

public class PathActions {
    private MapView map;

    public PathActions(MapView aMap){
        this.map=aMap;
    }

    public void drawPathToPoi(int poiID){
        map.drawPathToPoi(poiID);

    }

    public void resetPathDrawing() {
        map.resetPath();
    }
}
