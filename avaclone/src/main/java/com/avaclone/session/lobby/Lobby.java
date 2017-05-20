package com.avaclone.session.lobby;

import com.avaclone.db.store.Storable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jedzej on 07.05.2017.
 */

public class Lobby implements Storable {
    public String leaderId;
    public List<String> usersIds;

    Lobby(){}

    Lobby(String leaderId){
        this.leaderId = leaderId;
        this.usersIds = new ArrayList();
        this.usersIds.add(leaderId);
    }

    public String retrieveId(){
        return leaderId;
    }

    @Override
    public boolean exists() {
        return leaderId != null && usersIds != null;
    }
}
