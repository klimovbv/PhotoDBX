package com.example.bogdan.dropboxphoto;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/*import com.dropbox.chooser.android.DbxChooser;*/
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;


public class MainActivity extends Activity {

    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCES_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    private Button loginButton, photoButton, videoButton, photoListButton, videoListButton;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        if (!LoginClass.isLoggedIn) {
            LoginClass.makingSession(prefs.getString(ACCESS_KEY_NAME, null),
                    prefs.getString(ACCESS_SECRET_NAME, null));
        }
        loginButton = (Button)findViewById(R.id.dropbox_login);
        photoButton = (Button)findViewById(R.id.upload_photo);
        videoButton = (Button)findViewById(R.id.upload_video);
        photoListButton = (Button)findViewById(R.id.photo_list);
        videoListButton = (Button)findViewById(R.id.video_list);

        if (LoginClass.isLoggedIn) {
            loginButton.setText("Log out");
            photoButton.setVisibility(View.VISIBLE);
            videoButton.setVisibility(View.VISIBLE);
            photoListButton.setVisibility(View.VISIBLE);
            videoListButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (LoginClass.mDBApi.getSession().authenticationSuccessful()) {
            try {
                LoginClass.mDBApi.getSession().finishAuthentication();
                String accessToken = LoginClass.mDBApi.getSession().getOAuth2AccessToken();
                if (accessToken != null) {
                    prefs.edit().putString(ACCESS_KEY_NAME, "oauth2:").apply();
                    prefs.edit().putString(ACCESS_SECRET_NAME, accessToken).apply();
                    LoginClass.isLoggedIn = true;
                    loginButton.setText("Log out");
                    photoButton.setVisibility(View.VISIBLE);
                    videoButton.setVisibility(View.VISIBLE);
                    photoListButton.setVisibility(View.VISIBLE);
                    videoListButton.setVisibility(View.VISIBLE);
                    return;
                }
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }
    }

    public void onClickCameraButton(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    public void onClickLogin(View view) {
        if (LoginClass.isLoggedIn) {
            prefs.edit().clear().apply();
            LoginClass.mDBApi.getSession().unlink();
            LoginClass.isLoggedIn = false;
            loginButton.setText("Log out");
            photoButton.setVisibility(View.INVISIBLE);
            videoButton.setVisibility(View.INVISIBLE);
            photoListButton.setVisibility(View.INVISIBLE);
            videoListButton.setVisibility(View.INVISIBLE);
            Toast toast = Toast.makeText(this, "You have logged out", Toast.LENGTH_LONG);
            toast.show();
            loginButton.setText("Login to Dropbox");
        } else {
            LoginClass.mDBApi.getSession().startOAuth2Authentication(MainActivity.this);
        }
    }

    public void onClickVideoButton(View view) {
        Intent intent = new Intent(this, VideoActivity.class);
        startActivity(intent);
    }

    public void onClickPhotoList(View view) {
        Intent intent = new Intent(this, ListActivityMyAdapter.class);
        intent.putExtra("Type", "/Photos/");
        startActivity(intent);
    }

    public void onClickVideoList(View view) {
        Intent intent = new Intent(this, ListActivityMyAdapter.class);
        intent.putExtra("Type", "/Video/");
        startActivity(intent);
    }
}