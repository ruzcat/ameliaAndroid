package cl.brown.amelia.ui.device;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cl.brown.amelia.R;
import cl.brown.amelia.database.UtilsBD;
import cl.brown.amelia.ui.device.placeholder.OperateDeviceContent;
import cl.brown.amelia.ui.wifi.WifiFragment;

/**
 * A fragment representing a list of Items.
 */
public class OperateDeviceFragment extends Fragment {
    private static final String TAG = OperateDeviceFragment.class.getName();

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private boolean devicesLoad = false;
    private RecyclerView rvDevices;
    private OperateDeviceRecyclerViewAdapter operateDeviceListAdapter;
    private static ConnectivityManager connectivityManager;
    private Network networkConnected;
    private static OperateDeviceFragment instance;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public OperateDeviceFragment() {
        Log.d(TAG, "Constructor:");
        this.instance = this;
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static OperateDeviceFragment newInstance(int columnCount) {
        OperateDeviceFragment fragment = new OperateDeviceFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    public static OperateDeviceFragment getInstance(){
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_operate_device_list, container, false);

        connectivityManager = (ConnectivityManager) view.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            rvDevices = (RecyclerView) view;
            if (mColumnCount <= 1) {
                rvDevices.setLayoutManager(new LinearLayoutManager(context));
            } else {
                rvDevices.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            operateDeviceListAdapter = new OperateDeviceRecyclerViewAdapter(OperateDeviceContent.ITEMS);
            rvDevices.setAdapter(operateDeviceListAdapter);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        registerNetWork();
        if(!devicesLoad) {
            Handler handler = new Handler();
            @SuppressLint("NotifyDataSetChanged") Runnable r = () -> {
                operateDeviceListAdapter.updateList(UtilsBD.getAllDevices());
                if(operateDeviceListAdapter.getItemCount() > 0){
                    devicesLoad = true;
                }
                if (rvDevices != null && rvDevices.getAdapter() != null) {
                    rvDevices.getAdapter().notifyDataSetChanged();
                }

            };

            handler.postDelayed(r, 2000);
        }
    }

    public void registerNetWork(){
        connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                Log.d(TAG, "registerNetWork.onAvailable: The default network is now:" + network);
                networkConnected = network;
            }

            @Override
            public void onLosing(@NonNull Network network, int maxMsToLive) {
                super.onLosing(network, maxMsToLive);
                Log.d(TAG, "registerNetWork.onLosing: The application no longer has a default network. The last default network was " + network);
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                Log.d(TAG, "registerNetWork.onLost: The application no longer has a default network. The last default network was " + network);
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                Log.d(TAG, "registerNetWork.onUnavailable: The default network:");
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);
                Log.d(TAG, "registerNetWork.onCapabilitiesChanged: The default network changed capabilities: " + networkCapabilities);
            }

            @Override
            public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties);
                Log.d(TAG, "registerNetWork.onLinkPropertiesChanged: The default network changed link properties: " + linkProperties);
            }

            @Override
            public void onBlockedStatusChanged(@NonNull Network network, boolean blocked) {
                super.onBlockedStatusChanged(network, blocked);
                Log.d(TAG, "registerNetWork.sonBlockedStatusChanged: The default network blocked: " + network + " blocked " + blocked);
            }
        });
    }

    public Network getNetworkConnected() {
        return networkConnected;
    }
}