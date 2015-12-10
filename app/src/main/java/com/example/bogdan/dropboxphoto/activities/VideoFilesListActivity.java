package com.example.bogdan.dropboxphoto.activities;

import android.util.Log;

import com.example.bogdan.dropboxphoto.services.AccountService;
import com.squareup.otto.Subscribe;

public class VideoFilesListActivity extends BaseFilesListActivity {

    public VideoFilesListActivity() {
        super("/Video/", VideoPlayer.class);
    }

    @Subscribe
    public void onLoadFileList(AccountService.LoadFileListResponse response){
        Log.d("myLogs", "----LoadFileListResponse -----" + response.fileList.size());
        attachLoadedFileList(response.fileList);
    }

    @Subscribe
    public void onDeleteFile(AccountService.DeleteFileResponse response){
        deleteFiles(response.deletedFiles);
    }
}
