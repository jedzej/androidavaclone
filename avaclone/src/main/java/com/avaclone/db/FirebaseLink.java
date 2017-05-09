package com.avaclone.db;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by jedzej on 08.05.2017.
 */

public class FirebaseLink {
    private static DatabaseReference root;

    public void setRoot(String rootPath){
        root = FirebaseDatabase.getInstance().getReference(rootPath);
    }

    public static DatabaseReference getRoot(){
        if(root == null)
            root = FirebaseDatabase.getInstance().getReference();
        return root;
    }
}
