package com.avaclone.session;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by jedzej on 28.04.2017.
 */

public class ObservableUser {

    private static FirebaseAuth mAuth;

    public String name;
    public String email;
    public String lobbyId;

    static private DatabaseReference mDatabase;

    private DatabaseReference getRef(){
        return mDatabase.child("users").child(email);
    }

    private static void lazyInit() {
        if(mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance().getReference();
        }
    }

    public void setLobby(Lobby lobby){
        getRef().child("lobbyId").setValue(lobbyId);
    }

    public ObservableUser() {
        // Default constructor required for calls to DataSnapshot.getValue(ObservableUser.class)
    }

    public ObservableUser(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public static ObservableUser create(String name, String email) {
        lazyInit();
        ObservableUser user = new ObservableUser(name, email);
        user.getRef().setValue(user);
        return user;
    }


    public Observable<ObservableUser> toObservable(){

        return Observable.create(new ObservableOnSubscribe<ObservableUser>(){

            @Override
            public void subscribe(final ObservableEmitter<ObservableUser> emitter) throws Exception {
                getRef().addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ObservableUser u = dataSnapshot.getValue(ObservableUser.class);
                        emitter.onNext(u);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        emitter.onError(databaseError.toException());
                    }
                });
            }
        });
    }


}
