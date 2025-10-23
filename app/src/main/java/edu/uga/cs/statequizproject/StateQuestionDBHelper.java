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

    // Tables
    public static final String TABLE_STATEQUESTIONS = "statequestions";
    public static final String TABLE_QUIZ = "quiz";
    public static final String  TABLE_QQ = "qq";

    // Columns for state questions
    public static final String STATEQUESTIONS_COLUMN_ID            = "_id";
    public static final String STATEQUESTIONS_COLUMN_STATE         = "state";
    public static final String STATEQUESTIONS_COLUMN_CAPITAL       = "capital";
    public static final String STATEQUESTIONS_COLUMN_SECOND_CITY   = "second_city";
    public static final String STATEQUESTIONS_COLUMN_THIRD_CITY    = "third_city";

    // Columns for quiz
    public static final String QUIZ_COLUMN_ID = "_id";
    public static final String QUIZ_COLUMN_DATE = "date";
    public static final String QUIZ_COlUMN_CORRECT_COUNT = "correct_count";
    public static final String QUIZ_COLUMN_ANSWERED_COUNT = "answered_count";

    //columns for qq relation
    public static final String QQ_COLUMN_QUIZ = "quiz_id";
    public static final String QQ_COLUMN_QUESTION = "question_id";

    private static StateQuestionDBHelper helperInstance;

    // Create table state questions
    private static final String CREATE_STATEQUESTIONS =
            "CREATE TABLE " + TABLE_STATEQUESTIONS + " (" +
                    STATEQUESTIONS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    STATEQUESTIONS_COLUMN_STATE + " TEXT NOT NULL, " +
                    STATEQUESTIONS_COLUMN_CAPITAL + " TEXT NOT NULL, " +
                    STATEQUESTIONS_COLUMN_SECOND_CITY + " TEXT, " +
                    STATEQUESTIONS_COLUMN_THIRD_CITY + " TEXT" +
                    ");";

    private static final String CREATE_QUIZ =
            "CREATE TABLE " + TABLE_QUIZ + " (" +
                    QUIZ_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    QUIZ_COLUMN_DATE + " TEXT NOT NULL, " +
                    QUIZ_COlUMN_CORRECT_COUNT + "INTEGER NOT NULL, " +
                    QUIZ_COLUMN_ANSWERED_COUNT + " INTEGER NOT NULL" +
                    ");";

    private static final String CREATE_QQ = "CREATE TABLE " + TABLE_QQ + " (" +
            QQ_COLUMN_QUIZ + " INTEGER NOT NULL, " +
            QQ_COLUMN_QUESTION + " INTEGER NOT NULL, " +
            "FOREIGN KEY(" + QQ_COLUMN_QUIZ + ") REFERENCES " + TABLE_QUIZ + "(" + QUIZ_COLUMN_ID + ")," +
            "FOREIGN KEY(" + QQ_COLUMN_QUESTION + ") REFERENCES " + TABLE_STATEQUESTIONS + "(" + STATEQUESTIONS_COLUMN_ID + ")" +
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
        db.execSQL(CREATE_QUIZ);
        db.execSQL(CREATE_QQ);
        Log.d( DEBUG_TAG, "Table " + TABLE_STATEQUESTIONS + " created" );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL( "drop table if exists " + TABLE_QQ);
        db.execSQL( "drop table if exists " + TABLE_QUIZ);
        db.execSQL( "drop table if exists " + TABLE_STATEQUESTIONS);
        onCreate(db);
        Log.d( DEBUG_TAG, "Table " + TABLE_STATEQUESTIONS + "upgraded" );
    }

}
