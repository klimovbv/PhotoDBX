package com.example.bogdan.dropboxphoto;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Bogdan on 21.07.2015.
 */
public class MyAdapter extends BaseAdapter {
    private final Activity activity;
    private final ArrayList<String> names;
    private final DropboxAPI<AndroidAuthSession> mDBApi;
    private LayoutInflater layoutInflater;
    private ArrayList<LoadingThumbAsync> asyncs;
    private String directory;


    public MyAdapter(Activity activity, ArrayList <String> names, DropboxAPI<AndroidAuthSession> mDBApi,
                     String directory) {

        super();
        this.activity = activity;
        this.names = names;
        this.mDBApi = mDBApi;
        this.directory  = directory;
        layoutInflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    static class ViewHolder {
        public ImageView imageView;
        public TextView textView;
    }

    @Override
    public int getCount() {
        return names.size();
    }

    @Override
    public Object getItem(int position) {
        return names.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        System.out.println("---------getView " + position + " " + convertView);
        ViewHolder holder;
        View rowView = convertView;

        if (rowView == null) {
            rowView = layoutInflater.inflate(R.layout.list_item, null, true);
            holder = new ViewHolder();
            holder.textView = (TextView)rowView.findViewById(R.id.textViewList);
            holder.imageView = (ImageView)rowView.findViewById(R.id.imageViewList);


            rowView.setTag(holder);
        } else {
            holder = (ViewHolder)rowView.getTag();
        }

        holder.textView.setText(names.get(position));
        Log.d("myLogs", " int position = " + position + " fileName = " + names.get(position));
        /*holder.imageView.setTag(position);*/
        /*imageThumbnailLoader.DisplayImage(names.get(position), holder.imageView);*/





        if (cancelPotentialDownload(names.get(position), holder.imageView)){
            LoadingThumbAsync as = new LoadingThumbAsync(activity, this, holder.imageView,
                    names.get(position), mDBApi, holder.textView);
            DownloadedDrawable downloadedDrawable = new DownloadedDrawable(as);
            holder.imageView.setImageDrawable(downloadedDrawable);
            as.execute();
        }

        /*File thumbnailFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                names.get(position));
        String thumbnailFileName = thumbnailFile.getAbsolutePath();
        holder.imageView.setImageBitmap(BitmapFactory.decodeFile(thumbnailFileName));*/

        return rowView;
    }

    class LoadingThumbAsync extends AsyncTask<Void, Void, Bitmap> {
        private Activity activity;
        private MyAdapter myAdapter;
        private int position;
        private String fileName;
        private ImageView imageView;
        private DropboxAPI<AndroidAuthSession> mDBApi;
        private String thumbnailFileName;
        private TextView textView;
        private String path;

        private String url;
        private final WeakReference<ImageView> imageViewWeakReference;

        public LoadingThumbAsync (Activity activity, MyAdapter myAdapter,
                                  ImageView imageView, String fileName,
                                  DropboxAPI<AndroidAuthSession> mDBApi, TextView textView) {
            this.activity = activity;
            this.myAdapter = myAdapter;

            this.fileName = fileName;
        /*this.imageView = imageView;*/
            this.mDBApi = mDBApi;
            this.textView = textView;
        /*this.path = imageView.getTag().toString();*/


            imageViewWeakReference = new WeakReference<ImageView>(imageView);
        }
        @Override
        protected Bitmap doInBackground(Void... params) {
            Log.d("myLogs", " entered to ASYNC");
            File thumbnailFile;
            Bitmap bitmap = null;
            DropboxAPI.DropboxInputStream dis = null;
            thumbnailFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    fileName);
            thumbnailFileName = thumbnailFile.getAbsolutePath();
            if (!thumbnailFile.exists()) {
                Log.d("myLogs", " !EXIST "+ thumbnailFileName);
                try {
                    dis = mDBApi.getThumbnailStream(directory + fileName,
                            DropboxAPI.ThumbSize.ICON_256x256, DropboxAPI.ThumbFormat.JPEG);
                    FileOutputStream fos = new FileOutputStream(thumbnailFile);
                    bitmap = BitmapFactory.decodeStream(dis);
                    if (bitmap != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        Log.d("myLogs", " BITMAP IS NOT NULL");
                    } else Log.d("myLogs", " BITMAP IS NULL");
                    fos.close();

                    dis.close();


                } catch (DropboxException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.d("myLogs", " else in ASYNC");
                bitmap = BitmapFactory.decodeFile(thumbnailFileName);
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {

        /*if (isCancelled()){
            result = null;
        }*/

            if (imageViewWeakReference != null) {
                ImageView imageView = imageViewWeakReference.get();
                LoadingThumbAsync loadingThumbAsync = getBitmapDownloaderTask(imageView);
                if (this == loadingThumbAsync) {
                    imageView.setImageBitmap(result);
                }


            }
        }



    }

    private static LoadingThumbAsync getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof DownloadedDrawable) {
                DownloadedDrawable downloadedDrawable = (DownloadedDrawable)drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }

    static class DownloadedDrawable extends ColorDrawable {
        private final WeakReference<LoadingThumbAsync> bitmapDownloaderTaskReference;

        public DownloadedDrawable(LoadingThumbAsync bitmapDownloaderTask) {
            super(Color.BLACK);
            bitmapDownloaderTaskReference =
                    new WeakReference<LoadingThumbAsync>(bitmapDownloaderTask);
        }

        public LoadingThumbAsync getBitmapDownloaderTask() {
            return bitmapDownloaderTaskReference.get();
        }
    }

    private static boolean cancelPotentialDownload(String url, ImageView imageView) {
        LoadingThumbAsync bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.fileName;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);
            } else {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
    }


}
