package com.example.bogdan.dropboxphoto.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.example.bogdan.dropboxphoto.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MyAdapterOld extends BaseAdapter {
    private static int backgroundColor;
    private final ArrayList<String> names;
    private final DropboxAPI<AndroidAuthSession> mDBApi;
    private LayoutInflater layoutInflater;
    private String directory;

    public MyAdapterOld(Activity activity, ArrayList<String> names, DropboxAPI<AndroidAuthSession> mDBApi,
                        String directory) {
        super();
        this.names = names;
        this.mDBApi = mDBApi;
        this.directory  = directory;
        layoutInflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        backgroundColor = activity.getResources().getColor(R.color.background_list_color);
    }
    private static class ViewHolder {
        public ImageView imageView;
        public TextView textView;
    }

    @Override
    public int getCount() {
        return names.size();
    }

    @Override
    public String getItem(int position) {
        return names.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.textView = (TextView)convertView.findViewById(R.id.textViewList);
            holder.imageView = (ImageView)convertView.findViewById(R.id.imageViewList);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        holder.textView.setText(getItem(position));


        if (cancelPotentialDownload(getItem(position), holder.imageView)){
            LoadingThumbAsyncTask loadingThumbAsyncTask = new LoadingThumbAsyncTask(holder.imageView,
                    getItem(position), mDBApi);
            DownloadedDrawable downloadedDrawable = new DownloadedDrawable(loadingThumbAsyncTask);
            holder.imageView.setBackground(downloadedDrawable);
            holder.imageView.setImageResource(R.drawable.ic_camera_alt_black_24dp);

            loadingThumbAsyncTask.execute();
        }
        return convertView;
    }


    class LoadingThumbAsyncTask extends AsyncTask<Void, Void, Bitmap> {

        private String fileName;
        private DropboxAPI<AndroidAuthSession> mDBApi;
        private String thumbnailFileName;
        private ImageView imageView;
        public LoadingThumbAsyncTask (ImageView imageView, String fileName,
                                  DropboxAPI<AndroidAuthSession> mDBApi) {
            this.fileName = fileName;
            this.mDBApi = mDBApi;
            this.imageView = imageView;
        }
        @Override
        protected Bitmap doInBackground(Void... params) {
            File thumbnailFile;
            Bitmap bitmap = null;
            DropboxAPI.DropboxInputStream dis;
            thumbnailFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    fileName);
            thumbnailFileName = thumbnailFile.getAbsolutePath();
            if (!thumbnailFile.exists()) {
                try {
                    dis = mDBApi.getThumbnailStream(directory + fileName,
                            DropboxAPI.ThumbSize.ICON_256x256, DropboxAPI.ThumbFormat.JPEG);
                    FileOutputStream fos = new FileOutputStream(thumbnailFile);
                    bitmap = BitmapFactory.decodeStream(dis);
                    if (bitmap != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    }
                    fos.close();
                    dis.close();
                } catch (DropboxException | IOException e) {
                    e.printStackTrace();
                }
            } else {
                bitmap = BitmapFactory.decodeFile(thumbnailFileName);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
                LoadingThumbAsyncTask loadingThumbAsync = getBitmapDownloaderTask(imageView);
                if (this == loadingThumbAsync) {
                    imageView.setImageBitmap(result);
                }
        }
    }

    private static LoadingThumbAsyncTask getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getBackground();
            if (drawable instanceof DownloadedDrawable) {
                DownloadedDrawable downloadedDrawable = (DownloadedDrawable)drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }




    static class DownloadedDrawable extends ColorDrawable {
        private final WeakReference<LoadingThumbAsyncTask> bitmapDownloaderTaskReference;

        public DownloadedDrawable(LoadingThumbAsyncTask bitmapDownloaderTask) {
            super(backgroundColor);
            bitmapDownloaderTaskReference =
                    new WeakReference<LoadingThumbAsyncTask>(bitmapDownloaderTask);
        }

        public LoadingThumbAsyncTask getBitmapDownloaderTask() {
            return bitmapDownloaderTaskReference.get();
        }
    }

    private static boolean cancelPotentialDownload(String fileName, ImageView imageView) {
        LoadingThumbAsyncTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.fileName;
            if (!bitmapUrl.equals(fileName)) {
                bitmapDownloaderTask.cancel(true);
            } else {
                // The same URL is downloading in this time
                return false;
            }
        }
        return true;
    }
}
