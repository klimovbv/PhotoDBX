package com.example.bogdan.dropboxphoto.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.bogdan.dropboxphoto.R;
import com.example.bogdan.dropboxphoto.services.AccountService;
import com.example.bogdan.dropboxphoto.views.TouchImageView;
import com.squareup.otto.Subscribe;


public class PreviewImageActivity extends BaseAuthenticatedActivity {

    public static final int REQUEST_PHOTO_DELETE = 100;
    public static final String RESULT_EXTRA_PHOTO = "RESULT_EXTRA_PHOTO";
    private String filePath;
    private TouchImageView imageView;
    private String directory;
    private View progressFrame;
    private Uri fileUri;

    @Override
    protected void onDbxAppCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_preview_photo);
        Intent intent = getIntent();

        filePath = intent.getStringExtra("filepath");
        directory = "/Photos/";
        imageView = (TouchImageView)findViewById(R.id.imageView);
        progressFrame = findViewById(R.id.activity_preview_photo_progressFrame);
        progressFrame.setVisibility(View.VISIBLE);

        toolbar.setNavigationIcon(R.drawable.ic_ab_close);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMessage(RESULT_OK);
            }
        });

        bus.post(new AccountService.LoadFileRequest(filePath));
    }

    @Subscribe
    public void onImageLoaded(AccountService.LoadFileResponse response){
        progressFrame.setVisibility(View.GONE);
        fileUri = response.file;
        changeContent();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("myLogs", "Configuration changed " + newConfig.orientation);
        changeContent();
    }

    private void changeContent() {
        imageView.setImageURI(fileUri);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_preview_photo, menu);
        return true;
    }

    private void closeMessage(int resultCode){
        Intent data = new Intent();
        data.putExtra(RESULT_EXTRA_PHOTO, filePath);
        setResult(resultCode, data);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.activity_preview_photo_menuDelete){
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Delete Photo")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            progressFrame.setVisibility(View.VISIBLE);
                            bus.post(new AccountService.DeleteFileRequest(directory, null, filePath));
                        }
                    })
                    .setCancelable(false)
                    .setNeutralButton("Cancel", null)
                    .create();
            dialog.show();
        }

        return false;
    }

    @Subscribe
    public void onImageDeleted(AccountService.DeleteFileResponse response){
        progressFrame.setVisibility(View.GONE);
        closeMessage(REQUEST_PHOTO_DELETE);
    }
}

