package cl.brown.amelia.model;

import android.content.ContentValues;
import android.database.Cursor;

public class Device {
    public String SERIAL;
    public String IP;
    public String APIP;
    public String PORT;
    public String CONFIG;

    public Device(String SERIAL, String IP, String APIP, String PORT, String CONFIG) {
        this.SERIAL = SERIAL;
        this.IP = IP;
        this.APIP = APIP;
        this.PORT = PORT;
        this.CONFIG = CONFIG;
    }

    public Device(Cursor cursor) {
        cursor.moveToNext();
        this.SERIAL = cursor.getString(cursor.getColumnIndexOrThrow(DeviceSchema.DeviceEntry.SERIAL));
        this.IP = cursor.getString(cursor.getColumnIndexOrThrow(DeviceSchema.DeviceEntry.IP));
        this.APIP = cursor.getString(cursor.getColumnIndexOrThrow(DeviceSchema.DeviceEntry.AP_IP));
        this.PORT = cursor.getString(cursor.getColumnIndexOrThrow(DeviceSchema.DeviceEntry.PORT));
        this.CONFIG = cursor.getString(cursor.getColumnIndexOrThrow(DeviceSchema.DeviceEntry.CONFIG));
    }

    public String getSERIAL() {
        return SERIAL;
    }

    public String getIP() {
        return IP;
    }

    public String getAPIP() {
        return APIP;
    }

    public String getPORT() {
        return PORT;
    }

    public String getCONFIG() {
        return CONFIG;
    }

    /* Traducción de pares */
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(DeviceSchema.DeviceEntry.SERIAL, getSERIAL());
        values.put(DeviceSchema.DeviceEntry.IP, getIP());
        values.put(DeviceSchema.DeviceEntry.AP_IP, getAPIP());
        values.put(DeviceSchema.DeviceEntry.PORT, getPORT());
        values.put(DeviceSchema.DeviceEntry.CONFIG, getCONFIG());
        return values;
    }
}
