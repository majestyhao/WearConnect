package fu.hao.wearconnect;

// https://www.binpress.com/tutorial/a-guide-to-the-android-wear-message-api/152

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "MainActivity";
    private static final String START_ACTIVITY = "/start_activity";
    private static final String WEAR_MESSAGE_PATH = "/message";
    private static final String CONNECTED_PATH = "/connecting";
    private static final String STREAMING_PATH = "/streaming";
    private static final String FILE_TRANSFER = "/transFile";

    private GoogleApiClient mApiClient;

    private ArrayAdapter<String> mAdapter;

    private ListView mListView;
    private EditText mEditText;
    private Button mSendButton;

    private EditText txtData;
    private Button connectButton;
    private Button startButton;
    private Button stopButton;

    boolean stopFlag = false;

    public static String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        initGoogleApiClient();
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
                        // Request access only to the Wearable API
                .addApi(Wearable.API)
                .build();

        mApiClient.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mApiClient.disconnect();
    }

    private void init() {
        mListView = (ListView) findViewById(R.id.list_view);
        mEditText = (EditText) findViewById(R.id.input);
        mSendButton = (Button) findViewById(R.id.btn_send);

        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mListView.setAdapter(mAdapter);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = mEditText.getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    mAdapter.add(text);
                    mAdapter.notifyDataSetChanged();

                    sendMessage(WEAR_MESSAGE_PATH, text);
                }
            }
        });


        // file name to be entered
        txtData = (EditText) findViewById(R.id.editText2);
        txtData.setHint("Enter File Name here...");

        // connect button
        connectButton = (Button) findViewById(R.id.button);
        connectButton.setOnClickListener(new View.OnClickListener() {

                                             public void onClick(View v) {
                                                 try {
                                                     sendMessage(CONNECTED_PATH, "Hello Wear!");
                                                     Toast.makeText(getBaseContext(), "Connected to Android Wear!", Toast.LENGTH_SHORT).show();
                                                 } catch (Exception e) {
                                                     Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                 } finally {
                                                     stopFlag = false;
                                                 }
                                                 startButton.setEnabled(true);
                                                 connectButton.setEnabled(false);
                                             }
                                         }
        );

        // start button
        startButton = (Button) findViewById(R.id.button1);
        startButton.setOnClickListener(new View.OnClickListener() {

                                           public void onClick(View v) {
                                               try {
                                                   // start recording the sensor data
                                                   Date date = new Date();
                                                   CharSequence sdate = DateFormat.format("hh_mm_ss_MMMM_d", date.getTime());
                                                   fileName = new String(txtData.getText() + String.valueOf(sdate) + ".txt");
                                                   sendMessage(STREAMING_PATH, fileName);
                                                   Toast.makeText(getBaseContext(), "Start recording the data set", Toast.LENGTH_SHORT).show();
                                               } catch (Exception e) {
                                                   Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                               } finally {
                                                   stopFlag = false;
                                               }
                                               boolean mStartRecording = true;
                                               onRecord(mStartRecording);


                                               startButton.setEnabled(false);
                                               stopButton.setEnabled(true);
                                           }


                                       }
        );
        //startButton.setEnabled(true);

        // stop button
        stopButton = (Button) findViewById(R.id.button2);
        stopButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                boolean mStartRecording = false;
                onRecord(mStartRecording);
                // stop recording the sensor data
                try {
                    stopFlag = true;
                    //sendMessage(STREAMING_PATH, "STOP_NOW");
                    //Toast.makeText(getBaseContext(), "Done recording the data set", Toast.LENGTH_SHORT).show();
                    sendMessage(FILE_TRANSFER, "transfer");
                    Toast.makeText(getBaseContext(), "Done transfer the data set", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                connectButton.setEnabled(true);
                startButton.setEnabled(true);
                stopButton.setEnabled(false);

            }


        });
        stopButton.setEnabled(false);
    }


    private void sendMessage(final String path, final String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mApiClient).await();
                for (Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mApiClient, node.getId(), path, text.getBytes()).await();
                    Log.d(TAG, "Sending Message: " + text + " to Node: " + node);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mEditText.setText("");
                    }
                });
            }
        }).start();
    }

    @Override
    public void onConnected(Bundle bundle) {
        sendMessage(START_ACTIVITY, "");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    private static final String LOG_TAG = "AudioRecordTest";


    private MediaRecorder mRecorder = null;

    private void onRecord(boolean start) {
        if (start) {
            final int SAMPLE_DELAY = 75;
            thread = new Thread(new Runnable() {
                public void run() {
                    try{Thread.sleep(SAMPLE_DELAY);}catch(InterruptedException ie){ie.printStackTrace();}
                    startRecording();
                }
            });
            mAdapter.add(fileName + " Start Recording! " + System.currentTimeMillis());
            mAdapter.notifyDataSetChanged();
            thread.start();
        } else {
            stopRecording();
        }
    }

    private Thread thread;
    FileOutputStream fOut;
    OutputStreamWriter myOutWriter;
    BufferedWriter myBufferedWriter;
    PrintWriter myPrintWriter;
    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        File sdcard = Environment.getExternalStorageDirectory();
        File dir = new File(sdcard.getAbsolutePath() + "/CCS_Dataset/");
        mRecorder.setOutputFile(dir + "/" + fileName + ".mp4");
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();


        Log.e(LOG_TAG, fileName + " start: " + System.currentTimeMillis());



        try {
            if (!dir.exists()) {dir.mkdirs();} // Create folder if needed
            //myFile = new File("/sdcard/ResearchData/" + txtData.getText() + ".txt");
            final File myFile = new File(dir, "Audio" + fileName);
            if (myFile.exists()) myFile.delete();
            if (myFile.createNewFile())
                Log.d(TAG, "Successfully created the file!" + "Audio" + fileName);
            else
                Log.e(TAG, "Failed to create the file..");

            fOut = new FileOutputStream(myFile);
            myOutWriter = new OutputStreamWriter(fOut);
            myBufferedWriter = new BufferedWriter(myOutWriter);
            myPrintWriter = new PrintWriter(myBufferedWriter);

        } catch (Exception e) {
            Log.e(TAG, "Failed to create the file..");
        }

        while (!thread.isInterrupted()) {
            // Sense the voice...
            try {
                if (mRecorder != null) {
                    lastLevel = (float)getAmplitude();
                    myPrintWriter.write(String.valueOf(lastLevel) + ',');
                    Log.d(TAG, String.valueOf(lastLevel));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        Log.d(TAG, "Recorder Stopped!");
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
    }

    float lastLevel;
    public double getAmplitude() {
        if (mRecorder != null)
            return  mRecorder.getMaxAmplitude();
        else
            return 0;

    }

    private void stopRecording() {
        thread.interrupt();
    }

    @Override
    public void onPause() {
        super.onPause();
        thread.interrupt();
        thread = null;
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
    }
}






//    @Override
//    public void onMessageReceived(final MessageEvent messageEvent) {
//        runOnUiThread( new Runnable() {
//            @Override
//            public void run() {
//                if( messageEvent.getPath().equalsIgnoreCase(WEAR_MESSAGE_PATH)) {
//                    Log.d(TAG, "onMessageReceived: " + WEAR_MESSAGE_PATH);
//                }
//            }
//        });
//    }
