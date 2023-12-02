package cl.brown.amelia.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import cl.brown.amelia.model.Device;
import cl.brown.amelia.model.DeviceSchema;
import cl.brown.amelia.ui.device.ConfigDeviceActivity;

public class AmeliaDBHelper extends SQLiteOpenHelper {
    private static final String TAG = AmeliaDBHelper.class.getName();
    private static AmeliaDBHelper sInstance;
    public static final int DATABASE_VERSION = 1; /* V1:devices config */
    public static final String DATABASE_NAME = "amelia.db";

    public static synchronized AmeliaDBHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // it guarantees that only one database helper will exist
        // across the entire application’s lifecycle.
        if (sInstance == null) {
            sInstance = new AmeliaDBHelper(context.getApplicationContext());
            Log.i(TAG,"sInstance new");
        }else{
            Log.i(TAG,"sInstance allready");
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static factory method "getInstance()" instead.
     */
    private AmeliaDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create table...
        if(!checkForTableExists(db, DeviceSchema.DeviceEntry.TABLE_NAME)) {
            db.execSQL("CREATE TABLE " + DeviceSchema.DeviceEntry.TABLE_NAME + " ("
                    + DeviceSchema.DeviceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + DeviceSchema.DeviceEntry.SERIAL + " TEXT NOT NULL,"
                    + DeviceSchema.DeviceEntry.IP + " TEXT ,"
                    + DeviceSchema.DeviceEntry.AP_IP + " TEXT ,"
                    + DeviceSchema.DeviceEntry.PORT + " TEXT NOT NULL,"
                    + DeviceSchema.DeviceEntry.CONFIG + " TEXT NOT NULL,"
                    + "UNIQUE (" + DeviceSchema.DeviceEntry.SERIAL + "))");

            Log.i(TAG, "onCreate create table '" + DeviceSchema.DeviceEntry.TABLE_NAME + "' on "+ DATABASE_NAME);

        }else{
            Log.i(TAG, "onCreate table '" + DeviceSchema.DeviceEntry.TABLE_NAME + "' allReady exist ");
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private boolean checkForTableExists(SQLiteDatabase db, String table){
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='"+table+"'";
        Cursor mCursor = db.rawQuery(sql, null);
        if (mCursor.getCount() > 0) {
            return true;
        }
        mCursor.close();
        return false;
    }

    public long insertDevice(Device device) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        try {
            return sqLiteDatabase.insert(
                    DeviceSchema.DeviceEntry.TABLE_NAME,
                    null,
                    device.toContentValues());
        } catch (SQLiteConstraintException sqLiteConstraintException){
            Log.e(TAG, "insertDevice: SQLiteConstraintException ", sqLiteConstraintException);
            return 0;
        } catch (SQLiteException sqLiteException){
            Log.e(TAG, "insertDevice SQLiteException ", sqLiteException);
            return 0;
        }
    }

    public Cursor getAllDevice() {
        try {
            return getReadableDatabase()
                    .query(
                            DeviceSchema.DeviceEntry.TABLE_NAME,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null);
        } catch (SQLiteConstraintException exception){
            Log.e("", "getAllDevice:SQLiteConstraintException ", exception);
            return null;
        } catch (SQLiteException exception){
            Log.e("", "getAllPassenger: SQLiteException ", exception);
            return null;
        }
    }

}
