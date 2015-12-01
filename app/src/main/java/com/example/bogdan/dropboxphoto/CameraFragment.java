package com.example.bogdan.dropboxphoto;

/*import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;*/

/*public class CameraFragment extends Fragment implements View.OnClickListener {

    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCES_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";
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
    private static String key, secret;
    private File sdPath;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private TextureView mTextureView;
    private ImageReader mImageReader;
    private File mFile;

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            mFile = new File(sdPath, System.currentTimeMillis() + ".jpg");
            mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));
        }
    };
    private static Activity mActivity;

    private static class ImageSaver implements Runnable {

        private final Image mImage;
        private final File mFile;
        public ImageSaver(Image image, File file){
            mImage = image;
            mFile = file;
        }

        @Override
        public void run() {

            String fileName = mFile.getAbsolutePath();
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                output.write(bytes);
            } catch (FileNotFoundException e){
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            } finally {
                mImage.close();
                Intent intent = new Intent (mActivity, UploadService.class);
                intent.putExtra("key", key);
                intent.putExtra("secret",  secret);
                intent.putExtra("filePath",fileName);
                intent.putExtra("dirPath", "/Photos/");
                mActivity.startService(intent);
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }

            *//*camera.takePicture(null, null, new PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes, Camera camera) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (rotate) {
                        int angle = 0;
                        switch (cameraId) {
                            case 0:
                                angle = 90 + i;
                                break;
                            case 1:
                                angle = 270 - i;
                                break;
                        }
                        bitmap = RotateBitmap(bitmap, angle);
                    }

                }
            });*//*
        };
    }


    public static CameraFragment newInstance() {
        CameraFragment fragment = new CameraFragment();
        fragment.setRetainInstance(true);// можно удалить - activity не будет пересоздаваться
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        view.findViewById(R.id.button_photo).setOnClickListener(this);
        view.findViewById(R.id.button_change_camera).setOnClickListener(this);
        mTextureView = (TextureView) view.findViewById(R.id.textureView);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //mFile = new File(getActivity().getExternalFilesDir(null), "pic.jpg");
        sdPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        sdPath = new File(sdPath.getAbsolutePath() + "/PhotoToDBX");
        sdPath.mkdir();
        mActivity = getActivity();
        SharedPreferences prefs = mActivity.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        key = prefs.getString(ACCESS_KEY_NAME, null);
        secret = prefs.getString(ACCESS_SECRET_NAME, null);
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();

        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void openCamera(int width, int height){
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        Activity activity = getActivity();
        CameraManager manager = (CameraManager)activity.getSystemService(Context.CAMERA_SERVICE);

    }


    //устанавливаем камеру - заднюю, и определяем положение исходящей фотографии
    private void setUpCameraOutputs(int width, int height) {
        Activity activity = getActivity();
        CameraManager manager = (CameraManager)activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()){
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);
                if (characteristics.get(CameraCharacteristics.LENS_FACING)
                        == CameraCharacteristics.LENS_FACING_FRONT){
                    continue;
                }
                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                // For still image captures, we use the largest available size.
                Size largest = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizeByArea());
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                        ImageFormat.JPEG, *//*maxImages*//*2);
                mImageReader.setOnImageAvailableListener(
                        mOnImageAvailableListener, mBackgroundHandler);

                // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data.
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        width, height, largest);

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mTextureView.setAspectRatio(
                            mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    mTextureView.setAspectRatio(
                            mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }

                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            new ErrorDialog().show(getFragmentManager(), "dialog");
        }
    }


    static class CompareSizeByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long)lhs.getWidth() * lhs.getHeight() -
                    (long)rhs.getWidth() * rhs.getHeight());
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread(){
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {

    }

    *//* @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                requestWindowFeature(Window.FEATURE_NO_TITLE);
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
                SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
                key = prefs.getString(ACCESS_KEY_NAME, null);
                secret = prefs.getString(ACCESS_SECRET_NAME, null);
                if (!LoginClass.isLoggedIn) {
                    LoginClass.makingSession(key, secret);
                }
                int sensorType = Sensor.TYPE_GRAVITY;
                SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                sm.registerListener(orientationListener, sm.getDefaultSensor(sensorType),
                        SensorManager.SENSOR_DELAY_NORMAL);
            }*//*
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

   *//* @Override
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
    }*//*

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
        *//*parameters.setPreviewSize(cameraSize.width, cameraSize.height);
        camera.setParameters(parameters);*//*
        return lp;
    }
    private void getSizeForCamera(int surfaceHeight, int surfaceWidth,
                                  int cameraHeight, int cameraWidth){
        float scale = Math.min((float)surfaceHeight/(float)cameraHeight,
                (float)surfaceWidth/(float)cameraWidth);
        widthForCamera = (int)((float)cameraWidth*scale);
        heightForCamera = (int)((float)cameraHeight*scale);
    }

    *//*@Override
    public void surfaceChanged(SurfaceHolder holder, int i, int i2, int i3) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }*//*

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
        *//*File sdPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        sdPath = new File(sdPath.getAbsolutePath() + "/PhotoToDBX");
        sdPath.mkdir();*//*
        photoFile = new File(sdPath,
                "test" + System.currentTimeMillis() + ".jpg");
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
                    Intent intent = new Intent (CameraFragment.this, UploadService.class);
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



   *//* @Override
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
}*/