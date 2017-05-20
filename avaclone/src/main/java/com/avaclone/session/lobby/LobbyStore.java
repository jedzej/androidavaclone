package com.avaclone.session.lobby;

import android.util.Log;

import com.avaclone.db.FirebaseLink;
import com.avaclone.db.store.AbstractStore;
import com.avaclone.db.store.StoreException;
import com.avaclone.session.user.User;
import com.avaclone.session.user.UserProperties;
import com.avaclone.session.user.UserPropertiesStore;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * Created by jedzej on 07.05.2017.
 */

public class LobbyStore extends AbstractStore<Lobby> {

    private static Map<String, LobbyStore> instanceMap = new HashMap<>();
    private String lobbyId;

    private static final String TAG = "Store::Lobby";

    private LobbyStore(String lobbyId) {
        this.lobbyId = lobbyId;
    }

    public static LobbyStore getInstance(String lobbyId) {
        if (lobbyId == null)
            return new LobbyStore(null);
        if (!instanceMap.containsKey(lobbyId)) {
            instanceMap.put(lobbyId, new LobbyStore(lobbyId));
        }
        return instanceMap.get(lobbyId);
    }

    public void dispose(String lobbyId) {
        if (instanceMap.containsKey(lobbyId)) {
            instanceMap.get(lobbyId).dispose();
            instanceMap.remove(lobbyId);
        }
    }

    public static Completable create(UserProperties userProperties) {
        Log.v(TAG, "Creating lobby for " + userProperties.userId + " " + userProperties.username);
        Lobby lobby = new Lobby(userProperties.userId);
        LobbyStore lobbyStore = LobbyStore.getInstance(lobby.retrieveId());

        return lobbyStore.getSingle()
                .flatMapCompletable(l -> {
                    if (l.exists())
                        throw new StoreException("Lobby already exists");
                    else {
                        return lobbyStore.put(lobby);
                    }
                })
                .doOnComplete(() -> Log.i(TAG, "1"))
                .doOnError(throwable -> Log.i(TAG, "1e " + throwable.getMessage()))
                //.doOnComplete(() -> Log.i(TAG, "2"))
                //.doOnError(throwable -> Log.i(TAG, "2e " + throwable.getMessage()))
                .andThen(Completable.defer(() -> {
                    userProperties.lobbyId = lobby.retrieveId();
                    return UserPropertiesStore.getInstance(userProperties.userId).put(userProperties);
                }))
                .doOnComplete(() -> Log.i(TAG, "Lobby created"))
                .doOnError(throwable -> Log.i(TAG, "Lobby not created " + throwable.getMessage()));
    }

    public Observable<Iterable<UserProperties>> observeMembers() {
        return getObservable()
                .doOnNext(lobby -> Log.v(TAG, "Users " + lobby.usersIds.toString()))
                .flatMap(lobby -> {
                            Iterable<Observable<UserProperties>> membersPropertiesObservable = Observable.fromIterable(lobby.usersIds)
                                    .map(userId -> UserPropertiesStore.getInstance(userId).getObservable())
                                    .blockingIterable();
                            return Observable.combineLatest(membersPropertiesObservable,
                                    objects -> {
                                        List<UserProperties> userPropertiesList = new ArrayList<>();
                                        for(Object o: objects){
                                            userPropertiesList.add((UserProperties)o);
                                        }
                                        return userPropertiesList;
                                    });
                        }
                );
    }

    public Completable destroy() {
        return getSingle().flatMapCompletable(lobby -> {
            if (!lobby.exists())
                throw new StoreException("Lobby does not exist");
            else
                return Completable.merge(
                        // clear lobbyId in users' profiles
                        Observable.fromIterable(lobby.usersIds)
                                .map(uId -> UserPropertiesStore.getInstance(uId)
                                        .getSingle()
                                        .flatMapCompletable(userProperties -> {
                                            userProperties.lobbyId = null;
                                            return Completable.defer(() -> UserPropertiesStore.getInstance(uId).put(userProperties));
                                        }))
                                .blockingIterable())
                        // delete lobby entry
                        .andThen(Completable.defer(() -> put(null)));
        })

                .doOnComplete(() -> Log.i(TAG, "Lobby destroyed"))
                .doOnError(throwable -> Log.i(TAG, "Lobby not destroyed " + throwable.getMessage()));
    }

    @Override
    protected DatabaseReference getReference() {
        return FirebaseLink.getRoot().child("lobbies").child(lobbyId);
    }

    @Override
    public Lobby getDefault() {
        return noValue();
    }

    public static Observable<Lobby> getObservable(String lobbyId) {
        return getInstance(lobbyId).getObservable();
    }

    public static Observable<Lobby> getObservableFromUser(User user) {
        return UserPropertiesStore.getObservableFromUser(user)
                .flatMap(userProperties -> getObservable(userProperties.lobbyId));
    }

    public static Lobby noValue() {
        return new Lobby();
    }
}
