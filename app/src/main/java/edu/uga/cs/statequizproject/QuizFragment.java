
package edu.uga.cs.statequizproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

public class QuizFragment extends Fragment {

    private static final String ARG_QUIZ_ID = "quiz_id";
    private long quizId;

    public static QuizFragment newInstance(long quizId) {
        QuizFragment f = new QuizFragment();
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
        View v = inflater.inflate(R.layout.fragment_quiz, container, false);
        ViewPager2 pager = v.findViewById(R.id.pager);
        pager.setUserInputEnabled(true);

        new Thread(() -> {
            StateQuestionData repo = new StateQuestionData(requireContext());
            repo.open();
            QuizDto dto = repo.loadQuizDto(quizId);
            repo.close();
            requireActivity().runOnUiThread(() -> {
                pager.setAdapter(new QuizPagerAdapter(this, dto));
                pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        // when last item reached and user swipes again, finish
                    }
                });
            });
        }).start();

        return v;
    }
}
