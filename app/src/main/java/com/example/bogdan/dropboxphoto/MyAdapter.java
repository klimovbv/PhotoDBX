package com.example.bogdan.dropboxphoto;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Environment;
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

import java.io.File;
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


    public MyAdapter(Activity activity, ArrayList <String> names, DropboxAPI<AndroidAuthSession> mDBApi) {

        super();
        this.activity = activity;
        this.names = names;
        this.mDBApi = mDBApi;
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
        holder.imageView.setTag(position);
        LoadingThumbAsync as = new LoadingThumbAsync(activity, this, holder.imageView,
                names.get(position), mDBApi, holder.textView);

        as.execute();

        /*File thumbnailFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                names.get(position));
        String thumbnailFileName = thumbnailFile.getAbsolutePath();
        holder.imageView.setImageBitmap(BitmapFactory.decodeFile(thumbnailFileName));*/

    return rowView;
    }
}
