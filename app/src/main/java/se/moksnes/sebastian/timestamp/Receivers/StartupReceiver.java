package se.moksnes.sebastian.timestamp.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by sebas on 2016-11-19.
 */

public class StartupReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Intent myIntent = new Intent(context, LocationWatcherIntent.class);
            context.startService(myIntent);

        }
    }
