package com.example.bogdan.dropboxphoto;

import android.app.ActionBar;
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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;

/**
 * Created by Boss on 12.08.15.
 */
public class CameraPreviewActivity extends Activity {

    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCES_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    private final String PHOTO_DIR = "/Photos/";
    private static final int PORTRAIT_UP = 1;
    private static final int PORTRAIT_DOWN = 2;
    private static final int LANDSCAPE_LEFT = 3;
    private static final int LANDSCAPE_RIGHT = 4;
    private static final int PREVIOUS_ORIENTATION = 5;
    private static final String TAG = "myLogs";
    private Camera camera;
    private int cameraId;
    private boolean rotate;
    private int  orientation;
    private int widthForCamera, heightForCamera;
    private int identificator;
    private int firstHeight, firstWidth;
    private SurfaceHolder holder;
    private SurfaceView surface;
    private File photoFile;
    private String fileName;
    private ImageButton buttonPhoto, buttonChangeCamera;
    private String key, secret;
    private SensorManager sm;
    int sensorType;
    RelativeLayout frameLayout;
    CameraPreview cameraPreview;
    ImageButton button1, button2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_preview_camera);
        buttonPhoto = (ImageButton)findViewById(R.id.button_photo);
        button1 = new ImageButton(this);
        cameraId = 0;
        identificator = 0;
        buttonPhoto = (ImageButton)findViewById(R.id.button_photo);
        buttonChangeCamera = (ImageButton)findViewById(R.id.button_change_camera);


        rotate = false;
        orientation = PORTRAIT_UP;
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        key = prefs.getString(ACCESS_KEY_NAME, null);
        secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (!LoginClass.isLoggedIn) {
            LoginClass.makingSession(key, secret);
        }






        sensorType = Sensor.TYPE_GRAVITY;
        sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

    }
    final SensorEventListener orientationListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                float x = event.values[0];
                float y = event.values[1];

                if (Math.abs(x) <= 5 && Math.abs(y) >= 5) {
                    if (y >= 0) {
                        if (orientation != PORTRAIT_UP) {
                            buttonPhoto.setRotation(0);
                            buttonChangeCamera.setRotation(0);
                            if (button1 != null) {
                                button1.setRotation(0);
                            }
                            orientation = PORTRAIT_UP;
                        }
                    }
                    else {
                        if (orientation != PORTRAIT_DOWN) {
                            buttonPhoto.setRotation(180);
                            buttonChangeCamera.setRotation(180);
                            if (button1 != null) {
                                button1.setRotation(180);
                            }
                            orientation = PORTRAIT_DOWN;
                        }
                    }
                } else if (Math.abs(x) > 5 && Math.abs(y) < 5) {
                    if (x >=0) {
                        if (orientation != LANDSCAPE_LEFT) {
                            buttonPhoto.setRotation(90);
                            buttonChangeCamera.setRotation(90);
                            if (button1 != null) {
                                button1.setRotation(90);
                            }
                            orientation = LANDSCAPE_LEFT;
                        }
                    }
                    else {
                        if (orientation != LANDSCAPE_RIGHT){
                            buttonPhoto.setRotation(270);
                            buttonChangeCamera.setRotation(270);
                            if (button1 != null) {
                                button1.setRotation(270);
                            }
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

    public void onClickPhoto(View view) {
        int angle = 0;
        rotate = true;
        switch (orientation){
            case PORTRAIT_UP:
                break;
            case PORTRAIT_DOWN:
                angle = 180;
                break;
            case LANDSCAPE_LEFT:
                rotate = false;
                break;
            case LANDSCAPE_RIGHT:
                angle = 90;
                break;
        }
        takePicture(angle);
    }

    private void takePicture(final int i) {
        File sdPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        sdPath = new File(sdPath.getAbsolutePath() + "/PhotoToDBX");
        sdPath.mkdir();
        photoFile = new File(sdPath,
                "test" + System.currentTimeMillis() + ".jpg");
        fileName = photoFile.getAbsolutePath();
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (rotate){
                    int angle = 0;
                    switch (cameraId){
                        case 0:
                            angle = 90+i;
                            break;
                        case 1:
                            angle = 270-i;
                            break;
                    }
                    bitmap = RotateBitmap(bitmap, angle);
                }
                try {
                    FileOutputStream outStream = new FileOutputStream(photoFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                    outStream.close();
                    Intent intent = new Intent (CameraPreviewActivity.this, UploadService.class);
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
        /*sm.unregisterListener(orientationListener);
        orientation = PREVIOUS_ORIENTATION;*/
        camera.stopPreview();
        camera.release();
        camera = null;




        initializePreview();

        /*buttonChangeCamera.setVisibility(View.GONE);
        buttonPhoto.setVisibility(View.GONE);
        buttonPhoto.setRotation(0);
        buttonChangeCamera.setRotation(0);*/

        /*buttonChangeCamera.setVisibility(View.VISIBLE);
        buttonPhoto.setVisibility(View.VISIBLE);
        sm.registerListener(orientationListener,sm.getDefaultSensor(sensorType),
                SensorManager.SENSOR_DELAY_NORMAL);*/

    }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        camera.stopPreview();
        camera.release();
        camera = null;
        /*sm.unregisterListener(orientationListener);
        buttonChangeCamera.setVisibility(View.GONE);
        buttonPhoto.setVisibility(View.GONE);
        buttonPhoto.setRotation(0);
        buttonChangeCamera.setRotation(0);
        orientation = PREVIOUS_ORIENTATION;*/

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Camera.getNumberOfCameras() > 1) {
            buttonChangeCamera.setVisibility(View.VISIBLE);
        }
        buttonPhoto.setVisibility(View.VISIBLE);
        sm.registerListener(orientationListener,sm.getDefaultSensor(sensorType),
                SensorManager.SENSOR_DELAY_NORMAL);
        initializePreview();




    }

    @Override
    protected void onPause() {
        super.onPause();

    }
    private void initializePreview () {
        camera = Camera.open(cameraId);
        camera.setDisplayOrientation(90);
        if (cameraPreview != null) {
            frameLayout.removeView(cameraPreview);
            cameraPreview = null;
            /*frameLayout.removeView(button1);*/
        }
        cameraPreview = new CameraPreview(this, camera);
        RelativeLayout.LayoutParams previewParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        previewParams.addRule(RelativeLayout.CENTER_VERTICAL|RelativeLayout.CENTER_HORIZONTAL);
        cameraPreview.setLayoutParams(previewParams);

        /*FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.gravity = Gravity.BOTTOM|Gravity.LEFT;
        button1.setLayoutParams(buttonParams);
        button1.setImageResource(R.drawable.ic_camera_alt_black_24dp);*/



        frameLayout = (RelativeLayout)findViewById(R.id.frameLayout);
        frameLayout.addView(cameraPreview, 0);
        /*frameLayout.addView(button1, 2);*/
    }

}

