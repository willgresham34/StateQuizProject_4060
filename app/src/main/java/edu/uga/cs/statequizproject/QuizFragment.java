package edu.uga.cs.statequizproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

public class QuizFragment extends Fragment {

    private static final String ARG_QUIZ_ID = "quiz_id";
    private long quizId;
    private QuizDto currentDto; // cache

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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_quiz, container, false);
        ViewPager2 pager = v.findViewById(R.id.pager);

        // load quiz in background
        new Thread(() -> {
            StateQuestionData repo = new StateQuestionData(requireContext());
            repo.open();
            QuizDto dto = repo.loadQuizDto(quizId);
            repo.close();
            currentDto = dto;

            requireActivity().runOnUiThread(() -> {
                QuizPagerAdapter adapter = new QuizPagerAdapter(this, dto);
                pager.setAdapter(adapter);

                pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    int lastPos = 0;

                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);

                        // when we leave a question, repull latest answers and save counts
                        if (position != lastPos && lastPos < dto.getQuestions().size()) {
                            long qid = dto.getQuizId();
                            // recompute on db side
                            new Thread(() -> {
                                StateQuestionData r2 = new StateQuestionData(requireContext());
                                r2.open();
                                // just reload and update counts
                                QuizDto tmp = r2.loadQuizDto(qid);
                                tmp.recomputeCounters();
                                r2.finalizePartial(qid, tmp.getCorrectCount(), tmp.getAnsweredCount());
                                r2.close();
                            }).start();
                        }

                        lastPos = position;

                        // if this is the result page, we’re done (adapter put result as last)
                    }
                });
            });
        }).start();

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        // capture current progress
        if (currentDto != null) {
            currentDto.recomputeCounters();
            new Thread(() -> {
                StateQuestionData repo = new StateQuestionData(requireContext());
                repo.open();
                repo.finalizePartial(
                        currentDto.getQuizId(),
                        currentDto.getCorrectCount(),
                        currentDto.getAnsweredCount()
                );
                repo.close();
            }).start();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // reload from db in case orientation changed or app was backgrounded
        new Thread(() -> {
            StateQuestionData repo = new StateQuestionData(requireContext());
            repo.open();
            QuizDto dto = repo.loadQuizDto(quizId);
            repo.close();
            currentDto = dto;

            requireActivity().runOnUiThread(() -> {
                ViewPager2 pager = getView().findViewById(R.id.pager);
                if (pager != null) {
                    pager.setAdapter(new QuizPagerAdapter(this, dto));
                }
            });
        }).start();
    }

}
