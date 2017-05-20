package com.avaclone.session.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.avaclone.R;
import com.avaclone.session.Session;
import com.avaclone.session.SessionManager;
import com.avaclone.utils.forms.ValidableField;
import com.avaclone.utils.forms.ValidableForm;
import com.avaclone.utils.forms.validators.EmailValidator;
import com.avaclone.utils.forms.validators.NonEmptyValidator;
import com.avaclone.utils.forms.validators.PasswordValidator;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends Activity {

    enum RegisterFields {
        EMAIL,
        PASSWORD,
        USERNAME
    }

    private final CompositeDisposable disposables = new CompositeDisposable();
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mRegisterFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mUsernameView = (EditText) findViewById(R.id.username);
        mRegisterFormView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.register_progress);

        disposables.add(Session.getObservable().subscribe(
                session -> {
                    if (session.isSignedIn()) {
                        finish();
                    } else {
                        initialize();
                    }
                }
        ));
    }

    private void initialize() {
        disposables.add(
                RxView.clicks(findViewById(R.id.register_button))
                        .withLatestFrom(getForm().doOnNext(this::propagateErrors),
                                (o, validableForm) -> validableForm)
                        // pass though only valid form
                        .filter(validableForm -> validableForm.isValid())
                        .subscribe(form -> attemptRegister(form)));
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

    private Observable<ValidableField> getUsernameObservable() {
        return RxTextView.textChangeEvents(mUsernameView)
                .map(view -> view.text().toString())
                .map(username -> new ValidableField(username, new NonEmptyValidator()));
    }

    private Observable<ValidableForm> getForm() {
        return Observable.combineLatest(
                getEmailObservable(),
                getPasswordObservable(),
                getUsernameObservable(),
                (e, p, u) -> {
                    ValidableForm form = new ValidableForm();
                    form.addField(RegisterFields.EMAIL, e);
                    form.addField(RegisterFields.PASSWORD, p);
                    form.addField(RegisterFields.USERNAME, u);
                    return form;
                });
    }

    private void propagateErrors(ValidableForm validableForm) {
        validableForm.getField(RegisterFields.EMAIL).validate((Throwable e) -> {
            mEmailView.setError(e.getMessage());
            //mEmailView.requestFocus();
        });
        validableForm.getField(RegisterFields.PASSWORD).validate((Throwable e) -> {
            mPasswordView.setError(e.getMessage());
            //mPasswordView.requestFocus();
        });
        validableForm.getField(RegisterFields.USERNAME).validate((Throwable e) -> {
            mUsernameView.setError(e.getMessage());
            //mUsernameView.requestFocus();
        });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptRegister(ValidableForm form) {

        // Reset errors
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mUsernameView.setError(null);

        showProgress(true);

        String email = form.getValue(RegisterFields.EMAIL).toString();
        String password = form.getValue(RegisterFields.PASSWORD).toString();
        String username = form.getValue(RegisterFields.USERNAME).toString();

        disposables.add(
                SessionManager.createUser(email, password)
                        .andThen(SessionManager.createUserProperties(username))
                        .subscribe(() -> {
                                    Log.i("REGISTER ACTIVITY", "User " + username + " created");
                                },
                                error -> {
                                    Log.i("REGISTER ACTIVITY", "User " + username + " not created");
                                    Log.i("REGISTER ACTIVITY", error.getMessage());
                                    if (error instanceof FirebaseAuthInvalidCredentialsException) {
                                        mEmailView.setError(error.getMessage());
                                    } else if (error.getMessage().contains("WEAK_PASSWORD")) {
                                        mPasswordView.setError("Password is too weak");
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

        mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mRegisterFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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

