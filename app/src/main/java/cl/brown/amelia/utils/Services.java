package cl.brown.amelia.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.Method;

public class Services {
    private static final String TAG = Services.class.getName();

    /**
     * @return null if unconfirmed
     */
    public static Boolean isMobileDataEnabled(Context context){
        Object connectivityService = context.getSystemService(Context.CONNECTIVITY_SERVICE);
        ConnectivityManager cm = (ConnectivityManager) connectivityService;

        try {
            Class<?> c = Class.forName(cm.getClass().getName());
            Method m = c.getDeclaredMethod("getMobileDataEnabled");
            m.setAccessible(true);
            return (Boolean)m.invoke(cm);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static Boolean isWifiEnabled(Context context){
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if(wifiManager != null){
            return wifiManager.isWifiEnabled();
        }
        return false;
    }
    public static Boolean isGpsEnabled(Context context){
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "isGpsEnabled: ACCESS_FINE_LOCATION PERMISSION_GRANTED");
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                return true;
            }else{
                return false;
            }
        }else{
            Log.e(TAG, "isGpsEnabled: ACCESS_FINE_LOCATION PERMISSION_DENIED");
        }
        return false;
    }
}
