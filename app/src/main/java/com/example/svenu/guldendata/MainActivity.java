package com.example.svenu.guldendata;

import android.app.ProgressDialog;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.jjoe64.graphview.GraphView;

public class MainActivity extends AppCompatActivity implements SensorListener {
    
    private final static String TAG = "MainActivity";
    private String comparison = "NLG-EUR";

    private boolean isVisible;

    private GraphView graphView;
    private DataGetter dataGetter;
    private ProgressDialog progressDialog;
    private SensorManager sensorManager;

    private long lastUpdate = 0;
    private float last_x = 0;
    private float last_y = 0;
    private float last_z = 0;
    final static int SHAKE_THRESHOLD = 800;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setVariables();
        initializeSensorManager();
        drawGraph();
    }

    private void initializeSensorManager() {
        sensorManager.registerListener(this,
                SensorManager.SENSOR_ACCELEROMETER,
                SensorManager.SENSOR_DELAY_GAME);
    }

    private void setVariables() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        progressDialog = new ProgressDialog(this);
        graphView = findViewById(R.id.graph);
    }

    @Override
    public void onSensorChanged(int sensor, float[] values) {
        if (sensor == SensorManager.SENSOR_ACCELEROMETER && isVisible) {
            long curTime = System.currentTimeMillis();
            // only allow one update every 100ms.
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float x = values[SensorManager.DATA_X];
                float y = values[SensorManager.DATA_Y];
                float z = values[SensorManager.DATA_Z];

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > 2 * SHAKE_THRESHOLD) {
                    if (!progressDialog.isShowing()) {
                        drawGraph();
                    }
                }
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(int i, int i1) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        this.isVisible = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.isVisible = false;
    }

    private void drawGraph() {
        dataGetter = new DataGetter(this, comparison, DataGetter.fiveMinutes, 7, DataGetter.day);
        dataGetter.retrieveData(graphView, progressDialog);
    }
}
