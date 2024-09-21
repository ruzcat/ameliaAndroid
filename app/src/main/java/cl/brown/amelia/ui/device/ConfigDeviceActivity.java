package cl.brown.amelia.ui.device;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cl.brown.amelia.MainActivity;
import cl.brown.amelia.R;
import cl.brown.amelia.http.HttpExecutorRunner;
import cl.brown.amelia.http.HttpServices;
import cl.brown.amelia.ui.wifi.WifiFragment;
import cl.brown.amelia.utils.Constants;

public class ConfigDeviceActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = ConfigDeviceActivity.class.getName();
    private String mSSID;
    private WifiManager mWifiManager;
    private String[] mWifiList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_device);

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        getListWifi();
        final Spinner wifiNameEditText = findViewById(R.id.wifiName);
        wifiNameEditText.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) this);

        //Create a Array Adapter
        ArrayAdapter adapter = new ArrayAdapter<>(ConfigDeviceActivity.this, android.R.layout.simple_spinner_item, mWifiList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Assigning the adapter to Spinner
        wifiNameEditText.setAdapter(adapter);

        final EditText wifiPassEditText = findViewById(R.id.wifiPass);
        final Button wifiConnectBtn = findViewById(R.id.connectToWifi);

        wifiPassEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (v.getId() == R.id.password && !hasFocus) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            }
        });

        wifiConnectBtn.setOnClickListener(v -> {
            HttpExecutorRunner httpExecutorRunner = new HttpExecutorRunner();
            httpExecutorRunner.execute(new HttpServices(Constants.CONFIG_ACTION, mSSID, wifiPassEditText.getText().toString()), new HttpExecutorRunner.Callback<Boolean>() {
                @Override
                public void onComplete(Boolean result) {
                    Log.i(TAG, "httpExecutorRunner.onComplete: " + result);
                    if (result) {
                        WifiFragment.getInstance().unregisterNetworkCallback();
                        Toast.makeText(getBaseContext(), "Amelia Lista para atenderte", Toast.LENGTH_SHORT).show();
                        //showInfoAlert("Access Point New Connect", "Amelia Lista para atenderte" );
                        Intent myIntent = new Intent(ConfigDeviceActivity.this, MainActivity.class);
                        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        // Closing all the Activities
                        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        // Add new Flag to start new Activity
                        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        myIntent.addCategory(Intent.CATEGORY_HOME);
                        WifiFragment.getInstance().registerNetWork();
                        startActivity(myIntent);
                        ConfigDeviceActivity.this.finish();
                    } else {
                        showInfoAlert("Access Point Error Connection", "Intente Nuevamente");
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "httpExecutorRunner.onError: ", e);
                    showInfoAlert("Access Point Error Connection", "Intente Nuevamente");
                }
            });
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                wifiConnectBtn.setEnabled(true);
            }
        };

        wifiPassEditText.addTextChangedListener(afterTextChangedListener);
        wifiPassEditText.setOnEditorActionListener((v, actionId, event) -> {
            Log.i(TAG, "wifiPassEditText.onEditorAction: " + actionId);
            return actionId == EditorInfo.IME_ACTION_DONE;
        });

        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                Log.i(TAG, "OnBackPressedCallback ");
                onStop();
                finish();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);

    }

    public synchronized void showInfoAlert(String title, String message) {
        // Create the object of AlertDialog Builder class
        AlertDialog.Builder builder = new AlertDialog.Builder(ConfigDeviceActivity.this);

        // Set the message show for the Alert time
        builder.setMessage(message);

        // Set Alert Title
        builder.setTitle(title);

        // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
        builder.setCancelable(false);

        // Set the positive button with yes name Lambda OnClickListener method is use of DialogInterface interface.
        builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
            // When the user click yes button then app will close
            dialog.cancel();
        });

        // Set the Negative button with No name Lambda OnClickListener method is use of DialogInterface interface.
        builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
            // If user click no then dialog box is canceled.
            dialog.cancel();
        });

        // Create the Alert dialog
        AlertDialog alertDialog = builder.create();
        // Show the Alert Dialog box
        ConfigDeviceActivity.this.runOnUiThread(() -> {
            Log.i("alertDialog", "dialog show ");
            alertDialog.show();
        });
        //alertDialog.show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mSSID = mWifiList[position];
        Toast.makeText(getApplicationContext(), "You have Chosen :" + mWifiList[position], Toast.LENGTH_LONG).show();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void getListWifi() {
        ArrayList<String> wifiList = new ArrayList();
        mWifiList = null;
        if (mWifiManager == null) {
            mWifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        List<ScanResult> scanResults = mWifiManager.getScanResults();
        scanResults.forEach(wifiScan -> {
            if (wifiScan != null) {
                if (wifiScan.SSID != null) {
                    if(!wifiScan.SSID.isEmpty()) {
                        Log.d(TAG, "refreshWifiList: wifiScan "+wifiScan.SSID);
                        if (!wifiScan.SSID.contains("Amelia")) {
                            Log.d(TAG, "refreshWifiList: adding ");
                            Log.d(TAG, "refreshWifiList: wifiScan.SSID " + wifiScan.SSID);
                            Log.d(TAG, "refreshWifiList: wifiScan.level " + wifiScan.level);
                            Log.d(TAG, "refreshWifiList: wifiScan.capabilities " + wifiScan.capabilities);
                            Log.d(TAG, "refreshWifiList: wifiScan.BSSID " + wifiScan.BSSID);
                            Log.d(TAG, "refreshWifiList: wifiScan.frequency " + wifiScan.frequency);
                            Log.d(TAG, "refreshWifiList: wifiScan.channelWidth " + wifiScan.channelWidth);
                            if(!wifiList.contains(wifiScan.SSID)) {
                                wifiList.add(wifiScan.SSID);
                            }
                        }
                    }
                }
            }

        });
        if(wifiList.size() > 0) {
            Log.d(TAG, "refreshWifiList: ordering");
            Collections.sort(wifiList, new Comparator<String>() {
                public int compare(String obj1, String obj2) {
                    return obj1.compareTo(obj2);
                }
            });
            Log.d(TAG, "refreshWifiList: wifiList " + wifiList.size());
        }else {
            Log.d(TAG, "refreshWifiList: wifiList empty");
        }
        mWifiList = wifiList.stream().toArray(String[]::new);
    }
}