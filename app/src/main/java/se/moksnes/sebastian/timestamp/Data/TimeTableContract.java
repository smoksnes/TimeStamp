package se.moksnes.sebastian.timestamp.Data;

import android.provider.BaseColumns;

/**
 * Created by sebas on 2016-11-03.
 */

public final class TimeTableContract {

        // To prevent someone from accidentally instantiating the contract class,
        // make the constructor private.
        private TimeTableContract() {}

        /* Inner class that defines the table contents */
        public static class TableEntry implements BaseColumns {
            public static final String TABLE_NAME = "timeTable";
            public static final String COLUMN_NAME_Time = "time";
            public static final String COLUMN_NAME_Operation = "operation";
        }
}
