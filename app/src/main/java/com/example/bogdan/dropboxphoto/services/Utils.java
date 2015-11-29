package com.example.bogdan.dropboxphoto.services;

import android.content.Context;
import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    public String makeFileName (Context context) {
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_hhmmss", Locale.ROOT);
        return dateFormat.format(currentDate);
    }
}
