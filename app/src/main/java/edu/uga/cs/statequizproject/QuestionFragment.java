package edu.uga.cs.statequizproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuestionFragment extends Fragment {

    private static final String ARG_QUIZ_ID = "quiz_id";
    private static final String ARG_INDEX = "index";
    private int index;

    public static QuestionFragment newInstance(long quizId, int index) {
        QuestionFragment f = new QuestionFragment();
        Bundle b = new Bundle();
        b.putLong(ARG_QUIZ_ID, quizId);
        b.putInt(ARG_INDEX, index);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        index = getArguments().getInt(ARG_INDEX);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_question, container, false);
        TextView tv = v.findViewById(R.id.tvQuestion);
        RadioGroup group = v.findViewById(R.id.radioGroup);
        RadioButton rb1 = v.findViewById(R.id.choice1);
        RadioButton rb2 = v.findViewById(R.id.choice2);
        RadioButton rb3 = v.findViewById(R.id.choice3);

        QuizFragment parent = (QuizFragment) getParentFragment();
        if (parent == null || parent.getCurrentDto() == null) return v;

        QuizDto dto = parent.getCurrentDto();
        QuizQuestion q = dto.getQuestions().get(index);

        String prompt = getString(R.string.question_prompt, q.getState());
        tv.setText(prompt);

        List<String> choices = new ArrayList<>();
        choices.add(q.getCapital());
        choices.add(q.getSecondCity());
        choices.add(q.getThirdCity());
        Collections.shuffle(choices);

        rb1.setText(choices.get(0));
        rb2.setText(choices.get(1));
        rb3.setText(choices.get(2));

        // restore selection from DTO
        String ua = q.getUserAnswer();
        if (ua != null) {
            if (rb1.getText().toString().equalsIgnoreCase(ua)) rb1.setChecked(true);
            else if (rb2.getText().toString().equalsIgnoreCase(ua)) rb2.setChecked(true);
            else if (rb3.getText().toString().equalsIgnoreCase(ua)) rb3.setChecked(true);
        }

        // write to DTO
        group.setOnCheckedChangeListener((g, checkedId) -> {
            RadioButton selected = v.findViewById(checkedId);
            if (selected != null) {
                q.setUserAnswer(selected.getText().toString());
                new QuizFragment.SaveOrFinalizeTask(requireContext(), parent.getCurrentDto()).execute();
            }
        });

        return v;
    }

}
