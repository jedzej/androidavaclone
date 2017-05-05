package com.avaclone.session.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.avaclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;

import java.util.ArrayList;
import java.util.List;


import durdinapps.rxfirebase2.RxFirebaseAuth;
import io.reactivex.Observable;
import io.reactivex.android.plugins.RxAndroidPlugins;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {

    public static class SignInFormData {
        protected String email;
        protected String password;
        protected boolean submitClicked;

        protected boolean emailIsValid = false;
        protected boolean passwordIsValid = false;

        protected String emailError;
        protected String passwordError;

        private boolean isEmailValid(String email) {
            //TODO: Replace this with your own logic
            return email.contains("@");
        }

        private boolean isPasswordValid(String password) {
            //TODO: Replace this with your own logic
            return password.length() > 4;
        }

        protected SignInFormData(Context ctx, String email, String password) {
            this.email = email;
            this.password = password;

            if (TextUtils.isEmpty(email))
                emailError = ctx.getString(R.string.error_field_required);
            else if (!isEmailValid(email))
                emailError = ctx.getString(R.string.error_invalid_email);
            else
                emailIsValid = true;

            if (TextUtils.isEmpty(password))
                passwordError = ctx.getString(R.string.error_field_required);
            else if (!isPasswordValid(password))
                passwordError = ctx.getString(R.string.error_invalid_password);
            else
                passwordIsValid = true;
        }
    }
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);

        mPasswordView.setOnEditorActionListener((TextView textView, int id, KeyEvent keyEvent) -> {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);

        RxView.clicks(mEmailSignInButton).subscribe(o -> {
            attemptLogin();
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);


        Observable.combineLatest(
                Observable.just(mEmailView.getText().toString()),
                Observable.just(mPasswordView.getText().toString()),
                (emailVal, passwordVal) -> new SignInFormData(this, emailVal, passwordVal)
        )
                // prompt errors
                .doOnNext(signInFormData -> {
                    if (!signInFormData.emailIsValid) {
                        mEmailView.setError(signInFormData.emailError);
                        mEmailView.requestFocus();
                    }
                    if (!signInFormData.passwordIsValid) {
                        mPasswordView.setError(signInFormData.passwordError);
                        mPasswordView.requestFocus();
                    }
                })
                // filter only fully validated form
                .filter(signInFormData -> signInFormData.emailIsValid && signInFormData.passwordIsValid)
                //
                .flatMap(signInFormData -> {
                    showProgress(true);
                    return RxFirebaseAuth.signInWithEmailAndPassword(FirebaseAuth.getInstance(), signInFormData.email, signInFormData.password)
                            .map(authResult -> authResult.getUser() != null)
                            .defaultIfEmpty(false)
                            .toObservable();
                })
                .subscribe(isSignedIn -> {
                            Log.d("SIGNED IN", "is signed: " + isSignedIn);
                            finish();
                        },
                        error -> {
                            if (error instanceof FirebaseAuthInvalidUserException) {
                                Log.d("NOT SIGNED IN:", "Invalid user: " + error.getMessage());
                                mEmailView.setError(getString(R.string.error_user_does_not_exist));
                                mEmailView.requestFocus();
                            } else if (error instanceof FirebaseAuthInvalidCredentialsException) {
                                Log.d("NOT SIGNED IN:", "Invalid password: " + error.getMessage());
                                mPasswordView.setError(getString(R.string.error_incorrect_password));
                                mPasswordView.requestFocus();
                            } else {
                                Log.d("NOT SIGNED IN: ", "unknown error" + error.getMessage());
                            }
                            showProgress(false);
                        });
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
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
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}

