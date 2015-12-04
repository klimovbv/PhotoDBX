package com.example.bogdan.dropboxphoto.services;

import com.example.bogdan.dropboxphoto.infrastructure.DbxApplication;

public class Module {
    public static void register (DbxApplication application) {
        new AccountService(application);
    }
}
