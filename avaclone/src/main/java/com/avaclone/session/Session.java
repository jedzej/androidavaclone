package com.avaclone.session;

import android.util.Log;

import com.avaclone.db.FirebaseUtils;
import com.avaclone.session.lobby.Lobby;
import com.avaclone.session.lobby.LobbyStore;
import com.avaclone.session.user.User;
import com.avaclone.session.user.UserProperties;
import com.avaclone.session.user.UserPropertiesStore;
import com.avaclone.session.user.UserStore;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Vector;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Created by jedzej on 10.05.2017.
 */

public class Session {
    public static class SessionData {
        public final User user;
        public final UserProperties userProperties;
        public final Lobby lobby;
        public final Iterable<UserProperties> lobbyMembers;

        public SessionData(){
            user = null;
            userProperties = null;
            lobby = null;
            lobbyMembers = null;
        }

        public SessionData(User u, UserProperties up, Lobby lp, Iterable<UserProperties> lm){
            user = u;
            userProperties = up;
            lobby = lp;
            lobbyMembers = lm;
        }

        public boolean isSignedIn(){
            return user != null && user.exists();
        }

        public boolean hasProperties(){
            return userProperties != null && userProperties.exists();
        }

        public boolean isInLobby(){
            return lobby != null && lobby.exists();
        }

        public boolean isLeader(){
            return isInLobby() && lobby.leaderId.equals(user.getUid());
        }
    };




    private final static String TAG = "SESSION";

    private static BehaviorSubject<SessionData> sessionBehaviorSubject;


    public static Observable<SessionData> getObservable(){
        if(sessionBehaviorSubject == null) {
            sessionBehaviorSubject = BehaviorSubject.create();

            Observable<User> userObservable = UserStore.getInstance().getObservable();

            Observable<UserProperties> userPropertiesObservable = userObservable
                    .flatMap(user -> Observable.defer(
                            ()->UserPropertiesStore.getObservableFromUser(user)))
                    .doOnError(throwable -> Log.e(TAG,"User properites error: " + throwable))
                    .onErrorReturnItem(UserPropertiesStore.noValue());

            Observable<Lobby> lobbyObservable = userObservable
                    .flatMap(user -> Observable.defer(
                            ()->LobbyStore.getObservableFromUser(user)))
                    .doOnError(throwable -> Log.e(TAG,"Lobby error: " + throwable))
                    .onErrorReturnItem(LobbyStore.noValue());

            Observable<Iterable<UserProperties>> lobbyMembersObservable = lobbyObservable
                    .flatMap(lobby ->LobbyStore.getInstance(lobby.retrieveId()).observeMembers())
                    .doOnError(throwable -> {
                        Log.v(TAG, throwable.getMessage());
                    });

            Observable.combineLatest(
                    userObservable,
                    userPropertiesObservable,
                    lobbyObservable,
                    lobbyMembersObservable,
                    (user, up, lobby, users) -> new SessionData(user, up, lobby, users))
                    .doOnNext(sessionData -> {
                        if (sessionData.isSignedIn()) {
                            Log.i(TAG, "Signed in as " + sessionData.userProperties.username + " - " + sessionData.user.getUid());
                            if(sessionData.isInLobby()) {
                                Log.i(TAG, "In lobby " + sessionData.lobby.retrieveId() + " as " + (sessionData.isLeader() ? "leader" : "member"));
                                for(UserProperties up:sessionData.lobbyMembers){
                                    Log.i(TAG, "lobby members: " + up.username);
                                }
                            }
                            else
                                Log.i(TAG, "Not in lobby");
                        } else
                            Log.i(TAG, "Not signed in");
                    })
                    .subscribe(
                            sessionBehaviorSubject::onNext,
                            sessionBehaviorSubject::onError,
                            sessionBehaviorSubject::onComplete);
        }
        return sessionBehaviorSubject;
    }
}
