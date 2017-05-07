package com.avaclone.session.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;

import durdinapps.rxfirebase2.RxFirebaseAuth;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends Activity {
    enum RegisterFields {
        EMAIL_FIELD,
        PASSWORD_FIELD,
        USERNAME_FIELD
    }

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mRegisterFormView;

    private final CompositeDisposable disposables = new CompositeDisposable();

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

        Observable<ValidableField> emailValidated = RxTextView.textChangeEvents(mEmailView)
                .map(emailChanged ->
                        new ValidableField<>(emailChanged.text().toString(), email -> {
                            if (TextUtils.isEmpty(email))
                                throw new ValidationFailedException(getString(R.string.error_field_required));
                            else if (!email.contains("@"))
                                throw new ValidationFailedException(getString(R.string.error_invalid_email));
                        })
                );

        Observable<ValidableField> passwordValidated = RxTextView.textChangeEvents(mPasswordView)
                .map(passwordChanged ->
                        new ValidableField<>(passwordChanged.text(), password -> {
                            if (TextUtils.isEmpty(password))
                                throw new ValidationFailedException(getString(R.string.error_field_required));
                            else if (password.length() < 4)
                                throw new ValidationFailedException(getString(R.string.error_invalid_password));
                        })
                );

        Observable<ValidableField> usernameValidated = RxTextView.textChangeEvents(mUsernameView)
                .map(usernameChanged ->
                        new ValidableField<>(usernameChanged.text(), username -> {
                            if (TextUtils.isEmpty(username))
                                throw new ValidationFailedException(getString(R.string.error_field_required));
                        })
                );

        disposables.add(emailValidated
                .subscribe(validableField -> {
                    if (!validableField.isValid())
                        mEmailView.setError(validableField.getError().getMessage());
                }));

        disposables.add(passwordValidated
                .subscribe(validableField -> {
                    if (!validableField.isValid())
                        mPasswordView.setError(validableField.getError().getMessage());
                }));

        disposables.add(usernameValidated
                .subscribe(validableField -> {
                    if (!validableField.isValid())
                        mUsernameView.setError(validableField.getError().getMessage());
                }));

        disposables.add(RxView.clicks(findViewById(R.id.register_button)).flatMap((Object o) -> Observable.zip(
                emailValidated,
                passwordValidated,
                usernameValidated,
                (e, p, u) -> {
                    ValidableForm form = new ValidableForm();
                    form.addField(RegisterFields.EMAIL_FIELD, e);
                    form.addField(RegisterFields.PASSWORD_FIELD, p);
                    form.addField(RegisterFields.USERNAME_FIELD, u);
                    return form;
                }))
                .firstElement()
                .subscribe(form -> {
                    if (form.isValid()) {
                        Log.d("VALIDATION", "IS VALID");
                        attemptRegister(form);
                    } else
                        Log.d("VALIDATION", "IS INVALID");
                }));

    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptRegister(ValidableForm form) {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        disposables.add(Observable.just(form)
                .flatMapMaybe(validableForm -> {
                    showProgress(true);
                    String email = validableForm.getValue(RegisterFields.EMAIL_FIELD).toString();
                    String password = validableForm.getValue(RegisterFields.PASSWORD_FIELD).toString();

                    // create user via auth service
                    return RxFirebaseAuth.createUserWithEmailAndPassword(FirebaseAuth.getInstance(), email, password);
                })
                .flatMapMaybe(authResult -> User.getMaybe())
                .flatMapMaybe(user -> {
                    user.createProperties();
                    user.properties.username = form.getValue(RegisterFields.USERNAME_FIELD).toString();
                    return user.commitProperties();
                })
                .subscribe(userProperties -> finish(),
                        error -> {
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

