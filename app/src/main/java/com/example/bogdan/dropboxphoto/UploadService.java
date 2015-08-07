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
    private String key, secret, fileName, directoryName;
    private File file;
    private LoginClass loginClass = null;
    private DropboxAPI.UploadRequest mRequest;
    private ExecutorService es;
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

             } catch (FileNotFoundException | DropboxException e) {
                 e.printStackTrace();
             }
             Log.d ("myLogs", "STOPSELF RESULT with start id " + stopSelfResult(startId) + startId);
         }
     }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
