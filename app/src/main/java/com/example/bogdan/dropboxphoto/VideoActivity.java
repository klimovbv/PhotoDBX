package com.example.bogdan.dropboxphoto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
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
import android.widget.ImageButton;

import java.io.File;
import java.io.IOException;

public class VideoActivity extends Activity implements SurfaceHolder.Callback {

    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCES_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    private static final String VIDEO_DIR = "/Video/";
    private static final int PORTRAIT_UP = 1;
    private static final int PORTRAIT_DOWN = 2;
    private static final int LANDSCAPE_LEFT = 3;
    private static final int LANDSCAPE_RIGHT = 4;
    private String key, secret;
    private static final String TAG = "myLogs";
    private Camera camera;
    private int cameraId;
    private int orientation, angle;
    private int widthForCamera, heightForCamera;
    private int identificator;
    private int firstHeight, firstWidth;
    private SurfaceHolder holder;
    private SurfaceView surface;
    private MediaRecorder mediaRecorder;
    private File videoFile;
    private ImageButton buttonRecord, buttonChangeCamera;
    private boolean isRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_video);
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        key = prefs.getString(ACCESS_KEY_NAME, null);
        secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (!LoginClass.isLoggedIn) {
            LoginClass.makingSession(key, secret);
        }
        buttonRecord = (ImageButton) findViewById(R.id.record_button);
        buttonChangeCamera = (ImageButton) findViewById(R.id.change_button);
        surface = (SurfaceView) findViewById(R.id.surfaceViewVideo);
        holder = surface.getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.TRANSPARENT);
        cameraId = 0;
        identificator = 0;
        orientation = PORTRAIT_UP;
        angle = 90;
        int sensorType = Sensor.TYPE_GRAVITY;
        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sm.registerListener(orientationListener, sm.getDefaultSensor(sensorType),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    final SensorEventListener orientationListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                float x = event.values[0];
                float y = event.values[1];
                if (Math.abs(x) <= 5 && Math.abs(y) >= 5) {
                    if (y >= 0) {
                        if (orientation == PORTRAIT_UP) {
                        } else {
                            orientation = PORTRAIT_UP;
                            buttonRecord.setRotation(0);
                            buttonChangeCamera.setRotation(0);
                        }
                    } else {
                        if (orientation == PORTRAIT_DOWN) {
                        } else {
                            orientation = PORTRAIT_DOWN;
                            buttonRecord.setRotation(180);
                            buttonChangeCamera.setRotation(180);
                        }
                    }

                } else if (Math.abs(x) > 5 && Math.abs(y) < 5) {
                    if (x >= 0) {
                        if (orientation == LANDSCAPE_LEFT) {
                        } else {
                            orientation = LANDSCAPE_LEFT;
                            buttonRecord.setRotation(90);
                            buttonChangeCamera.setRotation(90);
                        }
                    } else {
                        if (orientation == LANDSCAPE_RIGHT) {
                        } else {
                            orientation = LANDSCAPE_RIGHT;
                            buttonRecord.setRotation(270);
                            buttonChangeCamera.setRotation(270);
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
            Camera.Parameters params = camera.getParameters();
            if (params.getSupportedFocusModes().contains(
                    Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                camera.setParameters(params);
            }
            camera.setDisplayOrientation(90);
        }
        else {
            Log.d(TAG, "camera opened");
        }
        try {
            LayoutParams lpr = layoutParams(surface);
            surface.setLayoutParams(lpr);
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            Log.d(TAG, "IO Exception" + e);
        }

        camera.startPreview();
    }

    //вынести в отдельный класс
    private LayoutParams layoutParams (SurfaceView surfaceView) {
        LayoutParams lp = surfaceView.getLayoutParams();
        if (identificator == 0) {
            firstHeight = surfaceView.getHeight();
            firstWidth = surfaceView.getWidth();
            identificator = 1;
        }
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size cameraSize = parameters.getPreviewSize();
        getSizeForCamera(firstHeight, firstWidth,
                cameraSize.width, cameraSize.height);
        lp.height = heightForCamera;
        lp.width = widthForCamera;
        /*parameters.setPreviewSize(heightForCamera, widthForCamera);
        camera.setParameters(parameters);*/
        return lp;
    }
    private void getSizeForCamera(int surfaceHeight, int surfaceWidth,
                                  int cameraHeight, int cameraWidth){
        float scale = Math.min((float) surfaceHeight / (float) cameraHeight,
                (float) surfaceWidth / (float) cameraWidth);
        widthForCamera = (int)((float)cameraWidth*scale);
        heightForCamera = (int)((float)cameraHeight*scale);
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
        if (isRecord) {
            onClickStopRecord();
            buttonRecord.setImageResource(R.drawable.ic_videocam_black_24dp);
            isRecord = false;
        } else {
            File sdPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            sdPath = new File(sdPath.getAbsolutePath() + "/PhotoToDBX");
            sdPath.mkdir();
            videoFile = new File(sdPath,
                    "testvideo" + System.currentTimeMillis() + ".3gp");
            if (prepareVideoRecorder()) {
                mediaRecorder.start();
                buttonRecord.setImageResource(R.drawable.ic_stop_black_24dp);
                isRecord = true;
                buttonChangeCamera.setVisibility(View.INVISIBLE);
            } else {
                releaseMediaRecorder();
            }
        }
    }
    public void onClickStopRecord() {
        if (mediaRecorder != null) {
            buttonRecord.setVisibility(View.VISIBLE);
            if (Camera.getNumberOfCameras() > 1) {
                buttonChangeCamera.setVisibility(View.VISIBLE);
            }
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
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        mediaRecorder.setProfile(CamcorderProfile
                .get(CamcorderProfile.QUALITY_HIGH));
        mediaRecorder.setOutputFile(videoFile.getAbsolutePath());
        mediaRecorder.setVideoSize(heightForCamera, widthForCamera);
        if (cameraId == 0) {
            switch (orientation){
                case PORTRAIT_UP:
                    angle = 90;
                    break;
                case PORTRAIT_DOWN:
                    angle = 270;
                    break;
                case LANDSCAPE_LEFT:
                    angle = 0;
                    break;
                case LANDSCAPE_RIGHT:
                    angle = 180;
                    break;
            }
        }
        else
            switch (orientation) {
                case PORTRAIT_UP:
                    angle = 270;
                    break;
                case PORTRAIT_DOWN:
                    angle = 90;
                    break;
                case LANDSCAPE_LEFT:
                    angle = 0;
                    break;
                case LANDSCAPE_RIGHT:
                    angle = 180;
                    break;
        }
        mediaRecorder.setOrientationHint(angle);
        try {
            mediaRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
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

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRecord = false;
        buttonRecord.setImageResource(R.drawable.ic_videocam_black_24dp);
        if (Camera.getNumberOfCameras() > 1) {
            buttonChangeCamera.setVisibility(View.VISIBLE);
        }
    }
}



