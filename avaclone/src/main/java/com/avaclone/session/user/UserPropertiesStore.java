package com.avaclone.session.user;

import com.avaclone.db.FirebaseLink;
import com.avaclone.db.store.AbstractStore;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * Created by jedzej on 07.05.2017.
 */

public class UserPropertiesStore extends AbstractStore<UserProperties> {

    private String userId;
    private static Map<String, UserPropertiesStore> instanceMap = new HashMap<>();

    private UserPropertiesStore(String userId){
        this.userId = userId;
    }

    public static UserPropertiesStore getInstance(String userId){
        if(userId == null)
            return new UserPropertiesStore(null);
        if(!instanceMap.containsKey(userId)){
            instanceMap.put(userId, new UserPropertiesStore(userId));
        }
        return instanceMap.get(userId);
    }

    public void dispose(String userId) {
        if(instanceMap.containsKey(userId)) {
            instanceMap.get(userId).dispose();
            instanceMap.remove(userId);
        }
    }

    public Completable create(String username){
        return put(new UserProperties(userId, username));
    }

    @Override
    protected DatabaseReference getReference() {
        return FirebaseLink.getRoot().child("userProperties").child(userId);
    }

    @Override
    public UserProperties getDefault() {
        return noValue();
    }

    public static UserProperties noValue() {
        return new UserProperties();
    }

    public static Observable<UserProperties> getObservable(String userId){
        return getInstance(userId).getObservable();
    }

    public static Observable<UserProperties> getObservableFromUser(User user){
        return getObservable(user.getUid());
    }
}
