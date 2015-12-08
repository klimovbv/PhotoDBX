package com.example.bogdan.dropboxphoto.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.bogdan.dropboxphoto.R;
import com.example.bogdan.dropboxphoto.services.AccountService;
import com.squareup.otto.Subscribe;

public class VideoPlayer extends BaseAuthenticatedActivity {
    private ProgressDialog pDialog;
    private VideoView videoView;
    private String directory;
    private View progressFrame;
    private String videoName;

    @Override
    protected void onDbxAppCreate(Bundle savedInstanceState) {
        setContentView(R.layout.video_player);
        Intent intent = getIntent();
        videoName = intent.getStringExtra("filepath");
        directory = "/Video/";
        progressFrame = findViewById(R.id.video_player_progressFrame);


        toolbar.setNavigationIcon(R.drawable.ic_ab_close);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });

        videoView = (VideoView)findViewById(R.id.videoView);
        pDialog = new ProgressDialog(this);
        pDialog.setTitle(videoName);
        pDialog.setMessage("Buffering...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        pDialog.show();

        bus.post(new AccountService.LoadFileRequest(videoName));
    }

    @Subscribe
    public void onLoadFile (AccountService.LoadFileResponse response){
        MediaController mediaController = new MediaController(VideoPlayer.this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(response.file);
        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                pDialog.dismiss();
                videoView.start();
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.activity_preview_photo_menuDelete){
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Delete Video")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            progressFrame.setVisibility(View.VISIBLE);
                            bus.post(new AccountService.DeleteFileRequest(directory, null, videoName));
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
        /*Toast.makeText(this, response.deletedFiles.toString() + " deleted", Toast.LENGTH_SHORT).show();*/
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_preview_photo, menu);
        return true;
    }


}
