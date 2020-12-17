package com.mario.videocallingapp.listeners;

import com.mario.videocallingapp.models.Users;

public interface UsersListeners {

    void initiateVideoMeeting(Users users);

    void initiateAudioMeeting(Users users);
}
