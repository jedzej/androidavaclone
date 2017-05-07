package com.avaclone.utils.forms;


public class ValidationFailedException extends Exception {
    public ValidationFailedException(String message){
        super(message);
    }
}