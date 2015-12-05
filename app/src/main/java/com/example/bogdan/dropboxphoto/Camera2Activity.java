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
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.example.bogdan.dropboxphoto.activities.BaseAuthenticatedActivity;
import com.example.bogdan.dropboxphoto.services.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Camera2Activity extends BaseAuthenticatedActivity implements SurfaceHolder.Callback {

    private final String PHOTO_DIR = "/Photos/";
    private static final int PORTRAIT_UP = 1;
    private static final int PORTRAIT_DOWN = 2;
    private static final int LANDSCAPE_LEFT = 3;
    private static final int LANDSCAPE_RIGHT = 4;
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

    @Override
    protected void onDbxAppCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);
        cameraId = 1;
        identificator = 0;
        buttonPhoto = (ImageButton)findViewById(R.id.button_photo);
        buttonChangeCamera = (ImageButton)findViewById(R.id.button_change_camera);
        if (Camera.getNumberOfCameras() < 2) {
            buttonChangeCamera.setVisibility(View.INVISIBLE);
        }
        surface = (SurfaceView) findViewById(R.id.surfaceView);
        holder = surface.getHolder();
        holder.setFormat(PixelFormat.TRANSPARENT);
        holder.addCallback(this);
        rotate = false;
        orientation = PORTRAIT_UP;
        int sensorType = Sensor.TYPE_GRAVITY;
        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sm.registerListener(orientationListener, sm.getDefaultSensor(sensorType),
                SensorManager.SENSOR_DELAY_NORMAL);
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
                            orientation = PORTRAIT_UP;
                        }
                    }
                    else {
                        if (orientation != PORTRAIT_DOWN) {
                            buttonPhoto.setRotation(180);
                            buttonChangeCamera.setRotation(180);
                            orientation = PORTRAIT_DOWN;
                        }
                    }
                } else if (Math.abs(x) > 5 && Math.abs(y) < 5) {
                    if (x >=0) {
                        if (orientation != LANDSCAPE_LEFT) {
                            buttonPhoto.setRotation(90);
                            buttonChangeCamera.setRotation(90);
                            orientation = LANDSCAPE_LEFT;
                        }
                    }
                    else {
                        if (orientation != LANDSCAPE_RIGHT){
                            buttonPhoto.setRotation(270);
                            buttonChangeCamera.setRotation(270);
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
            Camera.Parameters params = camera.getParameters();
            if (params.getSupportedFocusModes().contains(
                    Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                camera.setParameters(params);
            }
            camera.setDisplayOrientation(90);
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
        return lp;
    }
    private void getSizeForCamera(int surfaceHeight, int surfaceWidth,
                                  int cameraHeight, int cameraWidth){
        float scale = Math.min((float)surfaceHeight/(float)cameraHeight,
                (float)surfaceWidth/(float)cameraWidth);
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
                new Utils().makeFileName(getApplicationContext()) + ".jpg");
        fileName = photoFile.getAbsolutePath();
        camera.takePicture(null, null, new PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0 , bytes.length);
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
                surfaceDestroyed(holder);
                surfaceCreated(holder);
                try {
                    FileOutputStream outStream = new FileOutputStream(photoFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                    outStream.close();
                    Intent intent = new Intent (Camera2Activity.this, UploadService.class);
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

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}