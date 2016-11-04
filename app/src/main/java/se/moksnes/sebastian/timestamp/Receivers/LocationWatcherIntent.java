package se.moksnes.sebastian.timestamp.Receivers;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import se.moksnes.sebastian.timestamp.Data.TimeTableRepository;

/**
 * Created by sebas on 2016-11-04.
 */

public class LocationWatcherIntent extends Service {

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    Timer mTimer;
    WifiManager mWifiManager;
    TimeTableRepository repo;


    public void stateChanged(boolean isIn){
        Context context= getApplicationContext();
        Boolean currentState = repo.isIn(context);

        if(currentState != isIn){
            repo.insert(context, isIn);
        }
    }


    private final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                List<ScanResult> mScanResults = mWifiManager.getScanResults();
                boolean isIn = false;
                resetTimer();
                for(ScanResult result : mScanResults){
                    if(result.SSID.equals("Moksnes Wireless MkII")){
                        isIn = true;
                    }
                }
                stateChanged(isIn);
            }
        }
    };


    class getWifiTask extends TimerTask {

        @Override
        public void run() {
            mWifiManager.startScan();
        }
    };

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        registerReceiver(mWifiScanReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        repo = new TimeTableRepository();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void resetTimer(){
        mTimer = new Timer();
        mTimer.schedule(new getWifiTask(),10000);
    }
    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.

            resetTimer();
            mWifiManager.startScan();

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            //stopSelf(msg.arg1);
        }
    }
}
