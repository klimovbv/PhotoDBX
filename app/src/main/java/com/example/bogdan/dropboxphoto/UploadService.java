package com.example.bogdan.dropboxphoto;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UploadService extends Service {
    private static final String MESSENGER = "MESSENGER";
    private static final String FILE_NAME = "FILE_NAME";
    String key, secret, fileName, directoryName;
    File file;
    LoginClass loginClass = null;
    private final String PHOTO_DIR = "/Photos/";
    private DropboxAPI.UploadRequest mRequest;
    ExecutorService es;
    //срабатывает при создании
    @Override
    public void onCreate() {
        super.onCreate();
        es = Executors.newFixedThreadPool(1);

    }

    //срабатывает при уничтожении
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //срабатывает когда сервис запущен при помощи startService
    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        Log.d("myLogs", "uploadServise onStartCommand");
        key = intent.getStringExtra("key");
        secret = intent.getStringExtra("secret");
        loginClass = new LoginClass();
        loginClass.makingSession(key, secret);
        fileName = intent.getStringExtra("filePath");
        directoryName = intent.getStringExtra("dirPath");

        file = new File(fileName);
        String path = directoryName + file.getName();
        MyRun mr = new MyRun(startId, path, file);
        new Thread(mr).start();


        /*Thread downloadThread = new Thread (new Runnable() {
            @Override
            public void run() {


            }
        });*/


        /*downloadThread.start();*/
        return START_REDELIVER_INTENT;//сервис будет восстановлен после уничтожения
    }
     class MyRun implements Runnable {
         int startId;
         File file;
         String path;

         public MyRun (int startId, String path, File file) {
             this.startId = startId;
             this.file = file;
             this.path = path;
         }


         @Override
         public void run() {
             try {
                 Log.d("myLogs", "void run entered");
                 FileInputStream fis = new FileInputStream(file);

                 mRequest = loginClass.mDBApi.putFileOverwriteRequest(path, fis, file.length(), null);
                 if (mRequest != null) {
                     mRequest.upload();
                 }

             } catch (FileNotFoundException e) {
                 e.printStackTrace();
             }  catch (DropboxException e) {
                 e.printStackTrace();
             }
             Log.d ("myLogs", "STOPSELF RESULT with start id " + stopSelfResult(startId) + startId);

         }
     }

    // работа самого сервиса
   /* class LoadTask extends AsyncTask <Void, Long, Boolean> {

        private DropboxAPI<?> mApi;
        private String mPath;
        private File mFile;
        private long mFileLen;
        private DropboxAPI.UploadRequest mRequest;
        private Context mContext;
        private final ProgressDialog mDialog;

        public LoadTask(Context context, DropboxAPI<?> api, String dropboxPath,
                             File file) {
            mContext = context.getApplicationContext();
            mFileLen = file.length();
            mApi = api;
            mPath = dropboxPath;
            mFile = file;
            mDialog = new ProgressDialog(context);
            mDialog.setMax(100);
            mDialog.setMessage("Uploading " + file.getName());
            mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mDialog.setProgress(0);
            mDialog.setCancelable(false);
            mDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                FileInputStream fis = new FileInputStream(mFile);
                String path = mPath + mFile.getName();
                mRequest = mApi.putFileOverwriteRequest(path, fis, mFile.length(),
                        new ProgressListener() {
                            @Override
                            public long progressInterval() {
                                return 500;
                            }

                            @Override
                            public void onProgress(long bytes, long total) {
                                publishProgress(bytes);
                            }
                        });

                if (mRequest != null) {
                    mRequest.upload();
                    return true;
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }  catch (DropboxException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onProgressUpdate(Long... progress) {
            int percent = (int)(100.0*(double)progress[0]/mFileLen + 0.5);
            mDialog.setProgress(percent);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mDialog.dismiss();
            if (result) {
                showToast("Uploaded");
            } else {
                showToast("Did not upload");
            }
        }

        private void showToast(String msg) {
            Toast toast = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
            toast.show();
        }
    }*/

    @Override
    public IBinder onBind(Intent intent) {


        return null;
    }
}
