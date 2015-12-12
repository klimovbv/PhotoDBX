package com.example.bogdan.dropboxphoto.views;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
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
import java.util.List;

public class MyAdapterRecycler extends RecyclerView.Adapter<FilesViewHolder> implements View.OnClickListener, View.OnLongClickListener {
    private ArrayList<String> fileList;
    private LayoutInflater inflater;
    private BaseAuthenticatedActivity activity;
    private String directory;
    private final OnFileClickListener listener;

    public MyAdapterRecycler(BaseAuthenticatedActivity activity,
                             String directory, OnFileClickListener listener) {
        this.directory  = directory;
        this.activity = activity;
        this.listener = listener;
        fileList = new ArrayList<>();
        inflater = activity.getLayoutInflater();
    }

    public ArrayList<String> getFileList() {
        return fileList;
    }

    @Override
    public FilesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item, parent, false);
        view.setOnClickListener(this);

        return new FilesViewHolder(activity, view, directory);
    }

    @Override
    public void onBindViewHolder(FilesViewHolder holder, int position) {
        String fileName = fileList.get(position);
        holder.populate(fileName);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    @Override
    public void onClick(View view) {
        listener.onFileClicked((String) view.getTag());
    }

    @Override
    public boolean onLongClick(View view) {
        listener.onLongFileClicked((String)view.getTag());
        return true;
    }


    public interface OnFileClickListener {
        void onFileClicked(String fileName);
        void onLongFileClicked(String fileName);
    }


}
