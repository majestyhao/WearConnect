package fu.hao.wearconnect;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class DataReceiver extends WearableListenerService {
    private static final String TAG = "DataReceiver";
    private GoogleApiClient mApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
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

    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged: " + dataEvents);
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED &&
                    event.getDataItem().getUri().getPath().equals("/txt"))
            {
                // Get the Asset object
                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                Asset asset = dataMapItem.getDataMap().getAsset("com.example.company.key.TXT");

                ConnectionResult result =
                        mApiClient.blockingConnect(10000, TimeUnit.MILLISECONDS);
                if (!result.isSuccess()) {return;}

                // Convert asset into a file descriptor and block until it's ready
                InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                        mApiClient, asset).await().getInputStream();
                mApiClient.disconnect();
                if (assetInputStream == null) { return; }

                // Get folder for output
                File sdcard = Environment.getExternalStorageDirectory();
                File dir = new File(sdcard.getAbsolutePath() + "/CCS_Dataset/");
                if (!dir.exists()) {dir.mkdirs();} // Create folder if needed

                // Read data from the Asset and write it to a file on external storage
                final File file = new File(dir, MainActivity.fileName);
                try {
                    FileOutputStream fOut = new FileOutputStream(file);
                    int nRead;
                    byte[] data = new byte[16384];
                    while ((nRead = assetInputStream.read(data, 0, data.length)) != -1) {
                        fOut.write(data, 0, nRead);
                    }

                    fOut.flush();
                    fOut.close();
                    Log.d(TAG, "File Received! TXT");
                    Toast.makeText(getBaseContext(), "File Received! TXT", Toast.LENGTH_SHORT).show();
                }
                catch (Exception e)
                {
                }

                // Rescan folder to make it appear
                try {
                    String[] paths = new String[1];
                    paths[0] = file.getAbsolutePath();
                    MediaScannerConnection.scanFile(this, paths, null, null);
                } catch (Exception e) {
                }
            }


        if (event.getType() == DataEvent.TYPE_CHANGED &&
                event.getDataItem().getUri().getPath().equals("/audio"))
        {
            // Get the Asset object
            DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
            Asset asset = dataMapItem.getDataMap().getAsset("com.example.company.key.AUDIO");

            ConnectionResult result =
                    mApiClient.blockingConnect(10000, TimeUnit.MILLISECONDS);
            if (!result.isSuccess()) {return;}

            // Convert asset into a file descriptor and block until it's ready
            InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                    mApiClient, asset).await().getInputStream();
            mApiClient.disconnect();
            if (assetInputStream == null) { return; }

            // Get folder for output
            File sdcard = Environment.getExternalStorageDirectory();
            File dir = new File(sdcard.getAbsolutePath() + "/CCS_Dataset/");
            if (!dir.exists()) {dir.mkdirs();} // Create folder if needed

            // Read data from the Asset and write it to a file on external storage
            final File file = new File(dir, MainActivity.fileName);
            try {
                FileOutputStream fOut = new FileOutputStream(file);
                int nRead;
                byte[] data = new byte[16384];
                while ((nRead = assetInputStream.read(data, 0, data.length)) != -1) {
                    fOut.write(data, 0, nRead);
                }

                fOut.flush();
                fOut.close();
                Log.d(TAG, "File Received! Audio");
                Toast.makeText(getBaseContext(), "File Received! Audio", Toast.LENGTH_SHORT).show();
            }
            catch (Exception e)
            {
            }

            // Rescan folder to make it appear
            try {
                String[] paths = new String[1];
                paths[0] = file.getAbsolutePath();
                MediaScannerConnection.scanFile(this, paths, null, null);
            } catch (Exception e) {
            }
        }
    }
    }

    @Override
    public void onPeerConnected(Node peer) {
        Log.d(TAG, "onPeerConnected: " + peer);
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        Log.d(TAG, "onPeerDisconnected: " + peer);
    }



}
