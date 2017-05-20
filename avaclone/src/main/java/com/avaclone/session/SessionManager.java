package com.avaclone.session;

import com.avaclone.db.FirebaseUtils;
import com.avaclone.session.user.UserPropertiesStore;
import com.avaclone.session.user.UserStore;
import com.google.firebase.auth.FirebaseAuth;

import io.reactivex.Completable;

/**
 * Created by jedzej on 20.05.2017.
 */

public class SessionManager {
    public static Completable signIn(String email, String password){
        return FirebaseUtils.CompletableFromTask(FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password));
    }

    public static void signOut(){
        FirebaseAuth.getInstance().signOut();
    }

    public static Completable createUser(String email, String password){
        return FirebaseUtils.CompletableFromTask(FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password));
    }

    public static Completable createUserProperties(String username){
        return UserStore.getInstance().getObservable()
                .filter(u -> u.exists())
                .firstOrError()
                .flatMapCompletable(user -> UserPropertiesStore.getInstance(user.getUid()).create(username));
    }
}
