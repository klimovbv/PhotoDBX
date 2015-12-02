package com.example.bogdan.dropboxphoto;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.example.bogdan.dropboxphoto.activities.BaseAuthenticatedActivity;

import java.io.File;
import java.io.IOException;


public class PreviewImageActivity extends BaseAuthenticatedActivity {

    public static final int REQUEST_PHOTO_DELETE = 100;
    public static final String RESULT_EXTRA_PHOTO = "RESULT_EXTRA_PHOTO";
    private String filePath;
    private File thumbnailFile;
    private String thumbnailFileName;
    private String fileName;
    private Bitmap bitmap;
    private Handler handler;
    private TouchImageView imageView;

    @Override
    protected void onDbxAppCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_preview_photo);
        Intent intent = getIntent();

        filePath = intent.getStringExtra("filepath");
        imageView = (TouchImageView)findViewById(R.id.imageView);

        toolbar.setNavigationIcon(R.drawable.ic_ab_close);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMessage(RESULT_OK);
            }
        });

        handler = new Handler(){
            public void handleMessage(Message msg){
                changeContent();
            }
        };
        Thread previewThread = new Thread(new Runnable() {
            @Override
            public void run() {

                DropboxAPI.DropboxInputStream dis;
                fileName = "testthumb" + System.currentTimeMillis() + ".jpg";
                thumbnailFile = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        fileName);
                thumbnailFileName = thumbnailFile.getAbsolutePath();
                Log.d("myLogs", "thumbFileName  = " + thumbnailFileName);
                try {
                    dis = mDBApi.getThumbnailStream("/Photos/" + filePath,
                            DropboxAPI.ThumbSize.BESTFIT_1024x768, DropboxAPI.ThumbFormat.JPEG);
                    bitmap = BitmapFactory.decodeStream(dis);
                    dis.close();
                } catch (DropboxException | IOException e) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_preview_photo, menu);
        return true;
    }

    private void closeMessage(int resultCode){
        Intent data = new Intent();
        data.putExtra(RESULT_EXTRA_PHOTO, filePath);
        setResult(resultCode);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.activity_preview_photo_menuDelete){
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Delete Photo")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Thread deleteThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        mDBApi.delete("/Photos/" + filePath);
                                        closeMessage(REQUEST_PHOTO_DELETE);
                                    } catch (DropboxException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            deleteThread.start();
                        }
                    })
                    .setCancelable(false)
                    .setNeutralButton("Cancel", null)
                    .create();
            dialog.show();
            finish();
        }

        return false;
    }
}

