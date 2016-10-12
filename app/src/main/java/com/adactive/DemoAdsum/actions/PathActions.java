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

    public PathActions drawPathToPoi(int poiID){
        map.drawPathToPoi(poiID);
        return this;

    }

    public PathActions resetPathDrawing() {
        map.resetPath();
        return this;
    }

    public PathActions setMotionFalse(){
        map.getPathObject().setPathMotion(false);
        return this;
    }
    public PathActions setMotionOn(){
        map.getPathObject().setPathMotion(true);
        return this;
    }

}
