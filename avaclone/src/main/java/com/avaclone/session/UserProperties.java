package com.avaclone.session;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.Reference;
import java.util.concurrent.Callable;

import durdinapps.rxfirebase2.RxFirebaseDatabase;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import io.reactivex.functions.Function;

/**
 * Created by jedzej on 02.05.2017.
 */

public class UserProperties {

    public static class UserPropertiesException extends Throwable {

    } ;

    public String lobbyId;
    public String userId;
    public String nickname;

    private static final String TAG = "UserProperties";
    private static DatabaseReference mDatabase;

    public String toString() {
        return "<userId=" + userId + ", lobbyId=" + lobbyId + ", nickname=" + nickname + ">";
    }

    private static void lazyInit() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance().getReference();
        }
    }

    public DatabaseReference getReference(){
        lazyInit();
        return mDatabase.child("userProperties").child(userId);
    }

    public void commit(){
        getReference().setValue(this);
    }

    public void setLobbyId(String value){
        lobbyId = value;
        getReference().child("lobbyId").setValue(lobbyId);
    }

    public void setNickname(String value){
        nickname = value;
        getReference().child("nickname").setValue(nickname);
    }

    public void setUserId(String value){
        userId = value;
        getReference().child("userId").setValue(userId);
    }

    private UserProperties(){
    }

    private UserProperties(String userId){
        this.userId = userId;
    }

    public static Observable<UserProperties> observeFor(final User user){
        if(user.isSignedIn()){
            return observeFor(user.firebaseUser.getUid());
        }
        else {
            return Observable.empty();//<UserProperties>error(new UserPropertiesException());
        }
    }

    public static Observable<UserProperties> observeFor(final String userId){
        return RxFirebaseDatabase.observeValueEvent(new UserProperties(userId).getReference())
                .toObservable()
                .map(dataSnapshot -> {
                    if(dataSnapshot.exists()){
                        return dataSnapshot.getValue(UserProperties.class);
                    }
                    else {
                        return new UserProperties(userId);
                    }
                });
    }
}
