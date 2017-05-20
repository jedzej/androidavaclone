package com.avaclone.session.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.avaclone.R;
import com.avaclone.session.Session;
import com.avaclone.session.lobby.LobbyStore;

import io.reactivex.disposables.CompositeDisposable;

public class LobbyActivity extends Activity {

    private final CompositeDisposable disposables = new CompositeDisposable();
    private EditText mEditTextLobbyIdView;
    private View mButtonLobbyDropView;
    private View mButtonLobbyLeaveView;
    private View mButtonLobbyGameStartView;
    private Session.SessionData lastSessionData = null;

    private static final String TAG = "LobbyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        Log.d(TAG,"onCreate");

        mEditTextLobbyIdView = (EditText) findViewById(R.id.edit_text_lobby_id);
        mButtonLobbyDropView = findViewById(R.id.button_lobby_drop);
        mButtonLobbyLeaveView = findViewById(R.id.button_lobby_leave);
        mButtonLobbyGameStartView = findViewById(R.id.button_lobby_game_start);

    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");

        disposables.add(Session.getObservable().subscribe(session -> {
                    if (!session.isInLobby())
                        finish();
                    else {
                        initCommon(session);
                        if (session.isLeader()) {
                            initForLeader(session);
                        } else {
                            initForMember(session);
                        }
                    }
                    lastSessionData = session;
                },
                error -> finish()));
    }

    private void initCommon(Session.SessionData sessionData) {
        if(lastSessionData == null || !sessionData.lobby.retrieveId().equals(lastSessionData.lobby.retrieveId())) {
            mEditTextLobbyIdView.setText(sessionData.lobby.retrieveId());
        }
    }

    private void initForLeader(Session.SessionData sessionData) {
        if(lastSessionData == null || sessionData.isLeader() && !lastSessionData.isLeader()) {
            mButtonLobbyGameStartView.setVisibility(View.VISIBLE);
            mButtonLobbyDropView.setVisibility(View.VISIBLE);
            mButtonLobbyLeaveView.setVisibility(View.GONE);
        }
    }

    private void initForMember(Session.SessionData sessionData) {
        if(lastSessionData == null || !sessionData.isLeader() && lastSessionData.isLeader()) {
            mButtonLobbyGameStartView.setVisibility(View.GONE);
            mButtonLobbyDropView.setVisibility(View.GONE);
            mButtonLobbyLeaveView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
        Log.d(TAG,"onDestroy");
    }
}
