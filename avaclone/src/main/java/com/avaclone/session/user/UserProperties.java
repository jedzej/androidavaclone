package com.avaclone.session.user;

import com.avaclone.db.store.Storable;
import com.google.firebase.database.IgnoreExtraProperties;


@IgnoreExtraProperties
public class UserProperties implements Storable {
    public String lobbyId;
    public String userId;
    public String username;

    @Override
    public boolean exists() {
        return lobbyId != null && userId != null && username != null;
    }

    UserProperties(){}

    UserProperties(String userId, String username){
        this.userId = userId;
        this.username = username;
    }
}
