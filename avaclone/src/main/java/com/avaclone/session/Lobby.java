package com.avaclone.session;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

/**
 * Created by jedzej on 28.04.2017.
 */

public class Lobby {

    private static final String TAG = "Lobby";

    private static FirebaseAuth mAuth;

    public String id;
    public List<String> userEmails;


    public void addUser(ObservableUser user){
        user.setLobby(this);
        userEmails.add(user.email);
    }

    public boolean removeUser(ObservableUser user){
        return userEmails.remove(user.email);
    }

    public boolean contains(ObservableUser user){
        return userEmails.contains(user.email);
    }

    private static Lobby _instance;

    static private DatabaseReference mDatabase;

    private static void lazyInit() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance().getReference();
        }
    }

    public static Lobby createFor(ObservableUser host){
        lazyInit();
        Lobby instance = new Lobby();
        instance.addUser(host);
        mDatabase.child("lobbies").child(host.email).setValue(instance);
        return new Lobby();
    }


    public static Lobby forUser(ObservableUser user){

        return new Lobby();
    }
}
