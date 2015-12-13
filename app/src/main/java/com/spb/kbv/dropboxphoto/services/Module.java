package com.spb.kbv.dropboxphoto.services;

import com.spb.kbv.dropboxphoto.infrastructure.DbxApplication;

public class Module {
    public static void register (DbxApplication application) {
        new AccountService(application);
    }
}
