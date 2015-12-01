package com.example.bogdan.dropboxphoto.views;

import android.view.View;

import com.example.bogdan.dropboxphoto.Camera2Activity;
import com.example.bogdan.dropboxphoto.MainActivity;
import com.example.bogdan.dropboxphoto.R;
import com.example.bogdan.dropboxphoto.activities.BaseActivity;

public class MainNavDrawer extends NavDrawer {

    public MainNavDrawer(final BaseActivity activity) {
        super(activity);

        addItem(new ActivityNavDrawerItem(
                MainActivity.class,
                "Inbox",
                R.drawable.ic_launcher,
                R.id.include_main_nav_drawer_topItems));

        addItem(new ActivityNavDrawerItem(
                Camera2Activity.class,
                "Camera",
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