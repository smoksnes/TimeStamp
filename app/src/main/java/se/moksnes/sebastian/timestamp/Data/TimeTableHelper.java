package se.moksnes.sebastian.timestamp.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sebas on 2016-11-03.
 */

public class TimeTableHelper extends SQLiteOpenHelper {

    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TimeTableContract.TableEntry.TABLE_NAME + " (" +
                    TimeTableContract.TableEntry._ID + " INTEGER PRIMARY KEY," +
                    TimeTableContract.TableEntry.COLUMN_NAME_In + INTEGER_TYPE + COMMA_SEP +
                    TimeTableContract.TableEntry.COLUMN_NAME_Out + INTEGER_TYPE + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TimeTableContract.TableEntry.TABLE_NAME;

        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 2;
        public static final String DATABASE_NAME = "TimeTable.db";

        public TimeTableHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }

        public void dropDb(Context context){
            context.deleteDatabase(DATABASE_NAME);
        }
}
