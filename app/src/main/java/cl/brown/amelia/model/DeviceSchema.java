package cl.brown.amelia.model;

import android.provider.BaseColumns;

public class DeviceSchema {
    public static abstract class DeviceEntry implements BaseColumns {
        public static final String TABLE_NAME ="devices";
        public static final String SERIAL = "serial";
        public static final String IP = "ip";
        public static final String AP_IP = "ap_ip";
        public static final String PORT = "port";
        public static final String CONFIG = "config";
    }
}
