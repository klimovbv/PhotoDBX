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
    private Boolean isLoggedIn = false;
    LoginClass loginClass = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        Log.d ("myLogs", key + " _  " + secret);
        loginClass = new LoginClass();
        loginClass.makingSession(key, secret);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (loginClass.mDBApi.getSession().authenticationSuccessful()) {
            try {
                loginClass.mDBApi.getSession().finishAuthentication();
                String accessToken = loginClass.mDBApi.getSession().getOAuth2AccessToken();
                if (accessToken != null) {
                    SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
                    Editor edit = prefs.edit();
                    edit.putString(ACCESS_KEY_NAME, "oauth2:");
                    edit.putString(ACCESS_SECRET_NAME, accessToken);
                    edit.commit();
                    loginClass.isLoggedIn = true;
                    return;
                }
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }
    }

    public void onClickCameraButton(View view) {
        if (loginClass.isLoggedIn) {
            Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
        } else {
            Toast toast = Toast.makeText(this, "Please Login to Dropbox first", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public void onClickLogin(View view) {
        if (loginClass.isLoggedIn) {
            Toast toast = Toast.makeText(this, "You have already logged in", Toast.LENGTH_LONG);
            toast.show();
        } else {
            loginClass.mDBApi.getSession().startOAuth2Authentication(MainActivity.this);
        }
    }

    public void onClickVideoButton(View view) {
        if (loginClass.isLoggedIn) {
            Intent intent = new Intent(this, VideoActivity.class);
            startActivity(intent);
        } else {
            Toast toast = Toast.makeText(this, "Please Login to Dropbox first", Toast.LENGTH_LONG);
            toast.show();
        }
    }
}