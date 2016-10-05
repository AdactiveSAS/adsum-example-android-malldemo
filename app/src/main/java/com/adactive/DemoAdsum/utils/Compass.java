package com.adactive.DemoAdsum.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;

public class Compass implements SensorEventListener {

    private Sensor mSensor;

    SensorManager sensorManager;
    private ArrayList<CompassListener> compassListeners;


    public interface CompassListener {
        void onNorthChanged(float angle);
    }

    public Compass(Context ctx) {
        compassListeners = new ArrayList<>();

        sensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);


    }

    public void registerListener(CompassListener listener) {
        compassListeners.add(listener);
        if (mSensor != null) {
            sensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    public void unregisterListener(CompassListener listener) {
        compassListeners.remove(listener);
        sensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float[] orientation = new float[3];
        float R[] = new float[9];

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            // calculate th rotation matrix
            SensorManager.getRotationMatrixFromVector(R, sensorEvent.values);
            // get the azimuth value (orientation[0]) in degree
            float north = (float) ((Math.toDegrees(SensorManager.getOrientation(R, orientation)[0]) + 360) % 360);
            for (int i = 0; i < compassListeners.size(); i++) {
                compassListeners.get(i).onNorthChanged(north);
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public void pauseCompass() {
        sensorManager.unregisterListener(this);

    }

    public void startCompass() {
        if (mSensor != null) {
            sensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }
}
