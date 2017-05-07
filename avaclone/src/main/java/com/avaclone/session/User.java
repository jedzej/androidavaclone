package com.avaclone.session;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import durdinapps.rxfirebase2.RxFirebaseAuth;
import durdinapps.rxfirebase2.RxFirebaseDatabase;
import io.reactivex.Maybe;
import io.reactivex.Observable;


public class User {

    public static class UserAuthenticationError extends Exception {
        UserAuthenticationError(){
            super("Not signed in");
        }
    }

    private final FirebaseUser firebaseUser;
    public UserProperties properties;

    private boolean isSignedIn(){
        return firebaseUser != null;
    }

    @SuppressWarnings("WeakerAccess")
    public String getUid(){
        if(isSignedIn())
            return firebaseUser.getUid();
        else
            return null;
    }

    private User(FirebaseUser firebaseUser){
        this.firebaseUser = firebaseUser;
    }

    public void createProperties() {
        properties = new UserProperties();
    }

    public static Maybe<User> getMaybe(){
        return RxFirebaseAuth.observeAuthState(FirebaseAuth.getInstance()).firstElement()
                .flatMap(firebaseAuth -> Maybe.create(maybeEmitter -> {
                    User user = new User(firebaseAuth.getCurrentUser());
                    if(user.isSignedIn())
                        maybeEmitter.onSuccess(user);
                    else
                        maybeEmitter.onError(new FirebaseAuthException("Not signed in",""));
                }));
    }

    @SuppressWarnings("WeakerAccess")
    public static Observable<User> getObservable(){
        return RxFirebaseAuth.observeAuthState(FirebaseAuth.getInstance())
                .doOnNext(firebaseAuth -> Log.d("FIREBASE", "AUTH EVENT" + firebaseAuth.getCurrentUser()))
                .doOnSubscribe(firebaseAuth -> Log.d("FIREBASE", "AUTH SUBSCRIBE"))
                .map(firebaseAuth -> new User(firebaseAuth.getCurrentUser()));
    }


    private DatabaseReference getPropertiesRef(){
        if(isSignedIn())
            return FirebaseDatabase.getInstance().getReference().child("userProperties").child(getUid());
        else
            return null;
    }

    @SuppressWarnings("WeakerAccess")
    public Observable<UserProperties> getPropertiesObservable(){
        if(getPropertiesRef() != null)
            return RxFirebaseDatabase.observeValueEvent(getPropertiesRef())
                .toObservable()
                .map(dataSnapshot -> {
                    if(dataSnapshot.exists()){
                        return dataSnapshot.getValue(UserProperties.class);
                    }
                    else {
                        return new UserProperties();
                    }
                });
        else
            return Observable.error(new UserAuthenticationError());
    }

    public Maybe<UserProperties> commitProperties(){
        return Maybe.create(maybeEmitter -> {
            if(getPropertiesRef() == null) {
                maybeEmitter.onError(new UserAuthenticationError());
            }
            else if(properties == null){
                maybeEmitter.onError(new NullPointerException("Properties not set"));
            }
            else {
                getPropertiesRef().setValue(properties, (databaseError, databaseReference) -> {
                    Log.d("COMMIT", "LISTENER");
                    if (databaseError == null)
                        maybeEmitter.onSuccess(properties);
                    else
                        maybeEmitter.onError(databaseError.toException());
                });
            }
        });
    }

    public static Observable<User> getObservableWithProperties(){
        return Observable.combineLatest(
                getObservable(),
                getObservable().flatMap(User::getPropertiesObservable),
                (user, userProperties) -> {
                    if(!user.isSignedIn()) {
                        throw new UserAuthenticationError();
                    }
                    else {
                        user.properties = userProperties;
                        return user;
                    }
                }
        );
    }

    public Maybe<User> getMaybeWithProperties(){
        return getObservableWithProperties().firstElement();
    }
}
