package cl.brown.amelia.ui.device;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import cl.brown.amelia.MainActivity;
import cl.brown.amelia.R;
import cl.brown.amelia.service.HttpExecutorRunner;
import cl.brown.amelia.service.HttpServiceCallable;
import cl.brown.amelia.ui.wifi.WifiFragment;
import cl.brown.amelia.util.HttpServiceDeviceUtils;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConfigDeviceActivity extends AppCompatActivity {

    private static final String TAG = ConfigDeviceActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_device);

        final EditText wifiNameEditText = findViewById(R.id.wifiName);
        final EditText wifiPassEditText = findViewById(R.id.wifiPass);
        final Button wifiConnectBtn = findViewById(R.id.connectToWifi);

        wifiPassEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (v.getId() == R.id.password && !hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                }
            }
        });

        wifiConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpExecutorRunner httpExecutorRunner = new HttpExecutorRunner();
                httpExecutorRunner.execute(new HttpServiceCallable(HttpServiceDeviceUtils.CONFIG_ACTION, wifiNameEditText.getText().toString(), wifiPassEditText.getText().toString()), new HttpExecutorRunner.Callback<Boolean>() {
                    @Override
                    public void onComplete(Boolean result) {
                        Log.i(TAG, "httpExecutorRunner.onComplete: "+result);
                        if(result) {
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
                            startActivity(myIntent);
                            ConfigDeviceActivity.this.finish();
                        }else{
                            showInfoAlert("Access Point Error Connection", "Intente Nuevamente" );
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "httpExecutorRunner.onError: ", e);
                        showInfoAlert("Access Point Error Connection", "Intente Nuevamente" );
                    }
                });
            }
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
        wifiPassEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.i(TAG, "wifiPassEditText.onEditorAction: "+actionId);
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    return true;
                }
                return false;
            }
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
        ConfigDeviceActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("alertDialog", "dialog show ");
                alertDialog.show();
            }
        });
        //alertDialog.show();
    }
    
}