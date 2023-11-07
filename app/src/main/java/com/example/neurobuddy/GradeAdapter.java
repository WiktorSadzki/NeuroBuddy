// GradeAdapter.java
package com.example.neurobuddy.Plan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neurobuddy.R;

import java.util.List;

public class GradeAdapter extends RecyclerView.Adapter<GradeAdapter.GradeViewHolder> {

    private List<Grade> gradeList;

    public GradeAdapter(List<Grade> gradeList) {
        this.gradeList = gradeList;
    }

    @NonNull
    @Override
    public GradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grade, parent, false);
        return new GradeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GradeViewHolder holder, int position) {
        Grade grade = gradeList.get(position);

        holder.textViewSubject.setText("Subject: " + grade.getSubject());
        holder.textViewGrade.setText("Grade: " + grade.getGrade());
    }

    @Override
    public int getItemCount() {
        return gradeList.size();
    }

    static class GradeViewHolder extends RecyclerView.ViewHolder {
        TextView textViewSubject;
        TextView textViewGrade;

        GradeViewHolder(View itemView) {
            super(itemView);
            textViewSubject = itemView.findViewById(R.id.textViewSubject);
            textViewGrade = itemView.findViewById(R.id.textViewGrade);
        }
    }
}
