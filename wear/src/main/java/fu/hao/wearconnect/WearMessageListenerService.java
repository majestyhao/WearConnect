package fu.hao.wearconnect;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;

public class WearMessageListenerService extends WearableListenerService implements SensorEventListener {
    private static final String TAG = "WearableListenerService";
    private static final String START_ACTIVITY = "/start_activity";
    private static final String WEAR_MESSAGE_PATH = "/message";
    private static final String STREAMING = "/streaming";
    private static final String SENSOR_DATA_PATH = "/sensor-data";

    boolean streamingFlag = false;

    private GoogleApiClient mApiClient;

    public void onCreate() {
        super.onCreate();
        initGoogleApiClient();
    }

        @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if( messageEvent.getPath().equalsIgnoreCase(START_ACTIVITY) || messageEvent.getPath().equalsIgnoreCase(WEAR_MESSAGE_PATH)) {
            Log.d(TAG, "onMessageReceived: " + START_ACTIVITY);
            Intent intent = new Intent( this, MainActivity.class );
            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            startActivity(intent);
        } else {
            super.onMessageReceived(messageEvent);
        }

            if(messageEvent.getPath().equalsIgnoreCase(STREAMING)) {
                if (streamingFlag == false) {
                    startSensorListeners();
                    streamingFlag = true;
                } else {
                    stopSensorListeners();
                    streamingFlag = false;
                }
            }
    }

    private void initGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder( this )
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d(TAG, "onConnected: " + connectionHint);
                        // Now you can use the Data Layer API
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d(TAG, "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(TAG, "onConnectionFailed: " + result);
                    }
                })
                .addApi(Wearable.API)
                .build();

        if( mApiClient != null && !( mApiClient.isConnected() || mApiClient.isConnecting() ) )
            mApiClient.connect();
    }

    private SensorManager sensorManager;
    private PutDataMapRequest sensorData;

    @Override // SensorEventListener
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        return;
    }

    @Override // SensorEventListener
    public final void onSensorChanged(SensorEvent event) {
        String key = event.sensor.getName();
        float[] values = event.values;
        int currentAccuracy = sensorData.getDataMap().getInt(key + " Accuracy");
        if(event.accuracy > currentAccuracy) {
            Log.d(TAG, "New reading for sensor: " + key);
            sensorData.getDataMap().putFloatArray(key, values);
            sensorData.getDataMap().putInt(key + " Accuracy", event.accuracy);
        }
        if (event.accuracy == SensorManager.SENSOR_STATUS_ACCURACY_HIGH) {
            Log.d(TAG, "Unregistering sensor: " + key);
            sensorManager.unregisterListener(this, event.sensor);
        }
    }

    private void startSensorListeners() {
        Log.d(TAG, "startSensorListeners");
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        sensorData = PutDataMapRequest.create(SENSOR_DATA_PATH);
        sensorData.getDataMap().putLong("Timestamp", System.currentTimeMillis());
        float[] empty = new float[0];
        for (Sensor sensor : sensors) {
            sensorData.getDataMap().putFloatArray(sensor.getName(), empty);
            sensorData.getDataMap().putInt(sensor.getName() + " Accuracy", 0);
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void stopSensorListeners() {
        Log.d(TAG, "stopSensorListeners");
        sensorManager.unregisterListener(WearMessageListenerService.this);
    }

}
