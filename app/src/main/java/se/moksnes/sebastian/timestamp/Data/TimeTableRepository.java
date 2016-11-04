package se.moksnes.sebastian.timestamp.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;

/**
 * Created by sebas on 2016-11-03.
 */

public class TimeTableRepository {

    public void insert(Context context, Boolean in){


        TimeTableHelper mDbHelper = new TimeTableHelper(context);

        //mDbHelper.dropDb(context);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int operation = 0;
        if(in){
            operation = 1;
        }

// Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(TimeTableContract.TableEntry.COLUMN_NAME_Time, System.currentTimeMillis());
        values.put(TimeTableContract.TableEntry.COLUMN_NAME_Operation, operation);

// Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(TimeTableContract.TableEntry.TABLE_NAME, null, values);

    }

    public boolean isIn(Context context){
        TimeTableHelper mDbHelper = new TimeTableHelper(context);

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                TimeTableContract.TableEntry.COLUMN_NAME_Operation,
        };


// How you want the results sorted in the resulting Cursor
        String sortOrder =
                TimeTableContract.TableEntry.COLUMN_NAME_Time + " DESC";

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
        return false;
        /*
        if(c!= null && c.getCount() > 0) {
            long itemId = c.getInt(0);
            return itemId == 1;
        }
        return false;*/
    }
}
