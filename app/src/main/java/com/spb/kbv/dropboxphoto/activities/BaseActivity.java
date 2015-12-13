package com.spb.kbv.dropboxphoto.activities;

import android.animation.Animator;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.spb.kbv.dropboxphoto.R;
import com.spb.kbv.dropboxphoto.infrastructure.ActionScheduler;
import com.spb.kbv.dropboxphoto.infrastructure.DbxApplication;
import com.spb.kbv.dropboxphoto.views.NavDrawer;
import com.squareup.otto.Bus;

public class BaseActivity extends ActionBarActivity {
    private boolean isRegisteredWithBus;

    protected DbxApplication application;
    protected Toolbar toolbar;
    protected NavDrawer navDrawer;
    protected Bus bus;
    protected ActionScheduler scheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = (DbxApplication)getApplication();
        bus = application.getBus();
        bus.register(this);
        isRegisteredWithBus = true;
        scheduler = new ActionScheduler(application);
    }

    @Override
    protected void onResume() {
        super.onResume();
        scheduler.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scheduler.onPause();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        toolbar = (Toolbar)findViewById(R.id.include_toolbar);
        if (toolbar != null){
            setSupportActionBar(toolbar);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRegisteredWithBus){
            bus.unregister(this);
            isRegisteredWithBus = false;
        }
    }

    @Override
    public void finish() {
        super.finish();

        if(isRegisteredWithBus){
            bus.unregister(this);
            isRegisteredWithBus = false;
        }
    }

    protected void setNavdrawer(NavDrawer navDrawer){
        this.navDrawer = navDrawer;
        this.navDrawer.create();
        overridePendingTransition(0, 0);

        View rootView = findViewById(android.R.id.content);
        rootView.setAlpha(0);
        rootView.animate().alpha(1).setDuration(450).start();
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public interface FadeOutListener {
        void onFadeOutEnd();
    }

    public void fadeOut(final FadeOutListener listener) {
        View rootView = findViewById(android.R.id.content);
        rootView.animate()
                .alpha(0)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        listener.onFadeOutEnd();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                })
                .setDuration(300)
                .start();
    }

    public DbxApplication getDbxApplication(){
        return application;
    }
}
