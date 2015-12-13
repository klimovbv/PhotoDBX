package com.spb.kbv.dropboxphoto.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.spb.kbv.dropboxphoto.R;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private Button loginButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginButton = (Button)findViewById(R.id.activity_login_loginButton);
        loginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        application.getAuth().login();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (application.getAuth().getmDBApi() != null) {
            if (application.getAuth().isLoggedIn()) {
                try {
                    application.getAuth().finishAuth();
                    Intent intent = new Intent(this, PhotoFilesListActivity.class);
                    startActivity(intent);
                    finish();
                } catch (IllegalStateException e) {
                    Log.i("DbAuthLog", "Error authenticating", e);
                }
            }
        }
    }
}
