package edu.uga.cs.statequizproject;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SplashFragment extends Fragment {

    public SplashFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_splash, container, false);

        TextView tv = v.findViewById(R.id.tvDesc);
        ProgressBar pb = v.findViewById(R.id.progress);
        Button start = v.findViewById(R.id.btnStart);
        Button history = v.findViewById(R.id.btnHistory);

        // Seed DB
        new SeedDbTask(pb, requireContext()).execute();

        start.setOnClickListener(b -> {
            new CreateQuizTask(requireContext(), id -> {
                if (!isAdded()) return;
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, QuizFragment.newInstance(id))
                        .addToBackStack(null)
                        .commit();
            }).execute(6);
        });

        history.setOnClickListener(b -> {
            if (!isAdded()) return;
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HistoryFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return v;
    }

    private static class SeedDbTask extends AsyncTask<Void, Void, Void> {
        private final ProgressBar pb;
        private final Context appCtx;
        SeedDbTask(ProgressBar pb, Context ctx) {
            this.pb = pb;
            this.appCtx = ctx.getApplicationContext();
        }

        @Override protected void onPreExecute() {
            if (pb != null) pb.setVisibility(View.VISIBLE);
        }

        @Override protected Void doInBackground(Void... voids) {
            StateQuestionData repo = new StateQuestionData(appCtx);
            repo.open();
            repo.ensureSeededFromCsvIfEmpty();
            repo.close();
            return null;
        }

        @Override protected void onPostExecute(Void v) {
            if (pb != null) pb.setVisibility(View.GONE);
        }
    }

    private static class CreateQuizTask extends AsyncTask<Integer, Void, Long> {
        interface OnCreated { void accept(long quizId); }

        private final Context appCtx;
        private final OnCreated onCreated;

        CreateQuizTask(Context ctx, OnCreated onCreated) {
            this.appCtx = ctx.getApplicationContext();
            this.onCreated = onCreated;
        }

        @Override protected Long doInBackground(Integer... params) {
            int n = (params != null && params.length > 0) ? params[0] : 6;
            StateQuestionData repo = new StateQuestionData(appCtx);
            repo.open();
            repo.ensureSeededFromCsvIfEmpty();
            long quizId = repo.createNewQuiz(n);
            repo.close();
            return quizId;
        }

        @Override protected void onPostExecute(Long quizId) {
            if (quizId != null && quizId > 0 && onCreated != null) {
                onCreated.accept(quizId);
            }
        }
    }
}

