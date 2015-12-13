package com.spb.kbv.dropboxphoto.services;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.spb.kbv.dropboxphoto.infrastructure.DbxApplication;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;

public final class AccountService {
    private DropboxAPI<AndroidAuthSession> mDBApi;
    private Bus bus;
    private DbxApplication application;

    public AccountService(DbxApplication application){
        this.application = application;
        mDBApi = application.getAuth().getmDBApi();
        bus = application.getBus();
        bus.register(this);
    }

    public static class LoadFileListRequest {
        public String directory;
        public LoadFileListRequest(String directory) {
            this.directory = directory;
        }
    }

    public static class LoadFileListResponse{
        public ArrayList<String> fileList;
    }

    public class ListLoadingThread extends AsyncTask<String, Void, ArrayList<String>>{
        public ArrayList<String> fileUIArrayList;
        public ListLoadingThread() {
            this.fileUIArrayList = new ArrayList<>();
        }

        @Override
        protected ArrayList<String> doInBackground(String... directory) {
            try {
                DropboxAPI.Entry entries = mDBApi.metadata(directory[0], 0, null, true, null);
                for (DropboxAPI.Entry entry : entries.contents) {
                    fileUIArrayList.add(entry.fileName());
                }
            } catch (DropboxException e) {
                e.printStackTrace();
            }
            return fileUIArrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            LoadFileListResponse response = new LoadFileListResponse();
            response.fileList = fileUIArrayList;
            bus.post(response);
        }

    }

    public static class DeleteFileRequest {
        public HashSet<String> fileNames;
        public String directory;
        public String fileToDelete;

        public DeleteFileRequest(String directory, HashSet<String> fileNames, String fileToDelete) {
            this.fileNames = fileNames;
            this.directory = directory;
            this.fileToDelete = fileToDelete;
        }
    }

    public static class DeleteFileResponse {
        public HashSet<String> deletedFiles;
    }



    @Subscribe
    public void onLoadFileListRequest(LoadFileListRequest request) {
        Log.d("myLogs", "----LoadFileListRequest -----" + request.directory);
        new ListLoadingThread().execute(request.directory);
    }

    @Subscribe
    public void onDeleteFileFromList(DeleteFileRequest request){
        if (request.fileNames != null) {
            String fileNames[] = request.fileNames.toArray(new String[request.fileNames.size()]);
            new DeleteFileThread(request.directory).execute(fileNames);
        } else {
            String fileNames = request.fileToDelete;
            new DeleteFileThread(request.directory).execute(fileNames);
        }
    }

    private class DeleteFileThread extends AsyncTask<String, Void, HashSet<String>> {
        public String directory;
        public DeleteFileThread (String directory/*, HashSet<String> fileNames*/) {
            this.directory = directory;
        }

        @Override
        protected HashSet<String> doInBackground(String... params) {
            HashSet<String> deletedFiles = new HashSet<>();
            Log.d("myLogs", "directory in onBack = " + directory);
            for (String fileName : params) {
                Log.d("myLogs", "fileName before try = " + fileName);
                try {
                    mDBApi.delete(directory + fileName);
                    deletedFiles.add(fileName);
                    /*return fileName;*/
                } catch (DropboxException e) {
                    e.printStackTrace();
                }
            }
            Log.d("myLogs", "fileName in Thread doInBack = " + deletedFiles.size());
            return deletedFiles;
        }

        @Override
        protected void onPostExecute(HashSet<String> deletedFiles) {
            Log.d("myLogs", "fleName in onPostEx = " + deletedFiles.size());
            DeleteFileResponse response = new DeleteFileResponse();
            response.deletedFiles = deletedFiles;
            bus.post(response);
        }
    }

    public static class LoadFileRequest {
        public String fileName;

        public LoadFileRequest(String fileName) {
            this.fileName = fileName;
        }
    }

    public static class LoadFileResponse {
        public Uri file;
    }


    @Subscribe
    public void onLoadFile(LoadFileRequest request){
        new LoadFileTask().execute(request.fileName);
    }

    private class LoadFileTask extends AsyncTask<String, Void, Uri>{

        @Override
        protected Uri doInBackground(String... params) {
            File file = new File(application.getCacheDir(),
                    "video_" + params[0]);
            if (!file.exists()) {
                try {
                    FileOutputStream outputStream = new FileOutputStream(file);
                    mDBApi.getFile("/Video/" + params[0], null, outputStream, null);
                } catch (FileNotFoundException | DropboxException e) {
                    e.printStackTrace();
                }
            }
            return Uri.parse(file.getAbsolutePath());
        }

        @Override
        protected void onPostExecute(Uri uri) {
            LoadFileResponse response = new LoadFileResponse();
            response.file = uri;
            bus.post(response);
        }
    }

    public static class LoadPhotoRequest {
        public String fileName;

        public LoadPhotoRequest(String fileName) {
            this.fileName = fileName;
        }
    }

    public static class LoadPhotoResponse {
        public Bitmap fileBitmap;
    }

    @Subscribe
    public void onLoadPhotoRequest(LoadPhotoRequest request){
        new LoadPhotoTask().execute(request.fileName);
    }

    public class LoadPhotoTask extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... params) {
            DropboxAPI.DropboxInputStream dis;
            try {
                dis = mDBApi.getThumbnailStream("/Photos/" + params[0], DropboxAPI.ThumbSize.BESTFIT_640x480, DropboxAPI.ThumbFormat.JPEG);
                return BitmapFactory.decodeStream(dis);
            } catch (DropboxException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            LoadPhotoResponse response = new LoadPhotoResponse();
            response.fileBitmap = bitmap;
            bus.post(response);
        }
    }
}
