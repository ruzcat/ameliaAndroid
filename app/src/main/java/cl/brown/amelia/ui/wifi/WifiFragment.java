package cl.brown.amelia.ui.wifi;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
    private final int PERMISSION_REQUEST_CODE = 999;
    private static final String SECURITY_TYPE_WPA3 = "WPA3";
    private final String SECURITY_TYPE_WPA2 = "WPA2";
    private final String SECURITY_TYPE_WPA = "WPA";
    private final String SECURITY_TYPE_NA = "N/A";
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int INTERNET_PERMISSION_CODE = 200 ;
    private static final int ACCESS_FINE_LOCATION_PERMISSION_CODE = 300;
    private static final int ACCESS_COARSE_LOCATION_PERMISSION_CODE = 400;
    private static final int NEARBY_WIFI_DEVICES_PERMISSION_CODE = 500;
    private Boolean permissionRejected = false;
    private Boolean isDialogDisplayed = false;
    private Boolean gpsActivated = false;
    private Boolean gpsGranted = false;
    private Boolean wifiActivated = false;
    private Boolean allConfig = false;
    private ActivityResultLauncher<String[]> locationPermissionRequest;
    private static ConnectivityManager connectivityManager;
    private WifiManager wifiManager;
    private BroadcastReceiver wifiScanReceiver;
    private IntentFilter intentFilter;
    private BroadcastReceiver suggestionPostConnectionReceiver;
    private static ConnectivityManager.NetworkCallback networkCallback;
    private RecyclerView rvDevices;
    private WifiRecyclerViewAdapter wifiListAdapter;
    private Network deviceAmelia;
    private Network networkConnected;

    /* GPS SERVICE */
    private LocationManager locationManager;
    private AlertDialog dialog;
    private AlertDialog.Builder optionDialog;

    private static WifiFragment instance;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public WifiFragment() {
        Log.d(TAG, "Constructor:");
        this.instance = this;
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
        configPermissionGPS();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView:");
        view = inflater.inflate(R.layout.fragment_wifi_list, container, false);

        connectivityManager = (ConnectivityManager) view.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        wifiManager = (WifiManager) view.getContext().getSystemService(Context.WIFI_SERVICE);
        wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                Log.d(TAG, "BroadcastReceiver.onReceive: success= " + success);
                wifiActivated =true;
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

        wifiListAdapter = new WifiRecyclerViewAdapter(WifiContent.ITEMS);
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

        view.findViewById(R.id.refreshList).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: refreshing");
                refreshList();
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView: ");
        if(wifiScanReceiver != null) {
            requireActivity().unregisterReceiver(wifiScanReceiver);
        }
        if(suggestionPostConnectionReceiver != null) {
            try {
                requireActivity().unregisterReceiver(suggestionPostConnectionReceiver);
            } catch (Exception e) {
                Log.d(TAG, "onDestroyView: unregisterReceiver: e=", e);
            }
        }
        if(networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (Exception e) {
                Log.d(TAG, "onDestroyView: unregisterNetworkCallback: e=", e);
            }
        }
        super.onDestroy();
        super.onDestroyView();
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
        Log.d(TAG, "onResume: wait allConfig "+allConfig);
        if(!allConfig) {
            Handler handler = new Handler();
            Runnable r = new Runnable() {
                public void run() {
                    //what ever you do here will be done after 2 seconds delay.
                    Log.d(TAG, "onResume: init");
                    LocalDateTime localDateTime = LocalDateTime.now();
                    DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd(E) HH:mm:ss");
                    ((TextView) view.findViewById(R.id.status_text)).setText(dateTimeFormat.format(localDateTime));

                    registerNetWork();

                    if (!gpsGranted) {
                        initPermissionGPS();
                    } else {
                        verifyPermissions();
                    }


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
                wifiActivated = true;
            }

            @Override
            public void onLosing(@NonNull Network network, int maxMsToLive) {
                super.onLosing(network, maxMsToLive);
                Log.d(TAG, "registerNetWork.onLosing: The application no longer has a default network. The last default network was " + network);
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                wifiActivated = false;
                Log.d(TAG, "registerNetWork.onLost: The application no longer has a default network. The last default network was " + network);
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                wifiActivated = false;
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

    public void verifyPermissions(){
        optionDialog = null;
        if(isPermissionsGranted()){
            if(isProvidersActivated()) {
                if (requireContext().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "verifyPermissions: permission.ACCESS_FINE_LOCATION is granted.");
                    gpsActivated = true;
                } else {
                    Log.d(TAG, "verifyPermissions: permission.ACCESS_FINE_LOCATION is required.");
                    if (!permissionRejected) {
                        ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
                    } else if (!isDialogDisplayed) {
                        isDialogDisplayed = true;
                        new AlertDialog.Builder(getContext())
                                .setTitle("Amelia")
                                .setMessage("ACCESS_FINE_LOCATION permission is required for scanning Wi-Fi list. Restart app and grant ACCESS_FINE_LOCATION permission.")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();

                    }
                }

                if (wifiScanReceiver != null) {
                    try {
                        Log.d(TAG, "verifyPermissions: registerReceiver");
                        requireActivity().registerReceiver(wifiScanReceiver, intentFilter);
                    } catch (Exception e) {
                        // already registered
                    }
                }
                refreshList();
                allConfig = true;
            } else {
                showInfoAlertNoGpsWifi();
            }
        }else{
            requestPermissions();
        }
    }

    private void refreshList() {
        Log.d(TAG, "refreshWifiList:");
        ArrayList<WifiContent.WifiItem> wifiList = new ArrayList<WifiContent.WifiItem>();
        if (requireContext().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Log.d(TAG, "refreshWifiList: scanning");
        List<ScanResult> scanResults = wifiManager.getScanResults();
        scanResults.forEach(wifiScan -> {
            if (wifiScan != null) {
                if (wifiScan.SSID != null) {
                    if(!wifiScan.SSID.isEmpty()) {
                        Log.d(TAG, "refreshWifiList: wifiScan "+wifiScan.SSID);
                        if (wifiScan.SSID.contains("Amelia")) {
                            Log.d(TAG, "refreshWifiList: adding ");
                            Log.d(TAG, "refreshWifiList: wifiScan.SSID " + wifiScan.SSID);
                            Log.d(TAG, "refreshWifiList: wifiScan.level " + wifiScan.level);
                            Log.d(TAG, "refreshWifiList: wifiScan.capabilities " + wifiScan.capabilities);
                            Log.d(TAG, "refreshWifiList: wifiScan.BSSID " + wifiScan.BSSID);
                            Log.d(TAG, "refreshWifiList: wifiScan.frequency " + wifiScan.frequency);
                            Log.d(TAG, "refreshWifiList: wifiScan.channelWidth " + wifiScan.channelWidth);
                            wifiList.add(new WifiContent.WifiItem(wifiScan.SSID, wifiScan.level, getSecurityType(wifiScan.capabilities)));
                        }
                    }
                }
            }

        });
        if(wifiList.size() > 0) {
            Log.d(TAG, "refreshWifiList: ordering");
            wifiList.sort(new Comparator<WifiContent.WifiItem>() {
                @Override
                public int compare(WifiContent.WifiItem wifi1, WifiContent.WifiItem wifi2) {
                    return wifi2.waveLevel - wifi1.waveLevel;
                }
            });
            Log.d(TAG, "refreshWifiList: wifiList " + wifiList.size());
            wifiListAdapter.updateList(wifiList);
            if (rvDevices != null && rvDevices.getAdapter() != null) {
                rvDevices.getAdapter().notifyDataSetChanged();
            }
        }else {
            Log.d(TAG, "refreshWifiList: wifiList empty");
        }
    }

    private String getSecurityType(String capabilities){
        if(capabilities.contains(SECURITY_TYPE_WPA3))
            return SECURITY_TYPE_WPA3;
        if(capabilities.contains(SECURITY_TYPE_WPA2))
            return SECURITY_TYPE_WPA2;
        if(capabilities.contains(SECURITY_TYPE_WPA))
            return SECURITY_TYPE_WPA;
        return SECURITY_TYPE_NA;
    }

    private void requestPermissions(){
        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES};
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.INTERNET) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(requireActivity(), perms, INTERNET_PERMISSION_CODE);
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(requireActivity(), perms, ACCESS_FINE_LOCATION_PERMISSION_CODE);
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(requireActivity(), perms, ACCESS_COARSE_LOCATION_PERMISSION_CODE);
        }

        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(requireActivity(), perms, NEARBY_WIFI_DEVICES_PERMISSION_CODE);
        }

    }

    private boolean isPermissionsGranted(){
        if( ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.INTERNET) == PackageManager.PERMISSION_DENIED
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
                && ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED
                && ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_DENIED){
            return false;
        }
        return true;
    }

    private boolean isProvidersActivated(){
        if (locationManager == null)
            locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);

        if (!wifiActivated && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Log.e(TAG,"isLocationEnabled NETWORK_PROVIDER Disabled");
            return false;
        }
        Log.i(TAG,"isLocationEnabled NETWORK_PROVIDER Enabled");
        wifiActivated = true;

        if (requireContext().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.e(TAG, "isLocationEnabled GPS_PROVIDER Disabled");
                gpsActivated = false;
                return false;
            }
        }else{
            Log.e(TAG, "isLocationEnabled GPS_PROVIDER Disabled");
            return false;
        }
        Log.i(TAG,"isLocationEnabled GPS_PROVIDER Enabled");
        gpsActivated = true;

        return true;
    }

    public synchronized void showInfoAlertNoGpsWifi() {
        String provider = wifiActivated.equals(Boolean.FALSE) ? "Wifi":"GPS";

        if(optionDialog == null){
            optionDialog = new AlertDialog.Builder(requireContext())
                    .setTitle(provider+" Config.")
                    .setMessage(provider+" esta desactivado, se requiere Activarlo!")
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intentGPS = wifiActivated.equals(Boolean.FALSE) ? new Intent(Settings.ACTION_WIFI_SETTINGS): new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intentGPS);
                            /* result on onActivityResult */
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AlertDialog.Builder optionDialog = new AlertDialog.Builder(requireContext())
                                    .setTitle("Amelia")
                                    .setMessage("Si no activa el "+provider+" la app no funcionara correctamente")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });

                            optionDialog.setCancelable(false);

                            optionDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                }
                            });

                            // Create the alert dialog
                            final AlertDialog dialogGPS = optionDialog.create();
                            dialogGPS.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    final Drawable styleButtonDrawable = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.round_dialog, null);
                                    Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 10f);
                                    params.setMarginStart(70);
                                    params.setMarginEnd(70);

                                    positiveButton.setLayoutParams(params);
                                    positiveButton.setTextColor(Color.parseColor("#FFFF0400"));
                                    positiveButton.setBackground(styleButtonDrawable);
                                    positiveButton.setBackgroundResource(R.drawable.round_dialog);
                                }
                            });

                            dialogGPS.setCanceledOnTouchOutside(false);

                            dialogGPS.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                }
                            });
                            requireActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.i("dialogGPS", "dialog show " + dialogGPS);
                                    dialogGPS.show();
                                }
                            });
                            dialog.dismiss();
                        }
                    });

            if (optionDialog != null){
                optionDialog.show();
            }

        }
    }

    private void configPermissionGPS(){
        locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                        .RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_COARSE_LOCATION,false);
                            if (fineLocationGranted != null && fineLocationGranted) {
                                // Precise location access granted.
                                gpsGranted = true;
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                // Only approximate location access granted.
                                gpsGranted = true;
                            } else {
                                // No location access granted.
                                gpsGranted = false;
                            }
                        }
                );
    }

    private void initPermissionGPS(){
        locationPermissionRequest.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigurationChanged");
    }

    private static WifiNetworkSpecifier buildWifiConfig(String ssid, String pass, String securityType) {
        WifiNetworkSpecifier config = null;

        switch (securityType){
            case SECURITY_TYPE_WPA3:
                config = new WifiNetworkSpecifier.Builder()
                        .setSsid(ssid)
                        .setWpa3Passphrase(pass)
                        .build();
                break;

            default:
                config = new WifiNetworkSpecifier.Builder()
                        .setSsid(ssid)
                        .setWpa2Passphrase(pass)
                        .build();
        }

        // have to set a very high number in order to ensure that
        // Android doesn't immediately drop this connection and reconnect to //the a different AP.
        return config;
    }

    public void connectToAp(String ssid, String pass, String securityType) {

        if(!getNetWorkSSID().equals("\""+ssid+"\"")){
            Log.i(TAG, "connectToAp: Connecting to:" + ssid);
            NetworkRequest request =
                    new NetworkRequest.Builder()
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
        WifiManager wifiManager = (WifiManager) requireContext().getSystemService(Context.WIFI_SERVICE);
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