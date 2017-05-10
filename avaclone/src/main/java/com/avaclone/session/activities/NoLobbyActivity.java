package com.avaclone.session.activities;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.avaclone.R;
import com.avaclone.session.lobby.LobbyStore;
import com.jakewharton.rxbinding2.view.RxView;

import io.reactivex.disposables.CompositeDisposable;

public class NoLobbyActivity extends Activity {

    private final CompositeDisposable disposables = new CompositeDisposable();
    private EditText mLobbyIdView;
    private View mButtonLobbyCreateView;
    private View mButtonLobbyJoinView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_lobby);

        mLobbyIdView = (EditText) findViewById(R.id.edit_lobby_id);
        mButtonLobbyCreateView = findViewById(R.id.button_lobby_create);
        mButtonLobbyJoinView = findViewById(R.id.button_lobby_join);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // check if user is logged in
        disposables.add(UserOld.getUserWithProperties()
                .map(userWithProperties -> {
                    if(userWithProperties.properties.lobbyId != null)
                        throw new LobbyStore.LobbyError("Already in a lobby");
                    else
                        return userWithProperties;
                }).subscribe(userWithProperties -> {
                    RxView.clicks(mButtonLobbyCreateView)
                            .flatMapSingle(o -> LobbyStore.create(userWithProperties.properties))
                            .subscribe(lobby -> {
                                Log.d("LOBBY","LobbyStore created");
                            });
                },
                error ->finish()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }
}
