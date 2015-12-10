package com.example.bogdan.dropboxphoto.activities;

import android.content.Context;
import android.content.Intent;
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
import java.util.HashSet;

public class NewVideoActivity extends BaseAuthenticatedActivity {

    private static final String VIDEO_DIR = "/Video/";
    private static final int PORTRAIT_UP = 1;
    private static final int PORTRAIT_DOWN = 2;
    private static final int LANDSCAPE_LEFT = 3;
    private static final int LANDSCAPE_RIGHT = 4;
    private static final String TAG = "myLogs";
    private Camera camera;
    private int orientation, angle;
    private MediaRecorder mediaRecorder;
    private File videoFile;
    private ImageButton buttonRecord, buttonChangeCamera;
    private boolean isRecord;

    private Camera.CameraInfo cameraInfo;
    private static int currentCameraIndex;
    private static CameraPreview cameraPreview;
    private static HashSet<ImageButton> buttons;

    @Override
    protected void onDbxAppCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video);

        currentCameraIndex = 0;
        orientation = PORTRAIT_UP;
        angle = 90;

        cameraPreview = new CameraPreview(this);

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.activity_video_frameLayout);
        frameLayout.addView(cameraPreview, 0);

        buttonRecord = (ImageButton) findViewById(R.id.record_button);
        buttonChangeCamera = (ImageButton) findViewById(R.id.change_button);
        buttons = new HashSet<>();
        buttons.add(buttonRecord);
        buttons.add(buttonChangeCamera);

        if (Camera.getNumberOfCameras() < 2) {
            buttonChangeCamera.setVisibility(View.INVISIBLE);
        }

        int sensorType = Sensor.TYPE_GRAVITY;
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(orientationListener, sensorManager.getDefaultSensor(sensorType),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    final SensorEventListener orientationListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                float x = event.values[0];
                float y = event.values[1];
                orientation = new Utils().settingOrientation(buttons, orientation, x, y);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        establishCamera();
        isRecord = false;
        buttonRecord.setImageResource(R.drawable.ic_videocam_white_24dp);
        if (Camera.getNumberOfCameras() > 1) {
            buttonChangeCamera.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mediaRecorder != null) {
            mediaRecorder.stop();
            releaseMediaRecorder();
            startUploadFile();
        }

        clearCamera();
    }

    private void startUploadFile(){
        Intent intent = new Intent (NewVideoActivity.this, UploadService.class);
        intent.putExtra("filePath",videoFile.getAbsolutePath());
        intent.putExtra("dirPath", VIDEO_DIR);
        startService(intent);
    }

    private void clearCamera(){
        if (camera != null){
            cameraPreview.setCamera(null, null);
            camera.release();
            camera = null;
        }
    }

    private void establishCamera() {
        clearCamera();

        try {
            camera = Camera.open(currentCameraIndex);
        } catch (Exception e){
            Log.e(TAG, "Could not open camera " + currentCameraIndex, e);
            Toast.makeText(this, "Error establishing camera!", Toast.LENGTH_LONG).show();
            return;
        }

        Camera.Parameters params = camera.getParameters();
        if (params.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            camera.setParameters(params);
        }
        camera.setDisplayOrientation(90);
        cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(currentCameraIndex, cameraInfo);
        cameraPreview.setCamera(camera, cameraInfo);

    }

    public void onClickChangeCamera(View view) {
        currentCameraIndex = currentCameraIndex + 1 < Camera.getNumberOfCameras() ? currentCameraIndex + 1 : 0;
        establishCamera();
    }

       private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            /*camera.reconnect();*/
        }

        clearCamera();
    }

    public void onClickVideo (View view) {
        if (isRecord) {
            onClickStopRecord();
            buttonRecord.setImageResource(R.drawable.ic_videocam_white_24dp);
            isRecord = false;
        } else {
            File sdPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            sdPath = new File(sdPath.getAbsolutePath() + "/PhotoToDBX");
            sdPath.mkdir();
            videoFile = new File(sdPath,
                    new Utils().makeFileName(getApplicationContext()) + ".3gp");
            if (prepareVideoRecorder()) {
                mediaRecorder.start();
                buttonRecord.setImageResource(R.drawable.ic_stop_white_24dp);
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
            establishCamera();
            startUploadFile();
        }
    }
    private boolean prepareVideoRecorder() {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size cameraSize = parameters.getPreviewSize();
        camera.unlock();
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        mediaRecorder.setProfile(CamcorderProfile
                .get(CamcorderProfile.QUALITY_HIGH));
        mediaRecorder.setOutputFile(videoFile.getAbsolutePath());
        mediaRecorder.setVideoSize(cameraSize.width, cameraSize.height);
        if (currentCameraIndex == 0) {
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
    protected void onStop() {
        super.onStop();
    }


}



