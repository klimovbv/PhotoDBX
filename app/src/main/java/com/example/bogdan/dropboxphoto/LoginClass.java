package com.example.bogdan.dropboxphoto;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;



/**
 * Created by Bogdan on 18.02.2015.
 */
public class LoginClass {
    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCES_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    private static final String APP_KEY = "e7r6jtptl6t3rz9";
    private static final String APP_SECRET = "qqfvu5wtkqft9uz";
    private static Boolean isLoggedIn;
    static SharedPreferences prefs;
    private static String mKey;
    private static String mSecret;
    static AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);

    public static DropboxAPI<AndroidAuthSession> mDBApi;
    public static void makingSession(String key, String secret) {
        mKey = key;
        mSecret = secret;
        AndroidAuthSession session = buildSession();
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
    }

    private static AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        loadAuth(session);

        return session;
    }

    private static void loadAuth(AndroidAuthSession session) { //+

        if (mKey == null || mSecret == null || mKey.length() == 0 || mSecret.length() == 0) {
            isLoggedIn = false;
            return;
        } else {
            session.setOAuth2AccessToken(mSecret);
            isLoggedIn = true;
        }

    }








       /* prefs = getContext().getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key == null || secret == null || key.length() == 0 || secret.length() == 0) {
            isLoggedIn = false;
            return;
        } else {
            session.setOAuth2AccessToken(secret);
            isLoggedIn = true;
        }



        String accessToken = mDBApi.getSession().getOAuth2AccessToken();
        if (accessToken != null) {
            *//*prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);*//*
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, "oauth2:");
            edit.putString(ACCESS_SECRET_NAME, accessToken);
            edit.commit();
            isLoggedIn = true;
        }

    }*/






}
