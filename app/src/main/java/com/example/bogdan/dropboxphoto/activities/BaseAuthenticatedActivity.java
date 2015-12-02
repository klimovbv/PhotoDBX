package com.example.bogdan.dropboxphoto.activities;

import android.content.Intent;
import android.os.Bundle;

public abstract class BaseAuthenticatedActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!application.getAuth().hasAuthToken()){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        onDbxAppCreate(savedInstanceState);
    }

    protected abstract void onDbxAppCreate(Bundle savedInstanceState);
}
