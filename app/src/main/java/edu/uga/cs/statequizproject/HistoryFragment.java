package edu.uga.cs.statequizproject;

import android.os.Bundle;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView rv;
    private Button backButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_history, container, false);
        rv = v.findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        backButton = v.findViewById(R.id.btnBackToMenu);
        backButton.setOnClickListener(b -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new SplashFragment())
                    .commit();
        });

        new LoadHistoryTask().execute();
        return v;
    }

    private class LoadHistoryTask extends AsyncTask<Void, Void, List<Quiz>> {
        @Override
        protected List<Quiz> doInBackground(Void... voids) {
            StateQuestionData repo = new StateQuestionData(requireContext());
            repo.open();
            List<Quiz> data = repo.loadAllQuizzesDesc();
            repo.close();
            return data;
        }

        @Override
        protected void onPostExecute(List<Quiz> quizzes) {
            rv.setAdapter(new HistoryAdapter(quizzes));
        }
    }
}
