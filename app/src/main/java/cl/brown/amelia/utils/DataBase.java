package cl.brown.amelia.utils;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cl.brown.amelia.db.AmeliaHelper;
import cl.brown.amelia.model.Device;

public class DataBase {
    private static final String TAG = DataBase.class.getName();
    private static AmeliaHelper dbHelper;

    public static boolean initBD(Context context){
        dbHelper = AmeliaHelper.getInstance(context);
        if (dbHelper != null) {
            // Hacer las operaciones que queramos sobre la base de datos
            dbHelper.getWritableDatabase();
            Log.w(TAG,"initBD true "+dbHelper);
            return true;
        }else {
            Log.w(TAG,"initBD false");
            return false;
        }
    }

    public static boolean saveDeviceInfo(Device device){
        if (dbHelper != null) {
            long regId = dbHelper.insertDevice(device);
            Log.w(TAG, "saveDeviceInfo regId " + regId);
            if (regId > 0)
                return true;
            else
                return false;
        }else {
            Log.e(TAG, "saveDeviceInfo dbHelper null");
            return false;
        }
    }

    public static boolean deleteDeviceBySerial(Device device){
        if (dbHelper != null) {
            boolean delete  = dbHelper.deleteDeviceBySerial(device);
            Log.e(TAG, "deleteDevice "+delete);
            return delete;
        }else {
            Log.e(TAG, "deleteDevice dbHelper null");
            return false;
        }
    }

    public static List<Device> getAllDevices(){
        if (dbHelper != null) {
            Cursor regs = dbHelper.getAllDevice();
            if (regs != null) {
                if (regs.getCount() == 0) {
                    return Collections.emptyList();
                }else {
                    List<Device> devices = new ArrayList<>();
                    if (regs.moveToFirst()) {
                        do {
                            // on below line we are adding the data from
                            // cursor to our array list.
                            devices.add(new Device(
                                    regs.getString(1),
                                    regs.getString(2),
                                    regs.getString(3),
                                    regs.getString(4),
                                    regs.getString(5)
                            ));
                        } while (regs.moveToNext());
                        // moving our cursor to next.
                    }
                    regs.close();
                    return devices;
                }
            } else {
                return Collections.emptyList();
            }
        } else {
            Log.e("getPassengerInfo", "dbHelper null");
            return Collections.emptyList();
        }

    }

}
