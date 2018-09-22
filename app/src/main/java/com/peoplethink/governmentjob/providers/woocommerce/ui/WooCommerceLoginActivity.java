package com.peoplethink.governmentjob.providers.woocommerce.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.peoplethink.governmentjob.Config;
import com.peoplethink.governmentjob.HolderActivity;
import com.peoplethink.governmentjob.R;
import com.peoplethink.governmentjob.providers.woocommerce.WooCommerceTask;
import com.peoplethink.governmentjob.providers.woocommerce.model.CredentialStorage;
import com.peoplethink.governmentjob.providers.woocommerce.model.RestAPI;
import com.peoplethink.governmentjob.providers.woocommerce.model.users.User;
import com.peoplethink.governmentjob.util.Helper;
import com.peoplethink.governmentjob.util.Log;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A login screen that offers login via email/password.
 */
public class WooCommerceLoginActivity extends AppCompatActivity {

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_woocommerce_login);
        // Set up the login form.
        mEmailView = findViewById(R.id.user);

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = findViewById(R.id.user_sign_in_button);
        TextView mRegisterButton = findViewById(R.id.user_register_button);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                RestAPI api = new RestAPI(WooCommerceLoginActivity.this);
                HolderActivity.startWebViewActivity(WooCommerceLoginActivity.this,
                        api.getHost() + api.getRegister(),
                        Config.OPEN_EXPLICIT_EXTERNAL,
                        false, null);

            }
        });

        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
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

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            attemptLogin(email, password);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 3;
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

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    void attemptLogin(final String mEmail, final String mPassword) {
        RequestBody requestBody = new FormBody.Builder()
                .add("log", mEmail)
                .add("pwd", mPassword)
                .build();

        final RestAPI api = new RestAPI(getApplication());

        Request request = new Request.Builder()
                .url(api.getHost() + api.getLogin())
                .post(requestBody)
                .build();

        Log.i("INFO", "Requesting: " + request.url());

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(25, TimeUnit.SECONDS)
                .writeTimeout(25, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Helper.isOnlineShowDialog(WooCommerceLoginActivity.this);
                    }
                });
                loginAttemptCompleted(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                response.close();

                List<String> cookies = response.headers().values("Set-Cookie");
                for (String cookie : cookies){
                    Log.i("Cookie", cookie);
                    if (cookie.startsWith(api.getLoginCookie())) {
                        retrieveUserData(mEmail, mPassword);
                        return;
                    }
                }

                //No login cookie was found
                loginAttemptCompleted(false);
            }
        });
    }

    public void retrieveUserData(final String mEmail, final String mPassword){
        WooCommerceTask.WooCommerceBuilder builder = new WooCommerceTask.WooCommerceBuilder(this);
        builder.getUsers(new WooCommerceTask.Callback<User>() {
            @Override
            public void success(ArrayList<User> users) {
                if (users.size() == 1){
                    User user = users.get(0);
                    CredentialStorage.saveCredentials(WooCommerceLoginActivity.this,
                            mEmail,
                            mPassword,
                            user.getId(),
                            user.getFirstName());
                    loginAttemptCompleted(true);
                } else {
                    if (users.size() == 0){
                        Log.e("INFO", "No Customers found with this email. Perhaps this person is a user, but not a customer");
                    } else {
                        Log.e("INFO", "More than 1 Customer found with this email");
                    }
                    loginAttemptCompleted(false);
                }
            }

            @Override
            public void failed() {
                loginAttemptCompleted(false);
            }
        }, mEmail).execute();
    }


    public void loginAttemptCompleted(final Boolean success) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                showProgress(false);

                if (success) {
                    finish();
                } else {
                    mEmailView.setError(getString(R.string.error_incorrect_credentials));
                    mPasswordView.setError(getString(R.string.error_incorrect_credentials));
                    mPasswordView.requestFocus();
                }
            }
        });
    }
}

