package com.example.bogdan.dropboxphoto.views;

import android.content.Intent;
import android.view.View;

import com.example.bogdan.dropboxphoto.activities.NewPhotoActivity;
import com.example.bogdan.dropboxphoto.R;
import com.example.bogdan.dropboxphoto.activities.NewVideoActivity;
import com.example.bogdan.dropboxphoto.activities.BaseActivity;
import com.example.bogdan.dropboxphoto.activities.PhotoFilesListActivity;
import com.example.bogdan.dropboxphoto.activities.VideoFilesListActivity;

public class MainNavDrawer extends NavDrawer {

    public MainNavDrawer(final BaseActivity activity) {
        super(activity);

        addItem(new ActivityNavDrawerItem(
                PhotoFilesListActivity.class,
                activity.getString(R.string.navdrawer_photolist),
                R.drawable.ic_image_black_24dp,
                R.id.include_main_nav_drawer_topItems));

        addItem(new ActivityNavDrawerItem(
                VideoFilesListActivity.class,
                activity.getString(R.string.navdrawer_videolist),
                R.drawable.ic_camera_roll_black_24dp,
                R.id.include_main_nav_drawer_topItems
        ));

        addItem(new BasicNavDrawerItem(
                activity.getString(R.string.navdrawer_make_photo),
                R.drawable.ic_add_a_photo_black_24dp,
                R.id.include_main_nav_drawer_topItems) {
            @Override
            public void onClick(View view) {
                activity.startActivity(new Intent(activity, NewPhotoActivity.class));
            }
        });

        addItem(new BasicNavDrawerItem(
                activity.getString(R.string.navdrawer_make_video),
                R.drawable.ic_videocam_black_24dp,
                R.id.include_main_nav_drawer_topItems) {
            @Override
            public void onClick(View view) {
                activity.startActivity(new Intent(activity, NewVideoActivity.class));
            }
        });

        addItem(new BasicNavDrawerItem(
                activity.getString(R.string.navdrawer_logout),
                R.drawable.ic_close_black_24dp,
                R.id.include_main_nav_drawer_bottomItems) {

            @Override
            public void onClick(View view) {
                activity.getDbxApplication().getAuth().logout();
            }
        });
    }
}
