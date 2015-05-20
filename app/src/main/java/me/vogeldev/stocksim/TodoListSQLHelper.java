package me.vogeldev.stocksim;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.widget.EditText;

public class TodoListSQLHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "me.vogeldev.stocksim.db";

    public static final String TABLE_SHARES = "SHARES";
    public static final String TABLE_PLAYER = "PLAYER";

    public static final String SHARES_NAME = "NAME";
    public static final String SHARES_SYMBOL = "SYMBOL";
    public static final String SHARES_CURRENT = "CURRENT";
    public static final String SHARES_LASTCHECK = "LASTCHECK";
    public static final String SHARES_COUNT = "SHARES";
    public static final String SHARES_COST = "COST";
    public static final String PLAYER_NAME = "NAME";
    public static final String PLAYER_MONEY = "MONEY";

    public static final String _ID = BaseColumns._ID;

    public TodoListSQLHelper(Context context) {
        super(context, DB_NAME, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase sqlDB) {
        String createSharesTable = "CREATE TABLE " + TABLE_SHARES + " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SHARES_NAME + " TEXT, " + SHARES_SYMBOL + " TEXT, " + SHARES_CURRENT + " NUMBER, " + SHARES_LASTCHECK + " TEXT, " + SHARES_COUNT + " INTEGER, " + SHARES_COST + " NUMBER)";

        String createPlayerTable = "CREATE TABLE " + TABLE_PLAYER + " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " + PLAYER_NAME + " TEXT, " + PLAYER_MONEY + " NUMBER)";

        sqlDB.execSQL(createSharesTable);
        sqlDB.execSQL(createPlayerTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqlDB, int i, int i2) {
        sqlDB.execSQL("DROP TABLE IF EXISTS " + TABLE_SHARES);
        sqlDB.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYER);
        onCreate(sqlDB);
    }
}
