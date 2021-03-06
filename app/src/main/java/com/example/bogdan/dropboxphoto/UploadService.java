package com.example.bogdan.dropboxphoto;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UploadService extends Service {
    private ExecutorService executorPool;

    @Override
    public void onCreate() {
        super.onCreate();
        executorPool = Executors.newFixedThreadPool(1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        String key = intent.getStringExtra("key");
        String secret = intent.getStringExtra("secret");
        if (LoginClass.isLoggedIn) {
            LoginClass.makingSession(key, secret);
        }
        String fileName = intent.getStringExtra("filePath");
        String directoryName = intent.getStringExtra("dirPath");
        File file = new File(fileName);
        String path = directoryName + file.getName();
        UploadFile uploadFile = new UploadFile(startId, path, file);
        executorPool.execute(uploadFile);
        return START_REDELIVER_INTENT;
    }
     class UploadFile implements Runnable {
         int startId;
         File file;
         String path;

         public UploadFile (int startId, String path, File file) {
             this.startId = startId;
             this.file = file;
             this.path = path;
         }

         @Override
         public void run() {
             try {
                 Log.d("myLogs", "void run entered");
                 FileInputStream fis = new FileInputStream(file);

                 DropboxAPI.UploadRequest mRequest = LoginClass.mDBApi.putFileOverwriteRequest(path, fis, file.length(),
                         null);
                 if (mRequest != null) {
                     mRequest.upload();
                 }
                 file.delete();
                 stopSelfResult(startId);

             } catch (FileNotFoundException | DropboxException e) {
                 e.printStackTrace();
             }

         }
     }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
