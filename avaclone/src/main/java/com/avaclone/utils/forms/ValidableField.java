package com.avaclone.utils.forms;


public class ValidableField<T> {

    public interface Validable<T> {
        void validate(T data) throws ValidationFailedException;
    }

    public interface ValidationFailureListener {
        void doOnFailure(Throwable e);
    }

    public interface ValidationSuccessListener<T> {
        void doOnSuccess(T value);
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

    public void validate(ValidationSuccessListener<T> validationSuccessListener){
        validate(validationSuccessListener, null);
    }

    public void validate(ValidationFailureListener validationFailureListener){
        validate(null, validationFailureListener);
    }

    public void validate(ValidationSuccessListener<T> validationSuccessListener, ValidationFailureListener validationFailureListener){
        if(isValid() && validationSuccessListener != null)
            validationSuccessListener.doOnSuccess(getValue());
        if(!isValid() && validationFailureListener != null)
            validationFailureListener.doOnFailure(getError());
    }

}