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
        if (stateQuestionDBHelper != null) {
            stateQuestionDBHelper.close();
        }
    }

    public boolean isDbOpen() {
        return db != null && db.isOpen();
    }

    /* ------------------------------------------------------------------
       EXISTING METHODS FROM YOUR FILE
       ------------------------------------------------------------------ */

    // retrieve quiz results (old style, all quizzes, any order)
    public List<Quiz> retrieveAllQuizzes() {
        ArrayList<Quiz> quizzes = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = db.query(
                    StateQuestionDBHelper.TABLE_QUIZ,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.getCount() > 0) {

                while (cursor.moveToNext()) {

                    int columnIndex;

                    columnIndex = cursor.getColumnIndex(StateQuestionDBHelper.QUIZ_COLUMN_ID);
                    long id = cursor.getLong(columnIndex);

                    columnIndex = cursor.getColumnIndex(StateQuestionDBHelper.QUIZ_COLUMN_DATE);
                    String date = cursor.getString(columnIndex);

                    // NOTE: this is how your file spelled it:
                    columnIndex = cursor.getColumnIndex(StateQuestionDBHelper.QUIZ_COlUMN_CORRECT_COUNT);
                    int correct = cursor.getInt(columnIndex);

                    columnIndex = cursor.getColumnIndex(StateQuestionDBHelper.QUIZ_COLUMN_ANSWERED_COUNT);
                    int answered = cursor.getInt(columnIndex);

                    Quiz q = new Quiz(id, date, correct, answered);
                    quizzes.add(q);
                }
            }

            if (cursor != null)
                Log.d(DEBUG_TAG, "Number of records from DB: " + cursor.getCount());
            else
                Log.d(DEBUG_TAG, "Number of records from DB: 0");
        } catch (Exception e) {
            Log.d(DEBUG_TAG, "Retrieve Quiz Exception: " + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return quizzes;
    }

    // load a quiz + its questions
    public QuizDto loadQuizDto(long quizId) {
        QuizDto dto = new QuizDto(quizId);

        // 1) load quiz header
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

        // 2) load questions for this quiz
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

    // store new Quiz (your original method)
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

    // original seeder (I’ll leave it, but we’ll add a safer one below)
    public void seedStateQuestions(Context context) {
        long count = DatabaseUtils.queryNumEntries(db, StateQuestionDBHelper.TABLE_STATEQUESTIONS);
        if (count > 0) return;

        try {
            // your original name was "state_question.csv"
            // but your assets file is actually "state_capitals.csv"
            InputStream in_s = context.getAssets().open("state_capitals.csv");
            CSVReader reader = new CSVReader(new InputStreamReader(in_s));
            reader.readNext(); // skip header
            String[] nextRow;
            while ((nextRow = reader.readNext()) != null) {

                ContentValues values = new ContentValues();

                values.put(StateQuestionDBHelper.STATEQUESTIONS_COLUMN_STATE, nextRow[0]);
                values.put(StateQuestionDBHelper.STATEQUESTIONS_COLUMN_CAPITAL, nextRow[1]);
                values.put(StateQuestionDBHelper.STATEQUESTIONS_COLUMN_SECOND_CITY, nextRow[2]);
                values.put(StateQuestionDBHelper.STATEQUESTIONS_COLUMN_THIRD_CITY, nextRow[3]);

                db.insert(StateQuestionDBHelper.TABLE_STATEQUESTIONS, null, values);

            }
        } catch (Exception e) {
            Log.e(DEBUG_TAG, e.toString());
        }
    }

    /* ------------------------------------------------------------------
       NEW METHODS (fixed names)
       ------------------------------------------------------------------ */

    // 1) make sure DB is seeded
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

    // 2) create a new quiz with N random states
    public long createNewQuiz(int n) {
        // make sure DB is open
        if (!isDbOpen()) {
            open();
        }

        // 1. insert quiz header
        ContentValues qv = new ContentValues();
        // your table wants NON NULL date:
        qv.put(StateQuestionDBHelper.QUIZ_COLUMN_DATE,
                String.valueOf(System.currentTimeMillis()));
        // your column names from your helper:
        qv.put(StateQuestionDBHelper.QUIZ_COlUMN_CORRECT_COUNT, 0);
        qv.put(StateQuestionDBHelper.QUIZ_COLUMN_ANSWERED_COUNT, 0);

        long quizId = db.insert(StateQuestionDBHelper.TABLE_QUIZ, null, qv);
        if (quizId == -1) {
            Log.e(DEBUG_TAG, "createNewQuiz: insert into quiz failed");
            return -1;
        }

        // 2. pick N random states
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
            // IMPORTANT: use your column names
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


    // 3) save a single answer, then recompute quiz counters
    public void saveAnswer(long quizId, long questionRowId, String answer) {
        if (!isDbOpen()) {
            open();
        }

        ContentValues v = new ContentValues();
        v.put(StateQuestionDBHelper.QQ_COLUMN_ANSWER, answer);
        db.update(
                StateQuestionDBHelper.TABLE_QQ,
                v,
                StateQuestionDBHelper.QQ_COLUMN_QUIZ + "=? AND " +
                        StateQuestionDBHelper.QQ_COLUMN_QUESTION + "=?",
                new String[]{ String.valueOf(quizId), String.valueOf(questionRowId) }
        );

        // recompute
        QuizDto dto = loadQuizDto(quizId);
        dto.recomputeCounters();

        ContentValues qv = new ContentValues();
        qv.put(StateQuestionDBHelper.QUIZ_COlUMN_CORRECT_COUNT, dto.getCorrectCount());
        qv.put(StateQuestionDBHelper.QUIZ_COLUMN_ANSWERED_COUNT, dto.getAnsweredCount());
        db.update(
                StateQuestionDBHelper.TABLE_QUIZ,
                qv,
                StateQuestionDBHelper.QUIZ_COLUMN_ID + "=?",
                new String[]{ String.valueOf(quizId) }
        );
        // you can leave DB open if you reuse it, or:
        // close();
    }


    // 4) finalize quiz at the end
    public void finalizeQuiz(long quizId) {
        if (!isDbOpen()) {
            open();
        }

        QuizDto dto = loadQuizDto(quizId);
        dto.recomputeCounters();

        ContentValues qv = new ContentValues();
        qv.put(StateQuestionDBHelper.QUIZ_COlUMN_CORRECT_COUNT, dto.getCorrectCount());
        qv.put(StateQuestionDBHelper.QUIZ_COLUMN_ANSWERED_COUNT, dto.getAnsweredCount());
        qv.put(StateQuestionDBHelper.QUIZ_COLUMN_DATE, String.valueOf(System.currentTimeMillis()));

        db.update(
                StateQuestionDBHelper.TABLE_QUIZ,
                qv,
                StateQuestionDBHelper.QUIZ_COLUMN_ID + "=?",
                new String[]{String.valueOf(quizId)}
        );
    }

    // 5) load all quizzes newest → oldest (for HistoryFragment)
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
