package com.avaclone.session.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.app.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.avaclone.R;
import com.avaclone.session.Session;
import com.avaclone.session.SessionManager;
import com.avaclone.utils.forms.ValidableField;
import com.avaclone.utils.forms.ValidableForm;
import com.avaclone.utils.forms.validators.EmailValidator;
import com.avaclone.utils.forms.validators.PasswordValidator;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;


import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.exceptions.OnErrorNotImplementedException;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {

    enum LoginFields {
        EMAIL,
        PASSWORD
    }

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private View mSignInButton;
    private View mGoToRegistrationButton;

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mSignInButton = findViewById(R.id.sign_in_button);
        mGoToRegistrationButton = findViewById(R.id.go_to_registration_button);
    }


    @Override
    protected void onResume() {
        super.onResume();

        // check if user is logged in
        disposables.add(Session.getObservable().subscribe(
                session -> {
                    if (session.isSignedIn()) {
                        finish();
                    } else {
                        initialize();
                    }
                },
                throwable -> {
                    throw new OnErrorNotImplementedException(throwable);
                }

        ));
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin(ValidableForm form) {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        disposables.add(Observable.just(form)
                .flatMapCompletable(validableForm -> {
                    showProgress(true);
                    String email = validableForm.getValue(LoginFields.EMAIL).toString();
                    String password = validableForm.getValue(LoginFields.PASSWORD).toString();

                    // create user via auth service
                    return SessionManager.signIn(email, password);
                })
                .subscribe(() -> {},
                        error -> {
                            if (error instanceof FirebaseAuthInvalidUserException) {
                                mEmailView.setError(getString(R.string.error_user_does_not_exist));
                                mEmailView.requestFocus();
                            } else if (error instanceof FirebaseAuthInvalidCredentialsException) {
                                mPasswordView.setError(getString(R.string.error_incorrect_password));
                                mPasswordView.requestFocus();
                            } else {
                                mEmailView.setError(error.getMessage());
                            }
                            showProgress(false);
                        }));
    }





    private void initialize() {
        disposables.add(RxView.clicks(mGoToRegistrationButton).subscribe(o -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        }));

        disposables.add(
                RxView.clicks(mSignInButton)
                        .withLatestFrom(getForm().doOnNext(this::propagateErrors),
                                (o, validableForm) -> validableForm)
                        // pass though only valid form
                        .filter(validableForm -> validableForm.isValid())
                        .subscribe(form -> attemptLogin(form)));
    }

    private Observable<ValidableField> getPasswordObservable() {
        return RxTextView.textChangeEvents(mPasswordView)
                .map(view -> view.text().toString())
                .map(password -> new ValidableField(password, new PasswordValidator()));
    }

    private Observable<ValidableField> getEmailObservable() {
        return RxTextView.textChangeEvents(mEmailView)
                .map(view -> view.text().toString())
                .map(email -> new ValidableField(email, new EmailValidator()));
    }

    private Observable<ValidableForm> getForm() {
        return Observable.combineLatest(
                getEmailObservable(),
                getPasswordObservable(),
                (e, p) -> {
                    ValidableForm form = new ValidableForm();
                    form.addField(LoginFields.EMAIL, e);
                    form.addField(LoginFields.PASSWORD, p);
                    return form;
                });
    }

    private void propagateErrors(ValidableForm validableForm) {
        validableForm.getField(LoginFields.EMAIL).validate((Throwable e) -> {
            mEmailView.setError(e.getMessage());
            //mEmailView.requestFocus();
        });
        validableForm.getField(LoginFields.PASSWORD).validate((Throwable e) -> {
            mPasswordView.setError(e.getMessage());
            //mPasswordView.requestFocus();
        });
    }





    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }
}

