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
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class UploadService extends Service {
    private static final String MESSENGER = "MESSENGER";
    private static final String FILE_NAME = "FILE_NAME";

    public static Intent makeIntent (Context context,
                                     String fileName, Handler downloadHandler) {
        Intent intent = new Intent(context, UploadService.class);
        intent.putExtra(FILE_NAME, fileName);
        intent.putExtra(MESSENGER, new Messenger(downloadHandler));
        return intent;
    }

    public UploadService() {
    }

    //срабатывает при создании
    @Override
    public void onCreate() {
        super.onCreate();
    }

    //срабатывает при уничтожении
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //срабатывает когда сервис запущен при помощи startService
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LoadTask loadTask = new LoadTask();
        loadTask.execute();
        return START_REDELIVER_INTENT;//сервис будет восстановлен после уничтожения
    }

    // работа самого сервиса
    class LoadTask extends AsyncTask <Void, Long, Boolean> {

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
    }

    @Override
    public IBinder onBind(Intent intent) {

       
        return null;
    }
}
