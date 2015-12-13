package com.spb.kbv.dropboxphoto.activities;

import android.content.Intent;
import android.os.Bundle;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;

public abstract class BaseAuthenticatedActivity extends BaseActivity {

    protected DropboxAPI<AndroidAuthSession> mDBApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!application.getAuth().hasAuthToken()){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        mDBApi = application.getAuth().getmDBApi();
        onDbxAppCreate(savedInstanceState);
    }

    protected abstract void onDbxAppCreate(Bundle savedInstanceState);
}
