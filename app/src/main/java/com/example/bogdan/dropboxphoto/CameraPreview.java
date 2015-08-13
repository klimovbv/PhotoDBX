package com.example.bogdan.dropboxphoto;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.IOException;

/**
 * Created by Boss on 12.08.15.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{

    int heightForCamera, widthForCamera;


    SurfaceHolder holder;
    Camera camera;
    Context context;


    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.context = context;
        this.camera = camera;
        holder = getHolder();
        holder.addCallback(this);


    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            setLayoutParams(layoutParams());
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private ViewGroup.LayoutParams layoutParams () {
        ViewGroup.LayoutParams lp = getLayoutParams();
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size cameraSize = parameters.getPictureSize();
        getSizeForCamera(getHeight(), getWidth(),
                cameraSize.width, cameraSize.height);
        lp.height = heightForCamera;
        lp.width = widthForCamera;


        parameters.setPreviewSize(heightForCamera, widthForCamera);
        camera.setParameters(parameters);
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
