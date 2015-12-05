package com.example.bogdan.dropboxphoto.services;

import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.example.bogdan.dropboxphoto.infrastructure.Auth;
import com.example.bogdan.dropboxphoto.infrastructure.DbxApplication;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashSet;

public final class AccountService {
    private final Auth auth;
    private DropboxAPI<AndroidAuthSession> mDBApi;
    private Bus bus;

    public AccountService(DbxApplication application){
        auth = application.getAuth();
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
            /*this.fileNames = fileNames;
            Log.d("myLogs", "fileName in Thread Constructor = " + this.fileNames.size() + fileNames.size());*/
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

    @Subscribe
    public void login (LoginRequest request){
        auth.login();
    }


    public static class LoginRequest {
    }

    public static class LoginResponse{
    }


}
