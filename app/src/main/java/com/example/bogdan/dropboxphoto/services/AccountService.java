package com.example.bogdan.dropboxphoto.services;

import com.squareup.otto.Subscribe;

public final class AccountService {
    private AccountService(){
    }

    public static class LoginRequest {
    }

    public static class LoginResponse{
    }

    @Subscribe
    public void login (LoginRequest request){

    }


}
