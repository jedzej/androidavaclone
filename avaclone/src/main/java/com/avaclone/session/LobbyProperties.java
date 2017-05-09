package com.avaclone.session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by jedzej on 07.05.2017.
 */

public class LobbyProperties {
    public String leaderId;
    public List<String> usersIds;

    LobbyProperties(){}

    LobbyProperties(String leaderId){
        this.leaderId = leaderId;
        this.usersIds = new ArrayList<String>();
    }
}
