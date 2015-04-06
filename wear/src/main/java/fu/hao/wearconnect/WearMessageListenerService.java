package fu.hao.wearconnect;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

public class WearMessageListenerService extends WearableListenerService implements SensorEventListener {
    private static final String TAG = "WearableListenerService";
    private static final String START_ACTIVITY = "/start_activity";
    private static final String WEAR_MESSAGE_PATH = "/message";
    private static final String STREAMING = "/streaming";
    private static final String FILE_TRANSFER = "/transFile";

    private static String fileName;


    // boolean streamingFlag = false;

    private GoogleApiClient mApiClient;

    public void onCreate() {
        super.onCreate();
        initGoogleApiClient();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equalsIgnoreCase(STREAMING)) {
            //if (streamingFlag == false) {
            Log.d(TAG, "onMessageReceived: " + STREAMING);
            fileName = new String(messageEvent.getData());
            Log.d(TAG, fileName);
                     startSensorListeners();
            // streamingFlag = true;
            //} else {
            //   stopSensorListeners();
            //    streamingFlag = false;
            //}
        } else if (messageEvent.getPath().equalsIgnoreCase(FILE_TRANSFER)) {
            Log.d(TAG, "onMessageReceived: " + FILE_TRANSFER);
                     stopSensorListeners();
        } else if (messageEvent.getPath().equalsIgnoreCase(START_ACTIVITY)
                || messageEvent.getPath().equalsIgnoreCase(WEAR_MESSAGE_PATH)) {
            Log.d(TAG, "onMessageReceived: " + START_ACTIVITY);
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            super.onMessageReceived(messageEvent);
        }
    }

    private void initGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder(this)
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

        if (mApiClient != null && !(mApiClient.isConnected() || mApiClient.isConnecting()))
            mApiClient.connect();
    }

    // private SensorManager sensorManager;
    private PutDataMapRequest sensorData;

//    @Override // SensorEventListener
//    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
//        return;
//    }
//
//    @Override // SensorEventListener
//    public final void onSensorChanged(SensorEvent event) {
//        String key = event.sensor.getName();
//        float[] values = event.values;
//        int currentAccuracy = sensorData.getDataMap().getInt(key + " Accuracy");
//        if(event.accuracy > currentAccuracy) {
//            Log.d(TAG, "New reading for sensor: " + key);
//            sensorData.getDataMap().putFloatArray(key, values);
//            sensorData.getDataMap().putInt(key + " Accuracy", event.accuracy);
//        }
//        if (event.accuracy == SensorManager.SENSOR_STATUS_ACCURACY_HIGH) {
//            Log.d(TAG, "Unregistering sensor: " + key);
//            sensorManager.unregisterListener(this, event.sensor);
//        }
//    }

    private void startSensorListeners() {
        Log.d(TAG, "startSensorListeners");
//        Intent sensorCtr = new Intent(WearMessageListenerService.this, SensorStreamingService.class);
//        sensorCtr.putExtra("filename", fileName);
//        startService(sensorCtr);

//        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
//        sensorData = PutDataMapRequest.create(SENSOR_DATA_PATH);
//        sensorData.getDataMap().putLong("Timestamp", System.currentTimeMillis());
//        float[] empty = new float[0];
//        for (Sensor sensor : sensors) {
//            sensorData.getDataMap().putFloatArray(sensor.getName(), empty);
//            sensorData.getDataMap().putInt(sensor.getName() + " Accuracy", 0);
//            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
//        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager
                .getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);

        try {
            File sdcard = Environment.getExternalStorageDirectory();
            File dir = new File(sdcard.getAbsolutePath()+ "/SensorData/");
            if (!dir.exists()) {dir.mkdirs();} // Create folder if needed
            //myFile = new File("/sdcard/ResearchData/" + txtData.getText() + ".txt");
            final File myFile = new File(dir, fileName);
            if (myFile.exists()) myFile.delete();
            if (myFile.createNewFile())
                Log.d(TAG, "Successfully created the file!" + fileName);
            else
                Log.d(TAG, "Failed to create the file..");

            fOut = new FileOutputStream(myFile);
            myOutWriter = new OutputStreamWriter(fOut);
            myBufferedWriter = new BufferedWriter(myOutWriter);
            myPrintWriter = new PrintWriter(myBufferedWriter);
            isStreaming = true;

            Toast.makeText(getBaseContext(), "Start recording the data set", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Failed to create the file..");
        }
    }

    private void stopSensorListeners() {

        sensorManager.unregisterListener(this);
        isStreaming = false;

        try {
            myOutWriter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//        Log.d(TAG, "stopSensorListeners");
//        Intent intent = new Intent(WearMessageListenerService.this, SensorStreamingService.class);
//        stopService(intent);
        //sensorManager.unregisterListener(WearMessageListenerService.this);

//        while (SensorStreamingService.active){
//            // wait to terminate
//        }


            // Get folder for output
            File sdcard = Environment.getExternalStorageDirectory();
            File dir = new File(sdcard.getAbsolutePath() + "/SensorData/");
            if (!dir.exists()) {
                dir.mkdirs();
            } // Create folder if needed
            final File file = new File(dir, fileName);
            if (file.exists()) {
                Log.d(TAG, "File Found!");

                // Read the text file into a byte array
                FileInputStream fileInputStream = null;
                byte[] bFile = new byte[(int) file.length()];
                try {
                    fileInputStream = new FileInputStream(file);
                    fileInputStream.read(bFile);
                    fileInputStream.close();
                } catch (Exception e) {
                }

                // Create an Asset from the byte array, and send it via the DataApi
                Asset asset = Asset.createFromBytes(bFile);
                PutDataMapRequest request = PutDataMapRequest.create("/txt");
                DataMap map = request.getDataMap();
                map.putLong("time", new Date().getTime()); // MOST IMPORTANT LINE FOR TIMESTAMP
                map.putAsset("com.example.company.key.TXT", asset);
                Wearable.DataApi.putDataItem(mApiClient, request.asPutDataRequest());
                Log.d(TAG, "File Sent!");
                Toast.makeText(getBaseContext(), "File Sent!", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "No Such File!");
            }

    }


    private SensorManager sensorManager;
    Sensor accelerometer;
    //SensorListener fastestListener;

    float[] acceleration = new float[3];
    float[] rotationRate = new float[3];
    //float[] magneticField;


    private FileOutputStream fOut;
    private OutputStreamWriter myOutWriter;
    private BufferedWriter myBufferedWriter;
    private PrintWriter myPrintWriter;


    private long currentTime;
    private long startTime;
    boolean isFirstSet = true;
    boolean isStreaming = false;

    public static boolean active = false;

    //private String fileName;


//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        active = true;
//        Bundle bundle = intent.getExtras();
//        if (bundle != null) {
//            fileName = (String) bundle.get("filename");
//            Log.d(TAG, fileName);
//        }
//        // start recording the sensor data
//
//        return START_STICKY;
//
//    }



//    private void startSensorListeners() {
//        Log.d(TAG, "startSensorListeners");
//        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
//        sensorData = PutDataMapRequest.create(SENSOR_DATA_PATH);
//        sensorData.getDataMap().putLong("Timestamp", System.currentTimeMillis());
//        float[] empty = new float[0];
//        for (Sensor sensor : sensors) {
//            sensorData.getDataMap().putFloatArray(sensor.getName(), empty);
//            sensorData.getDataMap().putInt(sensor.getName() + " Accuracy", 0);
//            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
//        }
//    }

//    private void stopSensorListeners() {
//        Log.d(TAG, "stopSensorListeners");
//        sensorManager.unregisterListener(fastestListener);
//    }

    private int numSamples;
    private boolean isActive = false;
    private double samplingRate = 0.0;


    public double getSamplingRate() {
        return samplingRate;
    }

//    public void startRecording() {
//        startTime = System.currentTimeMillis();
//        numSamples = 0;
//        isActive = true;
//    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void samplingRateCal() {
        if (isActive) {
            numSamples++;
            long now = System.currentTimeMillis();
            if (now >= startTime + 5000) {
                samplingRate = numSamples / ((now - startTime) / 1000.0);
                isActive = false;
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //samplingRateCal();
        if (isStreaming) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                acceleration = event.values;
            }

            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                rotationRate[0] = event.values[0];
                rotationRate[1] = event.values[1];
                rotationRate[2] = event.values[2];
            }

//            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
//                magneticField[0] = event.values[0];
//                magneticField[1] = event.values[1];
//                magneticField[2] = event.values[2];
//            }

            if (isFirstSet) {
                startTime = System.currentTimeMillis();
                isFirstSet = false;
            }

            currentTime = System.currentTimeMillis();

            for (int i = 0; i < 1; i++) {
                save();
            }
        }
    }

    private void save() {
        Log.d(TAG, currentTime - startTime + "," + acceleration[0] + "," + acceleration[1] + "," + acceleration[2]
                + "," + rotationRate[0] + "," + rotationRate[1] + "," + rotationRate[2] + "\n");
        //+ "," + magneticField[0] + "," + magneticField[1] + "," + magneticField[2] + "\n");
        myPrintWriter.write(currentTime - startTime + "," + acceleration[0] + "," + acceleration[1] + "," + acceleration[2]
                + "," + rotationRate[0] + "," + rotationRate[1] + "," + rotationRate[2] + "\n");
        //+ "," + magneticField[0] + "," + magneticField[1] + "," + magneticField[2] + "\n");
    }

    @Override
    public void onDestroy() {
        //active = true;
        super.onDestroy();
        Log.d(TAG, "onDestroy() executed");
        //active = false;
    }
}
