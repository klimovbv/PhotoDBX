package com.example.bogdan.dropboxphoto.views;

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
import com.example.bogdan.dropboxphoto.activities.BaseAuthenticatedActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MyAdapter extends BaseAdapter {
    private static int backgroundColor;
    private ArrayList<String> fileList;
    private DropboxAPI<AndroidAuthSession> mDBApi;
    private LayoutInflater inflater;
    private BaseAuthenticatedActivity activity;
    private String directory;

    public MyAdapter(BaseAuthenticatedActivity activity,
                     String directory) {
        this.directory  = directory;
        this.activity = activity;
        fileList = new ArrayList<>();
        inflater = activity.getLayoutInflater();
        backgroundColor = activity.getResources().getColor(R.color.background_list_color);
        mDBApi = activity.getDbxApplication().getAuth().getmDBApi();
    }

    public ArrayList<String> getFileList() {
        return fileList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.fileName.setText(getItem(position));

        if (cancelPotentialDownload(getItem(position), holder.thumbnailImage)){
            LoadingThumbAsyncTask loadingThumbAsyncTask = new LoadingThumbAsyncTask(holder.thumbnailImage,
                    getItem(position), mDBApi);
            DownloadedDrawable downloadedDrawable = new DownloadedDrawable(loadingThumbAsyncTask);
            holder.thumbnailImage.setBackground(downloadedDrawable);
            holder.thumbnailImage.setImageResource(R.drawable.ic_insert_emoticon_black_24dp);

            loadingThumbAsyncTask.execute();
        }
        return convertView;
    }

    private static class ViewHolder {
        public ImageView thumbnailImage;
        public TextView fileName;

        private ViewHolder(View view) {
            this.thumbnailImage = (ImageView)view.findViewById(R.id.imageViewList);
            this.fileName = (TextView)view.findViewById(R.id.textViewList);
        }
    }

    @Override
    public int getCount() {
        return fileList.size();
    }

    @Override
    public String getItem(int position) {
        return fileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
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

    public interface ListActivityListener {
        void onFileClicked(String fileName);
    }
}
