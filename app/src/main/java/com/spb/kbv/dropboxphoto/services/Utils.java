package com.spb.kbv.dropboxphoto.services;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

public class Utils {
    private static final int PORTRAIT_UP = 1;
    private static final int PORTRAIT_DOWN = 2;
    private static final int LANDSCAPE_LEFT = 3;
    private static final int LANDSCAPE_RIGHT = 4;

    public String makeFileName (Context context) {
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_hhmmss", Locale.ROOT);
        return dateFormat.format(currentDate);
    }

    public int settingOrientation(HashSet<ImageButton> buttons, int orientation, float x, float y){
        if (Math.abs(x) <= 5 && Math.abs(y) >= 5) {
            if (y >= 0) {
                if (orientation != PORTRAIT_UP) {
                    for (View view : buttons) {
                        view.setRotation(0);
                    }
                    orientation = PORTRAIT_UP;
                }
            } else {
                if (orientation != PORTRAIT_DOWN) {
                    for (View view : buttons) {
                        view.setRotation(180);
                    }
                    orientation = PORTRAIT_DOWN;
                }
            }
        } else if (Math.abs(x) > 5 && Math.abs(y) < 5) {
            if (x >=0) {
                if (orientation != LANDSCAPE_LEFT) {
                    for (View view : buttons) {
                        view.setRotation(90);
                    }
                    orientation = LANDSCAPE_LEFT;
                }
            } else {
                if (orientation != LANDSCAPE_RIGHT){
                    for (View view : buttons) {
                        view.setRotation(270);
                    }
                    orientation = LANDSCAPE_RIGHT;
                }
            }
        }
        return orientation;
    }
}
