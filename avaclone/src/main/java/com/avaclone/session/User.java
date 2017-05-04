package com.avaclone.session;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import durdinapps.rxfirebase2.RxFirebaseAuth;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by jedzej on 02.05.2017.
 */

public class User {

    private static FirebaseAuth mAuth;
    public FirebaseUser firebaseUser;
    public UserProperties properties;

    public boolean isSignedIn(){
        return firebaseUser != null;
    }

    private User(FirebaseUser firebaseUser){
        this.firebaseUser = firebaseUser;
    }

    public static Observable<User> createObservable(){
        return RxFirebaseAuth.observeAuthState(FirebaseAuth.getInstance())
                .doOnNext(firebaseAuth -> {Log.d("FIREBASE", "AUTH EVENT" + firebaseAuth.getCurrentUser());})
                .doOnSubscribe(firebaseAuth -> {Log.d("FIREBASE", "AUTH SUBSCRIBE");})
                .map(firebaseAuth -> new User(firebaseAuth.getCurrentUser()));
    }
}
