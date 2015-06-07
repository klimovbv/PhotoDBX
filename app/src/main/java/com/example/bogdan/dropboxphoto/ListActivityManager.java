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

/**
 * Created by Boss on 04.06.15.
 */
public class ListActivityManager extends Activity {

    ListView lv;
    LoginClass loginClass1;
    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCES_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    LoginClass loginClass;
    Entry entries;
    Handler handler;//для передачи имен файлов из потока DBX в UI поток


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity);
        lv = (ListView) findViewById(R.id.listView);
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        loginClass1 = new LoginClass();
        loginClass1.makingSession(key, secret);
        final ArrayList<String> fileUIArrayList = new ArrayList<String>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.list_item, fileUIArrayList);
        lv.setAdapter(adapter);




        final String [] fileNameArray = null;//массив для имен файлов в UI-потоке
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
                String [] fNames = null;




                try {
                    Entry entries = loginClass.mDBApi.metadata("/Photos/", 0, null, true, null);
                    int i = 0;
                    for (Entry entry : entries.contents) {
                        files.add(entry);
                        /*dir.add(new String(files.get(i++).path));*/
                        dir.add(entry.fileName());
                        msg = handler.obtainMessage(0, 0, 0, entry.fileName());
                        handler.sendMessage(msg);
                        Log.d("myLogs", "FILES: " + entry.fileName());
                    }
                    fNames = dir.toArray(new String[dir.size()]);
                    Log.d("myLogs", "FILES Array: " + fNames);
                    Log.d("myLogs", "first FILE : " + (CharSequence)dir.get(0));//имя первого файла
                    Log.d("myLogs", "first FILE из fNames: " + fNames[0]);

                } catch (DropboxException e) {
                    Log.d("myLogs", "ERROR");
                    e.printStackTrace();
                }

            }
        });
        dataThread.start();


    }

}
