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
import android.util.Log;

import com.avaclone.R;
import com.avaclone.session.Session;
import com.google.firebase.auth.FirebaseAuth;
import com.jakewharton.rxbinding2.view.RxView;

import io.reactivex.disposables.CompositeDisposable;

public class MainActivity extends Activity {
    private static final String TAG = "RxAndroidSamples";

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Log.d(TAG, "on create");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "on resume");

        // Observe session
        disposables.add(
                Session.getObservable()
                        .firstElement()
                        .subscribe(session -> {
                            Log.d(TAG, "session update");
                            if (session.isInLobby()) {
                                Intent intent = new Intent(this, LobbyActivity.class);
                                startActivity(intent);
                            } else if (session.isSignedIn()) {
                                Intent intent = new Intent(this, NoLobbyActivity.class);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(this, LoginActivity.class);
                                startActivity(intent);
                            }
                        }));

        // Observe sign out clicks
        //disposables.add(RxView.clicks(findViewById(R.id.button_sign_out)).subscribe(o -> FirebaseAuth.getInstance().signOut()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }
}
