package com.example.bogdan.dropboxphoto;



import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;


public class LoginClass {

    private static final String APP_KEY = "e7r6jtptl6t3rz9";
    private static final String APP_SECRET = "qqfvu5wtkqft9uz";
    public static Boolean isLoggedIn;
    private static String mKey;
    private static String mSecret;

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

    private static void loadAuth(AndroidAuthSession session) {
        if (mKey == null || mSecret == null || mKey.length() == 0 || mSecret.length() == 0) {
            isLoggedIn = false;
            return;
        } else {
            session.setOAuth2AccessToken(mSecret);
            isLoggedIn = true;
        }
    }
}
