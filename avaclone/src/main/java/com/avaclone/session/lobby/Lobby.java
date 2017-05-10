package com.avaclone.session.lobby;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jedzej on 07.05.2017.
 */

public class Lobby {
    public String leaderId;
    public List<String> usersIds;

    Lobby(){}

    Lobby(String leaderId){
        this.leaderId = leaderId;
        this.usersIds = new ArrayList<String>();
    }

    public boolean isValid(){
        return leaderId != null && usersIds != null;
    }

    public String getId(){
        return leaderId;
    }
}
