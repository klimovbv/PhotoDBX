package com.spb.kbv.dropboxphoto.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.spb.kbv.dropboxphoto.R;
import com.spb.kbv.dropboxphoto.services.AccountService;
import com.spb.kbv.dropboxphoto.views.TouchImageView;
import com.squareup.otto.Subscribe;


public class PreviewImageActivity extends BaseAuthenticatedActivity {

    public static final int REQUEST_PHOTO_DELETE = 100;
    public static final String RESULT_EXTRA_PHOTO = "RESULT_EXTRA_PHOTO";
    private String viewingFileName;
    private TouchImageView imageView;
    private String directory;
    private View progressFrame;
    private Bitmap bitmap;

    @Override
    protected void onDbxAppCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_preview_photo);
        Intent intent = getIntent();

        viewingFileName = intent.getStringExtra(BaseFilesListActivity.EXTRA_FILE_NAME);
        directory = "/Photos/";
        imageView = (TouchImageView)findViewById(R.id.imageView);
        progressFrame = findViewById(R.id.activity_preview_photo_progressFrame);
        progressFrame.setVisibility(View.VISIBLE);

        getSupportActionBar().setTitle(viewingFileName);

        toolbar.setNavigationIcon(R.drawable.ic_ab_close);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMessage(RESULT_OK);
            }
        });

        bus.post(new AccountService.LoadPhotoRequest(viewingFileName));
    }

    @Subscribe
    public void onImageLoaded(AccountService.LoadPhotoResponse response){
        progressFrame.setVisibility(View.GONE);
        bitmap = response.fileBitmap;
        changeContent(bitmap);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("myLogs", "Configuration changed " + newConfig.orientation);
        changeContent(bitmap);
    }

    private void changeContent(Bitmap bitmap) {
        if (bitmap != null)
            imageView.setImageBitmap(bitmap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_preview_photo, menu);
        return true;
    }

    private void closeMessage(int resultCode){
        Intent data = new Intent();
        data.putExtra(RESULT_EXTRA_PHOTO, viewingFileName);
        setResult(resultCode, data);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.activity_preview_photo_menuDelete){
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.dialog_delete_photo))
                    .setPositiveButton(getString(R.string.dialog_delete_button), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            progressFrame.setVisibility(View.VISIBLE);
                            bus.post(new AccountService.DeleteFileRequest(directory, null, viewingFileName));
                        }
                    })
                    .setCancelable(false)
                    .setNeutralButton(getString(R.string.dialog_cancel_button), null)
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

