package se.moksnes.sebastian.timestamp.Fragments;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import se.moksnes.sebastian.timestamp.Data.TimeTableContract;
import se.moksnes.sebastian.timestamp.Data.TimeTableRepository;
import se.moksnes.sebastian.timestamp.Models.TimeStamp;
import se.moksnes.sebastian.timestamp.R;
import se.moksnes.sebastian.timestamp.Receivers.LocationWatcherIntent;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CurrentDayFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CurrentDayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CurrentDayFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Boolean mIsBound = false;
    private View _view;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Messenger mService = null;
    /** Flag indicating whether we have called bind on the service. */
    /** Some text view we are using to show state information. */

    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            if(msg.what == LocationWatcherIntent.MSG_STATE_IN || msg.what == LocationWatcherIntent.MSG_STATE_OUT){
                TextView textView = (TextView) _view.findViewById(R.id.current);
                long time = (long) msg.obj;
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                String dateString = formatter.format(new Date(time));
                if(msg.what == LocationWatcherIntent.MSG_STATE_IN){
                    textView.setText("In "+ dateString);
                }
                else{
                    textView.setText("Ute " + dateString);
                }
                //Toast.makeText(getApplicationContext(), "Got message.", Toast.LENGTH_SHORT).show();
            }

            super.handleMessage(msg);
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    private OnFragmentInteractionListener mListener;

    public CurrentDayFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CurrentDayFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CurrentDayFragment newInstance(String param1, String param2) {
        CurrentDayFragment fragment = new CurrentDayFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        if (isMyServiceRunning(LocationWatcherIntent.class)) {
            Activity activity = getActivity();
            Intent intent = new Intent(activity, LocationWatcherIntent.class);
            activity.startService(intent);
        }
        doBindService();
    }

    private void setInitialState() {
        TimeTableRepository repo = new TimeTableRepository(getActivity());
        Boolean isIn = repo.isIn();

        TimeStamp[] currentDay = repo.getDay();
        TextView textView = (TextView) _view.findViewById(R.id.current);
        if(isIn)  {
            textView.setText("Inne");
        }else{
            textView.setText("Ute");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        _view = inflater.inflate(R.layout.fragment_current_day, container, false);
        setInitialState();
        return _view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        doUnbindService();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        Activity activity = getActivity();
        ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mService = new Messenger(service);
            Toast.makeText(getActivity(), "Conntected to service", Toast.LENGTH_SHORT).show();

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                Message msg = Message.obtain(null,
                        LocationWatcherIntent.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            Toast.makeText(getActivity(), "Disconnected from service", Toast.LENGTH_SHORT).show();
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        if(!mIsBound) {
            Activity activity = getActivity();
            activity.bindService(new Intent(activity,
                    LocationWatcherIntent.class), mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            Activity activity = getActivity();
            activity.unbindService(mConnection);
            mIsBound = false;
        }
    }
}
