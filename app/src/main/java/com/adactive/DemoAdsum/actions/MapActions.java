package com.adactive.DemoAdsum.actions;

import com.adactive.DemoAdsum.utils.Compass;
import com.adactive.nativeapi.MapView;

/**
 * Created by Ambroise on 30/09/2016.
 */

public class MapActions implements Compass.CompassListener {
    private Compass compass;
    private MapView map;
    private String InactivePlaceColor = "#F9F6F8";
    static private MapView.CameraMode currentCameraMode = MapView.CameraMode.FULL;

    public MapActions(MapView aMap) {
        this.map = aMap;
    }

    public MapActions startMap() {

        if (!map.isMapDataAvailable()) {
            map.update(true);

        } else {
            map.start();
            map.update(true);
        }
        return this;
    }

    public MapActions setInitialState() {
        compass = new Compass(map.getContext());
        // Configure the map
        map.customizeInactivePlaces(InactivePlaceColor);
        map.limitCameraMovement(false);
        map.setSiteVisible(false);
        map.setCameraMode(currentCameraMode);
        map.centerOnPlace(0, 450, 0.9f);

        return this;
    }

    public void setCurrentFloorOnUser(Integer floorID) {
        if (map.getCurrentFloor() != floorID) {
            map.setCurrentFloor(floorID);
        }
    }

    public MapActions POIClicked(Integer placeId) {
        map.unLightAll();
        map.highLightPlace(placeId, "#e57d22");
        map.centerOnPlace(placeId);
        return this;
    }

    public void startCompass() {
        compass.startCompass();
        map.enableCompassMode();
        compass.registerListener(this);
    }

    public void stopCompass() {
        map.disableCompassMode();
        compass.pauseCompass();
        compass.unregisterListener(this);
    }

    @Override
    public void onNorthChanged(float angle) {
        map.rotateAbsolute(-angle);
    }

    public void setCurrentFloor(int floorId){
        map.setCurrentFloor(floorId);
    }
}

