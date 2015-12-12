package com.example.bogdan.dropboxphoto.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.example.bogdan.dropboxphoto.R;
import com.example.bogdan.dropboxphoto.activities.BaseAuthenticatedActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class FilesViewHolder extends RecyclerView.ViewHolder {
    private ImageView thumbnailImage;
    private TextView fileNameTextView;
    private View backgroundView;
    private DropboxAPI<AndroidAuthSession> mDBApi;
    private static int backgroundColor;
    private static String directory;

    public FilesViewHolder(BaseAuthenticatedActivity activity, View view, String directory) {
        super(view);
        mDBApi = activity.getDbxApplication().getAuth().getmDBApi();
        this.thumbnailImage = (ImageView) view.findViewById(R.id.imageViewList);
        this.fileNameTextView = (TextView) view.findViewById(R.id.textViewList);
        this.backgroundView = view.findViewById(R.id.list_item_background);
        this.directory = directory;
        backgroundColor = activity.getResources().getColor(R.color.background_list_color);
    }

    public View getBackgroundView() {
        return backgroundView;
    }

    public void populate  (String fileName) {
        itemView.setTag(fileName);

        fileNameTextView.setText(fileName);

        if (cancelPotentialDownload(fileName, thumbnailImage)) {
            LoadingThumbAsyncTask loadingThumbAsyncTask = new LoadingThumbAsyncTask(thumbnailImage,
                    fileName, mDBApi);
            DownloadedDrawable downloadedDrawable = new DownloadedDrawable(loadingThumbAsyncTask);
            thumbnailImage.setBackground(downloadedDrawable);
            thumbnailImage.setImageResource(R.drawable.ic_insert_emoticon_black_24dp);

            loadingThumbAsyncTask.execute();
        }
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