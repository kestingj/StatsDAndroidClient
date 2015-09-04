
package com.amazon.statsdclient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

//A NetworkStateManager is to be used by the user to reset the connection upon resuming an activity

public final class NetworkStateManager {

    //Method to be called by user in the onResume() method
    public static void testNetworkConnectionChange (final Context context) {
        final ConnectivityManager connectivityManager =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            //Only resets if network connection still exists
            if (networkInfo.isConnectedOrConnecting()) {
                StatsDClientContainer.INSTANCE.reset();
            }
        }
        Log.d("Network Connectivity", "There's no network connectivity");
    }
}

