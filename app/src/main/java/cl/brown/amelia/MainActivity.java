package cl.brown.amelia;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import cl.brown.amelia.utils.DataBase;
import cl.brown.amelia.databinding.ActivityMainBinding;
import cl.brown.amelia.utils.Services;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private ActivityResultLauncher<String[]> permissionRequest;
    private Boolean gpsGranted;
    private Boolean gpsConfigInit;
    private Boolean wifiActivated;
    private Boolean wifiConfigInit;
    private BroadcastReceiver wifiScanReceiver;
    private static ConnectivityManager connectivityManager;
    private IntentFilter intentFilter;
    private Handler handler;
    private Runnable r;
    private AlertDialog optionDialog;
    private long backPressedTime;
    private Toast backToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_operate_device, R.id.nav_setting_device)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        DataBase.initBD(getApplicationContext());
        backPressed();
        wifiActivated = false;
        wifiConfigInit = false;
        gpsConfigInit = false;
        gpsGranted = false;
        connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                Log.d(TAG, "BroadcastReceiver.onReceive: success= " + success);
                wifiActivated = true;
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
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);
        configPermissionGPS();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume:");
    }

    @Override
    public void onTopResumedActivityChanged(boolean isTopResumedActivity) {
        super.onTopResumedActivityChanged(isTopResumedActivity);
        Log.w(TAG, "onTopResumedActivityChanged: Init "+isTopResumedActivity);
        boolean isMobileDataEnabled = Services.isMobileDataEnabled(getApplicationContext());
        Log.i(TAG, "onTopResumedActivityChanged: isMobileDataEnabled "+isMobileDataEnabled);
        boolean isWifiEnabled = Services.isWifiEnabled(getApplicationContext());
        Log.i(TAG, "onTopResumedActivityChanged: isWifiEnabled "+isWifiEnabled);
        boolean isGpsEnabled = Services.isGpsEnabled(getApplicationContext());
        Log.i(TAG, "onTopResumedActivityChanged: isGpsEnabled "+isGpsEnabled);
        if(isTopResumedActivity) {
            registerNetWork();
            if (!gpsGranted) {
                initPermissionGPS();
                if (gpsGranted && !isGpsEnabled) {
                    if (optionDialog != null) {
                        if (!optionDialog.isShowing()) {
                            gpsConfigInit = false;
                        }
                    }
                    if (!gpsConfigInit) {
                        gpsConfigInit = true;
                        optionDialog = null;
                        showInfoAlertNoGpsWifi("GPS");
                    }
                }
            } else {
                if (!isGpsEnabled) {
                    if (optionDialog != null) {
                        if (!optionDialog.isShowing()) {
                            gpsConfigInit = false;
                        }
                    }
                    if (!gpsConfigInit) {
                        gpsConfigInit = true;
                        optionDialog = null;
                        showInfoAlertNoGpsWifi("GPS");
                    }
                } else {
                    if (!isWifiEnabled) {
                        wifiActivated = false;
                        if (optionDialog != null) {
                            if (!optionDialog.isShowing()) {
                                wifiConfigInit = false;
                            }
                        }
                        if (!wifiConfigInit) {
                            wifiConfigInit = true;
                            optionDialog = null;
                            showInfoAlertNoGpsWifi("Wifi");
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "App Lista para operar", Toast.LENGTH_LONG).show();
                    }
                }
            }

        }
        Log.i(TAG, "onTopResumedActivityChanged: Finish "+isTopResumedActivity);
    }
    private void configPermissionGPS(){
        Log.d(TAG, "configPermissionGPS: Init "+gpsGranted);
        permissionRequest =
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
        Log.d(TAG, "configPermissionGPS: Finish "+gpsGranted);
    }
    private void initPermissionGPS(){
        Log.d(TAG, "initPermissionGPS: Init ");
        if (this.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "initPermissionGPS: permission.ACCESS_FINE_LOCATION is granted.");
            gpsGranted = true;
        } else {
            Log.d(TAG, "initPermissionGPS: permission.ACCESS_FINE_LOCATION is required.");
            permissionRequest.launch(new String[] {
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }

        Log.d(TAG, "initPermissionGPS: Finish "+gpsGranted);
    }

    private void registerNetWork(){
        Log.d(TAG, "registerNetWork: Init");
        connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                Log.d(TAG, "registerNetWork.onAvailable: The default network is now:" + network);
                //networkConnected = network;
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
        Log.d(TAG, "registerNetWork: Finish");
    }

    private synchronized void showInfoAlertNoGpsWifi(String provider) {
        if(optionDialog == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder .setTitle(provider+" Config.")
                    .setMessage(provider+" esta desactivado, se requiere Activarlo!")
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intentGPS = provider.equals("Wifi") ? new Intent(Settings.ACTION_WIFI_SETTINGS): new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intentGPS);
                            /* result on onActivityResult */
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(provider.equals("Wifi")){
                                wifiConfigInit = false;
                            }else{
                                gpsConfigInit = false;
                            }
                            AlertDialog.Builder optionDialog = new AlertDialog.Builder(getApplicationContext())
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
                                    final Drawable styleButtonDrawable = ResourcesCompat.getDrawable(getApplicationContext().getResources(), R.drawable.round_dialog, null);
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
                                    if(provider.equals("Wifi")){
                                        wifiConfigInit = false;
                                    }else{

                                    }
                                }
                            });
                            if(getParent() != null) {
                                getParent().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i("dialogGPS", "dialog show " + dialogGPS);
                                        dialogGPS.show();
                                    }
                                });
                            }
                            dialog.dismiss();
                        }
                    });


            optionDialog = builder.create();
            if(optionDialog != null){
                optionDialog.show();
            }

        }
    }

    private void backPressed() {
        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                Log.i(TAG, "OnBackPressedCallback ");
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    backToast.cancel();
                    //super.onBackPressed();
                    minimizeApp();
                    return;
                } else {
                    backToast = Toast.makeText(getBaseContext(), "Presione de nuevo para minimizar app", Toast.LENGTH_SHORT);
                    backToast.show();
                }

                backPressedTime = System.currentTimeMillis();
                return;
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void minimizeApp() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }


}