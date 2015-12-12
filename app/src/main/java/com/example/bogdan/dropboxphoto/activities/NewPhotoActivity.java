package com.example.bogdan.dropboxphoto.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.bogdan.dropboxphoto.R;
import com.example.bogdan.dropboxphoto.services.UploadService;
import com.example.bogdan.dropboxphoto.services.Utils;
import com.example.bogdan.dropboxphoto.views.CameraPreview;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;

public class NewPhotoActivity extends BaseAuthenticatedActivity {

    private static final String PHOTO_DIR = "/Photos/";
    private static final int PORTRAIT_UP = 1;
    private static final int PORTRAIT_DOWN = 2;
    private static final int LANDSCAPE_LEFT = 3;
    private static final int LANDSCAPE_RIGHT = 4;
    private static final String TAG = "CameraActivity";
    private Camera camera;
    private boolean rotate;
    private int  orientation;
    private File photoFile;
    private HashSet <ImageButton> buttons;
    private int currentCameraIndex;
    private CameraPreview cameraPreview;

    @Override
    protected void onDbxAppCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);
        currentCameraIndex = 0;
        orientation = PORTRAIT_UP;
        rotate = false;

        cameraPreview = new CameraPreview(this);

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.activity_camera_frameLayout);
        frameLayout.addView(cameraPreview, 0);

        buttons = new HashSet<>();
        ImageButton buttonPhoto = (ImageButton) findViewById(R.id.button_photo);
        ImageButton buttonChangeCamera = (ImageButton) findViewById(R.id.button_change_camera);
        buttons.add(buttonPhoto);
        buttons.add(buttonChangeCamera);

        if (Camera.getNumberOfCameras() < 2) {
            buttonChangeCamera.setVisibility(View.INVISIBLE);
        }

        int sensorType = Sensor.TYPE_GRAVITY;
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(orientationListener, sensorManager.getDefaultSensor(sensorType),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onResume() {
        super.onResume();
        establishCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null) {
            cameraPreview.setCamera(null, null);
            camera.release();
            camera = null;
        }
    }

    private void establishCamera() {
        if (camera != null) {
            cameraPreview.setCamera(null, null);
            camera.release();
            camera = null;
        }

        try {
            camera = Camera.open(currentCameraIndex);
        } catch (Exception e){
            Log.e(TAG, "Could not open camera " + currentCameraIndex, e);
            Toast.makeText(this, "Error establishing camera!", Toast.LENGTH_LONG).show();
            return;
        }
        Camera.Parameters params = camera.getParameters();
        if (params.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            camera.setParameters(params);
        }
        camera.setDisplayOrientation(90);
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(currentCameraIndex, cameraInfo);
        cameraPreview.setCamera(camera, cameraInfo);
    }

    final SensorEventListener orientationListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                float x = event.values[0];
                float y = event.values[1];
                orientation = new Utils().settingOrientation(buttons, orientation, x, y);
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
        photoFile = new File(getCacheDir(),
                new Utils().makeFileName(getApplicationContext()) + ".jpg");
        camera.takePicture(null, null, new PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (rotate) {
                    int angle = 0;
                    switch (currentCameraIndex) {
                        case 0:
                            angle = 90 + i;
                            break;
                        case 1:
                            angle = 270 - i;
                            break;
                    }
                    bitmap = RotateBitmap(bitmap, angle);
                }
                establishCamera();
                try {
                    FileOutputStream outStream = new FileOutputStream(photoFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                    outStream.close();
                    Intent intent = new Intent(NewPhotoActivity.this, UploadService.class);
                    intent.putExtra("filePath", photoFile.getAbsolutePath());
                    intent.putExtra("dirPath", PHOTO_DIR);
                    startService(intent);
                } catch (IOException e) {
                    Toast.makeText(NewPhotoActivity.this, "Error while saving file", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void onClickChangeCamera(View view) {
        currentCameraIndex = currentCameraIndex + 1 < Camera.getNumberOfCameras() ? currentCameraIndex + 1 : 0;
        establishCamera();
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

}