package com.avaclone.session.user;

import com.google.firebase.auth.FirebaseAuth;

import durdinapps.rxfirebase2.RxFirebaseAuth;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Created by jedzej on 10.05.2017.
 */

public class UserStore {
    private static UserStore instance;
    private BehaviorSubject<User> firebaseUserSubject;
    private Disposable disposable;

    public UserStore(){
        Observable<User> userObservable = RxFirebaseAuth.observeAuthState(FirebaseAuth.getInstance())
                .map(firebaseAuth -> new User(firebaseAuth.getCurrentUser()));
        firebaseUserSubject = BehaviorSubject.create();

        disposable = userObservable.subscribe(
                firebaseUserSubject::onNext,
                firebaseUserSubject::onError,
                firebaseUserSubject::onComplete);
    }

    public static UserStore getInstance(){
        if(instance == null){
            instance = new UserStore();
        }
        return instance;
    }

    public Observable<User> getObservable() {
        return firebaseUserSubject;
    }
}
