package com.example.bogdan.dropboxphoto;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class CameraActivity extends Activity implements SurfaceHolder.Callback {

    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCES_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    LoginClass loginClass = null;

    private static final String TAG = "myLogs";
    private Camera camera;
    File photoFile;
    private int cameraId;
    SurfaceHolder holder;
    SurfaceView surface;
    Button buttonPhoto;
    String fileName;
    private final String PHOTO_DIR = "/Photos/";
    Handler uploadHandler;
    public String key, secret;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        buttonPhoto = (Button)findViewById(R.id.button2);
        setContentView(R.layout.activity_camera);
        surface = (SurfaceView) findViewById(R.id.surfaceView);
        holder = surface.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(this);
        cameraId = 0;

        uploadHandler = new DownloadHandler(this);

        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        key = prefs.getString(ACCESS_KEY_NAME, null);
        secret = prefs.getString(ACCESS_SECRET_NAME, null);
        Log.d ("myLogs", key + " _  " + secret);
        loginClass = new LoginClass();
        loginClass.makingSession(key, secret);
     }

    /**
     * @class DownloadHandler
     *
     * @brief A nested class that inherits from Handler and uses its
     *        handleMessage() hook method to process Messages sent to
     *        it from the DownloadService.
     */
    private static class DownloadHandler extends Handler {
        /**
         * Allows Activity to be garbage collected properly.
         */
        private WeakReference<CameraActivity> mActivity;

        /**
         * Class constructor constructs mActivity as weak reference
         * to the activity
         *
         * @param activity The corresponding activity
         */
        public DownloadHandler(CameraActivity activity) {
            mActivity = new WeakReference<CameraActivity>(activity);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (camera == null) {
            Log.d(TAG, "camera == null");
            camera = Camera.open(cameraId);
            Log.d(TAG, "Camera opened ID = " + cameraId);
        }
        else {
            Log.d(TAG, "camera opened");
        }
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            Log.d(TAG, "IO Exception" + e);
        }
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        float aspect = (float) previewSize.width/previewSize.height;
        int previewSurfaceHeight = surface.getHeight();
        LayoutParams lp = surface.getLayoutParams();
        camera.setDisplayOrientation(90);
        lp.height = previewSurfaceHeight;
        lp.width = (int) (previewSurfaceHeight / aspect);
        surface.setLayoutParams(lp);
        camera.startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int i, int i2, int i3) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    public void onClickPhoto(View view) {
        takePicture();
    }

    private void takePicture() {
        File sdPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        sdPath = new File(sdPath.getAbsolutePath() + "/PhotoToDBX");
        sdPath.mkdir();
        photoFile = new File(sdPath,
                "test" + System.currentTimeMillis() + ".jpg");
        fileName = photoFile.getAbsolutePath();
        camera.takePicture(null, null, new PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                surfaceDestroyed(holder);
                surfaceCreated(holder);
                try {
                   FileOutputStream outStream = new FileOutputStream(photoFile);
                    outStream.write(bytes);
                    outStream.close();
                    Intent intent = new Intent (CameraActivity.this, UploadService.class);
                    intent.putExtra("key", key);
                    intent.putExtra("secret",  secret);
                    intent.putExtra("filePath",fileName);
                    intent.putExtra("dirPath", PHOTO_DIR);
                    startService(intent);
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "File  Not Found!!!", e);
                } catch (IOException e) {
                    Log.d(TAG, "IO Exception", e);
                }
            }
        });
    }

    public void onClickChangeCamera(View view) {
        if (cameraId == 0) {
            cameraId = 1;
        } else {
            cameraId = 0;
        }
        surfaceDestroyed(holder);
        surfaceCreated(holder);
    }
}