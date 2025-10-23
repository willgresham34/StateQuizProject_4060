package edu.uga.cs.statequizproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class StateQuestionDBHelper extends SQLiteOpenHelper {

    private static final String DEBUG_TAG = "StateQuestionDBHelper";

    private static final String DB_NAME = "statequestion.db";
    private static final int DB_VERSION = 1;

    // Table
    public static final String TABLE_STATEQUESTIONS = "statequestions";

    // Columns
    public static final String STATEQUESTIONS_COLUMN_ID            = "_id";
    public static final String STATEQUESTIONS_COLUMN_STATE         = "state";
    public static final String STATEQUESTIONS_COLUMN_CAPITAL       = "capital";
    public static final String STATEQUESTIONS_COLUMN_SECOND_CITY   = "second_city";
    public static final String STATEQUESTIONS_COLUMN_THIRD_CITY    = "third_city";

    private static StateQuestionDBHelper helperInstance;

    // Create table SQL
    private static final String CREATE_STATEQUESTIONS =
            "CREATE TABLE " + TABLE_STATEQUESTIONS + " (" +
                    STATEQUESTIONS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    STATEQUESTIONS_COLUMN_STATE + " TEXT NOT NULL, " +
                    STATEQUESTIONS_COLUMN_CAPITAL + " TEXT NOT NULL, " +
                    STATEQUESTIONS_COLUMN_SECOND_CITY + " TEXT, " +
                    STATEQUESTIONS_COLUMN_THIRD_CITY + " TEXT" +
                    ");";

    private StateQuestionDBHelper(Context context) {super(context, DB_NAME, null, DB_VERSION);}

    public synchronized static StateQuestionDBHelper getInstance(Context context) {
        if(helperInstance == null){
            helperInstance = new StateQuestionDBHelper((context.getApplicationContext()));
        }
        return helperInstance;
    }

    @Override public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_STATEQUESTIONS);
        Log.d( DEBUG_TAG, "Table " + TABLE_STATEQUESTIONS + " created" );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL( "drop table if exists " + TABLE_STATEQUESTIONS);
        onCreate(db);
        Log.d( DEBUG_TAG, "Table " + TABLE_STATEQUESTIONS + "upgraded" );
    }

}
