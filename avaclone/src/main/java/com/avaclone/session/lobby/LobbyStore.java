package com.avaclone.session.lobby;

import com.avaclone.db.FirebaseLink;
import com.avaclone.db.FirebaseUtils;
import com.avaclone.session.user.UserProperties;
import com.avaclone.session.user.UserStore;
import com.google.firebase.database.DatabaseReference;

import durdinapps.rxfirebase2.RxFirebaseDatabase;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Created by jedzej on 07.05.2017.
 */

public class LobbyStore {
    private static BehaviorSubject<Lobby> lobbyBehaviorSubject;
    private static Disposable disposable;

    public static class LobbyError extends Exception {
        public LobbyError(String message){
            super(message);
        }
    }


    private static DatabaseReference getPropertiesRef(String lobbyId){
        return FirebaseLink.getRoot().child("lobbies").child(lobbyId);
    }

    private static void initialize(String lobbyId) {
        if (lobbyBehaviorSubject != null) {
            if(lobbyBehaviorSubject.getValue().getId() != lobbyId){
                disposable.dispose();
                lobbyBehaviorSubject = null;
            }
        }
        if (lobbyBehaviorSubject == null) {
            // create the observable
            Observable<Lobby> userPropertiesObservable = RxFirebaseDatabase.observeValueEvent(
                    getPropertiesRef(lobbyId),
                    dataSnapshot -> {
                        if (dataSnapshot.exists()) {
                            return dataSnapshot.getValue(Lobby.class);
                        } else {
                            return new Lobby();
                        }
                    }).toObservable();

            // create and subscribe subject for specific user
            BehaviorSubject<Lobby> subject = BehaviorSubject.create();
            disposable = userPropertiesObservable.subscribe(
                    subject::onNext,
                    subject::onError,
                    subject::onComplete);
        }
    }

    public static Observable<Lobby> getObservable(String lobbyId) {
        initialize(lobbyId);
        return lobbyBehaviorSubject;
    }

    public static Completable create(UserProperties up){
        Lobby lobby = new Lobby(up.userId);
        lobby.leaderId = up.userId;
        lobby.usersIds.add(up.userId);
        up.lobbyId = lobby.leaderId;

        return LobbyStore.putProperties(lobby.getId(), lobby)
                .andThen(UserStore.putProperties(up.userId, up));
    }

    public static Completable putProperties(String lobbyId, Lobby properties){
        return FirebaseUtils.CompletableFromTask(getPropertiesRef(lobbyId).setValue(properties));
    }
}
