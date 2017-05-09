package com.avaclone.session.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.app.Activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.avaclone.R;
import com.avaclone.session.User;
import com.avaclone.utils.forms.ValidableField;
import com.avaclone.utils.forms.ValidableForm;
import com.avaclone.utils.forms.ValidationFailedException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;


import durdinapps.rxfirebase2.RxFirebaseAuth;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

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

        // email validated observable
        Observable<ValidableField> emailValidated = RxTextView.textChangeEvents(mEmailView)
                .map(emailChanged ->
                        new ValidableField<>(emailChanged.text().toString(), email -> {
                            if (TextUtils.isEmpty(email))
                                throw new ValidationFailedException(getString(R.string.error_field_required));
                            else if (!email.contains("@"))
                                throw new ValidationFailedException(getString(R.string.error_invalid_email));
                        })
                );

        // password validated observable
        Observable<ValidableField> passwordValidated = RxTextView.textChangeEvents(mPasswordView)
                .map(passwordChanged ->
                        new ValidableField<>(passwordChanged.text(), password -> {
                            if (TextUtils.isEmpty(password))
                                throw new ValidationFailedException(getString(R.string.error_field_required));
                            else if (password.length() < 4)
                                throw new ValidationFailedException(getString(R.string.error_invalid_password));
                        })
                );

        // check if user is logged in
        disposables.add(User.getObservableUser().subscribe(user ->finish(),
                error -> {
                    // if no user subscribe for login attempts
                    RxView.clicks(mSignInButton).subscribe((Object o) -> Observable.zip(
                            emailValidated,
                            passwordValidated,
                            (e,p) -> {
                                ValidableForm form = new ValidableForm();
                                form.addField(LoginFields.EMAIL, e);
                                form.addField(LoginFields.PASSWORD, p);

                                if(!e.isValid()){
                                    mEmailView.setError(e.getError().getMessage());
                                    mEmailView.requestFocus();
                                }
                                if(!p.isValid()){
                                    mPasswordView.setError(p.getError().getMessage());
                                    mPasswordView.requestFocus();
                                }
                                return form;
                            })
                            .firstOrError()
                            .subscribe(form -> {
                                if(form.isValid()){
                                    attemptLogin(form);
                                }
                            }));

                    // subscribe for registration request
                    disposables.add(RxView.clicks(mGoToRegistrationButton).subscribe(o -> {
                        Intent intent = new Intent(this, RegisterActivity.class);
                        startActivity(intent);
                    }));
                }));
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
                .flatMap(validableForm -> {
                    showProgress(true);
                    String email = validableForm.getValue(LoginFields.EMAIL).toString();
                    String password = validableForm.getValue(LoginFields.PASSWORD).toString();

                    // create user via auth service
                    return User.signInWithEmailAndPassword(email, password);
                })
                .subscribe(userProperties -> finish(),
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

