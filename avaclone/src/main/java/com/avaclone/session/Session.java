package com.avaclone.session;

import android.util.Log;

import com.avaclone.db.FirebaseUtils;
import com.avaclone.session.lobby.Lobby;
import com.avaclone.session.user.User;
import com.avaclone.session.user.UserProperties;
import com.avaclone.session.user.UserStore;
import com.google.firebase.auth.FirebaseAuth;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Created by jedzej on 10.05.2017.
 */

public class Session {
    public final User user;
    public final UserProperties userProperties;
    public final Lobby lobby;

    private static BehaviorSubject<Session> sessionBehaviorSubject;

    private Session(User u, UserProperties up, Lobby lp){
        user = u;
        userProperties = up;
        lobby = lp;
    }

    public boolean isSignedIn(){
        return user != null && user.exists();
    }

    public boolean hasProperties(){
        return userProperties != null && userProperties.isValid();
    }

    public boolean isInLobby(){
        return lobby != null && lobby.isValid();
    }

    public boolean isLeader(){
        return isInLobby() && lobby.leaderId.equals(user.getUid());
    }

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
        return UserStore.getObservableUser()
                .filter(u -> u.exists())
                .firstOrError()
                .flatMapCompletable(u -> {
                    UserProperties up = new UserProperties();
                    up.userId = u.getUid();
                    up.username = username;
                    return UserStore.putProperties(up.userId, up);
                });
    }

    public static Observable<Session> getObservable(){
        if(sessionBehaviorSubject == null) {
            sessionBehaviorSubject = BehaviorSubject.create();
            Observable.combineLatest(
                    UserStore.getObservableUser(),
                    UserStore.getObservableUser().flatMap(user->{
                        if(user.exists())
                            return UserStore.getObservableUserProperties(user.getUid());
                        else
                            return Observable.just(new UserProperties());
                    }),
                    (user, up) -> new Session(user, up, null))
                    .doOnNext(session -> Log.v("SESSION","Update"))
                    .subscribe(
                            sessionBehaviorSubject::onNext,
                            sessionBehaviorSubject::onError,
                            sessionBehaviorSubject::onComplete);

        }
        return sessionBehaviorSubject;
    }
}
