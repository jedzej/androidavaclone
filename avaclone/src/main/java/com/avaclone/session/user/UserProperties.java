package com.avaclone.session.user;

import com.google.firebase.database.IgnoreExtraProperties;


@IgnoreExtraProperties
public class UserProperties {
    public String lobbyId;
    public String userId;
    public String username;

    public boolean isValid(){
        return lobbyId != null && userId != null && username != null;
    }
}
