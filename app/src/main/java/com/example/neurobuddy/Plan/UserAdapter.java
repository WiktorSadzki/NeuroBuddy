package com.example.neurobuddy.Plan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neurobuddy.Login.RegisterUsers;
import com.example.neurobuddy.R;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<RegisterUsers> userList;

    public UserAdapter(List<RegisterUsers> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        RegisterUsers user = userList.get(position);
        holder.userItemTextView.setText((position + 1) + ". " + user.getLogin() + " - Points: " + user.getPoints());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userItemTextView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userItemTextView = itemView.findViewById(R.id.userItemTextView);
        }
    }
}

