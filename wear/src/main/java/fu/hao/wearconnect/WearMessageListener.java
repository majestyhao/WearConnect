package fu.hao.wearconnect;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;


public class WearMessageListener extends Activity implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks, SensorEventListener{
        private static final String TAG = "WearableListenerService";
        private static final String START_ACTIVITY = "/start_activity";
        private static final String WEAR_MESSAGE_PATH = "/message";
        private static final String STREAMING = "/streaming";
        private static final String FILE_TRANSFER = "/transFile";

        private static String fileName;


        // boolean streamingFlag = false;

        private GoogleApiClient mApiClient;

    private WindowManager mWindowManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_message_listener);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initGoogleApiClient();
        mWindowManager = getWindow().getWindowManager();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wear_message_listener, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equalsIgnoreCase(STREAMING)) {
            //if (streamingFlag == false) {
            Log.d(TAG, "onMessageReceived: " + STREAMING);
            fileName = new String(messageEvent.getData());
            Log.d(TAG, fileName);
            validateMicAvailability();
            thread = new Thread(new Runnable() {


                public void run() {
                    startRecording();
                }
            });

            thread.start();
            //startRecording();
            startSensorListeners();

            // streamingFlag = true;
            //} else {
            //   stopSensorListeners();
            //    streamingFlag = false;
            //}
        } else if (messageEvent.getPath().equalsIgnoreCase(FILE_TRANSFER)) {
            Log.d(TAG, "onMessageReceived: " + FILE_TRANSFER);
            stopSensorListeners();
            stopRecording();
        } else if (messageEvent.getPath().equalsIgnoreCase(START_ACTIVITY)
                || messageEvent.getPath().equalsIgnoreCase(WEAR_MESSAGE_PATH)) {
            Log.d(TAG, "onMessageReceived: " + START_ACTIVITY);
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
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
                .addConnectionCallbacks(this)
                .build();

        if (mApiClient != null && !(mApiClient.isConnected() || mApiClient.isConnecting()))
            mApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if( mApiClient != null && !( mApiClient.isConnected() || mApiClient.isConnecting() ) )
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


            isFirstSet = true;

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
        //accelerometer = sensorManager
          //      .getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_FASTEST);

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

    private Thread thread;

    private static final int RECORDER_SAMPLERATE = 8000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;


    private int bufferSize;
    private void startRecording() {
        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE , AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, bufferSize) ;

        recorder.startRecording();
        isRecording = true;
        while (isRecording) {
            lastLevel = readAudioBuffer();
            if (!isRecording) {
                recorder.stop();
                recorder.release();
                recorder = null;
                Log.d(TAG, "Recorder Stopped!");
                break;
            }
        }
    }

    /**
     * Functionality that gets the sound level out of the sample
     */
    private float readAudioBuffer() {
        short[] buffer = new short[bufferSize];

        int bufferReadResult = 1;
        // Sense the voice...
        bufferReadResult = recorder.read(buffer, 0, bufferSize);
        double sumLevel = 0;
        try {
            if (recorder != null) {

                for (int i = 0; i < bufferReadResult; i++) {
                    sumLevel += buffer[i];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (float)Math.abs((sumLevel / bufferReadResult));

    }

    private boolean isRecording;

    private void stopRecording() {
        // stops the recording activity
        if (null != recorder) {
            isRecording=false;
        }
//            File sdcard = Environment.getExternalStorageDirectory();
//            File dir = new File(sdcard.getAbsolutePath() + "/SensorData/");
//            if (!dir.exists()) {
//                dir.mkdirs();
//            } // Create folder if needed
//            final File file = new File(dir,
//                    fileName + ".wav");
//            if (file.exists()) {
//                Log.d(TAG, "File Found! Audio!");
//
//                // Read the text file into a byte array
//                FileInputStream fileInputStream = null;
//                byte[] bFile = new byte[(int) file.length()];
//                try {
//                    fileInputStream = new FileInputStream(file);
//                    fileInputStream.read(bFile);
//                    fileInputStream.close();
//                } catch (Exception e) {
//                }
//
//                // Create an Asset from the byte array, and send it via the DataApi
//                Asset asset = Asset.createFromBytes(bFile);
//                PutDataMapRequest request = PutDataMapRequest.create("/audio");
//                DataMap map = request.getDataMap();
//                map.putLong("time", new Date().getTime()); // MOST IMPORTANT LINE FOR TIMESTAMP
//                map.putAsset("com.example.company.key.AUDIO", asset);
//                Wearable.DataApi.putDataItem(mApiClient, request.asPutDataRequest());
//                Log.d(TAG, "File Sent! AUDIO");
//                Toast.makeText(getBaseContext(), "File Sent! AUDIO!", Toast.LENGTH_SHORT).show();
//                file.delete();
//                try {
//                    fileInputStream.close();
//                } catch (IOException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                } catch (NullPointerException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            } else {
//                Log.d(TAG, "No Such File! Audio!");
//            }

    }

    private void validateMicAvailability()  {
        AudioRecord recorder =
                new AudioRecord(MediaRecorder.AudioSource.MIC, 44100,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_DEFAULT, 44100);

        if (recorder.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED)
            Log.e(TAG,"Mic didn't successfully initialized");


        recorder.startRecording();
        Log.e(TAG,"Mic initialized!");
        if (recorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
            recorder.stop();
            Log.e(TAG,"Mic is in use and can't be accessed");
        }
        recorder.stop();
        recorder.release();
    }



    private void stopSensorListeners() {

        sensorManager.unregisterListener(this);
        isStreaming = false;

        try {
           // myPrintWriter.write(String.valueOf(startTime) + ',' + String.valueOf(currentTime) + '\n');
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
            file.delete();
            try {
                fileInputStream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NullPointerException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "No Such File!");
        }

    }


    private SensorManager sensorManager;
    //Sensor accelerometer;
    //SensorListener fastestListener;

    float[] acceleration = new float[3];
    float[] rotationRate = new float[3];
    //float[] magneticField;
    //float[] rotationVector = new float[4];

    float lastLevel; // last audio level


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

//    private int numSamples;
//    private boolean isActive = false;
//    private double samplingRate = 0.0;



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

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        //samplingRateCal();
        if (isStreaming) {
            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
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
            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                updateOrientation(event.values);
                //rotationVector = event.values;
            }

            if (isFirstSet) {
                startTime = System.currentTimeMillis();
                isFirstSet = false;
                currentTime = startTime;
                //startRecording();
            } else {
                currentTime = System.currentTimeMillis();
            }
           //lastLevel = readAudioBuffer();

           // for (int i = 0; i < 1; i++) {
                save();
            //}
        }
    }

    float[] orientation = new float[3];

    private void updateOrientation(float[] rotationVector) {
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);

        // By default, remap the axes as if the front of the
        // device screen was the instrument panel.
        int worldAxisForDeviceAxisX = SensorManager.AXIS_X;
        int worldAxisForDeviceAxisY = SensorManager.AXIS_Z;
        //int worldAxisForDeviceAxisZ = SensorManager.AXIS_Y;

        // Adjust the rotation matrix for the device orientation
        int screenRotation = mWindowManager.getDefaultDisplay().getRotation();
        if (screenRotation == Surface.ROTATION_0) {
            worldAxisForDeviceAxisX = SensorManager.AXIS_X;
            worldAxisForDeviceAxisY = SensorManager.AXIS_Z;
            //worldAxisForDeviceAxisZ = SensorManager.AXIS_Y;
        } else if (screenRotation == Surface.ROTATION_90) {
            worldAxisForDeviceAxisX = SensorManager.AXIS_Z;
            worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_X;
           // worldAxisForDeviceAxisZ = SensorManager.AXIS_Y;
        } else if (screenRotation == Surface.ROTATION_180) {
            worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_X;
            worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_Z;
        } else if (screenRotation == Surface.ROTATION_270) {
            worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_Z;
            worldAxisForDeviceAxisY = SensorManager.AXIS_X;
            //worldAxisForDeviceAxisZ = SensorManager.AXIS_Y;
        }

        float[] adjustedRotationMatrix = new float[9];
        SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisForDeviceAxisX,
                worldAxisForDeviceAxisY, adjustedRotationMatrix);

        // Transform rotation matrix into azimuth/pitch/roll

        SensorManager.getOrientation(adjustedRotationMatrix, orientation);

//        // Convert radians to degrees
//        float pitch = orientation[1] * -57;
//        float roll = orientation[2] * -57;
//        float yaw = orientation[3] * -57;

    }

    private void save() {
        //Log.d(TAG, currentTime - startTime + "," + acceleration[0] + "," + acceleration[1] + "," + acceleration[2]
       // + "," + rotationRate[0] + "," + rotationRate[1] + "," + rotationRate[2] + ","
       // + orientation[0] * -57 + ',' + orientation[1] * -57 + ',' + orientation[2] * -57 + '\n');
              //  + rotationVector[0] + ',' + rotationVector[1] + ',' + rotationVector[2] + ',' + rotationVector[3] + '\n');
                //+ "," + rotationRate[0] + "," + rotationRate[1] + "," + rotationRate[2] + "\n");
        Log.d(TAG, String.valueOf(lastLevel));
                //+ "," + magneticField[0] + "," + magneticField[1] + "," + magneticField[2] + "\n");

        myPrintWriter.write(currentTime - startTime + "," + acceleration[0] + "," + acceleration[1] + "," + acceleration[2]
                //+ "," + rotationRate[0] + "," + rotationRate[1] + "," + rotationRate[2] + "\n");
                + "," + rotationRate[0] + "," + rotationRate[1] + "," + rotationRate[2] + ","
                + lastLevel + ","
                + orientation[0] * -57 + ',' + orientation[1] * -57 + ',' + orientation[2] * -57 + "," + currentTime + '\n');
                //+ rotationVector[0] + ',' + rotationVector[1] + ',' + rotationVector[2] + ',' + rotationVector[3] + '\n');
        //+ "," + magneticField[0] + "," + magneticField[1] + "," + magneticField[2] + "\n");
    }

    @Override
    public void onDestroy() {
        //active = true;
        super.onDestroy();
        Log.d(TAG, "onDestroy() executed");
        //active = false;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(mApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onStop() {
        if ( mApiClient != null ) {
            Wearable.MessageApi.removeListener( mApiClient, this );
            if ( mApiClient.isConnected() ) {
                mApiClient.disconnect();
            }
        }
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

}
