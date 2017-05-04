/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.avaclone.session.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;

import com.avaclone.R;
import com.avaclone.session.User;
import com.avaclone.session.UserProperties;
import com.avaclone.session.activities.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

import durdinapps.rxfirebase2.RxFirebaseAuth;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import java.util.concurrent.Callable;

public class MainActivity extends Activity {
    private static final String TAG = "RxAndroidSamples";

    private final CompositeDisposable disposables = new CompositeDisposable();

    static Observable<String> sampleObservable() {
        return Observable.defer(new Callable<ObservableSource<? extends String>>() {
            @Override
            public ObservableSource<? extends String> call() throws Exception {
                // Do some long running operation
                SystemClock.sleep(5000);
                return Observable.just("one", "two", "three", "four", "five");
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        findViewById(R.id.button_run_scheduler).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRunSchedulerExampleButtonClicked();
            }
        });

        findViewById(R.id.button_sign_in).setOnClickListener(v -> {
            Log.d(TAG, "SIGN IN clicked");
            FirebaseAuth.getInstance().signInWithEmailAndPassword("guwno@gmail.com", "123123");
        });

        findViewById(R.id.button_sign_out).setOnClickListener(v -> {
            Log.d(TAG, "SIGN OUT clicked");
            FirebaseAuth.getInstance().signOut();
        });

        Log.d(TAG, "CREATE USER");
        User.createObservable()
                .take(1)
                .subscribe(user -> {
                    if (!user.isSignedIn()) {
                        Intent intent = new Intent(this, LoginActivity.class);
                        Log.d(TAG, "START ACTIVITY");
                        startActivity(intent);
                    }
                });

        /*findViewById(R.id.button_edit_props).setOnClickListener(v -> {
            Log.d(TAG,"Propos edited");
            User.create().subscribe(fou-> {
                if(fou.firebaseUser != null){
                    fou.userProperties.lobbyId = ((EditText)findViewById(R.id.edit_user_props)).getText().toString();
                    fou.userPropertiesPush();
                }
            });
        });*/
        Observable<User> userObservable = User.createObservable()
                .doOnNext(user -> {
                    Log.d(TAG, "userObservable: " + user.toString());
                })
                .doOnSubscribe(disposable -> {
                    Log.d(TAG, "user subscribed");
                });
        Log.d(TAG, "step1");
        Observable<UserProperties> userPropertiesObservable = userObservable
                .flatMap(user -> UserProperties.observeFor(user));
        Log.d(TAG, "step2");
        userPropertiesObservable.subscribe(
                userProperties -> {
                    Log.d(TAG, "USERPROPERTIES1 sub: " + userProperties.toString());
                },
                error -> {
                    throw new OnErrorNotImplementedException(error);
                });
        Log.d(TAG, "step3");
        userPropertiesObservable.subscribe(
                userProperties -> {
                    Log.d(TAG, "USERPROPERTIES2 sub: " + userProperties.toString());
                },
                error -> {
                    throw new OnErrorNotImplementedException(error);
                });
        /*Observable<User> combined = userObservable.combineLatest(userObservable,userPropertiesObservable,(user,userProperties)->{
            user.properties = userProperties;
            return user;
        })
                .doOnNext(user->{
                    Log.d(TAG, "combined: " + user.toString());
                });
        combined.subscribe(user -> {
            Log.d(TAG, "combined: " + user.toString());
        },
                throwable -> {
                    Log.d(TAG,"Error");
                });
*/

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }

    void onRunSchedulerExampleButtonClicked() {
        disposables.add(sampleObservable()
                // Run on a background thread
                .subscribeOn(Schedulers.io())
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<String>() {
                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete()");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError()", e);
                    }

                    @Override
                    public void onNext(String string) {
                        Log.d(TAG, "onNext(" + string + ")");
                    }
                }));
    }
}
