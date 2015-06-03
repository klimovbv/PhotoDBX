package com.example.bogdan.dropboxphoto;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
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

public class VideoActivity extends Activity implements SurfaceHolder.Callback {

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
    private final String VIDEO_DIR = "/Video/";


    MediaRecorder mediaRecorder;
    File videoFile;

    public String key, secret;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        buttonPhoto = (Button)findViewById(R.id.button2);
        setContentView(R.layout.activity_video);
        surface = (SurfaceView) findViewById(R.id.surfaceView);
        holder = surface.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(this);
        cameraId = 0;

        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        key = prefs.getString(ACCESS_KEY_NAME, null);
        secret = prefs.getString(ACCESS_SECRET_NAME, null);
        Log.d("myLogs", key + " _  " + secret);
        loginClass = new LoginClass();
        loginClass.makingSession(key, secret);


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
       /* camera.stopPreview();
        camera.release();
        camera = null;*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera = Camera.open();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();
        if (camera != null)
            camera.release();
        camera = null;
    }

    /*public void onClickPhoto(View view) {
        takePicture();
    }*/

    /*private void takePicture() {
        videoFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "testvideo" + System.currentTimeMillis() + ".3gp");
        fileName = videoFile.getAbsolutePath();
        camera.takePicture(null, null, new PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                surfaceDestroyed(holder);
                surfaceCreated(holder);
                try {
                   FileOutputStream outStream = new FileOutputStream(photoFile);
                    outStream.write(bytes);
                    outStream.close();
                    UploadPicture upload = new UploadPicture(VideoActivity.this, loginClass.mDBApi,
                            VIDEO_DIR, photoFile);
                    upload.execute();
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "File  Not Found!!!", e);
                } catch (IOException e) {
                    Log.d(TAG, "IO Exception", e);
                }
            }
        });
    }*/

    public void onClickChangeCamera(View view) {
        if (cameraId == 0) {
            cameraId = 1;
        } else {
            cameraId = 0;
        }
        camera.stopPreview();
        camera.release();
        camera = null;
        surfaceCreated(holder);
    }

    /*@Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();
        if (camera != null)
            camera.release();
        camera = null;
    }*/

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            camera.lock();
        }
    }

    public void onClickVideo (View view) {
        videoFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "testvideo" + System.currentTimeMillis() + ".3gp");
        if (prepareVideoRecorder()) {
            mediaRecorder.start();
        } else {
            releaseMediaRecorder();
        }
    }
    public void onClickStopRecord(View view) {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            releaseMediaRecorder();
            Intent intent = new Intent (VideoActivity.this, UploadService.class);
            intent.putExtra("key", key);
            intent.putExtra("secret",  secret);
            intent.putExtra("filePath",videoFile.getAbsolutePath());
            intent.putExtra("dirPath", VIDEO_DIR);
            /*UploadPicture upload = new UploadPicture(VideoActivity.this, loginClass.mDBApi,
                      VIDEO_DIR, videoFile);
            upload.execute();*/
            startService(intent);
        }
    }
    private boolean prepareVideoRecorder() {

        camera.unlock();

        mediaRecorder = new MediaRecorder();

        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(CamcorderProfile
                .get(CamcorderProfile.QUALITY_HIGH));
        mediaRecorder.setOutputFile(videoFile.getAbsolutePath());
        mediaRecorder.setPreviewDisplay(surface.getHolder().getSurface());

        try {
            mediaRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        }
        return true;
    }
}