package com.example.bogdan.dropboxphoto.infrastructure;

import android.app.Application;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.example.bogdan.dropboxphoto.services.Module;
import com.squareup.otto.Bus;

public class DbxApplication extends Application {


    private DropboxAPI<AndroidAuthSession> mDBApi;
    private Auth auth;
    private Bus bus;

    public DbxApplication() {
        bus = new Bus();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        auth = new Auth(this);
        Module.register(this);
    }

    public Auth getAuth() {
        return auth;
    }

    public Bus getBus() {
        return bus;
    }
}
