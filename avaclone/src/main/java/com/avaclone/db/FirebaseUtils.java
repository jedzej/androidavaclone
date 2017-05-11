package com.avaclone.db;

import android.util.Log;

import com.google.android.gms.tasks.Task;

import io.reactivex.Completable;

/**
 * Created by jedzej on 09.05.2017.
 */

public class FirebaseUtils {

    public static Completable CompletableFromTask(Task t){
        return Completable.create(completableEmitter -> {
            t.addOnCompleteListener(task -> {
                        if (task.isSuccessful())
                            completableEmitter.onComplete();
                        else
                            completableEmitter.onError(task.getException());
                    });
        });
    }
}
