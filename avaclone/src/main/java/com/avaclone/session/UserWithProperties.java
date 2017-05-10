package com.avaclone.session;


import com.avaclone.session.user.UserProperties;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by jedzej on 09.05.2017.
 */

public class UserWithProperties {
    public final FirebaseUser user;
    public final UserProperties properties;

    public UserWithProperties(FirebaseUser u, UserProperties p){
        user = u;
        properties = p;
    }
}
