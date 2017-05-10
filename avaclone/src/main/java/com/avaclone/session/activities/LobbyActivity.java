package com.avaclone.session.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.avaclone.R;
import com.avaclone.session.lobby.LobbyStore;
import com.avaclone.session.lobby.Lobby;
import com.avaclone.session.user.UserProperties;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

public class LobbyActivity extends Activity {
    enum RX {
        User,
        Lobby
    }

    ;

    private final CompositeDisposable disposables = new CompositeDisposable();
    private EditText mEditTextLobbyIdView;
    private View mButtonLobbyDropView;
    private View mButtonLobbyLeaveView;
    private View mButtonLobbyGameStartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        mEditTextLobbyIdView = (EditText) findViewById(R.id.edit_text_lobby_id);
        mButtonLobbyDropView = findViewById(R.id.button_lobby_drop);
        mButtonLobbyLeaveView = findViewById(R.id.button_lobby_leave);
        mButtonLobbyGameStartView = findViewById(R.id.button_lobby_game_start);

    }

    private Observable<Lobby> observeLobby() {
        return UserOld.getUserWithProperties().flatMap(u -> LobbyStore.getObservable(u.properties));
    }

    private Observable<Observable<UserProperties>> observeUsers() {
        return observeLobby()
                .map(lobby -> Observable.defer(() -> Observable.fromIterable(lobby.usersIds))
                        .flatMap(userIdObservable -> UserOld.getObservableProperties(userIdObservable))
                        .take(lobby.usersIds.size())
                );
    }

    @Override
    protected void onResume() {
        super.onResume();
        Observable<Boolean> isLeaderObservable = Observable.combineLatest(
                UserOld.getUserWithProperties(),
                observeLobby(),
                (u, l) -> u.properties.userId.equals(l.leaderId));

        disposables.add(isLeaderObservable.subscribe(isLeaderBoolean -> {
                    initCommon();
                    if (isLeaderBoolean) {
                        initForLeader();
                    } else {
                        initForMember();
                    }
                },
                error -> finish()));
    }

    private void initCommon() {
        observeLobby().subscribe(
                lobby -> {
                    mEditTextLobbyIdView.setText(lobby.leaderId);
                    observeUsers()
                            .subscribe(userPropertiesIterable -> {
                                userPropertiesIterable.subscribe(
                                        properties -> Log.d("LOBBY MEMBERS", properties.username),
                                        throwable -> {
                                        },
                                        () -> Log.d("DONE", "DONE"));
                            });
                }
        );

    }

    private void initForLeader() {
        mButtonLobbyGameStartView.setVisibility(View.VISIBLE);
        mButtonLobbyDropView.setVisibility(View.VISIBLE);
        mButtonLobbyLeaveView.setVisibility(View.GONE);

    }

    private void initForMember() {
        mButtonLobbyGameStartView.setVisibility(View.GONE);
        mButtonLobbyDropView.setVisibility(View.GONE);
        mButtonLobbyLeaveView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }
}
