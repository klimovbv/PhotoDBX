package com.example.bogdan.dropboxphoto.activities;

import android.util.Log;

import com.example.bogdan.dropboxphoto.services.AccountService;
import com.squareup.otto.Subscribe;

public class PhotoFilesListActivity extends BaseFilesListActivity {

    public PhotoFilesListActivity() {
        super("/Photos", PreviewImageActivity.class);
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
