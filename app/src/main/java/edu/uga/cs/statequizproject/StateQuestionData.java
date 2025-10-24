package edu.uga.cs.statequizproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.opencsv.CSVReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class StateQuestionData {
    public static final String DEBUG_TAG = "StateQuestionData";
    private SQLiteDatabase db;
    private SQLiteOpenHelper stateQuestionDBHelper;

    public StateQuestionData(Context context) {
       stateQuestionDBHelper = StateQuestionDBHelper.getInstance(context);
    }

    public void open() {
        db = stateQuestionDBHelper.getWritableDatabase();
    }

    public void close() {
        if(stateQuestionDBHelper != null) {
            stateQuestionDBHelper.close();
        }
    }

    public boolean isDbOpen() {return db.isOpen();}


    // retrieve quiz results
    public List<Quiz> retrieveAllQuizzes() {
        ArrayList<Quiz> quizzes = new ArrayList<Quiz>();
        Cursor cursor = null;
        int columnIndex;

        try {
            cursor = db.query( StateQuestionDBHelper.TABLE_QUIZ, null, null , null , null ,null , null);

            if(cursor != null && cursor.getCount() > 0 ) {

                while(cursor.moveToNext()) {

                    columnIndex = cursor.getColumnIndex( StateQuestionDBHelper.QUIZ_COLUMN_ID);
                    long id = cursor.getLong( columnIndex );
                    columnIndex = cursor.getColumnIndex( StateQuestionDBHelper.QUIZ_COLUMN_DATE );
                    String date = cursor.getString( columnIndex );
                    columnIndex = cursor.getColumnIndex( StateQuestionDBHelper.QUIZ_COlUMN_CORRECT_COUNT );
                    int correct = cursor.getInt( columnIndex );
                    columnIndex = cursor.getColumnIndex( StateQuestionDBHelper.QUIZ_COLUMN_ANSWERED_COUNT );
                    int answered = cursor.getInt( columnIndex );

                    Quiz q = new Quiz(id, date, correct, answered);
                    quizzes.add(q);
                }
            }

            if( cursor != null )
                Log.d( DEBUG_TAG, "Number of records from DB: " + cursor.getCount() );
            else
                Log.d( DEBUG_TAG, "Number of records from DB: 0" );
        }
        catch (Exception e) {
            Log.d( DEBUG_TAG, "Retrieve Quiz Exception: " + e);
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return quizzes;
    }

    public QuizDto loadQuizDto(long quizId) {
        QuizDto dto = new QuizDto(quizId);

        try (Cursor qc = db.query(
                StateQuestionDBHelper.TABLE_QUIZ,
                null,
                StateQuestionDBHelper.QUIZ_COLUMN_ID + "=?",
                new String[]{ String.valueOf(quizId) },
                null, null, null)) {
            if (qc.moveToFirst()) {
                String date = qc.getString(
                        qc.getColumnIndexOrThrow(StateQuestionDBHelper.QUIZ_COLUMN_DATE));
                int ans = qc.getInt(
                        qc.getColumnIndexOrThrow(StateQuestionDBHelper.QUIZ_COLUMN_ANSWERED_COUNT));
                int cor = qc.getInt(
                        qc.getColumnIndexOrThrow(StateQuestionDBHelper.QUIZ_COlUMN_CORRECT_COUNT));

                dto.setQuizDate(date);
                dto.setAnswserCount(ans);
                dto.setCorrectCount(cor);
            }
        }

        // join tables to get list of quiz questions
        String sql =
                "SELECT " +
                        "  qq." + StateQuestionDBHelper.QQ_COLUMN_QUESTION + " AS question_id, " +
                        "  qq." + StateQuestionDBHelper.QQ_COLUMN_ANSWER + " AS answer, " +
                        "  sq." + StateQuestionDBHelper.STATEQUESTIONS_COLUMN_STATE + " AS state, " +
                        "  sq." + StateQuestionDBHelper.STATEQUESTIONS_COLUMN_CAPITAL + " AS capital, " +
                        "  sq." + StateQuestionDBHelper.STATEQUESTIONS_COLUMN_SECOND_CITY + " AS second_city, " +
                        "  sq." + StateQuestionDBHelper.STATEQUESTIONS_COLUMN_THIRD_CITY + " AS third_city " +
                        "FROM " + StateQuestionDBHelper.TABLE_QQ + " qq " +
                        "JOIN " + StateQuestionDBHelper.TABLE_STATEQUESTIONS + " sq " +
                        "  ON qq." + StateQuestionDBHelper.QQ_COLUMN_QUESTION + " = sq." + StateQuestionDBHelper.STATEQUESTIONS_COLUMN_ID + " " +
                        "WHERE qq." + StateQuestionDBHelper.QQ_COLUMN_QUIZ + " = ?";

        try (Cursor c = db.rawQuery(sql, new String[]{ String.valueOf(quizId) })) {
            while (c.moveToNext()) {
                long questionRowId = c.getLong(
                        c.getColumnIndexOrThrow(StateQuestionDBHelper.QQ_COLUMN_QUESTION));
                String state = c.getString(
                        c.getColumnIndexOrThrow(StateQuestionDBHelper.STATEQUESTIONS_COLUMN_STATE));
                String capital = c.getString(
                        c.getColumnIndexOrThrow(StateQuestionDBHelper.STATEQUESTIONS_COLUMN_CAPITAL));
                String second = c.getString(
                        c.getColumnIndexOrThrow(StateQuestionDBHelper.STATEQUESTIONS_COLUMN_SECOND_CITY));
                String third = c.getString(
                        c.getColumnIndexOrThrow(StateQuestionDBHelper.STATEQUESTIONS_COLUMN_THIRD_CITY));
                String answer = c.getString(
                        c.getColumnIndexOrThrow(StateQuestionDBHelper.QQ_COLUMN_ANSWER));

                QuizQuestion qq = new QuizQuestion(
                        questionRowId, state, capital, second, third,
                        quizId, answer
                );
                dto.addQuestion(qq);
            }
        }

        dto.recomputeCounters();
        return dto;
    }


    // store new Quiz
    public QuizDto storeQuiz(QuizDto dto) {

        ContentValues values = new ContentValues();
        values.put(StateQuestionDBHelper.QUIZ_COLUMN_DATE, dto.getQuizDate());
        values.put(StateQuestionDBHelper.QUIZ_COLUMN_ANSWERED_COUNT, dto.getAnsweredCount());
        values.put(StateQuestionDBHelper.QUIZ_COlUMN_CORRECT_COUNT, dto.getCorrectCount());

        long quizId = db.insert(StateQuestionDBHelper.TABLE_QUIZ, null, values);

        dto.setQuizId(quizId);

        for (QuizQuestion item : dto.getQuestions()) {
            ContentValues val = new ContentValues();
            val.put(StateQuestionDBHelper.QQ_COLUMN_QUIZ, quizId);
            val.put(StateQuestionDBHelper.QQ_COLUMN_QUESTION, item.getId());
            if (item.getUserAnswer() != null) {
                val.put(StateQuestionDBHelper.QQ_COLUMN_ANSWER, item.getUserAnswer());
            } else {
                val.putNull(StateQuestionDBHelper.QQ_COLUMN_ANSWER);
            }
            db.insert(StateQuestionDBHelper.TABLE_QQ, null, val);
        }

        Log.d(DEBUG_TAG, "Stored quiz with id: " + quizId + " and " + dto.getQuestions().size() + " questions");

        return dto;
    }

    //updateQuiz


    //seed StateQuestion
    public void seedStateQuestions(Context context) {
        long count = DatabaseUtils.queryNumEntries(db, StateQuestionDBHelper.TABLE_STATEQUESTIONS);
        if(count > 0) return;

        try {
            InputStream in_s = context.getAssets().open("state_question.csv");
            CSVReader reader = new CSVReader(new InputStreamReader(in_s));
            reader.readNext(); // skip header
            String[] nextRow;
            while((nextRow = reader.readNext()) != null ) {

                ContentValues values = new ContentValues();

                values.put(StateQuestionDBHelper.STATEQUESTIONS_COLUMN_STATE, nextRow[0]);
                values.put(StateQuestionDBHelper.STATEQUESTIONS_COLUMN_CAPITAL, nextRow[1]);
                values.put(StateQuestionDBHelper.STATEQUESTIONS_COLUMN_SECOND_CITY, nextRow[2]);
                values.put(StateQuestionDBHelper.STATEQUESTIONS_COLUMN_THIRD_CITY, nextRow[3]);

                db.insert(StateQuestionDBHelper.TABLE_STATEQUESTIONS, null, values);

            }
        }
        catch (Exception e) {
            Log.e( DEBUG_TAG, e.toString() );
        }
    }
}
