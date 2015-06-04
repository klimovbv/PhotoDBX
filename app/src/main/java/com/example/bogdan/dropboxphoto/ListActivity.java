package com.example.bogdan.dropboxphoto;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.util.Log;
import android.widget.ListView;

import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;

/**
 * Created by Boss on 04.06.15.
 */
public class ListActivity extends Activity {

    ListView lv;
    LoginClass loginClass1;
    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCES_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    Entry entries;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity);
        lv = (ListView)findViewById(R.id.listView);
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        loginClass1 = new LoginClass();
        loginClass1.makingSession(key, secret);


        try {
            entries = loginClass1.mDBApi.metadata("/", 1000, null, true, null);
        } catch (DropboxException e) {
            e.printStackTrace();
        }
        Log.d("myLogs", "FILES: " /*+ entries.rev*/);


    }
}
