package edu.uga.cs.statequizproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {

    private final List<Quiz> items;

    public HistoryAdapter(List<Quiz> items) { this.items = items; }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_history, parent, false);
        return new VH(v);
    }


    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Quiz q = items.get(pos);

        String readableDate = "Unknown date";
        try {
            long epochMillis = Long.parseLong(q.getQuizDate());
            java.text.SimpleDateFormat sdf =
                    new java.text.SimpleDateFormat("MMM dd, yyyy h:mm a", java.util.Locale.getDefault());
            readableDate = sdf.format(new java.util.Date(epochMillis));
        } catch (Exception e) {
        }

        h.title.setText("Quiz taken on " + readableDate);
        h.subtitle.setText(q.getCorrectCount() + " / " + q.getAnsweredCount());
    }


    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, subtitle;
        VH(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            subtitle = v.findViewById(R.id.subtitle);
        }
    }
}
