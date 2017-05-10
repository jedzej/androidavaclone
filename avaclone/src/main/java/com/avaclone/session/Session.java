package com.avaclone.session;

import com.avaclone.db.FirebaseUtils;
import com.avaclone.session.lobby.Lobby;
import com.avaclone.session.user.User;
import com.avaclone.session.user.UserProperties;
import com.avaclone.session.user.UserStore;
import com.google.firebase.auth.FirebaseAuth;

import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * Created by jedzej on 10.05.2017.
 */

public class Session {
    public final User user;
    public final UserProperties userProperties;
    public final Lobby lobby;

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

    public static Completable signIn(String email, String password){
        return FirebaseUtils.CompletableFromTask(FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password));
    }

    public static Observable<Session> getObservable(){
        return Observable.combineLatest(
                UserStore.getObservableUser(),
                UserStore.getObservableUser().flatMap(user->{
                    if(user.exists())
                        return UserStore.getObservableUserProperties(user.getUid());
                    else
                        return Observable.just(new UserProperties());
                }),
                (user, up) -> new Session(user, up, null));
    }
}
