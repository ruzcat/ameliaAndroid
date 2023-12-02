package cl.brown.amelia.service;

import android.net.Network;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import cl.brown.amelia.database.UtilsBD;
import cl.brown.amelia.model.Device;
import cl.brown.amelia.ui.device.OperateDeviceFragment;
import cl.brown.amelia.ui.wifi.WifiFragment;
import cl.brown.amelia.util.HttpServiceDeviceUtils;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpServiceCallable implements Callable<Boolean> {
    private static final String TAG = HttpServiceCallable.class.getName();
    private final String[] params;

    public HttpServiceCallable(String... params) {
        this.params = params;
    }

    @Override
    public Boolean call() {
        try {
            OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
            Network network = null;
            if(params[0].equals(HttpServiceDeviceUtils.CONFIG_ACTION)){
                network = WifiFragment.getInstance().getDeviceAmelia();
            }else {
                network = OperateDeviceFragment.getInstance().getNetworkConnected();
            }

            Log.i(TAG, "network " + network);
            if(network != null) {
                okHttpBuilder.socketFactory(network.getSocketFactory())
                        .callTimeout(17000, TimeUnit.MILLISECONDS)
                        .readTimeout(17000, TimeUnit.MILLISECONDS)
                        .writeTimeout(17000, TimeUnit.MILLISECONDS);

                OkHttpClient client = okHttpBuilder.build();

                Request request = generateRequest(params);

                try (Response response = client.newCall(request).execute()) {
                    if (response.body() != null) {
                        Gson gson = new Gson();
                        AmeliaResponseBody responseBody = gson.fromJson(response.body().charStream(), AmeliaResponseBody.class);
                        Log.i(TAG, "responseBody " + responseBody);

                        if (responseBody != null) {
                            if (responseBody.getResult().equals("success")) {
                                Device device = new Device(responseBody.getData().getSerial(), responseBody.getData().getIp(), responseBody.getData().getApIp(), responseBody.getData().getPort(), "true");
                                UtilsBD.saveDeviceInfo(device);
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
            case HttpServiceDeviceUtils.CONFIG_ACTION:
                return new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("ssid", params[1])
                        .addFormDataPart("pass", params[2])
                        .addFormDataPart("ip", "")
                        .build();
            case HttpServiceDeviceUtils.UP_ACTION:

            case HttpServiceDeviceUtils.DOWN_ACTION:
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
            case HttpServiceDeviceUtils.CONFIG_ACTION:
                return new Request.Builder()
                    .url(HttpServiceDeviceUtils.CONFIG_URL+HttpServiceDeviceUtils.CONFIG_ACTION)
                    .post(generateBody(params))
                    .build();
            case HttpServiceDeviceUtils.UP_ACTION:
                return new Request.Builder()
                        .url(HttpServiceDeviceUtils.UP_URL+HttpServiceDeviceUtils.UP_ACTION)
                        .post(generateBody(params))
                        .build();
            case HttpServiceDeviceUtils.DOWN_ACTION:
                return new Request.Builder()
                        .url(HttpServiceDeviceUtils.DOWN_URL+HttpServiceDeviceUtils.DOWN_ACTION)
                        .post(generateBody(params))
                        .build();

        };

        return null;

    }
}
