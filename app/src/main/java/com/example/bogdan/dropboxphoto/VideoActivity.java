package com.example.bogdan.dropboxphoto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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
    private static final int PREVIOUS_ORIENTATION = 5;
    private String key, secret;
    private LoginClass loginClass = null;
    private static final String TAG = "myLogs";
    private Camera camera;
    private int cameraId;
    private float x, y;
    private int orientation, angle;
    private int widthForCamera, heightForCamera;
    private int identificator;
    private int firstHeight, firstWidth;
    private SurfaceHolder holder;
    private SurfaceView surface;
    private MediaRecorder mediaRecorder;
    private File videoFile;
    private ImageButton buttonRecord, buttonStop, buttonChangeCamera;
    private SensorManager sm;
    private int sensorType;
    private boolean isRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video);
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        key = prefs.getString(ACCESS_KEY_NAME, null);
        secret = prefs.getString(ACCESS_SECRET_NAME, null);
        loginClass = new LoginClass();
        loginClass.makingSession(key, secret);
        buttonRecord = (ImageButton) findViewById(R.id.record_button);
        buttonChangeCamera = (ImageButton) findViewById(R.id.change_button);
        /*buttonStop = (ImageButton) findViewById(R.id.stop_button);*/
        /*buttonStop.setVisibility(View.INVISIBLE);*/
        surface = (SurfaceView) findViewById(R.id.surfaceViewVideo);
        holder = surface.getHolder();
        holder.addCallback(this);
        cameraId = 0;
        identificator = 0;
        orientation = PORTRAIT_UP;
        angle = 90;
        sensorType = Sensor.TYPE_GRAVITY;
        sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

    }

    final SensorEventListener orientationListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                x = event.values[0];
                y = event.values[1];
                if (Math.abs(x) <= 5 && Math.abs(y) >= 5) {
                    if (y >= 0) {
                        if (orientation == PORTRAIT_UP) {
                        } else {
                            orientation = PORTRAIT_UP;
                            buttonRecord.setRotation(0);
                            /*buttonStop.setRotation(0);*/
                            buttonChangeCamera.setRotation(0);
                        }
                    } else {
                        if (orientation == PORTRAIT_DOWN) {
                        } else {
                            orientation = PORTRAIT_DOWN;
                            buttonRecord.setRotation(180);
                            /*buttonStop.setRotation(180);*/
                            buttonChangeCamera.setRotation(180);
                        }
                    }

                } else if (Math.abs(x) > 5 && Math.abs(y) < 5) {
                    if (x >= 0) {
                        if (orientation == LANDSCAPE_LEFT) {
                        } else {
                            orientation = LANDSCAPE_LEFT;
                            buttonRecord.setRotation(90);
                            /*buttonStop.setRotation(90);*/
                            buttonChangeCamera.setRotation(90);
                        }
                    } else {
                        if (orientation == LANDSCAPE_RIGHT) {
                        } else {
                            orientation = LANDSCAPE_RIGHT;
                            buttonRecord.setRotation(270);
                            /*buttonStop.setRotation(270);*/
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
            camera.setDisplayOrientation(90);
        }
        else {
            Log.d(TAG, "camera opened");
        }
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            Log.d(TAG, "IO Exception" + e);
        }
        LayoutParams lpr = layoutParams(surface);
        surface.setLayoutParams(lpr);
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
        Camera.Size cameraSize = parameters.getPictureSize();
        getSizeForCamera(firstHeight, firstWidth,
                cameraSize.width, cameraSize.height);
        lp.height = heightForCamera;
        lp.width = widthForCamera;

        parameters.setPreviewSize(heightForCamera, widthForCamera);
        camera.setParameters(parameters);
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

        sm.unregisterListener(orientationListener);
        orientation = PREVIOUS_ORIENTATION;
        buttonChangeCamera.setRotation(0);
        buttonRecord.setRotation(0);
        /*buttonStop.setRotation(0);*/
        /*buttonStop.setVisibility(View.INVISIBLE);*/
        buttonRecord.setVisibility(View.INVISIBLE);
        buttonChangeCamera.setVisibility(View.INVISIBLE);
        camera.stopPreview();
        camera.release();
        camera = null;
        surfaceCreated(holder);
        buttonRecord.setVisibility(View.VISIBLE);
        buttonChangeCamera.setVisibility(View.VISIBLE);
        sm.registerListener(orientationListener,sm.getDefaultSensor(sensorType),
                SensorManager.SENSOR_DELAY_NORMAL);
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
                /*buttonStop.setVisibility(View.VISIBLE);*/
                buttonRecord.setImageResource(R.drawable.ic_stop_black_24dp);
                isRecord = true;
                /*buttonRecord.setVisibility(View.INVISIBLE);*/
                buttonChangeCamera.setVisibility(View.INVISIBLE);
            } else {
                releaseMediaRecorder();
            }
        }
    }
    public void onClickStopRecord() {
        if (mediaRecorder != null) {
            /*buttonStop.setVisibility(View.INVISIBLE);*/
            buttonRecord.setVisibility(View.VISIBLE);
            buttonChangeCamera.setVisibility(View.VISIBLE);
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

    }

    @Override
    protected void onStop() {
        super.onStop();
        sm.unregisterListener(orientationListener);
        /*buttonStop.setVisibility(View.INVISIBLE);*/
        buttonRecord.setVisibility(View.INVISIBLE);
        buttonChangeCamera.setVisibility(View.INVISIBLE);
        buttonChangeCamera.setRotation(0);
        buttonRecord.setRotation(0);
        /*buttonStop.setRotation(0);*/
        orientation = PREVIOUS_ORIENTATION;
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
    protected void onResume() {
        super.onResume();
        isRecord = false;
        /*buttonStop.setVisibility(View.INVISIBLE);*/
        buttonRecord.setVisibility(View.VISIBLE);
        buttonRecord.setImageResource(R.drawable.ic_videocam_black_24dp);
        buttonChangeCamera.setVisibility(View.VISIBLE);
        sm.registerListener(orientationListener,sm.getDefaultSensor(sensorType),
                SensorManager.SENSOR_DELAY_NORMAL);

    }
}



