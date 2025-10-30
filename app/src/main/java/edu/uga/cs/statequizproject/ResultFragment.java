
package edu.uga.cs.statequizproject;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_result, container, false);
        TextView tv = v.findViewById(R.id.tvScore);
        Button again = v.findViewById(R.id.btnAgain);
        Button history = v.findViewById(R.id.btnSeeHistory);

        new Thread(() -> {
            StateQuestionData repo = new StateQuestionData(requireContext());
            repo.open();
            repo.finalizeQuiz(quizId);
            repo.close();

            // new repo for loadQuizDto
            StateQuestionData repo2 = new StateQuestionData(requireContext());
            repo2.open();
            QuizDto dto = repo2.loadQuizDto(quizId);
            repo2.close();
//            QuizDto dto = repo.loadQuizDto(quizId);
//            requireActivity().runOnUiThread(() -> {
//                tv.setText(getString(R.string.your_score, dto.getCorrectCount(), dto.getQuestions().size()));
//            });
        }).start();

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
}
