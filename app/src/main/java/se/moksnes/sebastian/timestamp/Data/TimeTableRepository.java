package se.moksnes.sebastian.timestamp.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import se.moksnes.sebastian.timestamp.Models.TimeStamp;

/**
 * Created by sebas on 2016-11-03.
 */

public class TimeTableRepository {

    private Context mContext;

    public TimeTableRepository(Context context){

        mContext = context;
    }

    public TimeStamp[] getDay(){
        TimeTableHelper mDbHelper = new TimeTableHelper(mContext);

        //mDbHelper.dropDb(context);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {
                TimeTableContract.TableEntry.COLUMN_NAME_In,
                TimeTableContract.TableEntry.COLUMN_NAME_Out,
              //  "strftime('%Y-%m', Time / 1000, 'unixepoch')"
             //   "date(" + TimeTableContract.TableEntry.COLUMN_NAME_Time + "/1000, 'unixepoch')",
               // "date('now')"
        };



        String whereClause = "date(" + TimeTableContract.TableEntry.COLUMN_NAME_In + "/1000, 'unixepoch')=date('now')";
        String [] whereArgs = {"date('now')"};

        String sortOrder =
                TimeTableContract.TableEntry.COLUMN_NAME_In + " DESC";

        Cursor c = db.query(
                TimeTableContract.TableEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                whereClause,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        List<TimeStamp> items = new ArrayList<TimeStamp>();

        if (c != null ) {
            if  (c.moveToFirst()) {
                do {
                    TimeStamp stamp = new TimeStamp();
                    stamp.in = c.getLong(0);
                    stamp.out = c.getLong(1);
                    items.add(stamp);
                }while (c.moveToNext());
            }
        }
        c.close();
        TimeStamp[] arr = new TimeStamp[ items.size() ];

        for( int j = 0; j < arr.length; j++ )
            arr[ j ] = items.get( j );
        return arr;
    }


    public Long stampOut(){
        Long ms = System.currentTimeMillis();


        Date myDate = new Date();
        long timeMilliseconds = myDate.getTime();

        Date date = new Date(ms);

        TimeTableHelper mDbHelper = new TimeTableHelper(mContext);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        //Cursor c = db.rawQuery("SELECT _id FROM ? WHERE ? IS NOT NULL ORDER BY _id DESC LIMIT 1;", new String[]{TimeTableContract.TableEntry.TABLE_NAME, TimeTableContract.TableEntry.COLUMN_NAME_Out});


        // Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                TimeTableContract.TableEntry._ID,
                TimeTableContract.TableEntry.COLUMN_NAME_Out,
        };

      //  String whereClause = "date(" + TimeTableContract.TableEntry.COLUMN_NAME_In + "/1000, 'unixepoch')=date('now')";
        //String [] whereArgs = {"date('now')"};


// How you want the results sorted in the resulting Cursor
        String sortOrder =
                TimeTableContract.TableEntry._ID + " DESC";
        Cursor c = db.query(
            TimeTableContract.TableEntry.TABLE_NAME,                     // The table to query
            projection,                               // The columns to return
            null,                                // The columns for the WHERE clause
            null,                            // The values for the WHERE clause
            null,                                     // don't group the rows
            null,
            sortOrder,                                     // don't filter by row groups
            "1");                                 // The sort order

        if(c == null || c.getCount() == 0){
            return null;
        }
        c.moveToFirst();
        if(!c.isNull(1)){
            // Already stamped out...
            return null;
        }
        int id = c.getInt(0);

// Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(TimeTableContract.TableEntry.COLUMN_NAME_Out, ms);

        String whereClause = "_id = ?";
        String [] whereArgs = {String.valueOf(id)};
        db = mDbHelper.getWritableDatabase();

// Insert the new row, returning the primary key value of the new row
        db.update(TimeTableContract.TableEntry.TABLE_NAME, values, whereClause, whereArgs);
        return ms;
    }

    public Long stampIn(){
        Long ms = System.currentTimeMillis();

        Date myDate = new Date();
        long timeMilliseconds = myDate.getTime();

        Date date = new Date(ms);

        TimeTableHelper mDbHelper = new TimeTableHelper(mContext);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();


// Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(TimeTableContract.TableEntry.COLUMN_NAME_In, ms);

// Insert the new row, returning the primary key value of the new row
        db.insert(TimeTableContract.TableEntry.TABLE_NAME, null, values);
        return ms;
    }

    public boolean isIn(){
        TimeTableHelper mDbHelper = new TimeTableHelper(mContext);

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
//TODO: Check if already in...

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                TimeTableContract.TableEntry.COLUMN_NAME_Out,
        };


// How you want the results sorted in the resulting Cursor
        String sortOrder =
                TimeTableContract.TableEntry.COLUMN_NAME_In + " DESC";

        Cursor c = db.query(
                TimeTableContract.TableEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
        c.moveToFirst();


        if(c!= null && c.getCount() > 0) {
            boolean isNull = c.isNull(0);
            return isNull;
        }
        return false;
    }
}
