package com.example.bogdan.dropboxphoto.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.example.bogdan.dropboxphoto.R;
import com.example.bogdan.dropboxphoto.services.AccountService;
import com.example.bogdan.dropboxphoto.views.MainNavDrawer;
import com.example.bogdan.dropboxphoto.views.MyAdapter;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashSet;

public class PhotoFilesListActivity extends BaseAuthenticatedActivity implements View.OnClickListener {
    private static final int REQUEST_SHOW_PHOTO = 1;
    private ArrayList<String> fileUIArrayList;
    private FilesListAdapter adapter;

    private String directory;

    private ActionMode actionMode;
    private HashSet<String> selectedFiles;
    private String selectedItem;
    private View progressFrame;


    @Override
    protected void onDbxAppCreate (Bundle savedInstanceState) {
        setContentView(R.layout.activity_file_list);

        setNavdrawer(new MainNavDrawer(this));
        directory = "/Photos/";
        getSupportActionBar().setTitle(directory);

        ImageButton newPhotoButton = (ImageButton) findViewById(R.id.activity_file_list_newPhotoButton);
        progressFrame = findViewById(R.id.activity_file_list_progressFrame);
        progressFrame.setVisibility(View.VISIBLE);

        newPhotoButton.setOnClickListener(this);

        selectedFiles = new HashSet<>();

        adapter = new FilesListAdapter(this, directory);
        fileUIArrayList = adapter.getFileList();

        ListView fileList = (ListView) findViewById(R.id.activity_file_list_listView);
        fileList.setAdapter(adapter);
        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedItem = adapter.getItem(position);
                if (actionMode == null) {
                    showFile(selectedItem);
                } else {
                    toggledFileSelection(selectedItem);
                }
            }
        });

        fileList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                toggledFileSelection(adapter.getItem(position));
                return true;
            }
        });

        bus.post(new AccountService.LoadFileListRequest(directory));
    }


    @Subscribe
    public void onLoadFileList(AccountService.LoadFileListResponse response){
        fileUIArrayList.clear();
        fileUIArrayList.addAll(response.fileList);
        adapter.notifyDataSetChanged();
        progressFrame.setVisibility(View.GONE);
    }

    private void deleteSelectedItems (HashSet <String> toggledItems){
        progressFrame.setVisibility(View.VISIBLE);
        bus.post(new AccountService.DeleteFileRequest(directory, toggledItems, null));
    }

    @Subscribe
    public void onDeleteFile(AccountService.DeleteFileResponse response){
        HashSet<String> fileNames = response.deletedFiles;
        Log.d("myLogs", "fileName in onDeleteFile = " + fileNames.size());
        for (String fileName : fileNames){
            fileUIArrayList.remove(fileName);
            Toast.makeText(getApplicationContext(),
                    fileName + " удален.", Toast.LENGTH_SHORT).show();
        }
        adapter.notifyDataSetChanged();
        progressFrame.setVisibility(View.GONE);
    }

    private void toggledFileSelection(String selectedItem) {
        if (selectedFiles.contains(selectedItem)) {
            selectedFiles.remove(selectedItem);
        } else {
            selectedFiles.add(selectedItem);
        }

        if (selectedFiles.size() == 0 && actionMode != null){
            actionMode.finish();
            return;
        }

        if (actionMode == null){
            actionMode = startSupportActionMode(new ItemsActionModeCallback());
        } else {
            actionMode.invalidate();
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(this, CameraActivity.class));
    }

    private class ItemsActionModeCallback implements ActionMode.Callback{

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            getMenuInflater().inflate(R.menu.menu_main_files, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            if (selectedFiles.size() == 1){
                menu.setGroupVisible(R.id.menu_main_files_singleOnlyGroup, true);
            } else {
                menu.setGroupVisible(R.id.menu_main_files_singleOnlyGroup, false);
            }
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.menu_main_files_delete){
                deleteSelectedItems(selectedFiles);
                actionMode.finish();
                return true;
            }

            if (itemId == R.id.menu_main_files_show){
                if (selectedFiles.size() != 1)
                    throw new RuntimeException("Show button can be shown and pressed if only one item selected");

                String fileToShow = selectedFiles.iterator().next();
                showFile(fileToShow);
                return true;
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            PhotoFilesListActivity.this.actionMode = null;
            selectedFiles.clear();
            adapter.notifyDataSetChanged();
        }
    }

    private void showFile(String fileToShow) {
        Intent intent = new Intent(this, PreviewImageActivity.class);
        intent.putExtra("filepath", fileToShow);
        startActivity(intent);
    }
    private class FilesListAdapter extends MyAdapter {

        public FilesListAdapter(BaseAuthenticatedActivity activity, String directory) {
            super(activity, directory);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            if (selectedFiles.contains(adapter.getItem(position))){
                view.setBackgroundColor(Color.parseColor("#B2EBF2"));
            } else {
                view.setBackground(null);
            }
            return view;
        }
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SHOW_PHOTO && resultCode == PreviewImageActivity.REQUEST_PHOTO_DELETE && data != null){
            fileUIArrayList.remove(data.getStringExtra(PreviewImageActivity.RESULT_EXTRA_PHOTO));
            adapter.notifyDataSetChanged();
        }
    }*/
}
