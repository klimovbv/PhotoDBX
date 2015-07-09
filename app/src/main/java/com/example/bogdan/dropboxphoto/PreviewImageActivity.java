package com.example.bogdan.dropboxphoto;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by Bogdan on 12.06.2015.
 */
public class PreviewImageActivity extends Activity {
    LoginClass loginClass;
    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCES_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    Bitmap bitmap;
    Handler handler;
    String filePath;
    File thumbnailFile;
    String thumbnailFileName;
    String fileName;
    TouchImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview_activity);
        Intent intent = getIntent();
        filePath = intent.getStringExtra("filepath");
        imageView = (TouchImageView)findViewById(R.id.imageView);
        handler = new Handler(){
            public void handleMessage(Message msg){

                changeContent();



            }
        };
        Thread previewThread = new Thread(new Runnable() {
            Message msg;
            @Override
            public void run() {
                SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
                String key = prefs.getString(ACCESS_KEY_NAME, null);
                String secret = prefs.getString(ACCESS_SECRET_NAME, null);

                loginClass.makingSession(key, secret);
                DropboxAPI.DropboxInputStream dis = null;
                fileName = "testthumb" + System.currentTimeMillis() + ".jpg";
                thumbnailFile = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        fileName);
                thumbnailFileName = thumbnailFile.getAbsolutePath();
                Log.d("myLogs", "thumbFileName  = " + thumbnailFileName);
                try {
                    dis = loginClass.mDBApi.getThumbnailStream("/Photos/" + filePath,
                            DropboxAPI.ThumbSize.BESTFIT_1024x768, DropboxAPI.ThumbFormat.JPEG);
                    bitmap = BitmapFactory.decodeStream(dis);
                    /*FileOutputStream fos = new FileOutputStream(thumbnailFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.close();*/
                    dis.close();
                } catch (DropboxException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                handler.sendEmptyMessage(0);
            }
        });
        previewThread.start();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
        Log.d("myLogs", "Configuration changed " + newConfig.orientation);
        changeContent();
    }

    private void changeContent() {

        imageView.setImageBitmap(bitmap);
    }
}

