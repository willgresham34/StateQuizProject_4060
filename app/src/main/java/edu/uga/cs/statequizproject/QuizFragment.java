package edu.uga.cs.statequizproject;

import android.content.Context;
import android.os.AsyncTask;
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
        new LoadQuizTask(requireContext(), pager, quizId, this).execute();

        return v;
    }

    private static class LoadQuizTask extends AsyncTask<Void, Void, QuizDto> {
        private final Context ctx;
        private final ViewPager2 pager;
        private final long quizId;
        private final Fragment fragment;

        LoadQuizTask(Context ctx, ViewPager2 pager, long quizId, Fragment fragment) {
            this.ctx = ctx.getApplicationContext();
            this.pager = pager;
            this.quizId = quizId;
            this.fragment = fragment;
        }

        @Override
        protected QuizDto doInBackground(Void... voids) {
            StateQuestionData repo = new StateQuestionData(ctx);
            repo.open();
            QuizDto dto = repo.loadQuizDto(quizId);
            repo.close();
            return dto;
        }

        @Override
        protected void onPostExecute(QuizDto dto) {
            if (dto == null || fragment.getContext() == null) return;

            ((QuizFragment) fragment).currentDto = dto;

            QuizPagerAdapter adapter = new QuizPagerAdapter(fragment, dto);
            pager.setAdapter(adapter);

            pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);

                    int lastIndex = adapter.getItemCount() - 1;
                    boolean isLastPage = (position == lastIndex);

                    if (!isLastPage) return;

                    QuizDto cur = ((QuizFragment) fragment).currentDto;

                    cur.recomputeCounters();
                    if (cur.getAnsweredCount() == cur.getTotalCount()) {
                        if (cur.getQuizDate() == null || cur.getQuizDate().trim().isEmpty()) {
                            cur.setQuizDate(String.valueOf(System.currentTimeMillis()));
                        }
                        adapter.notifyItemChanged(position);
                    }

                    new SaveOrFinalizeTask(fragment.requireContext(), cur).execute();
                }

            });
        }
    }

    public static class SaveOrFinalizeTask extends AsyncTask<Void, Void, Void> {
        private final Context ctx;
        private final QuizDto dto;

        SaveOrFinalizeTask(Context ctx, QuizDto dto) {
            this.ctx = ctx.getApplicationContext();
            this.dto = dto;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (dto == null) return null;

            StateQuestionData repo = new StateQuestionData(ctx);
            repo.open();

            dto.recomputeCounters();

            repo.storeQuiz(dto);
            repo.close();

            return null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (currentDto != null) {
            currentDto.recomputeCounters();
            if (currentDto.getAnsweredCount() == currentDto.getTotalCount()) {
                if (currentDto.getQuizDate() == null || currentDto.getQuizDate().trim().isEmpty()) {
                    currentDto.setQuizDate(String.valueOf(System.currentTimeMillis()));
                }
            }
            new SaveOrFinalizeTask(requireContext(), currentDto).execute();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        View v = getView();
        if (v == null) return;
        ViewPager2 pager = v.findViewById(R.id.pager);
        new LoadQuizTask(requireContext(), pager, quizId, this).execute();
    }

    public QuizDto getCurrentDto() {
        return currentDto;
    }


}
