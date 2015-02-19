package com.example.bogdan.dropboxphoto;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;

public class MainActivity extends Activity {

    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCES_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    private final String APP_KEY = "e7r6jtptl6t3rz9";
    private final String APP_SECRET = "qqfvu5wtkqft9uz";
    public static DropboxAPI<AndroidAuthSession> mDBApi;
    private Boolean isLoggedIn;
    LoginClass loginClass = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        /*AndroidAuthSession session = buildSession();
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);//+*/
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        loginClass = new LoginClass();
        loginClass.makingSession(key, secret);
    }

    @Override
    protected void onResume() {
        super.onResume();
       /* if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                mDBApi.getSession().finishAuthentication();
                String accessToken = mDBApi.getSession().getOAuth2AccessToken();
                if (accessToken != null) {
                    SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
                    Editor edit = prefs.edit();
                    edit.putString(ACCESS_KEY_NAME, "oauth2:");
                    edit.putString(ACCESS_SECRET_NAME, accessToken);
                    edit.commit();
                    isLoggedIn = true;
                    return;
                }
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }*/
    }

    public void onClickCameraButton(View view) {
        if (isLoggedIn) {
            Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
        } else {
            Toast toast = Toast.makeText(this, "Please Login to Dropbox first", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void loadAuth(AndroidAuthSession session) { //+
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key == null || secret == null || key.length() == 0 || secret.length() == 0) {
            isLoggedIn = false;
            return;
        } else {
            session.setOAuth2AccessToken(secret);
            isLoggedIn = true;
        }

    }

    private AndroidAuthSession buildSession() {  //+
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        loadAuth(session);

        return session;
    }

    public void onClickLogin(View view) {
        /*SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);*/
        loginClass.mDBApi.getSession().startOAuth2Authentication(MainActivity.this);

    }
}