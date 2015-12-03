package com.example.bogdan.dropboxphoto.services;

import android.os.AsyncTask;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.example.bogdan.dropboxphoto.activities.BaseActivity;
import com.example.bogdan.dropboxphoto.infrastructure.Auth;
import com.example.bogdan.dropboxphoto.infrastructure.DbxApplication;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

public final class AccountService {
    private final Auth auth;
    public AccountService(DbxApplication application){
        auth = application.getAuth();
    }

    public static class LoginRequest {
    }

    public static class LoginResponse{
    }

    public static class LoadFileListRequest {
        private String directory;
        private BaseActivity activity;

        public LoadFileListRequest(BaseActivity activity, String directory) {
            this.directory = directory;
            this.activity = activity;
        }

    }

    public static class LoadFileListResponse{
        public ArrayList<String> fileList;
    }

    public class ListLoadingThread extends AsyncTask<String, Void, ArrayList<String>>{
        private BaseActivity activity;
        private ArrayList<String> fileUIArrayList;
        private DropboxAPI<AndroidAuthSession> mDBApi;
        private Bus bus;

        public ListLoadingThread(BaseActivity activity) {
            this.activity = activity;
            this.fileUIArrayList = new ArrayList<>();
            mDBApi = activity.getDbxApplication().getAuth().getmDBApi();
            bus = activity.getDbxApplication().getBus();
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
            response.fileList.clear();
            response.fileList.addAll(fileUIArrayList);
            bus.post(response);
        }
    }

    @Subscribe
    public void onLoadFileListRequest(LoadFileListRequest request) {
        new ListLoadingThread(request.activity).execute(request.directory);
    }


    @Subscribe
    public void login (LoginRequest request){
        auth.login();
    }


}
