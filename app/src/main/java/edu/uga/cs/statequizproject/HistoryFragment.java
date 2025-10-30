
package edu.uga.cs.statequizproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_history, container, false);
        RecyclerView rv = v.findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        new Thread(() -> {
            StateQuestionData repo = new StateQuestionData(requireContext());
            repo.open();
            List<Quiz> data = repo.loadAllQuizzesDesc();
            repo.close();
            requireActivity().runOnUiThread(() -> rv.setAdapter(new HistoryAdapter(data)));
        }).start();

        return v;
    }
}
