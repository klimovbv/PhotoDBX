package com.example.bogdan.dropboxphoto.services;

import com.example.bogdan.dropboxphoto.infrastructure.Auth;
import com.example.bogdan.dropboxphoto.infrastructure.DbxApplication;
import com.squareup.otto.Subscribe;

public final class AccountService {
    private final Auth auth;
    public AccountService(DbxApplication application){
        auth = application.getAuth();
    }

    public static class LoginRequest {
    }

    public static class LoginResponse{
    }

    @Subscribe
    public void login (LoginRequest request){
        auth.login();
    }


}
