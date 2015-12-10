package com.example.bogdan.dropboxphoto.infrastructure;

import android.os.Handler;

import java.util.ArrayList;

public class ActionScheduler {
    private final DbxApplication applicationl;
    private final Handler handler;
    private final ArrayList<TimedCallback> timedCallbacks;
    private boolean isPaused;

    public ActionScheduler(DbxApplication application) {
        this.applicationl = application;
        handler = new Handler();
        timedCallbacks = new ArrayList<>();
    }

    public void onPause(){
        isPaused = true;
    }

    public void onResume(){
        isPaused = false;

        for (TimedCallback callback : timedCallbacks) {
            callback.schedule();
        }
    }

    public void postEveryMilliseconds(final Object request, long milliseconds){
        TimedCallback callback = new TimedCallback(new Runnable() {
            @Override
            public void run() {
                applicationl.getBus().post(request);
            }
        }, milliseconds);
        timedCallbacks.add(callback);
        callback.run();
    }


    private class TimedCallback implements Runnable {
        private final Runnable runnable;
        private final long delay;
        public TimedCallback (Runnable runnable, long delay){
            this.runnable = runnable;
            this.delay = delay;
        }

        @Override
        public void run() {
            if (isPaused) {
                return;
            }

            runnable.run();
            schedule();
        }

        public void schedule() {
            handler.postDelayed(this, delay);
        }
    }
}
