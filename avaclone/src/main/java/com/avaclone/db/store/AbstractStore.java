package com.avaclone.db.store;

import android.util.Log;

import com.avaclone.db.FirebaseUtils;
import com.google.firebase.database.DatabaseReference;

import durdinapps.rxfirebase2.RxFirebaseDatabase;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Created by jedzej on 12.05.2017.
 */

public abstract class AbstractStore<T extends Storable> implements Disposable {

    private static final String TAG = "STORE";

    private BehaviorSubject<T> subject;
    private Disposable disposable;

    protected abstract DatabaseReference getReference();
    public abstract T getDefault();

    public void initialize() {
        if (subject == null) {
            // create the observable
            Observable<T> observable = RxFirebaseDatabase.observeValueEvent(
                    getReference(),
                    dataSnapshot -> {
                        if (dataSnapshot.exists()) {
                            return dataSnapshot.getValue(getDefault().getClass());
                        } else {
                            return this.getDefault();
                        }
                    })
                    // map from Object to T type
                    .map(o -> (T)o)
                    .doOnNext(t -> Log.d(TAG,getObservable() + " source emitting " + t.toString()))
                    .toObservable();


            Log.v(TAG,"Creating subject for " + getReference().toString());
            // create and subscribe subject for specific user
            subject = BehaviorSubject.create();
            subject
                .doOnNext(t -> Log.d(TAG,subject.toString() + " subject emitting " + t.toString()))
                .doOnSubscribe(disp -> Log.d(TAG,subject.toString() + " subscribed"));
            disposable = observable.subscribe(
                    subject::onNext,
                    subject::onError,
                    subject::onComplete);
        }
    }

    public void dispose(){
        if (subject != null) {
            disposable.dispose();
            subject = null;
        }
    }

    public boolean isDisposed(){
        return disposable.isDisposed();
    }

    public Observable<T> getObservable() {
        initialize();
        Log.d(TAG, "getObservable for " + getReference());
        return subject;
    }

    public Single<T> getSingle() {
        return getObservable()
                .firstOrError();
    }

    public Completable put(T data){
        Log.v(TAG, "put value for " + getReference());
        return FirebaseUtils.CompletableFromTask(getReference().setValue(data));
    }
}
