package com.example.bogdan.dropboxphoto;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Boss on 04.06.15.
 */
public class ListActivityMyAdapter extends Activity {
    private static final int DELETE_ID =1;
    ListView lv;
    LoginClass loginClass1;
    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCES_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    LoginClass loginClass;
    Entry entries;
    private Handler handler;//для передачи имен файлов из потока DBX в UI поток
    private Drawable mDrawable = null;
    private String mFilnameName;
    File thumbFile, thumbFile2;
    final String ATTRIBUTE_NAME_TEXT = "text";
    final String ATTRIBUTE_NAME_IMAGE = "image";
    private String m;
    Bitmap bitmap;
    ArrayList<String> fileUIArrayList;
    MyAdapter adapter;
    String itemForDelete;
    String directory;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0,DELETE_ID,0,"Delete file");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int position = info.position;
        View view = info.targetView;
        if (item.getItemId() == DELETE_ID) {
            TextView v = (TextView)view.findViewById(R.id.textViewList);
            itemForDelete = v.getText().toString();
            Toast.makeText(getApplicationContext(),
                    "Вы выбрали " + itemForDelete, Toast.LENGTH_SHORT).show();
            fileUIArrayList.remove(position);
            adapter.notifyDataSetChanged();
            Thread deleteThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        loginClass.mDBApi.delete(directory + itemForDelete);
                    } catch (DropboxException e) {
                        e.printStackTrace();
                    }
                }
            });
            deleteThread.start();
            return true;
        } else return super.onContextItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity);
        Intent intent = getIntent();
        directory = intent.getStringExtra("Type");
        lv = (ListView) findViewById(R.id.listView);
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        loginClass.makingSession(key, secret);
        fileUIArrayList = new ArrayList<String>();
        adapter = new MyAdapter(this, fileUIArrayList, loginClass.mDBApi, directory);
        lv.setAdapter(adapter);
        registerForContextMenu(lv);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                if (directory.equals("/Photos/")) {
                    intent = new Intent (getApplicationContext(), PreviewImageActivity.class);
                }
                else {
                    if (directory.equals("/Video/")) ;
                    intent = new Intent(getApplicationContext(), VideoPlayer.class);
                }
                TextView v = (TextView)view.findViewById(R.id.textViewList);
                intent.putExtra("filepath", v.getText().toString());
                startActivity(intent);
            }
        });

        handler = new Handler(){
            public void handleMessage(Message msg){

                        adapter.notifyDataSetChanged();
            }
        };

        Thread dataThread = new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
                String key = prefs.getString(ACCESS_KEY_NAME, null);
                String secret = prefs.getString(ACCESS_SECRET_NAME, null);
                loginClass.makingSession(key, secret);
                try {
                    Entry entries = loginClass.mDBApi.metadata(directory, 0, null, true, null);
                    for (Entry entry : entries.contents) {
                        fileUIArrayList.add(entry.fileName());
                    } handler.sendEmptyMessage(0);
                } catch (DropboxException e) {
                    Log.d("myLogs", "ERROR");
                    e.printStackTrace();
                }
            }
        });
        dataThread.start();
    }
}
