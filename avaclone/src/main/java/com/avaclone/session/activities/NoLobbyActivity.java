package com.avaclone.session.activities;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.avaclone.R;
import com.avaclone.session.Session;
import com.avaclone.session.lobby.LobbyStore;
import com.jakewharton.rxbinding2.view.RxView;

import io.reactivex.disposables.CompositeDisposable;

public class NoLobbyActivity extends Activity {

    private final CompositeDisposable disposables = new CompositeDisposable();
    private EditText mLobbyIdView;
    private View mButtonLobbyCreateView;
    private View mButtonLobbyJoinView;
    private View mButtonSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_lobby);

        mLobbyIdView = (EditText) findViewById(R.id.edit_lobby_id);
        mButtonLobbyCreateView = findViewById(R.id.button_lobby_create);
        mButtonLobbyJoinView = findViewById(R.id.button_lobby_join);
        mButtonSignOut = findViewById(R.id.no_lobby_button_sign_out);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // check if user is logged in
        disposables.add(Session.getObservable()
                .subscribe(session -> {
                            if (!session.isSignedIn() || session.isInLobby())
                                finish();
                            else
                                disposables.add(RxView.clicks(mButtonLobbyCreateView)
                                        .flatMapCompletable(o -> LobbyStore.create(session.userProperties))
                                        .subscribe(() -> Log.d("LOBBY", "LobbyStore created"),
                                                throwable -> Log.d("LOBBY", "LobbyStore not created: " + throwable.getMessage()))
                                );
                        },
                        error -> finish()));

        disposables.add(RxView.clicks(mButtonSignOut)
                .doOnNext(o -> Log.v("NO LOBBY ACTIVITY","Sing out clicked"))
                .subscribe(o -> Session.signOut()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }
}
