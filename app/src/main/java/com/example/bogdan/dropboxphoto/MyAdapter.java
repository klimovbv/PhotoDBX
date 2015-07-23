package com.example.bogdan.dropboxphoto;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;

import java.util.ArrayList;

/**
 * Created by Bogdan on 21.07.2015.
 */
public class MyAdapter extends ArrayAdapter <String> {
    private final Activity activity;
    private final ArrayList<String> names;
    private final DropboxAPI<AndroidAuthSession> mDBApi;
    private LayoutInflater layoutInflater;


    public MyAdapter(Activity activity, ArrayList <String> names, DropboxAPI<AndroidAuthSession> mDBApi) {

        super(activity, R.layout.list_item, names);
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
    public View getView(int position, View convertView, ViewGroup parent) {
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
        new LoadingThumbAsync(activity, this, holder.imageView,
                names.get(position), mDBApi, holder.textView).execute();




    return rowView;
    }

}
