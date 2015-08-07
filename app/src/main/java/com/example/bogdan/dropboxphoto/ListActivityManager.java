package com.example.bogdan.dropboxphoto;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;

import java.util.ArrayList;

public class ListActivityManager extends Activity {

    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCES_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    private ListView lv;
    private LoginClass loginClass;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity);
        final ArrayList<String> fileUIArrayList = new ArrayList<String>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.list_item, fileUIArrayList);
        lv = (ListView) findViewById(R.id.listView);
        lv.setAdapter(adapter);

        handler = new Handler(){
            public void handleMessage(Message msg){
                switch(msg.what){
                    case 0:
                        String message = (String)msg.obj;
                        fileUIArrayList.add(message);
                        Log.d("myLogs", " in UI obtained message :" + message);
                        adapter.notifyDataSetChanged();
                        break;
                }
            }
        };

        Thread dataThread = new Thread(new Runnable() {
            Message msg;
            @Override
            public void run() {
                SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
                String key = prefs.getString(ACCESS_KEY_NAME, null);
                String secret = prefs.getString(ACCESS_SECRET_NAME, null);
                loginClass = new LoginClass();
                loginClass.makingSession(key, secret);
                Log.d("myLogs", key + " _ " + secret);
                Log.d("myLogs", "Entry");
                ArrayList<Entry> files = new ArrayList<Entry>();
                ArrayList<String> dir = new ArrayList<String>();

                try {
                    Entry entries = loginClass.mDBApi.metadata("/Photos/", 0, null, true, null);
                    for (Entry entry : entries.contents) {
                        files.add(entry);
                        dir.add(entry.fileName());
                        msg = handler.obtainMessage(0, 0, 0, entry.fileName());
                        handler.sendMessage(msg);
                        Log.d("myLogs", "FILES: " + entry.fileName());
                    }
                } catch (DropboxException e) {
                    e.printStackTrace();
                }
            }
        });
        dataThread.start();
    }
}
