package com.example.bogdan.dropboxphoto;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;


import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Boss on 04.06.15.
 */
public class ListActivity extends Activity {
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
    private Map <String, Object> m;
    Bitmap bitmap;
    ArrayList<Map<String, Object>> fileUIArrayList;
    SimpleAdapter adapter;
    String itemForDelete;

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
                        loginClass.mDBApi.delete("Photos/" + itemForDelete);
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
        lv = (ListView) findViewById(R.id.listView);
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        loginClass.makingSession(key, secret);
        fileUIArrayList = new ArrayList<Map<String, Object>>();
        String[] from = {ATTRIBUTE_NAME_TEXT, ATTRIBUTE_NAME_IMAGE};
        int [] to = {R.id.textViewList, R.id.imageViewList};
        adapter = new SimpleAdapter(this, fileUIArrayList, R.layout.list_item,
                from, to);


        //позволяем адаптеру получать на вход Bitmap для ImageView
        adapter.setViewBinder(new SimpleAdapter.ViewBinder(){

            @Override
            public boolean setViewValue(View view, Object data,
                                        String textRepresentation) {
                if( (view instanceof ImageView) & (data instanceof Bitmap) ) {
                    ImageView iv = (ImageView) view;
                    Bitmap bm = (Bitmap) data;
                    iv.setImageBitmap(bm);
                    return true;
                }
                return false;
            }
        });

        lv.setAdapter(adapter);
        registerForContextMenu(lv);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent (getApplicationContext(), PreviewImageActivity.class);
                TextView v = (TextView)view.findViewById(R.id.textViewList);
                intent.putExtra("filepath", v.getText().toString());
                startActivity(intent);
            }
        });

        final String [] fileNameArray = null;//массив для имен файлов в UI-потоке
        handler = new Handler(){
            public void handleMessage(Message msg){
                        fileUIArrayList.add(m) ;
                        adapter.notifyDataSetChanged();
            }
        };

        Thread dataThread = new Thread(new Runnable() {
            Message msg;
            @Override
            public void run() {
                SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
                String key = prefs.getString(ACCESS_KEY_NAME, null);
                String secret = prefs.getString(ACCESS_SECRET_NAME, null);

                loginClass.makingSession(key, secret);
                Log.d("myLogs", key + " _ " + secret);
                Log.d("myLogs", "Entry");
                ArrayList<Entry> files = new ArrayList<Entry>();
                ArrayList<String> dir = new ArrayList<String>();
                String [] fNames = null;

                /*HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String resultJson = "";
                try {
                    thumbFile = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                            "testthumb" + System.currentTimeMillis() + ".jpg");
                    mFilnameName = thumbFile.getAbsolutePath();
                    Log.d("myLogs", "thumbFilw  = " + mFilnameName);
                    DropboxAPI.DropboxInputStream dis = loginClass.mDBApi.getThumbnailStream("/Photos/test1433543735914.jpg", DropboxAPI.ThumbSize.ICON_256x256, DropboxAPI.ThumbFormat.JPEG);
                    FileOutputStream fos = new FileOutputStream(thumbFile);
                    BitmapFactory.decodeStream(dis).compress(Bitmap.CompressFormat.JPEG, 100, fos);


                    *//*mDrawable = Drawable.createFromStream(dis, mFilnameName);*//*
                    fos.close();
                    dis.close();
                    *//*fos.close();*//*
                    *//*InputStreamURL url = new URL("https://api.dropbox.com/1/metadata/auto/test1433543735914.jpg");
                    urlConnection = (HttpURLConnection)url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                     inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null){
                        buffer.append(line);
                    }
                    resultJson = buffer.toString();*//*


                *//*} catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();*//*
                } catch (DropboxException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("myLogs", "resultJson = " + resultJson);*/


                try {
                    Entry entries = loginClass.mDBApi.metadata("/Photos/", 0, null, true, null);
                    int i = 0;
                    for (Entry entry : entries.contents) {
                        files.add(entry);
                        dir.add(new String(files.get(i++).path));
                        dir.add(entry.fileName());
                        Log.d("myLogs", "FILES: " + entry.fileName());
                        DropboxAPI.DropboxInputStream dis = loginClass.mDBApi.getThumbnailStream("/Photos/" + entry.fileName(),
                                DropboxAPI.ThumbSize.ICON_256x256, DropboxAPI.ThumbFormat.JPEG);
                        bitmap = BitmapFactory.decodeStream(dis);
                        dis.close();
                        m = new HashMap<String, Object>();
                        m.put(ATTRIBUTE_NAME_TEXT,entry.fileName());
                        m.put(ATTRIBUTE_NAME_IMAGE, bitmap);
                        /*msg = handler.obtainMessage(0, 0, 0, m);*/
                        handler.sendEmptyMessage(0);
                    }
                    fNames = dir.toArray(new String[dir.size()]);
                    Log.d("myLogs", "FILES Array: " + fNames);
                    Log.d("myLogs", "first FILE : " + (CharSequence)dir.get(0));//имя первого файла
                    Log.d("myLogs", "first FILE из fNames: " + fNames[0]);
                } catch (DropboxException e) {
                    Log.d("myLogs", "ERROR");
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        dataThread.start();
    }
}
