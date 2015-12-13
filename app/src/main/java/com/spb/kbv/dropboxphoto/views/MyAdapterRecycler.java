package com.spb.kbv.dropboxphoto.views;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spb.kbv.dropboxphoto.R;
import com.spb.kbv.dropboxphoto.activities.BaseAuthenticatedActivity;

import java.util.ArrayList;

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
