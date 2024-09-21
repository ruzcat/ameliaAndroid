package cl.brown.amelia.ui.wifi;

import static android.content.Context.WIFI_SERVICE;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cl.brown.amelia.R;
import cl.brown.amelia.ui.device.ConfigDeviceActivity;
import cl.brown.amelia.ui.wifi.placeholder.WifiContent;

/**
 * A fragment representing a list of Items.
 */
public class WifiFragment extends Fragment {

    private static final String TAG = WifiFragment.class.getName();

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private View view;
    private static final String SECURITY_TYPE_WPA3 = "WPA3";
    private static ConnectivityManager connectivityManager;
    private WifiManager wifiManager;
    private BroadcastReceiver wifiScanReceiver;
    private IntentFilter intentFilter;
    private static ConnectivityManager.NetworkCallback networkCallback;
    private RecyclerView rvDevices;
    private WifiRecyclerViewAdapter wifiListAdapter;
    private Network deviceAmelia;
    private Network networkConnected;

    private static WifiFragment instance;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public WifiFragment() {
        Log.d(TAG, "Constructor:");
        instance = this;
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static WifiFragment newInstance(int columnCount) {
        Log.d(TAG, "newInstance:");
        WifiFragment fragment = new WifiFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    public static WifiFragment getInstance(){
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate:");
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView:");
        view = inflater.inflate(R.layout.fragment_wifi_list, container, false);

        connectivityManager = (ConnectivityManager) view.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        wifiManager = (WifiManager) view.getContext().getSystemService(WIFI_SERVICE);
        wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                Log.d(TAG, "BroadcastReceiver.onReceive: success= " + success);
            }

            @Override
            public IBinder peekService(Context myContext, Intent service) {
                Log.d(TAG, "BroadcastReceiver.peekService:");
                return super.peekService(myContext, service);
            }

            @Override
            public int getSentFromUid() {
                Log.d(TAG, "BroadcastReceiver.getSentFromUid:");
                return super.getSentFromUid();
            }

            @Nullable
            @Override
            public String getSentFromPackage() {
                return super.getSentFromPackage();
            }
        };

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        view.getContext().registerReceiver(wifiScanReceiver, intentFilter);

        wifiListAdapter = new WifiRecyclerViewAdapter(refreshList());
        rvDevices = (RecyclerView) view.findViewById(R.id.recyclerViewList);
        // Set the adapter
        if (rvDevices != null) {
            Context context = rvDevices.getContext();
            if (mColumnCount <= 1) {
                rvDevices.setLayoutManager(new LinearLayoutManager(context));
            } else {
                rvDevices.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            rvDevices.setAdapter(wifiListAdapter);
        }

        view.findViewById(R.id.refreshList).setOnClickListener(view -> {
            Log.d(TAG, "onClick: refreshing");
            wifiListAdapter.updateList(refreshList());
            wifiListAdapter.notifyItemRangeChanged(0, WifiContent.ITEMS.size());
        });
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause:");
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(wifiScanReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: init");
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy (E) HH:mm:ss");
        ((TextView) view.findViewById(R.id.status_text)).setText(dateTimeFormat.format(localDateTime));

        registerNetWork();
        verifyPermissions();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void registerNetWork(){
        if(connectivityManager != null) {
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
        }else{
            Log.i(TAG, "registerNetWork: NULL");
        }
    }

    public void verifyPermissions(){
        if (wifiScanReceiver != null) {
            try {
                Log.d(TAG, "verifyPermissions: registerReceiver");
                requireActivity().registerReceiver(wifiScanReceiver, intentFilter);
            } catch (Exception e) {
                // already registered
            }
        }
    }

    public ArrayList<WifiContent.WifiItem> refreshList() {
        Log.d(TAG, "refreshWifiList:");
        ArrayList<String> list = new ArrayList();
        ArrayList<WifiContent.WifiItem> wifiList = new ArrayList<>();
        ArrayList<WifiContent.WifiItem> configList = new ArrayList<>();
        if (requireContext().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return configList;
        }
        Log.d(TAG, "refreshWifiList: scanning");
        List<ScanResult> scanResults = wifiManager.getScanResults();
        scanResults.forEach(wifiScan -> {
            if (wifiScan != null) {
                if (wifiScan.SSID != null) {
                    if(!wifiScan.SSID.isEmpty()) {
                        Log.d(TAG, "refreshWifiList: wifiScan "+wifiScan.SSID);
                        //if (wifiScan.SSID.contains("Amelia")) {
                        Log.d(TAG, "refreshWifiList: adding ");
                        Log.d(TAG, "refreshWifiList: wifiScan.SSID " + wifiScan.SSID);
                        Log.d(TAG, "refreshWifiList: wifiScan.level " + wifiScan.level);
                        Log.d(TAG, "refreshWifiList: wifiScan.capabilities " + wifiScan.capabilities);
                        Log.d(TAG, "refreshWifiList: wifiScan.BSSID " + wifiScan.BSSID);
                        Log.d(TAG, "refreshWifiList: wifiScan.frequency " + wifiScan.frequency);
                        Log.d(TAG, "refreshWifiList: wifiScan.channelWidth " + wifiScan.channelWidth);
                        if(!list.contains(wifiScan.SSID)) {
                            list.add(wifiScan.SSID);
                            wifiList.add(new WifiContent.WifiItem( String.valueOf(wifiList.size()+1),  wifiScan.SSID, wifiScan.level, wifiScan.BSSID));
                        }
                        //}
                    }
                }
            }

        });
        if(wifiList.size() > 0) {
            Log.d(TAG, "refreshWifiList: ordering");
            wifiList.sort((wifi1, wifi2) -> wifi1.ssid.compareTo(wifi2.ssid));
            Log.d(TAG, "refreshWifiList: wifiList " + wifiList.size());
            wifiList.forEach(wifi -> {
                WifiContent.WifiItem item = new WifiContent.WifiItem( String.valueOf(configList.size()+1),  wifi.ssid, wifi.waveLevel, wifi.securityType);
                configList.add(item);
                WifiContent.addItem(item);
            });
        }else {
            Log.d(TAG, "refreshWifiList: wifiList empty");
        }
        return configList;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigurationChanged");
    }

    private static WifiNetworkSpecifier buildWifiConfig(String ssid, String pass, String securityType) {
        WifiNetworkSpecifier config = null;

        if (securityType != null && securityType.equals(SECURITY_TYPE_WPA3)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                config = new WifiNetworkSpecifier.Builder()
                        .setSsid(ssid)
                        .setWpa3Passphrase(pass)
                        .build();
            }
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                config = new WifiNetworkSpecifier.Builder()
                        .setSsid(ssid)
                        .setWpa2Passphrase(pass)
                        .build();
            }
        }

        // have to set a very high number in order to ensure that
        // Android doesn't immediately drop this connection and reconnect to //the a different AP.
        return config;
    }

    public void connectToAp(String ssid, String pass, String securityType) {

        if(!getNetWorkSSID().equals("\""+ssid+"\"")){
            Log.i(TAG, "connectToAp: Connecting to:" + ssid);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                NetworkRequest request = new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .setNetworkSpecifier(buildWifiConfig(ssid, pass, securityType))
                        .build();

                networkCallback = new ConnectivityManager.NetworkCallback(){
                    @Override
                    public void onAvailable(@NonNull Network network) {
                        super.onAvailable(network);
                        deviceAmelia = network;
                        Log.d(TAG, "connectToAp.onAvailable: The default network is now:" + network);
                        Intent list = new Intent(requireContext(), ConfigDeviceActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("connectToAmelia", true);
                        list.putExtras(bundle);
                        startActivity(list);
                    }

                    @Override
                    public void onLosing(@NonNull Network network, int maxMsToLive) {
                        super.onLosing(network, maxMsToLive);
                        Log.d(TAG, "connectToAp.onLosing: The default network is now:" + network);
                    }

                    @Override
                    public void onLost(@NonNull Network network) {
                        super.onLost(network);
                        Log.d(TAG, "connectToAp.onLost: The default network is now:" + network);
                    }

                    @Override
                    public void onUnavailable() {
                        super.onUnavailable();
                    }

                    @Override
                    public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                        super.onCapabilitiesChanged(network, networkCapabilities);
                        Log.d(TAG, "connectToAp.onCapabilitiesChanged: The default network is now:" + network);
                    }

                    @Override
                    public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
                        super.onLinkPropertiesChanged(network, linkProperties);
                        Log.d(TAG, "connectToAp.onLinkPropertiesChanged: The default network is now:" + network);
                    }

                    @Override
                    public void onBlockedStatusChanged(@NonNull Network network, boolean blocked) {
                        super.onBlockedStatusChanged(network, blocked);
                        Log.d(TAG, "connectToAp.onBlockedStatusChanged: The default network is now:" + network);
                    }
                };

                connectivityManager.requestNetwork(request, networkCallback);
            }else{

                Log.d(TAG, "connectToAp.onBlockedStatusChanged: The default network is now:");

            }

        }else{
            Log.i(TAG, "connectToAp: Already connected to:" + ssid);
            Intent list = new Intent(requireContext(), ConfigDeviceActivity.class);
            Bundle bundle = new Bundle();
            bundle.putBoolean("connectToAmelia", true);
            list.putExtras(bundle);
            startActivity(list);
        }
    }

    public Network getDeviceAmelia() {
        return deviceAmelia != null ? deviceAmelia:networkConnected;
    }

    public String getNetWorkSSID(){
        WifiManager wifiManager = (WifiManager) requireContext().getSystemService(WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        Log.i(TAG, "getNetWorkSSID: " + info.getSSID());
        return info.getSSID();
    }

    public void unregisterNetworkCallback(){
        if(networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (Exception e) {
                Log.d(TAG, "unregisterNetworkCallback: unregisterNetworkCallback: e=", e);
            }
        }
    }

}
