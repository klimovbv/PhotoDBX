package com.example.bogdan.dropboxphoto;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Bogdan on 21.07.2015.
 */
public class LoadingThumbAsync extends AsyncTask <Void, Void, Bitmap> {
    private  Activity activity;
    private MyAdapter myAdapter;
    private int position;
    private String fileName;
    private ImageView imageView;
    private DropboxAPI<AndroidAuthSession> mDBApi;
    private String thumbnailFileName;
    private TextView textView;
    private String path;

    public LoadingThumbAsync (Activity activity, MyAdapter myAdapter,
                              ImageView imageView, String fileName,
                              DropboxAPI<AndroidAuthSession> mDBApi, TextView textView) {
        this.activity = activity;
        this.myAdapter = myAdapter;

        this.fileName = fileName;
        this.imageView = imageView;
        this.mDBApi = mDBApi;
        this.textView = textView;
        this.path = imageView.getTag().toString();


    }
    @Override
    protected Bitmap doInBackground(Void... params) {
        File thumbnailFile;
        Bitmap bitmap = null;
        DropboxAPI.DropboxInputStream dis = null;
        thumbnailFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                fileName);
        thumbnailFileName = thumbnailFile.getAbsolutePath();
        if (!thumbnailFile.exists()) {
            try {
                dis = mDBApi.getThumbnailStream("/Photos/" + fileName,
                        DropboxAPI.ThumbSize.ICON_256x256, DropboxAPI.ThumbFormat.JPEG);
                FileOutputStream fos = new FileOutputStream(thumbnailFile);
                bitmap = BitmapFactory.decodeStream(dis);
                if (bitmap != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                } else Log.d("myLogs", " BITMAP IS NULL");
                fos.close();

                dis.close();


            } catch (DropboxException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        bitmap = BitmapFactory.decodeFile(thumbnailFileName);


        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {

        if (!imageView.getTag().toString().equals(path)){
            return;
        } else {
        imageView.setImageBitmap(result);
        }

    }
}
