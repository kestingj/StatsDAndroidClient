

package com.amazon.statsdclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//A BroadcastReceiver that detects changes in network connection

public class NetworkChangeReceiver extends BroadcastReceiver {

    //Called whenever the network connection changes
    @Override
    public void onReceive(final Context context, final Intent intent) {

        if (intent.getExtras() != null) {
            NetworkStateManager.testNetworkConnectionChange(context);
        }
    }
}
