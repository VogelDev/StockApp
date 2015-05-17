package me.vogeldev.stocksim;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class TodoListSQLHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "chaos.list.db";
    public static final String TABLE_SHARES = "SHARES";
    public static final String SHARES_SYMBOL = "SYMBOL";
    public static final String SHARES_CURRENT = "CURRENT";
    public static final String SHARES_LASTCHECK = "LASTCHECK";
    public static final String SHARES_COUNT = "SHARES";
    public static final String SHARES_TOTALCOST = "TOTALCOST";
    public static final String _ID = BaseColumns._ID;

    public TodoListSQLHelper(Context context) {
        //1 is database version
        super(context, DB_NAME, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase sqlDB) {
        String createSharesTable = "CREATE TABLE " + TABLE_SHARES + " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SHARES_SYMBOL + " TEXT, " + SHARES_CURRENT + " NUMBER, " + SHARES_LASTCHECK + " TEXT, " + SHARES_COUNT + " INTEGER, " + SHARES_TOTALCOST + " NUMBER)";

        sqlDB.execSQL(createSharesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqlDB, int i, int i2) {
        sqlDB.execSQL("DROP TABLE IF EXISTS " + TABLE_SHARES);
        onCreate(sqlDB);
    }
}
