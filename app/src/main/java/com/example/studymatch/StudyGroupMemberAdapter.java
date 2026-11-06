package com.example.studymatch;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class StudyGroupMemberAdapter extends RecyclerView.Adapter<StudyGroupMemberAdapter.MemberViewHolder> {

    private final List<StudyGroupMember> members = new ArrayList<>();

    public void setMembers(List<StudyGroupMember> newMembers) {
        members.clear();
        if (newMembers != null) {
            members.addAll(newMembers);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_study_group_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        StudyGroupMember member = members.get(position);
        holder.name.setText(member.getName());
        holder.studentNumber.setText("N.º de estudante: " + member.getStudentNumber());
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView studentNumber;

        MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvMemberName);
            studentNumber = itemView.findViewById(R.id.tvMemberStudentNumber);
        }
    }
}
