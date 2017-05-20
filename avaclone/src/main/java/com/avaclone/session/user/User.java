package com.avaclone.session.user;

import com.google.firebase.auth.FirebaseUser;

/**
 * Created by jedzej on 10.05.2017.
 */

public class User {
    private final FirebaseUser data;

    public User(FirebaseUser data){
        this.data = data;
    }

    public boolean exists(){
        return data != null;
    }

    public FirebaseUser getRaw(){
        return data;
    }

    public String getUid(){
        if(exists())
            return data.getUid();
        else
            return null;
    }

    public String getEmail(){
        return data.getEmail();
    }
}
