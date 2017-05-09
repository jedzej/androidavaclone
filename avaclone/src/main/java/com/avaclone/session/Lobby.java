package com.avaclone.session;

import android.util.Log;
import android.util.Pair;

import com.avaclone.db.FirebaseLink;
import com.avaclone.db.FirebaseUtils;
import com.google.firebase.database.DatabaseReference;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import durdinapps.rxfirebase2.RxFirebaseDatabase;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by jedzej on 07.05.2017.
 */

public class Lobby {

    LobbyProperties properties;

    public String getLobbyId(){
        return properties.leaderId;
    }

    public static class LobbyError extends Exception {
        public LobbyError(String message){
            super(message);
        }
    }

    public static Observable<LobbyProperties> getObservable(UserProperties up){
        return getObservable(up.lobbyId);
    }

    public static Observable<LobbyProperties> getObservable(String lobbyId){
        return RxFirebaseDatabase.observeValueEvent(getPropertiesRef(lobbyId))
                .toObservable()
                .map(dataSnapshot -> {
                    if(dataSnapshot.exists()){
                        return dataSnapshot.getValue(LobbyProperties.class);
                    }
                    else {
                        throw new LobbyError("Lobby not initialized");
                    }
                });
    }


    private static DatabaseReference getPropertiesRef(String lobbyId){
        return FirebaseLink.getRoot().child("lobbies").child(lobbyId);
    }

    public static Single<LobbyProperties> create(UserProperties up){
        LobbyProperties lp = new LobbyProperties(up.userId);
        lp.leaderId = up.userId;
        lp.usersIds.add(up.userId);
        up.lobbyId = lp.leaderId;

        return Lobby.setProperties(lp.leaderId, lp)
                .andThen(User.setProperties(up.userId, up))
                .toSingle(() -> lp);
    }

    public static Completable setProperties(String lobbyId, LobbyProperties properties){
        return FirebaseUtils.CompletableFromTask(getPropertiesRef(lobbyId).setValue(properties));
    }
}
