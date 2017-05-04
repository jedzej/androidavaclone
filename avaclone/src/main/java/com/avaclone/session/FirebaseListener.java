package com.avaclone.session;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Callable;

/**
 * Created by jedzej on 03.05.2017.
 */

public class FirebaseListener implements ValueEventListener {

    public interface FirebaseListenerCallback {
        public void onDataChange(DataSnapshot dataSnapshot);
    }
    FirebaseListenerCallback callback;
    DatabaseReference databaseReference;

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        callback.onDataChange(dataSnapshot);
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        //TODO
    }

    public void start(DatabaseReference databaseReference, FirebaseListenerCallback callback) {
        this.databaseReference = databaseReference;
        this.callback = callback;
        this.databaseReference.addValueEventListener(this);
    }

    public void stop() {
        databaseReference.removeEventListener(this);
    }
}
