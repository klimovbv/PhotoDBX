package com.spb.kbv.dropboxphoto.views;

import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.spb.kbv.dropboxphoto.activities.BaseActivity;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "CameraPreview";

    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private Camera.CameraInfo cameraInfo;
    private boolean isSurfaceCreated;

    private int firstWidth, firstHeight;
    private int heightForCamera;
    private int widthForCamera;
    public CameraPreview(BaseActivity activity) {
        super(activity);
        isSurfaceCreated = false;
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
    }

    public void setCamera(Camera camera, Camera.CameraInfo cameraInfo) {
        if (this.camera != null) {
            try {
                this.camera.stopPreview();
            } catch(Exception e){
                Log.e(TAG, "Could not stop camera preview. ", e);
            }
        }

        this.camera = camera;
        this.cameraInfo = cameraInfo;

        if (camera == null){
            return;
        }

        if(!isSurfaceCreated){
            return;
        }

        try {
            camera.setPreviewDisplay(surfaceHolder);
            /*configureCamera();*/
        } catch (IOException e) {
            Log.e(TAG, "Could not start camera preview. ", e);
        }
        ViewGroup.LayoutParams lpr = layoutParams(this);
        this.setLayoutParams(lpr);
        camera.startPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (surfaceHolder != holder){
            surfaceHolder = holder;
            surfaceHolder.addCallback(this);
        }
        isSurfaceCreated = true;
        if (camera != null){
            setCamera(camera, cameraInfo);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isSurfaceCreated = false;
        surfaceHolder.removeCallback(this);
        surfaceHolder = null;

        if (camera == null) {
            return;
        }

        try {
            camera.stopPreview();
            camera = null;
            cameraInfo = null;
        } catch (Exception e) {
            Log.e(TAG, "Could not stop camera preview. ", e);
        }
    }

    private void configureCamera() {
        Camera.Parameters params = camera.getParameters();
        if (params.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            camera.setParameters(params);
        }
        camera.setDisplayOrientation(90);
    }

    private ViewGroup.LayoutParams layoutParams (SurfaceView surfaceView) {
        ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
        /*if (identificator == 0) {*/
            firstHeight = surfaceView.getHeight();
            firstWidth = surfaceView.getWidth();
        /*    identificator = 1;
        }*/
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
}
