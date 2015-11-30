package com.example.bogdan.dropboxphoto.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.bogdan.dropboxphoto.R;
import com.example.bogdan.dropboxphoto.services.AccountService;
import com.squareup.otto.Subscribe;

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
    public void onClick(View v) {
        bus.post(new AccountService.LoginRequest());
    }

    @Subscribe
    public void onLoginResponse() {
        finish();
    }
}
