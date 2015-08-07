package com.example.bogdan.dropboxphoto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CameraActivity extends Activity implements SurfaceHolder.Callback {

    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCES_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    private final String PHOTO_DIR = "/Photos/";
    private static final int PORTRAIT_UP = 1;
    private static final int PORTRAIT_DOWN = 2;
    private static final int LANDSCAPE_LEFT = 3;
    private static final int LANDSCAPE_RIGHT = 4;
    private LoginClass loginClass;
    private static final String TAG = "myLogs";
    private Camera camera;
    private int cameraId;
    private float x, y;
    private boolean rotate;
    private int  orientation;
    private int widthforCamera, heightForCamera;
    private int identificator;
    private int firstHeight, firstWidth;
    private SurfaceHolder holder;
    private SurfaceView surface;
    private File photoFile;
    private String fileName;
    private ImageButton buttonPhoto, buttonChangeCamera;
    private String key, secret;
    private FrameLayout frameLayout;
    FrameLayout.LayoutParams rightParams, leftParams;
    /*Handler uploadHandler;*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_camera);
        frameLayout = (FrameLayout)findViewById(R.id.frameLayout);
        cameraId = 0;
        identificator = 0;
        /*buttonPhoto = (ImageButton)findViewById(R.id.button2);
        buttonChangeCamera = (ImageButton)findViewById(R.id.button);*/
        surface = (SurfaceView) findViewById(R.id.surfaceView);
        buttonPhoto = new ImageButton(this);
        buttonChangeCamera = new ImageButton(this);


        leftParams = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rightParams = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        leftParams.gravity = Gravity.BOTTOM;
        leftParams.gravity = Gravity.LEFT;
        rightParams.gravity = Gravity.BOTTOM;
        rightParams.gravity = Gravity.RIGHT;
        buttonPhoto.setBackgroundResource(R.drawable.ic_camera_alt_black_24dp);
        buttonPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int scale = 0;
                rotate = true;
                switch (orientation){
                    case PORTRAIT_UP:
                        break;
                    case PORTRAIT_DOWN:
                        scale = 180;
                        break;
                    case LANDSCAPE_LEFT:
                        rotate = false;
                        break;
                    case LANDSCAPE_RIGHT:
                        scale = 90;
                        break;
                }
                takePicture(scale);
            }
        });

        buttonChangeCamera.setBackgroundResource(R.drawable.ic_switch_camera_black_24dp);
        buttonChangeCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraId == 0) {
                    cameraId = 1;
                } else {
                    cameraId = 0;
                }
                frameLayout.removeView(buttonPhoto);
                frameLayout.removeView(buttonChangeCamera);
                camera.stopPreview();
                camera.release();
                camera = null;
                surfaceCreated(holder);
            }
        });

        /*surface.setZOrderOnTop(false);*/

        holder = surface.getHolder();
        holder.setFormat(PixelFormat.TRANSPARENT);
        holder.addCallback(this);
        /*uploadHandler = new DownloadHandler(this);*/
        rotate = false;
        orientation = PORTRAIT_UP;
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        key = prefs.getString(ACCESS_KEY_NAME, null);
        secret = prefs.getString(ACCESS_SECRET_NAME, null);
        Log.d ("myLogs", key + " _  " + secret);
        loginClass = new LoginClass();
        loginClass.makingSession(key, secret);
        SensorManager sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        int sensorType = Sensor.TYPE_GRAVITY;
        sm.registerListener(orientationListener,sm.getDefaultSensor(sensorType),
                SensorManager.SENSOR_DELAY_NORMAL);
     }
    final SensorEventListener orientationListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                Animation animation = AnimationUtils.loadAnimation
                        (getApplicationContext(), R.anim.rotation);
                x = event.values[0];
                y = event.values[1];

                if (Math.abs(x) <= 5 && Math.abs(y) >= 5) {
                    if (y >= 0) {
                        if (orientation == PORTRAIT_UP) {
                        } else {
                            /*buttonPhoto.setRotation(0);
                            buttonChangeCamera.setRotation(0);*/
                            orientation = PORTRAIT_UP;
                        }
                    }
                    else {
                        if (orientation == PORTRAIT_DOWN) {
                        } else {
                            /*buttonPhoto.setRotation(180);
                            buttonChangeCamera.setRotation(180);*/
                            orientation = PORTRAIT_DOWN;
                        }
                    }
                } else if (Math.abs(x) > 5 && Math.abs(y) < 5) {
                        if (x >=0) {
                            if (orientation == LANDSCAPE_LEFT) {
                            } else {
                                /*buttonPhoto.setRotation(90);
                                buttonChangeCamera.setRotation(90);*/
                                orientation = LANDSCAPE_LEFT;
                            }
                        }
                        else {
                            if (orientation == LANDSCAPE_RIGHT){
                            } else {
                                /*buttonPhoto.setRotation(270);
                                buttonChangeCamera.setRotation(270);*/
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

    /**
     * @class DownloadHandler
     *
     * @brief A nested class that inherits from Handler and uses its
     *        handleMessage() hook method to process Messages sent to
     *        it from the DownloadService.
     */
 /*   private static class DownloadHandler extends Handler {
        *//**
         * Allows Activity to be garbage collected properly.
         *//*
        private WeakReference<CameraActivity> mActivity;

        *//**
         * Class constructor constructs mActivity as weak reference
         * to the activity
         *
         * @param activity The corresponding activity
         *//*
        public DownloadHandler(CameraActivity activity) {
            mActivity = new WeakReference<CameraActivity>(activity);
        }
    }*/

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (camera == null) {
            Log.d(TAG, "camera == null");
            camera = Camera.open(cameraId);
            camera.setDisplayOrientation(90);
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
        LayoutParams lpr = layoutParams(surface);
        surface.setLayoutParams(lpr);

        camera.startPreview();
        frameLayout.addView(buttonChangeCamera, rightParams);
        frameLayout.addView(buttonPhoto, leftParams);
        switch (orientation) {
            case PORTRAIT_UP:
                buttonPhoto.setRotation(0);
                buttonChangeCamera.setRotation(0);
                break;
            case PORTRAIT_DOWN:
                buttonPhoto.setRotation(0);
                buttonChangeCamera.setRotation(0);
                buttonPhoto.setRotation(180);
                buttonChangeCamera.setRotation(180);
                break;
            case LANDSCAPE_LEFT:
                buttonPhoto.setRotation(0);
                buttonChangeCamera.setRotation(0);
                buttonPhoto.setRotation(90);
                buttonChangeCamera.setRotation(90);
                break;
            case LANDSCAPE_RIGHT:
                buttonPhoto.setRotation(0);
                buttonChangeCamera.setRotation(0);
                buttonPhoto.setRotation(270);
                buttonChangeCamera.setRotation(270);
                break;
        }
    }
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
        lp.width = widthforCamera;
        parameters.setPreviewSize(heightForCamera, widthforCamera);
        camera.setParameters(parameters);
        return lp;
    }
    private void getSizeForCamera(int surfaceHeight, int surfaceWidth,
                                  int cameraHeight, int cameraWidth){
        float scale = Math.min((float)surfaceHeight/(float)cameraHeight,
                (float)surfaceWidth/(float)cameraWidth);
        widthforCamera = (int)((float)cameraWidth*scale);
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

    public void onClickPhoto(View view) {
        int scale = 0;
        rotate = true;
        switch (orientation){
            case PORTRAIT_UP:
                break;
            case PORTRAIT_DOWN:
                scale = 180;
                break;
            case LANDSCAPE_LEFT:
                rotate = false;
                break;
            case LANDSCAPE_RIGHT:
                scale = 90;
                break;
        }
        takePicture(scale);
    }

    private void takePicture(final int i) {
        File sdPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        sdPath = new File(sdPath.getAbsolutePath() + "/PhotoToDBX");
        sdPath.mkdir();
        photoFile = new File(sdPath,
                "test" + System.currentTimeMillis() + ".jpg");
        fileName = photoFile.getAbsolutePath();
        camera.takePicture(null, null, new PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0 , bytes.length);
                if (rotate){
                    int scale = 0;
                    switch (cameraId){
                        case 0:
                            scale = 90+i;
                            break;
                        case 1:
                            scale = 270-i;
                            break;
                    }
                    bitmap = RotateBitmap(bitmap, scale);
                }
                surfaceDestroyed(holder);
                surfaceCreated(holder);
                try {
                   FileOutputStream outStream = new FileOutputStream(photoFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
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

        camera.stopPreview();
        camera.release();
        camera = null;
        surfaceCreated(holder);
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}