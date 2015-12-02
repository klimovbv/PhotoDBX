package com.example.bogdan.dropboxphoto.views;

import android.view.View;

import com.example.bogdan.dropboxphoto.Camera2Activity;
import com.example.bogdan.dropboxphoto.R;
import com.example.bogdan.dropboxphoto.VideoActivity;
import com.example.bogdan.dropboxphoto.activities.BaseActivity;
import com.example.bogdan.dropboxphoto.activities.PhotoFilesListActivity;
import com.example.bogdan.dropboxphoto.activities.VideoFilesListActivity;

public class MainNavDrawer extends NavDrawer {

    public MainNavDrawer(final BaseActivity activity) {
        super(activity);

        addItem(new ActivityNavDrawerItem(
                PhotoFilesListActivity.class,
                "Photo List",
                R.drawable.ic_launcher,
                R.id.include_main_nav_drawer_topItems));

        addItem(new ActivityNavDrawerItem(
                VideoFilesListActivity.class,
                "Video List",
                R.drawable.ic_launcher,
                R.id.include_main_nav_drawer_topItems
                ));

        addItem(new ActivityNavDrawerItem(
                Camera2Activity.class,
                "Make Photo",
                R.drawable.ic_launcher,
                R.id.include_main_nav_drawer_topItems));

        addItem(new ActivityNavDrawerItem(
                VideoActivity.class,
                "Make Video",
                R.drawable.ic_launcher,
                R.id.include_main_nav_drawer_topItems));

        addItem(new BasicNavDrawerItem(
                "Logout",
                R.drawable.ic_launcher,
                R.id.include_main_nav_drawer_bottomItems) {

            @Override
            public void onClick(View view) {
                activity.getDbxApplication().getAuth().logout();
            }
        });
    }
}
