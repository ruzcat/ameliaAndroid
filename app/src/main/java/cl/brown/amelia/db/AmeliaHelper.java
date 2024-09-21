package cl.brown.amelia.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import cl.brown.amelia.model.Device;
import cl.brown.amelia.model.DeviceSchema;

public class AmeliaHelper extends SQLiteOpenHelper {
    private static final String TAG = AmeliaHelper.class.getName();
    private static AmeliaHelper sInstance;
    public static final int DATABASE_VERSION = 1; /* V1:devices config */
    public static final String DATABASE_NAME = "amelia.db";

    public static synchronized AmeliaHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // it guarantees that only one database helper will exist
        // across the entire application’s lifecycle.
        if (sInstance == null) {
            sInstance = new AmeliaHelper(context.getApplicationContext());
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
    private AmeliaHelper(Context context) {
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

            if(existDeviceBySerial(device)){
                return sqLiteDatabase.update(
                        DeviceSchema.DeviceEntry.TABLE_NAME,
                        device.toContentValues(),
                        "serial=?",
                        new String[]{device.getSERIAL()});
            }else{
                return sqLiteDatabase.insert(
                        DeviceSchema.DeviceEntry.TABLE_NAME,
                        null,
                        device.toContentValues());
            }

        } catch (SQLiteConstraintException sqLiteConstraintException){
            Log.e(TAG, "insertDevice: SQLiteConstraintException ", sqLiteConstraintException);
            return 0;
        } catch (SQLiteException sqLiteException){
            Log.e(TAG, "insertDevice SQLiteException ", sqLiteException);
            return 0;
        }
    }
    public boolean deleteDeviceBySerial(Device device) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        try {
            return sqLiteDatabase.delete(
                    DeviceSchema.DeviceEntry.TABLE_NAME,
                    DeviceSchema.DeviceEntry.SERIAL+"=?",
                    new String[]{device.getSERIAL()}) > 0;
        } catch (SQLiteConstraintException sqLiteConstraintException){
            Log.e(TAG, "deleteDevice: SQLiteConstraintException ", sqLiteConstraintException);
            return false;
        } catch (SQLiteException sqLiteException){
            Log.e(TAG, "deleteDevice SQLiteException ", sqLiteException);
            return false;
        }
    }
    public boolean existDeviceBySerial(Device device) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        String where = " serial = " + device.getSERIAL();
        try (Cursor cursor = sqLiteDatabase.query(DeviceSchema.DeviceEntry.TABLE_NAME, null, where, null, null, null, null, "1"))
        {
            return !cursor.moveToFirst();
        } catch (SQLiteConstraintException sqLiteConstraintException){
            Log.e(TAG, "deleteDevice: SQLiteConstraintException ", sqLiteConstraintException);
            return false;
        } catch (SQLiteException sqLiteException){
            Log.e(TAG, "deleteDevice SQLiteException ", sqLiteException);
            return false;
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
