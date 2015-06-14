package com.example.bogdan.dropboxphoto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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
    private ScaleGestureDetector mScaledetector;
    private float mScaleFactor = 1f;
    private Matrix mMatrix = new Matrix();
    ImageView imageView;
    File thumbnailFile;
    String thumbnailFileName;
    WebView webView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview_activity);
        Intent intent = getIntent();
        filePath = intent.getStringExtra("filepath");
        webView = (WebView)findViewById(R.id.webView);
        //поддержка масштабирования
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        //больше места для нашей картинки
        webView.setPadding(0, 0, 0, 0);
        //полосы прокрутки – внутри изображения, увеличение места для просмотра
        webView.setScrollbarFadingEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        /*webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);*/

        /*imageView = (ImageView)findViewById(R.id.imageView);
        mScaledetector = new ScaleGestureDetector(this, new ScaleListener());
*/
        handler = new Handler(){
            public void handleMessage(Message msg){
                /*imageView.setImageBitmap(bitmap);*/
                /*webView.loadUrl("file:///" + thumbnailFileName);*/
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
                thumbnailFile = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        "testthumb" + System.currentTimeMillis() + ".jpg");
                thumbnailFileName = thumbnailFile.getAbsolutePath();
                Log.d("myLogs", "thumbFileName  = " + thumbnailFileName);
                try {
                    dis = loginClass.mDBApi.getThumbnailStream("/Photos/" + filePath,
                            DropboxAPI.ThumbSize.BESTFIT_1024x768, DropboxAPI.ThumbFormat.JPEG);
                    bitmap = BitmapFactory.decodeStream(dis);
                    FileOutputStream fos = new FileOutputStream(thumbnailFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.close();
                    dis.close();
                } catch (DropboxException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                //*mDrawable = Drawable.createFromStream(dis, mFilnameName);*//*

                /*try {
                    dis = loginClass.mDBApi.getThumbnailStream("/Photos/" + filePath,
                            DropboxAPI.ThumbSize.BESTFIT_1024x768, DropboxAPI.ThumbFormat.JPEG);
                    bitmap = BitmapFactory.decodeStream(dis);
                    dis.close();
                } catch (DropboxException e) {
                    e.printStackTrace();
                }catch (IOException e) {
                    e.printStackTrace();
                }*/
                handler.sendEmptyMessage(0);
            }
        });
        previewThread.start();
    }
    /*private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(1f, Math.min(mScaleFactor, 5.0f));
            mMatrix.setScale(mScaleFactor, mScaleFactor);
            imageView.setImageMatrix(mMatrix);
            imageView.invalidate();
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaledetector.onTouchEvent(event);
        return true;
    }*/
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d("myLogs", "Configuration changed " + newConfig.orientation);
        super.onConfigurationChanged(newConfig);
        changeContent();
    }

    private void changeContent() {


        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        int width = display.getWidth();
        int height = display.getHeight();


        /*Bitmap img = thumbnailFileName.getImageBitmap();*/

        int picWidth = bitmap.getWidth();
        int picHeight = bitmap.getHeight();



// scale web view depend of rotate device
        Double val = 1d;

        if (picWidth > width)
            val = new Double(width) / new Double(picWidth);

        val = val * 100d;


        webView.setInitialScale( val.intValue() );
        webView.loadUrl("file:///" + thumbnailFileName);
    }
}

