
package edu.uga.cs.statequizproject;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_splash, container, false);
        TextView tv = v.findViewById(R.id.tvDesc);
        ProgressBar pb = v.findViewById(R.id.progress);
        Button start = v.findViewById(R.id.btnStart);
        Button history = v.findViewById(R.id.btnHistory);

        // Seed DB if needed in background
        pb.setVisibility(View.VISIBLE);
        new Thread(() -> {
            StateQuestionData repo = new StateQuestionData(requireContext());
            repo.open();
            repo.ensureSeededFromCsvIfEmpty();
            repo.close();
            requireActivity().runOnUiThread(() -> pb.setVisibility(View.GONE));
        }).start();

        start.setOnClickListener(b -> {
            // create a new quiz
            new Thread(() -> {
                StateQuestionData repo = new StateQuestionData(requireContext());
                repo.open();
                long quizId = repo.createNewQuiz(6);
                repo.close();
                requireActivity().runOnUiThread(() -> {
                    requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, QuizFragment.newInstance(quizId))
                        .addToBackStack(null)
                        .commit();
                });
            }).start();
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
