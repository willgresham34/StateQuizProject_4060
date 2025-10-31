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

    private final Context context;
    private SQLiteDatabase db;
    private SQLiteOpenHelper stateQuestionDBHelper;

    public StateQuestionData(Context context) {
        this.context = context.getApplicationContext();
        stateQuestionDBHelper = StateQuestionDBHelper.getInstance(this.context);
    }

    public void open() {
        db = stateQuestionDBHelper.getWritableDatabase();
    }

    public void close() {
    }

    public boolean isDbOpen() {
        return db != null && db.isOpen();
    }

    // load a quiz and its questions
    public QuizDto loadQuizDto(long quizId) {
        QuizDto dto = new QuizDto(quizId);

        // load quiz header
        try (Cursor qc = db.query(
                StateQuestionDBHelper.TABLE_QUIZ,
                null,
                StateQuestionDBHelper.QUIZ_COLUMN_ID + "=?",
                new String[]{String.valueOf(quizId)},
                null, null, null
        )) {

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

        // load questions for this quiz
        String sql =
                "SELECT " +
                        "qq." + StateQuestionDBHelper.QQ_COLUMN_QUESTION + " AS question_id, " +
                        "qq." + StateQuestionDBHelper.QQ_COLUMN_ANSWER + " AS answer, " +
                        "sq." + StateQuestionDBHelper.STATEQUESTIONS_COLUMN_STATE + " AS state, " +
                        "sq." + StateQuestionDBHelper.STATEQUESTIONS_COLUMN_CAPITAL + " AS capital, " +
                        "sq." + StateQuestionDBHelper.STATEQUESTIONS_COLUMN_SECOND_CITY + " AS second_city, " +
                        "sq." + StateQuestionDBHelper.STATEQUESTIONS_COLUMN_THIRD_CITY + " AS third_city " +
                        "FROM " + StateQuestionDBHelper.TABLE_QQ + " qq " +
                        "JOIN " + StateQuestionDBHelper.TABLE_STATEQUESTIONS + " sq " +
                        "  ON qq." + StateQuestionDBHelper.QQ_COLUMN_QUESTION + " = sq." + StateQuestionDBHelper.STATEQUESTIONS_COLUMN_ID + " " +
                        "WHERE qq." + StateQuestionDBHelper.QQ_COLUMN_QUIZ + " = ?";

        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(quizId)})) {
            while (c.moveToNext()) {
                long questionRowId = c.getLong(
                        c.getColumnIndexOrThrow("question_id"));
                String state = c.getString(
                        c.getColumnIndexOrThrow("state"));
                String capital = c.getString(
                        c.getColumnIndexOrThrow("capital"));
                String second = c.getString(
                        c.getColumnIndexOrThrow("second_city"));
                String third = c.getString(
                        c.getColumnIndexOrThrow("third_city"));
                String answer = c.getString(
                        c.getColumnIndexOrThrow("answer"));

                QuizQuestion qq = new QuizQuestion(
                        questionRowId,
                        state,
                        capital,
                        second,
                        third,
                        quizId,
                        answer
                );
                dto.addQuestion(qq);
            }
        }

        dto.recomputeCounters();
        return dto;
    }

    // store quiz
    public QuizDto storeQuiz(QuizDto dto) {
        if (!isDbOpen()) open();

        long quizId = dto.getQuizId();

        ContentValues values = new ContentValues();
        String d = dto.getQuizDate();
        if (d == null || d.trim().isEmpty()) {
            values.putNull(StateQuestionDBHelper.QUIZ_COLUMN_DATE);
        } else {
            values.put(StateQuestionDBHelper.QUIZ_COLUMN_DATE, d);
        }
        values.put(StateQuestionDBHelper.QUIZ_COLUMN_ANSWERED_COUNT, dto.getAnsweredCount());
        values.put(StateQuestionDBHelper.QUIZ_COlUMN_CORRECT_COUNT, dto.getCorrectCount());

        db.update(
                StateQuestionDBHelper.TABLE_QUIZ,
                values,
                StateQuestionDBHelper.QUIZ_COLUMN_ID + "=?",
                new String[]{ String.valueOf(quizId) }
        );

        for (QuizQuestion item : dto.getQuestions()) {
            ContentValues val = new ContentValues();
            if (item.getUserAnswer() != null) {
                val.put(StateQuestionDBHelper.QQ_COLUMN_ANSWER, item.getUserAnswer());
            } else {
                val.putNull(StateQuestionDBHelper.QQ_COLUMN_ANSWER);
            }

            db.update(
                    StateQuestionDBHelper.TABLE_QQ,
                    val,
                    StateQuestionDBHelper.QQ_COLUMN_QUIZ + "=? AND " +
                            StateQuestionDBHelper.QQ_COLUMN_QUESTION + "=?",
                    new String[]{ String.valueOf(quizId), String.valueOf(item.getId()) }
            );
        }

        Log.d(DEBUG_TAG, "Updated quiz with id: " + quizId + " and " + dto.getQuestions().size() + " questions");

        return dto;
    }

    // make sure db is seeded
    public void ensureSeededFromCsvIfEmpty() {
        // make sure db is open
        if (!isDbOpen()) {
            open();
        }
        long count = DatabaseUtils.queryNumEntries(db, StateQuestionDBHelper.TABLE_STATEQUESTIONS);
        if (count >= 50) {
            return;
        }
        try {
            InputStream is = context.getAssets().open("state_capitals.csv");
            CSVReader reader = new CSVReader(new InputStreamReader(is));
            String[] row;
            boolean header = true;
            while ((row = reader.readNext()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                ContentValues values = new ContentValues();
                values.put(StateQuestionDBHelper.STATEQUESTIONS_COLUMN_STATE, row[0]);
                values.put(StateQuestionDBHelper.STATEQUESTIONS_COLUMN_CAPITAL, row[1]);
                values.put(StateQuestionDBHelper.STATEQUESTIONS_COLUMN_SECOND_CITY, row[2]);
                values.put(StateQuestionDBHelper.STATEQUESTIONS_COLUMN_THIRD_CITY, row[3]);
                db.insert(StateQuestionDBHelper.TABLE_STATEQUESTIONS, null, values);
            }
        } catch (Exception e) {
            Log.e(DEBUG_TAG, "Seeding failed: " + e);
        }
    }

    // create a new quiz with n random states
    public long createNewQuiz(int n) {
        if (!isDbOpen()) {
            open();
        }

        // insert quiz header
        ContentValues qv = new ContentValues();
        qv.put(StateQuestionDBHelper.QUIZ_COlUMN_CORRECT_COUNT, 0);
        qv.put(StateQuestionDBHelper.QUIZ_COLUMN_ANSWERED_COUNT, 0);
        qv.putNull(StateQuestionDBHelper.QUIZ_COLUMN_DATE);

        long quizId = db.insert(StateQuestionDBHelper.TABLE_QUIZ, null, qv);
        if (quizId == -1) {
            Log.e(DEBUG_TAG, "createNewQuiz: insert into quiz failed");
            return -1;
        }

        // pick n random states
        Cursor c = db.rawQuery(
                "SELECT " +
                        StateQuestionDBHelper.STATEQUESTIONS_COLUMN_ID +
                        " FROM " + StateQuestionDBHelper.TABLE_STATEQUESTIONS +
                        " ORDER BY RANDOM() LIMIT ?",
                new String[]{ String.valueOf(n) }
        );

        while (c.moveToNext()) {
            long questionRowId = c.getLong(0);

            ContentValues v = new ContentValues();
            v.put(StateQuestionDBHelper.QQ_COLUMN_QUIZ, quizId);
            v.put(StateQuestionDBHelper.QQ_COLUMN_QUESTION, questionRowId);
            v.putNull(StateQuestionDBHelper.QQ_COLUMN_ANSWER);

            long res = db.insert(StateQuestionDBHelper.TABLE_QQ, null, v);
            if (res == -1) {
                Log.e(DEBUG_TAG, "createNewQuiz: insert into quiz_questions failed for quizId="
                        + quizId + " questionId=" + questionRowId);
            }
        }
        c.close();

        return quizId;
    }


    // load all quizzes newest to oldest for HistoryFragment
    public List<Quiz> loadAllQuizzesDesc() {
        if (!isDbOpen()) {
            open();
        }
        List<Quiz> out = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT " +
                        StateQuestionDBHelper.QUIZ_COLUMN_ID + "," +
                        StateQuestionDBHelper.QUIZ_COLUMN_DATE + "," +
                        StateQuestionDBHelper.QUIZ_COlUMN_CORRECT_COUNT + "," +
                        StateQuestionDBHelper.QUIZ_COLUMN_ANSWERED_COUNT +
                        " FROM " + StateQuestionDBHelper.TABLE_QUIZ +
                        " ORDER BY " + StateQuestionDBHelper.QUIZ_COLUMN_DATE + " DESC",
                null
        );
        while (c.moveToNext()) {
            long id = c.getLong(0);
            String date = c.getString(1);
            int correct = c.getInt(2);
            int answered = c.getInt(3);
            out.add(new Quiz(id, date, correct, answered));
        }
        c.close();
        return out;
    }
}
