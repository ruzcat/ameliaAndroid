package cl.brown.amelia.http;

import android.net.Network;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import cl.brown.amelia.utils.DataBase;
import cl.brown.amelia.model.Device;
import cl.brown.amelia.ui.device.OperateDeviceFragment;
import cl.brown.amelia.ui.wifi.WifiFragment;
import cl.brown.amelia.utils.Constants;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpServices implements Callable<Boolean> {
    private static final String TAG = HttpServices.class.getName();
    private final String[] params;

    public HttpServices(String... params) {
        this.params = params;
    }

    @Override
    public Boolean call() {
        try {
            OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
            Network network = null;
            if(params[0].equals(Constants.CONFIG_ACTION)){
                network = WifiFragment.getInstance().getDeviceAmelia();
            }else {
                network = OperateDeviceFragment.getInstance().getNetworkConnected();
            }

            Log.i(TAG, "network " + network);
            if(network != null) {
                okHttpBuilder.socketFactory(network.getSocketFactory())
                        .callTimeout(100000, TimeUnit.MILLISECONDS)
                        .readTimeout(100000, TimeUnit.MILLISECONDS)
                        .writeTimeout(100000, TimeUnit.MILLISECONDS);

                OkHttpClient client = okHttpBuilder.build();

                Request request = generateRequest(params);
                Log.i(TAG, "Executing request");
                try (Response response = client.newCall(request).execute()) {
                    if (response.body() != null) {
                        Gson gson = new Gson();
                        AmeliaResponseBody responseBody = gson.fromJson(response.body().charStream(), AmeliaResponseBody.class);
                        Log.i(TAG, "responseBody " + responseBody);

                        if (responseBody != null) {
                            if (responseBody.getResult().equals("success")) {
                                if(responseBody.getData() != null) {
                                    Device device = new Device(responseBody.getData().getSerial(), responseBody.getData().getIp(), responseBody.getData().getApIp(), responseBody.getData().getPort(), "true");
                                    DataBase.saveDeviceInfo(device);
                                }
                                return true;
                            }
                        }
                    }
                    return false;
                } catch (IOException e) {
                    Log.e(TAG, "IOException ", e);
                    return false;
                } catch (JsonIOException jsonIoEx) {
                    Log.e(TAG, "JsonIOException ", jsonIoEx);
                    return false;
                } catch (JsonSyntaxException jsonSyntaxEx) {
                    Log.e(TAG, "JsonSyntaxException ", jsonSyntaxEx);
                    return false;
                } catch (JsonParseException jsonParseEx) {
                    Log.e(TAG, "JsonParseException ", jsonParseEx);
                    return false;
                }
            }else{
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private RequestBody generateBody(String... params){
        switch (params[0]){
            case Constants.CONFIG_ACTION:
                return new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("ssid", params[1])
                        .addFormDataPart("pass", params[2])
                        .addFormDataPart("ip", "")
                        .build();
            case Constants.UP_ACTION:

            case Constants.UP_HOLDER_ACTION:

            case Constants.DOWN_ACTION:

            case Constants.DOWN_HOLDER_ACTION:

            case Constants.STOP_ACTION:

            case Constants.SET_CONFIG_ACTION:

            case Constants.RESET_CONFIG_ACTION:

            case Constants.RESET_HARD_ACTION:

                return new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("serial", params[1])
                        .addFormDataPart("ip", params[2])
                        .build();

        };

        return null;
    }

    private Request generateRequest(String... params){

        switch (params[0]){
            case Constants.CONFIG_ACTION:
                return new Request.Builder()
                    .url(Constants.CONFIG_URL+ Constants.CONFIG_ACTION)
                    .post(generateBody(params))
                    .build();
            case Constants.UP_ACTION:
                return new Request.Builder()
                        .url(Constants.ACTION_URL+ Constants.UP_ACTION)
                        .post(generateBody(params))
                        .build();
            case Constants.UP_HOLDER_ACTION:
                return new Request.Builder()
                        .url(Constants.ACTION_URL+ Constants.UP_HOLDER_ACTION)
                        .post(generateBody(params))
                        .build();
            case Constants.DOWN_ACTION:
                return new Request.Builder()
                        .url(Constants.ACTION_URL+ Constants.DOWN_ACTION)
                        .post(generateBody(params))
                        .build();
            case Constants.DOWN_HOLDER_ACTION:
                return new Request.Builder()
                        .url(Constants.ACTION_URL+ Constants.DOWN_HOLDER_ACTION)
                        .post(generateBody(params))
                        .build();
            case Constants.STOP_ACTION:
                return new Request.Builder()
                        .url(Constants.ACTION_URL+ Constants.STOP_ACTION)
                        .post(generateBody(params))
                        .build();
            case Constants.SET_CONFIG_ACTION:
                return new Request.Builder()
                        .url(Constants.ACTION_URL+ Constants.SET_CONFIG_ACTION)
                        .post(generateBody(params))
                        .build();
            case Constants.RESET_CONFIG_ACTION:
                return new Request.Builder()
                        .url(Constants.ACTION_URL+ Constants.RESET_CONFIG_ACTION)
                        .post(generateBody(params))
                        .build();

        };

        return null;

    }
}
