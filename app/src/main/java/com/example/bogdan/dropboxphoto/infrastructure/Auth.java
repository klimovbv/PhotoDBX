package com.example.bogdan.dropboxphoto.infrastructure;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;
import com.example.bogdan.dropboxphoto.MainActivity;

public class Auth {
    private static final String APP_KEY = "e7r6jtptl6t3rz9";
    private static final String APP_SECRET = "qqfvu5wtkqft9uz";

    private static final String AUTH_PREFERENCES = "AUTH_PREFERENCES";
    private static final String AUTH_PREFERENCES_TOKEN = "AUTH_PREFERENCES_TOKEN";

    private final Context context;
    private final SharedPreferences preferences;
    private String authToken;
    private AndroidAuthSession session;
    private DropboxAPI<AndroidAuthSession> mDBApi;

    public Auth(Context context) {
        this.context = context;

        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        session = new AndroidAuthSession(appKeyPair);
        preferences = context.getSharedPreferences(AUTH_PREFERENCES, Context.MODE_PRIVATE);

        setMDBApi();
    }

    public void setMDBApi(){

        authToken = preferences.getString(AUTH_PREFERENCES_TOKEN, null);

        if (authToken != null && authToken.length() != 0){
            session.setOAuth2AccessToken(authToken);
        } else {
            mDBApi = null;
        }
        mDBApi= new DropboxAPI<AndroidAuthSession>(session);
    }

    public void login (){
        mDBApi= new DropboxAPI<AndroidAuthSession>(session);
        mDBApi.getSession().startOAuth2Authentication(context);
    }

    public DropboxAPI<AndroidAuthSession> getmDBApi() {
        return mDBApi;
    }

    public void finishAuth(){
        mDBApi.getSession().finishAuthentication();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(AUTH_PREFERENCES, mDBApi.getSession().getOAuth2AccessToken());
    }


    public boolean isLoggedIn(){
        if (mDBApi != null){
            return true;
        }

        return false;
    }


    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(AUTH_PREFERENCES_TOKEN, authToken);
        editor.commit();
    }

    public boolean hasAuthToken (){
        return authToken != null && !authToken.isEmpty();
    }

    public void logout(){
        setAuthToken(null);
        mDBApi = null;

        Intent loginIntent = new Intent(context, MainActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(loginIntent);

    }
}
