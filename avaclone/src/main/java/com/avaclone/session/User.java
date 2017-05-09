package com.avaclone.session;

import com.avaclone.db.FirebaseLink;
import com.avaclone.db.FirebaseUtils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;

import durdinapps.rxfirebase2.RxFirebaseAuth;
import durdinapps.rxfirebase2.RxFirebaseDatabase;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by jedzej on 08.05.2017.
 */

public class User {

    private static DatabaseReference getPropertiesRef(String userId){
        return FirebaseLink.getRoot().child("userProperties").child(userId);
    }


    public static Completable setProperties(String userId, UserProperties properties){
        return FirebaseUtils.CompletableFromTask(getPropertiesRef(userId).setValue(properties));
    }

    public static Observable<UserProperties> getObservableProperties(String userId){
        return RxFirebaseDatabase.observeValueEvent(getPropertiesRef(userId), dataSnapshot -> {
            if(dataSnapshot.exists()){
                return dataSnapshot.getValue(UserProperties.class);
            }
            else{
                throw new FirebaseException("User Properties not present");
            }
        }).toObservable();
    }

    public static Single<UserProperties> getSingleProperties(String userId){
        return getObservableProperties(userId).firstOrError();
    }

    public static Observable<FirebaseUser> signInWithEmailAndPassword(String email, String password){
        return RxFirebaseAuth.signInWithEmailAndPassword(FirebaseAuth.getInstance(), email, password)
                .flatMapObservable(authResult -> User.getObservableUser());
    }

    public static Observable<UserWithProperties> getUserWithProperties(){
        return Observable.combineLatest(
                getObservableUser(),
                getObservableUser().flatMap(firebaseUser -> getObservableProperties(firebaseUser.getUid())),
                (firebaseUser, userProperties) -> new UserWithProperties(firebaseUser, userProperties)
        );
    }

    public static Maybe<FirebaseUser> createWithEmailAndPassword(String email, String password){
        return RxFirebaseAuth.createUserWithEmailAndPassword(FirebaseAuth.getInstance(), email, password)
                .map(authResult -> {
                    if(authResult.getUser() != null)
                        return authResult.getUser();
                    else
                        throw new FirebaseException("User not created");
                });
    }

    public static Observable<FirebaseUser> getObservableUser(){
        return RxFirebaseAuth.observeAuthState(FirebaseAuth.getInstance())
                .map(firebaseAuth -> firebaseAuth.getCurrentUser());
    }
}
