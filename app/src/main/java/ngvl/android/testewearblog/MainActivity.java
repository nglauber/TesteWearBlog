package ngvl.android.testewearblog;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

public class MainActivity extends ActionBarActivity
        implements
            View.OnClickListener,
            AdapterView.OnItemClickListener,
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener,
            DataApi.DataListener {

    private static final String TAG         = "NGVL";
    private static final String PATH_MOBILE = "/messageMobile";
    private static final String PATH_WEAR   = "/messageWear";
    private static final String KEY_MESSAGE = "message";

    private TextView txtDataSync;
    private EditText edtMessage;
    private ListView lstNotifications;
    private Button   btnSend;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

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
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    // View.OnClickListener
    @Override
    public void onClick(View v) {
        saveMessage(edtMessage.getText().toString());
        edtMessage.setText(null);
    }

    // AdapterView.OnItemClickListener
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i){
            case 0 :
                NotificationUtils.simpleNotification(this);
                break;
            case 1:
                NotificationUtils.notificacaoComBigView(this);
                break;
            case 2:
                NotificationUtils.notificationWithAction(this);
                break;
            case 3:
                NotificationUtils.notificationWithReply(this);
                break;
            case 4:
                NotificationUtils.notificacaoWithPages(this);
                break;
            case 5:
                NotificationUtils.groupedNotifications(this);
                break;
        }
    }

    // GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected: " + connectionHint);
        Wearable.DataApi.addListener(mGoogleApiClient, MainActivity.this);
        loadMessage();

        btnSend.setEnabled(true);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended: " + cause);
        btnSend.setEnabled(false);
    }

    // GoogleApiClient.OnConnectionFailedListener
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG, "onConnectionFailed: " + result);
        btnSend.setEnabled(false);
        Toast.makeText(this, R.string.error_connect_google_play, Toast.LENGTH_SHORT).show();
    }

    // DataApi.DataListener
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.d(TAG, "MOBILE - DataItem deleted: " + event.getDataItem().getUri());
            } else if (event.getType() == DataEvent.TYPE_CHANGED) {
                Log.d(TAG, "MOBILE - DataItem changed: " + event.getDataItem().getUri());
            }
        }
        loadMessage();
    }

    // Private Methods
    private void initViews(){
        txtDataSync      = (TextView) findViewById(R.id.textView);
        edtMessage       = (EditText) findViewById(R.id.editText);

        btnSend = (Button)   findViewById(R.id.button);
        btnSend.setOnClickListener(this);

        lstNotifications = (ListView) findViewById(R.id.listView);
        lstNotifications.setOnItemClickListener(this);
        lstNotifications.setAdapter(
                new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_list_item_1,
                        getResources().getStringArray(R.array.notifications_example))
        );
    }

    private void saveMessage(final String text) {
        PutDataMapRequest dataMap = PutDataMapRequest.create(PATH_MOBILE);
        dataMap.getDataMap().clear();
        dataMap.getDataMap().putString(KEY_MESSAGE, text);

        PutDataRequest request = dataMap.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, request);

        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                if (dataItemResult.getStatus().isSuccess()) {
                    Log.d(TAG, "MOBILE - Data item set: " + dataItemResult.getDataItem().getUri());
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

                    Log.d(TAG, "MOBILE - Loaded: " + dataMapItem.getUri());

                    if (PATH_WEAR.equals(dataMapItem.getUri().getPath())){
                        String value = dataMapItem.getDataMap().getString(KEY_MESSAGE);
                        txtDataSync.setText(value);
                    }
                }
                dataItems.release();
            }
        });
    }
}
