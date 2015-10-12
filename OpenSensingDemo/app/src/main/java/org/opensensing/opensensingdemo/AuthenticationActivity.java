package org.opensensing.opensensingdemo;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;


/**
 * Created by arks on 27/09/15.
 */
public class AuthenticationActivity extends Activity {

    private static String APP_KEY = "";
    private static String APP_SECRET = "";

    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCESS_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";

    DropboxAPI<AndroidAuthSession> mApi;

    private boolean mLoggedIn;

    private Button linkToPdsButton;

    private LocalFunfManager localFunfManager;

    public static boolean isActive;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isActive = false;
        APP_KEY = this.getString(R.string.DROPBOX_APP_KEY);
        APP_SECRET = this.getString(R.string.DROPBOX_APP_SECRET);

        AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);

        localFunfManager = LocalFunfManager.getLocalFunfManager(this);
        localFunfManager.start();

        setContentView(R.layout.activity_authentication);
        linkToPdsButton = (Button) findViewById(R.id.linkToPdsButton);
        linkToPdsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLoggedIn) {
                    logOut();
                } else {
                    mApi.getSession().startOAuth2Authentication(AuthenticationActivity.this);
                }
            }
        });

        setLoggedIn(mApi.getSession().isLinked());
    }

    @Override
    protected void onResume() {
        super.onResume();
        AndroidAuthSession session = mApi.getSession();

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(getResources().getInteger(R.integer.notification_id));

        // The next part must be inserted in the onResume() method of the
        // activity from which session.startAuthentication() was called, so
        // that Dropbox authentication completes properly.
        if (session.authenticationSuccessful()) {
            try {
                // Mandatory call to complete the auth
                session.finishAuthentication();

                // Store it locally in our app for later use
                storeAuth(session);
                setLoggedIn(true);
            } catch (IllegalStateException e) {
                showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
                Log.i(MainActivity.TAG, "Error authenticating", e);
            }
        }
        isActive = true;
    }

    protected void onPause() {
        super.onPause();
        isActive = false;
    }

    protected void onDestroy() {
        super.onDestroy();
        localFunfManager.destroy();
    }

    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     */
    private void storeAuth(AndroidAuthSession session) {
        // Store the OAuth 2 access token, if there is one.
        String oauth2AccessToken = session.getOAuth2AccessToken();
        if (oauth2AccessToken != null) {
            SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, "oauth2:");
            edit.putString(ACCESS_SECRET_NAME, oauth2AccessToken);
            edit.commit();
            Log.i(MainActivity.TAG, "oauth2 token: " + oauth2AccessToken);
            localFunfManager.setAuthToken(oauth2AccessToken);
            return;
        }
    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }

    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);

        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        loadAuth(session);
        return session;
    }

    private void loadAuth(AndroidAuthSession session) {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key == null || secret == null || key.length() == 0 || secret.length() == 0) return;

        if (key.equals("oauth2:")) {
            // If the key is set to "oauth2:", then we can assume the token is for OAuth 2.
            session.setOAuth2AccessToken(secret);
        } else {
            // Still support using old OAuth 1 tokens.
            session.setAccessTokenPair(new AccessTokenPair(key, secret));
        }
    }

    private void logOut() {
        // Remove credentials from the session
        mApi.getSession().unlink();
        // Clear our stored keys
        clearKeys();
        // Change UI state to display logged out version
        setLoggedIn(false);
    }

    private void clearKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
        localFunfManager.setAuthToken("");
    }

    private void setLoggedIn(boolean loggedIn) {
        //TODO only link when server is set
        mLoggedIn = loggedIn;
        if (loggedIn) {
            linkToPdsButton.setText("Unlink from openPDS");
        } else {
            linkToPdsButton.setText("Link with openPDS");
        }
    }
}
