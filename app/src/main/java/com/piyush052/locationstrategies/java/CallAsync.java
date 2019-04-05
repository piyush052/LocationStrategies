package com.piyush052.locationstrategies.java;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.piyush052.locationstrategies.Contants;
import com.piyush052.locationstrategies.Position;
import com.piyush052.locationstrategies.R;

public class CallAsync {

    public void callAsyncAPI(final Context context, Position position, final String alarm){
        String request = ProtocolFormatter.formatRequest(
                new Contants().getURL(), position, alarm);

        RequestManager.sendRequestAsync(request, new RequestManager.RequestHandler() {
            @Override
            public void onComplete(boolean success) {
                Log.e("callAsyncAPI", "onComplete: "+success +"  "+alarm );
                if (success) {
                    Toast.makeText(context, R.string.status_send_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, R.string.status_send_fail, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
