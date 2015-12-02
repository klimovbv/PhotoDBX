package com.example.bogdan.dropboxphoto.views;

import android.view.View;

import com.example.bogdan.dropboxphoto.Camera2Activity;
import com.example.bogdan.dropboxphoto.R;
import com.example.bogdan.dropboxphoto.VideoActivity;
import com.example.bogdan.dropboxphoto.activities.BaseActivity;
import com.example.bogdan.dropboxphoto.activities.FileListActivity;

public class MainNavDrawer extends NavDrawer {

    public MainNavDrawer(final BaseActivity activity) {
        super(activity);

        addItem(new ActivityNavDrawerItem(
                FileListActivity.class,
                "Photo List",
                R.drawable.ic_launcher,
                R.id.include_main_nav_drawer_topItems,
                "/Photos/"));

        addItem(new ActivityNavDrawerItem(
                FileListActivity.class,
                "Video List",
                R.drawable.ic_launcher,
                R.id.include_main_nav_drawer_topItems,
                "/Video/"
                ));

        addItem(new ActivityNavDrawerItem(
                Camera2Activity.class,
                "Make Photo",
                R.drawable.ic_launcher,
                R.id.include_main_nav_drawer_topItems,
                null));

        addItem(new ActivityNavDrawerItem(
                VideoActivity.class,
                "Make Video",
                R.drawable.ic_launcher,
                R.id.include_main_nav_drawer_topItems,
                null));

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
