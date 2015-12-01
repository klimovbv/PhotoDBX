package com.example.bogdan.dropboxphoto.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.bogdan.dropboxphoto.MainActivity;
import com.example.bogdan.dropboxphoto.R;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private Button logninButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        logninButton = (Button)findViewById(R.id.activity_login_loginButton);
        logninButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        application.getAuth().login();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (application.getAuth().getmDBApi() != null) {
            if (application.getAuth().getmDBApi().getSession().authenticationSuccessful()) {
                try {
                    application.getAuth().finishAuth();
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } catch (IllegalStateException e) {
                    Log.i("DbAuthLog", "Error authenticating", e);
                }
            }
        }
    }
}
