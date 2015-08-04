package com.example.bogdan.dropboxphoto;

import android.app.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.MediaController;
import android.widget.VideoView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.DropboxAPI.DropboxLink;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


/**
 * Created by Bogdan on 13.07.2015.
 */
public class VideoPlayer extends Activity {
    ProgressDialog pDialog;
    VideoView videoView;
    MediaController mediaController;
    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCES_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    LoginClass loginClass;
    DropboxAPI.DropboxLink fileLink;
    String url;
    Handler handler;
    File file;
    String videoUrl, videoName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player);
        Intent intent = getIntent();
        videoName = intent.getStringExtra("filepath");
        videoUrl = ("/Video/" + videoName);
        videoView = (VideoView)findViewById(R.id.videoView);
        pDialog = new ProgressDialog(VideoPlayer.this);
        pDialog.setTitle(videoName);
        pDialog.setMessage("Buffering...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();


        Thread dataThread = new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
                String key = prefs.getString(ACCESS_KEY_NAME, null);
                String secret = prefs.getString(ACCESS_SECRET_NAME, null);

                loginClass.makingSession(key, secret);

                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        "testvideo" + videoName);
                if (!file.exists()) {
                    try {
                        FileOutputStream outputStream = new FileOutputStream(file);
                        loginClass.mDBApi.getFile(videoUrl, null, outputStream, null);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (DropboxException e) {
                        e.printStackTrace();
                    }
                }
                handler.sendEmptyMessage(0);
            }
        });
        dataThread.start();
        handler = new Handler() {
            public void handleMessage (Message msg) {
                mediaController = new MediaController(VideoPlayer.this);
                mediaController.setAnchorView(videoView);
                videoView.setMediaController(mediaController);
                videoView.setVideoURI(Uri.parse(file.getAbsolutePath()));
                videoView.requestFocus();
                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        pDialog.dismiss();
                        videoView.start();
                    }
                });
            }
        };


    }
}
