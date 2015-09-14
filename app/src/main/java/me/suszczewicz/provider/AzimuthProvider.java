package me.suszczewicz.provider;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class AzimuthProvider extends StreamedDataProvider<Float> implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private float[] gravity;
    private float[] geomagnetic;

    private float[] rotationMatrix;
    private float[] rotationMatrixResult;

    public AzimuthProvider(Context context) {
        super(context);

        init(context);
    }

    @Override
    public void start() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        saveSensorValues(event);
        emitAzimuth();
    }

    private void saveSensorValues(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            gravity = event.values.clone();

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            geomagnetic = event.values.clone();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void init(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        rotationMatrix = new float[9];
        rotationMatrixResult = new float[3];
    }

    private void emitAzimuth() {
        if (gravity != null && geomagnetic != null) {
            boolean success = SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic);

            if (success) {
                SensorManager.getOrientation(rotationMatrix, rotationMatrixResult);
                emitData(rotationMatrixResult[0]);
            }
        }
    }
}
