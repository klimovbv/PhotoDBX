package com.example.bogdan.dropboxphoto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
    private int cameraId;
    SurfaceHolder holder;
    SurfaceView surface;
    Button buttonPhoto;
    private final String VIDEO_DIR = "/Video/";


    MediaRecorder mediaRecorder;
    File videoFile;

    public String key, secret;
    private static final int PORTRAIT_UP = 1;
    private static final int PORTRAIT_DOWN = 2;
    private static final int LANDSCAPE_LEFT = 3;
    private static final int LANDSCAPE_RIGHT = 4;
    float x, y, z;
    int orientation, scale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        buttonPhoto = (Button) findViewById(R.id.button2);
        setContentView(R.layout.activity_video);
        surface = (SurfaceView) findViewById(R.id.surfaceView);
        holder = surface.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(this);
        cameraId = 1;

        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        key = prefs.getString(ACCESS_KEY_NAME, null);
        secret = prefs.getString(ACCESS_SECRET_NAME, null);
        Log.d("myLogs", key + " _  " + secret);
        loginClass = new LoginClass();
        loginClass.makingSession(key, secret);

        orientation = PORTRAIT_UP;
        scale = 90;

        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        int sensorType = Sensor.TYPE_GRAVITY;
        sm.registerListener(orientationListener, sm.getDefaultSensor(sensorType),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    final SensorEventListener orientationListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];
                if (Math.abs(x) <= 5 && Math.abs(y) >= 5) {
                    if (y >= 0) {
                        if (orientation == PORTRAIT_UP) {
                        } else {
                            orientation = PORTRAIT_UP;
                        }
                    } else {
                        if (orientation == PORTRAIT_DOWN) {
                        } else {
                            orientation = PORTRAIT_DOWN;
                        }
                    }

                } else if (Math.abs(x) > 5 && Math.abs(y) < 5) {
                    if (x >= 0) {
                        if (orientation == LANDSCAPE_LEFT) {
                        } else {
                            orientation = LANDSCAPE_LEFT;
                        }
                    } else {
                        if (orientation == LANDSCAPE_RIGHT) {
                        } else {
                            orientation = LANDSCAPE_RIGHT;
                        }
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

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
        surface.setLayoutParams(layoutParams(surface));
        camera.startPreview();
    }

    //вынести в отдельный класс
    private LayoutParams layoutParams (SurfaceView surfaceView) {
        camera.setDisplayOrientation(90);
        Camera.Size cameraSize = camera.getParameters().getPictureSize();
        float aspect = (float) cameraSize.height/cameraSize.width;
        int previewSurfaceWidth = surfaceView.getWidth();
        int previewSurfaceHeight = surfaceView.getHeight();
        Log.d("myLogs", "cameraSize.height/cameraSize.width = " + cameraSize.height + " " +
                cameraSize.width + "surface.getHeight()/surface.getWidth()" +
                surfaceView.getHeight() + " / " + surfaceView.getWidth());
        LayoutParams lp = surfaceView.getLayoutParams();
        Log.d("myLogs", "lp. height / width" + lp.height + " / " + lp.width);
        lp.height = (int) (previewSurfaceWidth / aspect);
        lp.width = previewSurfaceWidth;
        Log.d("myLogs", "new  lp. height / width" + lp.height + " / " + lp.width);
        return lp;
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

   /* @Override
    protected void onResume() {
        super.onResume();
        camera = Camera.open();
    }*/

    /*@Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();
        if (camera != null)
            camera.release();
        camera = null;
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

       private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            camera.lock();
        }
    }

    public void onClickVideo (View view) {
        File sdPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        sdPath = new File(sdPath.getAbsolutePath() + "/PhotoToDBX");
        sdPath.mkdir();
        videoFile = new File(sdPath,
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
        if (cameraId == 0) {
            switch (orientation){
                case PORTRAIT_UP:
                    scale = 90;
                    break;
                case PORTRAIT_DOWN:
                    scale = 270;
                    break;
                case LANDSCAPE_LEFT:
                    scale = 0;
                    break;
                case LANDSCAPE_RIGHT:
                    scale = 180;
                    break;
            }
        }
        else
            switch (orientation) {
                case PORTRAIT_UP:
                    scale = 270;
                    break;
                case PORTRAIT_DOWN:
                    scale = 90;
                    break;
                case LANDSCAPE_LEFT:
                    scale = 0;
                    break;
                case LANDSCAPE_RIGHT:
                    scale = 180;
                    break;
        }
        mediaRecorder.setOrientationHint(scale);
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