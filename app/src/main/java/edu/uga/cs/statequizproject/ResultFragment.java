package edu.uga.cs.statequizproject;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ResultFragment extends Fragment {

    private static final String ARG_QUIZ_ID = "quiz_id";
    private long quizId;

    public static ResultFragment newInstance(long quizId) {
        ResultFragment f = new ResultFragment();
        Bundle b = new Bundle();
        b.putLong(ARG_QUIZ_ID, quizId);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        quizId = getArguments().getLong(ARG_QUIZ_ID);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_result, container, false);
        TextView tv = v.findViewById(R.id.tvScore);
        Button again = v.findViewById(R.id.btnAgain);
        Button history = v.findViewById(R.id.btnSeeHistory);

        Context appCtx = requireContext().getApplicationContext();
        new FinalizeQuizTask(appCtx, tv).execute(quizId);

        again.setOnClickListener(b -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new SplashFragment())
                    .commit();
        });

        history.setOnClickListener(b -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HistoryFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return v;
    }

    private class FinalizeQuizTask extends android.os.AsyncTask<Long, Void, QuizDto> {

        private final Context appContext;
        private final TextView scoreView;

        FinalizeQuizTask(Context appContext, TextView scoreView) {
            this.appContext = appContext;
            this.scoreView = scoreView;
        }

        @Override
        protected QuizDto doInBackground(Long... params) {
            long qid = params[0];
            StateQuestionData repo = new StateQuestionData(appContext);
            repo.open();

            QuizDto dto = repo.loadQuizDto(qid);
            dto.recomputeCounters();
            repo.finalizeQuizWithCounts(
                    qid,
                    dto.getCorrectCount(),
                    dto.getAnsweredCount()
            );
            repo.close();
            return dto;
        }

        @Override
        protected void onPostExecute(QuizDto dto) {
            if (!isAdded()) {
                return;
            }
            scoreView.setText(
                    getString(
                            R.string.your_score,
                            dto.getCorrectCount(),
                            dto.getQuestions().size()
                    )
            );
        }
    }
}
