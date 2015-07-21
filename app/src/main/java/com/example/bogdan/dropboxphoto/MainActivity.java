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

import com.dropbox.chooser.android.DbxChooser;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;


public class MainActivity extends Activity {

    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCES_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    private static final String APP_KEY = "e7r6jtptl6t3rz9";
    private Boolean isLoggedIn = false;
    LoginClass loginClass = null;
    /*private DbxAccountManager mDbxAcctMgr;*/
    static final int DBX_CHOOSER_REQUEST = 0;  // You can change this if needed

    private Button mChooserButton;
    private DbxChooser mChooser;

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

        mChooser = new DbxChooser(APP_KEY);

        mChooserButton = (Button) findViewById(R.id.file_manager);
        mChooserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChooser.forResultType(DbxChooser.ResultType.PREVIEW_LINK)
                        .launch(MainActivity.this, DBX_CHOOSER_REQUEST);
            }
        });




        /*mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(), "e7r6jtptl6t3rz9", "qqfvu5wtkqft9uz");
        Log.d("myLogs", "Manage logged in" + mDbxAcctMgr.hasLinkedAccount());*/
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

    public void onClickListButton(View view) {
        Intent intent = new Intent(this, ListActivityMyAdapter.class);
        startActivity(intent);
    }

    public void onClickManagerButton(View view) {
        Intent intent = new Intent(this, ListActivityManager.class);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DBX_CHOOSER_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                DbxChooser.Result result = new DbxChooser.Result(data);
                Log.d("main", "Link to selected file: " + result.getLink());

                // Handle the result
            } else {
                // Failed or was cancelled by the user.
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onPlayVideoButton(View view) {
        Intent intent = new Intent(this, VideoPlayer.class);
        startActivity(intent);
    }
}