package com.mario.videocallingapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mario.videocallingapp.R;
import com.mario.videocallingapp.listeners.UsersListeners;
import com.mario.videocallingapp.models.Users;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder>{

    private List<Users> usersList;
    private UsersListeners usersListeners;

    public UsersAdapter(List<Users> usersList , UsersListeners usersListeners) {
        this.usersList = usersList;
        this.usersListeners = usersListeners;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_user , parent , false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(usersList.get(position));
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

     class UserViewHolder extends RecyclerView.ViewHolder{

        TextView textFirstChar ,textUsername , textEmail;
        ImageView imageAudioCall , imageVideoCall;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textFirstChar = itemView.findViewById(R.id.textFirstChar);
            textUsername = itemView.findViewById(R.id.textUsername);
            textEmail = itemView.findViewById(R.id.textEmail);
            imageAudioCall = itemView.findViewById(R.id.imageAudioCall);
            imageVideoCall = itemView.findViewById(R.id.imageVideoCall);
        }

        void setUserData(Users users){
            textFirstChar.setText(users.firstName.substring(0,1));
            textUsername.setText(String.format("%s %s" , users.firstName , users.lastName));
            textEmail.setText(users.email);
            imageAudioCall.setOnClickListener(v -> { usersListeners.initiateAudioMeeting(users); });
            imageVideoCall.setOnClickListener(v -> { usersListeners.initiateVideoMeeting(users); });
        }
    }
}
