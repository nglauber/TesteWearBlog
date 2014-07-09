package ngvl.android.testewearblog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

public class MyActivity extends Activity
    implements
        View.OnClickListener,
        WatchViewStub.OnLayoutInflatedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener {

    private static final String TAG         = "NGVL";
    private static final String PATH_MOBILE = "/messageMobile";
    private static final String PATH_WEAR   = "/messageWear";
    private static final String KEY_MESSAGE = "message";

    private TextView mTextView;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            Log.d("NGVL", "WEAR - Texto falado "+ spokenText);
            mTextView.setText(spokenText);
            saveMessage(spokenText);
        }
    }

    // View.OnClickListener
    @Override
    public void onClick(View view) {
        Intent intent = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        startActivityForResult(intent, 1 /* Request code */);
    }

    // WatchViewStub.OnLayoutInflatedListener
    @Override
    public void onLayoutInflated(WatchViewStub stub) {
        mTextView = (TextView)
                stub.findViewById(R.id.text);
        stub.findViewById(R.id.button)
                .setOnClickListener(MyActivity.this);
    }

    // GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d("NGVL", "WEAR - onConnected: " + connectionHint);
        Wearable.DataApi.addListener(mGoogleApiClient, MyActivity.this);
        loadMessage();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d("NGVL", "WEAR - onConnectionSuspended: " + cause);
    }

    // GoogleApiClient.OnConnectionFailedListener
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d("NGVL", "WEAR - onConnectionFailed: " + result);
    }

    // DataApi.DataListener
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.d("NGVL", "WEAR - DataItem deleted: " + event.getDataItem().getUri());
            } else if (event.getType() == DataEvent.TYPE_CHANGED) {
                Log.d("NGVL", "WEAR - DataItem changed: " + event.getDataItem().getUri());
            }
        }
        loadMessage();
    }

    private void saveMessage(String text){
        PutDataMapRequest dataMap = PutDataMapRequest.create(PATH_WEAR);
        dataMap.getDataMap().clear();
        dataMap.getDataMap().putString(KEY_MESSAGE, text);
        PutDataRequest request = dataMap.asPutDataRequest();
        
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, request);

        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                if (dataItemResult.getStatus().isSuccess()) {
                    Log.d("NGVL", "WEAR - Data item set: " + dataItemResult.getDataItem().getUri());
                }
            }
        });
    }

    private void loadMessage(){
        PendingResult<DataItemBuffer> results = Wearable.DataApi.getDataItems(mGoogleApiClient);
        results.setResultCallback(new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(DataItemBuffer dataItems) {
                for (int i = 0; i < dataItems.getCount(); i++) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItems.get(i));

                    Log.d("NGVL", "WEAR - Loaded: " + dataMapItem.getUri());

                    if (PATH_MOBILE.equals(dataMapItem.getUri().getPath())){
                        String value = dataMapItem.getDataMap().getString(KEY_MESSAGE);
                        mTextView.setText(value);
                    }
                }
                dataItems.release();
            }
        });
    }
}
