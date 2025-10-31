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

        Fragment parent = getParentFragment();
        QuizDto dto = (parent instanceof QuizFragment) ? ((QuizFragment) parent).getCurrentDto() : null;

        if (dto != null) {
            tv.setText(
                    getString(
                            R.string.your_score,
                            dto.getCorrectCount(),
                            dto.getQuestions().size()
                    )
            );
        } else {
            tv.setText("Your score: —/—");
        }

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
