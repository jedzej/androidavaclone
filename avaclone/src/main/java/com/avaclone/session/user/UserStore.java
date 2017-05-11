package com.avaclone.session.user;

import com.avaclone.db.FirebaseLink;
import com.avaclone.db.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

import durdinapps.rxfirebase2.RxFirebaseAuth;
import durdinapps.rxfirebase2.RxFirebaseDatabase;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Created by jedzej on 10.05.2017.
 */

public class UserStore {
    private static Map<String, BehaviorSubject<UserProperties>> userPropertiesSubjectMap = new HashMap<>();
    private static BehaviorSubject<User> firebaseUserSubject;
    private static CompositeDisposable disposables = new CompositeDisposable();

    private static DatabaseReference getPropertiesRef(String userId) {
        return FirebaseLink.getRoot().child("userProperties").child(userId);
    }

    public static Completable putProperties(String userId, UserProperties properties){
        return FirebaseUtils.CompletableFromTask(getPropertiesRef(userId).setValue(properties));
    }

    private static void initializeUserPropertiesSubject(String userId) {
        if (!userPropertiesSubjectMap.containsKey(userId)) {
            // create the observable
            Observable<UserProperties> userPropertiesObservable = RxFirebaseDatabase.observeValueEvent(
                    getPropertiesRef(userId),
                    dataSnapshot -> {
                        if (dataSnapshot.exists()) {
                            return dataSnapshot.getValue(UserProperties.class);
                        } else {
                            return new UserProperties();
                        }
                    }).toObservable();

            // create and subscribe subject for specific user
            BehaviorSubject<UserProperties> subject = BehaviorSubject.create();
            disposables.add(userPropertiesObservable.subscribe(
                    subject::onNext,
                    subject::onError,
                    subject::onComplete));

            // put the subject to the map
            userPropertiesSubjectMap.put(userId, subject);
        }
    }

    private static void initializeFirebaseUserSubject() {
        if (firebaseUserSubject == null) {
            Observable<User> userObservable = RxFirebaseAuth.observeAuthState(FirebaseAuth.getInstance())
                    .map(firebaseAuth -> new User(firebaseAuth.getCurrentUser()));
            firebaseUserSubject = BehaviorSubject.create();

            disposables.add(userObservable.subscribe(
                    firebaseUserSubject::onNext,
                    firebaseUserSubject::onError,
                    firebaseUserSubject::onComplete));
        }
    }

    public static Observable<User> getObservableUser() {
        initializeFirebaseUserSubject();
        return firebaseUserSubject;
    }

    public static Observable<UserProperties> getObservableUserProperties(String userId) {
        initializeUserPropertiesSubject(userId);
        return userPropertiesSubjectMap.get(userId);
    }
}
