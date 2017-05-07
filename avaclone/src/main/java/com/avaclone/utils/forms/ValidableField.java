package com.avaclone.utils.forms;


public class ValidableField<T> {

    public interface Validable<T> {
        void validate(T data) throws ValidationFailedException;
    }

    private final T value;
    private Throwable error;

    T getValue() {
        return value;
    }

    public Throwable getError(){
        return error;
    }

    public Boolean isValid(){
        return error == null;
    }

    public ValidableField(T value, Validable<T> validable){
        this.value = value;
        try {
            validable.validate(value);
        } catch(ValidationFailedException vfe){
            this.error = vfe;
        }
    }
}