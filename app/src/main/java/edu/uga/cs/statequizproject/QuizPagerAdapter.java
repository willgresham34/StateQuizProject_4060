package edu.uga.cs.statequizproject;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class QuizPagerAdapter extends FragmentStateAdapter {

    private final QuizDto dto;

    public QuizPagerAdapter(@NonNull Fragment fragment, QuizDto dto) {
        super(fragment);
        this.dto = dto;
    }

    @NonNull @Override
    public Fragment createFragment(int position) {
        if (position < dto.getQuestions().size()) {
            return QuestionFragment.newInstance(dto.getQuizId(), position);
        } else {
            return ResultFragment.newInstance(dto.getQuizId());
        }
    }

    @Override
    public int getItemCount() {
        // questions and  final result page
        return dto.getQuestions().size() + 1;
    }
}
