package ngvl.android.testewearblog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.RemoteInput;
import android.widget.TextView;

public class DetalheActivity extends Activity {

    public static final String EXTRA_VOICE_REPLY = "voiceReply";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe);

        TextView txt = (TextView) findViewById(R.id.txtVoiceReply);
        txt.setText(obterTextoFalado(getIntent()));
    }

    private CharSequence obterTextoFalado(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(EXTRA_VOICE_REPLY);
        }
        return null;
    }
}
