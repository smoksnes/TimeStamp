package se.moksnes.sebastian.timestamp.Receivers;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import se.moksnes.sebastian.timestamp.Data.TimeTableRepository;

/**
 * Created by sebas on 2016-11-04.
 */

public class LocationWatcherIntent extends Service {

    private Looper mServiceLooper;
    ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    private ServiceHandler mServiceHandler;
    /**
     * Command to the service to register a client, receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client where callbacks should be sent.
     */
    public static final int MSG_REGISTER_CLIENT = 90;

    /**
     * Command to the service to unregister a client, ot stop receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client as previously given with MSG_REGISTER_CLIENT.
     */
    public static final int MSG_UNREGISTER_CLIENT = 91;

    /**
     * Command to service to set a new value.  This can be sent to the
     * service to supply a new value, and will be sent by the service to
     * any registered clients with the new value.
     */
    public static final int MSG_STATE_IN = 1;
    public static final int MSG_STATE_OUT = 2;

    Timer mTimer;
    WifiManager mWifiManager;
    TimeTableRepository repo;


    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    public void stateChanged(boolean isIn){
        Boolean currentState = repo.isIn();

        if(currentState != isIn){
            long ms;
            if(isIn){
                ms = repo.stampIn();
                Toast.makeText(getApplicationContext(), "Stämplat in.", Toast.LENGTH_SHORT).show();
            }
            else{
                ms = repo.stampOut();
                Toast.makeText(getApplicationContext(), "Stämplat ut.", Toast.LENGTH_SHORT).show();
            }

            int state = isIn ? MSG_STATE_IN : MSG_STATE_OUT;
            Message msg = Message.obtain(null, state, ms);
            for (int i=mClients.size()-1; i>=0; i--) {
                try {
                    mClients.get(i).send(msg);
                } catch (RemoteException e) {
                    // The client is dead.  Remove it from the list;
                    // we are going through the list from back to front
                    // so this is safe to do inside the loop.
                    mClients.remove(i);
                }
            }
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
                    if(result.SSID.equals("SCB-Client")){ // Moksnes Wireless MkII
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
        repo = new TimeTableRepository(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job

        Message msg = mServiceHandler.obtainMessage();
        mServiceHandler.sendMessage(msg);
        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    private void resetTimer(){
        mTimer = new Timer();
        mTimer.schedule(new getWifiTask(),60000);
    }
    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            resetTimer();
            mWifiManager.startScan();
        }
    }
}
