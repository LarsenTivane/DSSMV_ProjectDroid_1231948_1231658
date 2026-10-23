package com.example.studymatch;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class StudyGroupAdapter extends RecyclerView.Adapter<StudyGroupAdapter.StudyGroupViewHolder> {

    public interface OnDeleteClickListener {
        void onDeleteClick(StudyGroup group);
    }

    private final List<StudyGroup> groups = new ArrayList<>();
    private final OnDeleteClickListener deleteClickListener;

    public StudyGroupAdapter(OnDeleteClickListener deleteClickListener) {
        this.deleteClickListener = deleteClickListener;
    }

    public void setGroups(List<StudyGroup> newGroups) {
        groups.clear();
        if (newGroups != null) {
            groups.addAll(newGroups);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudyGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_study_group, parent, false);
        return new StudyGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudyGroupViewHolder holder, int position) {
        StudyGroup group = groups.get(position);
        holder.titleTextView.setText(group.getTitle());
        holder.subjectTextView.setText(group.getSubject());
        holder.vacanciesTextView.setText("Vagas: " + group.getVacancies());
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(group);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    static class StudyGroupViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView subjectTextView;
        private final TextView vacanciesTextView;
        private final Button deleteButton;

        StudyGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tvGroupTitle);
            subjectTextView = itemView.findViewById(R.id.tvGroupSubject);
            vacanciesTextView = itemView.findViewById(R.id.tvGroupVacancies);
            deleteButton = itemView.findViewById(R.id.btnDeleteGroup);
        }
    }
}
