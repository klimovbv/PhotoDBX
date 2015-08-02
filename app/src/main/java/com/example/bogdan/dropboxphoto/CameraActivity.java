package com.example.bogdan.dropboxphoto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
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
import java.lang.ref.WeakReference;

public class CameraActivity extends Activity implements SurfaceHolder.Callback {

    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCES_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    private static final int PORTRAIT_UP = 1;
    private static final int PORTRAIT_DOWN = 2;
    private static final int LANDSCAPE_LEFT = 3;
    private static final int LANDSCAPE_RIGHT = 4;
    LoginClass loginClass = null;

    private static final String TAG = "myLogs";
    private Camera camera;
    File photoFile;
    private int cameraId;
    SurfaceHolder holder;
    SurfaceView surface;
    Button buttonPhoto, buttonChangeCamera;
    String fileName;
    private final String PHOTO_DIR = "/Photos/";
    Handler uploadHandler;
    public String key, secret;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    float x, y, z;
    boolean rotate;
    int  orientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_camera);
        surface = (SurfaceView) findViewById(R.id.surfaceView);
        holder = surface.getHolder();
        /*holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);*/
        holder.addCallback(this);
        cameraId = 1;

        uploadHandler = new DownloadHandler(this);
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
    final SensorEventListener orientationListener =new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                Animation animation = AnimationUtils.loadAnimation
                        (getApplicationContext(), R.anim.rotation);
                buttonPhoto = (Button)findViewById(R.id.button2);
                buttonChangeCamera = (Button)findViewById(R.id.button);
                int prevOrientation = orientation;

                x = event.values[0];
                y = event.values[1];
                z = event.values[2];
                if (Math.abs(x) <= 5 && Math.abs(y) >= 5) {
                    if (y >= 0) {
                        if (orientation == PORTRAIT_UP) {
                        } else {
                            orientation = PORTRAIT_UP;
                            if (prevOrientation == LANDSCAPE_LEFT) {
                                buttonPhoto.setRotation(270);
                                buttonChangeCamera.setRotation(270);
                            } else if (prevOrientation == PORTRAIT_DOWN) {
                                buttonPhoto.setRotation(180);
                                buttonChangeCamera.setRotation(180);
                            } else {
                                buttonPhoto.setRotation(90);
                                buttonChangeCamera.setRotation(90);
                            }
                        }
                    }
                    else {
                        if (orientation == PORTRAIT_DOWN) {
                        } else {
                            orientation = PORTRAIT_DOWN;
                            if (prevOrientation == LANDSCAPE_LEFT) {
                                buttonPhoto.setRotation(90);
                                buttonChangeCamera.setRotation(90);
                            } else if (prevOrientation == PORTRAIT_UP) {
                                buttonPhoto.setRotation(180);
                                buttonChangeCamera.setRotation(180);
                            } else {
                                buttonPhoto.setRotation(270);
                                buttonChangeCamera.setRotation(270);
                            }
                        }
                    }

                } else if (Math.abs(x) > 5 && Math.abs(y) < 5) {
                        if (x >=0) {
                            if (orientation == LANDSCAPE_LEFT) {
                            } else {
                                orientation = LANDSCAPE_LEFT;
                                if (prevOrientation == PORTRAIT_UP) {
                                    buttonPhoto.setRotation(90);
                                    buttonChangeCamera.setRotation(90);
                                } else if (prevOrientation == PORTRAIT_DOWN) {
                                    buttonPhoto.setRotation(270);
                                    buttonChangeCamera.setRotation(270);
                                } else {
                                    buttonPhoto.setRotation(180);
                                    buttonChangeCamera.setRotation(180);
                                }
                            }
                        }
                        else {
                            if (orientation == LANDSCAPE_RIGHT){
                            } else {
                                orientation = LANDSCAPE_RIGHT;
                                if (prevOrientation == PORTRAIT_UP) {
                                buttonPhoto.setRotation(270);
                                buttonChangeCamera.setRotation(270);
                                } else if (prevOrientation == PORTRAIT_DOWN) {
                                buttonPhoto.setRotation(90);
                                buttonChangeCamera.setRotation(90);
                                } else {
                                buttonPhoto.setRotation(180);
                                buttonChangeCamera.setRotation(180);
                                }
                            /*buttonChangeCamera.startAnimation(animation);
                            buttonPhoto.startAnimation(animation);*/
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
        surface.setLayoutParams(layoutParams(surface));
        camera.startPreview();
    }
    private LayoutParams layoutParams (SurfaceView surfaceView) {
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        float aspect = (float) previewSize.height/previewSize.width;
        int previewSurfaceWidth = surfaceView.getWidth();
        Log.d("myLogs", "previewSize.height/previewSize.width = " + previewSize.height + " " +
                previewSize.width + "surface.getWidth()/surface.getWidth()" +
                surfaceView.getHeight() + " / " + surfaceView.getWidth());
        LayoutParams lp = surfaceView.getLayoutParams();
        camera.setDisplayOrientation(90);
        Log.d("myLogs", "lp. height / width" + lp.height + " / " + lp.width);
        lp.height = (int) (previewSurfaceWidth / aspect);
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

    public void onClickPhoto(View view) {
        Log.d("myLogs",  "ORIENTATION ==== " + x + " / " + y + " / " + z);
        int scale = 0;
        rotate = true;
        switch (orientation){
            case PORTRAIT_UP:
                Log.d("myLogs", "PORTRAIT_UP");
                break;
            case PORTRAIT_DOWN:
                Log.d("myLogs", "PORTRAIT_DOWN");
                scale = 180;
                break;
            case LANDSCAPE_LEFT:
                rotate = false;
                Log.d("myLogs", "LANDSCAPE_LEFT");
                break;
            case LANDSCAPE_RIGHT:
                scale = 90;
                Log.d("myLogs", "LANDSCAPE_RIGHT");
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
                    /*outStream.write(bytes);*/
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

        surfaceDestroyed(holder);
        surfaceCreated(holder);
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}