//package fu.hao.wearconnect;
//
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.hardware.Sensor;
//import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
//import android.hardware.SensorManager;
//import android.os.Bundle;
//import android.os.Environment;
//import android.os.IBinder;
//import android.text.format.DateFormat;
//import android.util.Log;
//import android.widget.Toast;
//
//
//import com.google.android.gms.wearable.PutDataMapRequest;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.io.PrintWriter;
//import java.util.Date;
//
//public class SensorStreamingService extends Service implements SensorEventListener {
//    public static final String TAG = "SensorStreamingService";
//
//    private SensorManager sensorManager;
//    Sensor accelerometer;
//    //SensorListener fastestListener;
//
//    float[] acceleration = new float[3];
//    float[] rotationRate = new float[3];
//    //float[] magneticField;
//
//
//    private FileOutputStream fOut;
//    private OutputStreamWriter myOutWriter;
//    private BufferedWriter myBufferedWriter;
//    private PrintWriter myPrintWriter;
//
//
//    private long currentTime;
//    private long startTime;
//    boolean isFirstSet = true;
//    boolean isStreaming = false;
//
//    public static boolean active = false;
//
//    private String fileName;
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        // TODO: Return the communication channel to the service.
//
//        return null;
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        active = true;
//        Bundle bundle = intent.getExtras();
//        if (bundle != null) {
//            fileName = (String) bundle.get("filename");
//            Log.d(TAG, fileName);
//        }
//        // start recording the sensor data
//        try {
//            File sdcard = Environment.getExternalStorageDirectory();
//            File dir = new File(sdcard.getAbsolutePath()+ "/SensorData/");
//            if (!dir.exists()) {dir.mkdirs();} // Create folder if needed
//            //myFile = new File("/sdcard/ResearchData/" + txtData.getText() + ".txt");
//            final File myFile = new File(dir, fileName);
//            if (myFile.exists()) myFile.delete();
//            if (myFile.createNewFile())
//                Log.d(TAG, "Successfully created the file!" + fileName);
//            else
//                Log.d(TAG, "Failed to create the file..");
//
//            fOut = new FileOutputStream(myFile);
//            myOutWriter = new OutputStreamWriter(fOut);
//            myBufferedWriter = new BufferedWriter(myOutWriter);
//            myPrintWriter = new PrintWriter(myBufferedWriter);
//            isStreaming = true;
//
//            Toast.makeText(getBaseContext(), "Start recording the data set", Toast.LENGTH_SHORT).show();
//        } catch (Exception e) {
//            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
//            Log.d(TAG, "Failed to create the file..");
//        }
//        return START_STICKY;
//
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//
//        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        accelerometer = sensorManager
//                .getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
//
//        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
//        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
//        //sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
//
////        normalListener = new Listener(this);
////        sensorManager.registerListener(normalListener, accelerometer,
////                SensorManager.SENSOR_DELAY_NORMAL);
////        uiListener = new Listener(this);
////        sensorManager.registerListener(uiListener, accelerometer,
////                SensorManager.SENSOR_DELAY_UI);
////        gameListener = new Listener(this);
////        sensorManager.registerListener(gameListener, accelerometer,
////                SensorManager.SENSOR_DELAY_GAME);
////        fastestListener = new SensorListener();
////        sensorManager.registerListener(fastestListener, accelerometer,
////                SensorManager.SENSOR_DELAY_FASTEST);
//        //fastestListener.startRecording();
//
//
//    }
//
//
////    private void startSensorListeners() {
////        Log.d(TAG, "startSensorListeners");
////        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
////        sensorData = PutDataMapRequest.create(SENSOR_DATA_PATH);
////        sensorData.getDataMap().putLong("Timestamp", System.currentTimeMillis());
////        float[] empty = new float[0];
////        for (Sensor sensor : sensors) {
////            sensorData.getDataMap().putFloatArray(sensor.getName(), empty);
////            sensorData.getDataMap().putInt(sensor.getName() + " Accuracy", 0);
////            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
////        }
////    }
//
////    private void stopSensorListeners() {
////        Log.d(TAG, "stopSensorListeners");
////        sensorManager.unregisterListener(fastestListener);
////    }
//
//    private int numSamples;
//    private boolean isActive = false;
//    private double samplingRate = 0.0;
//
//
//    public double getSamplingRate() {
//        return samplingRate;
//    }
//
////    public void startRecording() {
////        startTime = System.currentTimeMillis();
////        numSamples = 0;
////        isActive = true;
////    }
//
//    public boolean isActive() {
//        return isActive;
//    }
//
//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//    }
//
//    private void samplingRateCal() {
//        if (isActive) {
//            numSamples++;
//            long now = System.currentTimeMillis();
//            if (now >= startTime + 5000) {
//                samplingRate = numSamples / ((now - startTime) / 1000.0);
//                isActive = false;
//            }
//        }
//    }
//
//    @Override
//    public void onSensorChanged(SensorEvent event) {
//        //samplingRateCal();
//        if (isStreaming) {
//            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//                acceleration = event.values;
//            }
//
//            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
//                rotationRate[0] = event.values[0];
//                rotationRate[1] = event.values[1];
//                rotationRate[2] = event.values[2];
//            }
//
////            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
////                magneticField[0] = event.values[0];
////                magneticField[1] = event.values[1];
////                magneticField[2] = event.values[2];
////            }
//
//            if (isFirstSet) {
//                startTime = System.currentTimeMillis();
//                isFirstSet = false;
//            }
//
//            currentTime = System.currentTimeMillis();
//
//            for (int i = 0; i < 1; i++) {
//                save();
//            }
//        }
//    }
//
//    private void save() {
//        Log.d(TAG, currentTime - startTime + "," + acceleration[0] + "," + acceleration[1] + "," + acceleration[2]
//                + "," + rotationRate[0] + "," + rotationRate[1] + "," + rotationRate[2] + "\n");
//        //+ "," + magneticField[0] + "," + magneticField[1] + "," + magneticField[2] + "\n");
//        myPrintWriter.write(currentTime - startTime + "," + acceleration[0] + "," + acceleration[1] + "," + acceleration[2]
//                + "," + rotationRate[0] + "," + rotationRate[1] + "," + rotationRate[2] + "\n");
//        //+ "," + magneticField[0] + "," + magneticField[1] + "," + magneticField[2] + "\n");
//    }
//
//    @Override
//    public void onDestroy() {
//        active = true;
//        super.onDestroy();
//        sensorManager.unregisterListener(this);
//        isStreaming = false;
//        try {
//            myOutWriter.close();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (NullPointerException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        try {
//            fOut.close();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (NullPointerException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        Log.d(TAG, "onDestroy() executed");
//        active = false;
//    }
//}
