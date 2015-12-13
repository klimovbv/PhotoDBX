package com.spb.kbv.dropboxphoto.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.spb.kbv.dropboxphoto.R;
import com.spb.kbv.dropboxphoto.services.AccountService;
import com.spb.kbv.dropboxphoto.views.BaseFileListAdapter;
import com.spb.kbv.dropboxphoto.views.MainNavDrawer;

import java.util.ArrayList;
import java.util.HashSet;

public abstract class BaseFilesListActivity extends BaseAuthenticatedActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    public static final String EXTRA_FILE_NAME = "EXTRA_FILE_NAME";
    protected ArrayList<String> filesNamesList;
    protected FileListAdapter adapter;

    protected String directory;
    protected ImageButton newFileButton;

    protected ActionMode actionMode;
    protected HashSet<String> selectedFiles;
    protected String selectedItem;
    protected View progressFrame;
    protected Class showFileActivity;
    protected Class makeFileActivity;
    protected SwipeRefreshLayout swipeRefresh;

    public BaseFilesListActivity(String directory, Class showFileActivity, Class makeFileActivity) {
        this.directory = directory;
        this.showFileActivity = showFileActivity;
        this.makeFileActivity = makeFileActivity;
    }

    @Override
    public void onDbxAppCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_file_list);

        setNavdrawer(new MainNavDrawer(this));
        getSupportActionBar().setTitle(directory);

        newFileButton = (ImageButton) findViewById(R.id.activity_file_list_newPhotoButton);
        if (directory.equals("/Video/")){
            newFileButton.setImageResource(R.drawable.ic_videocam_white_24dp);
        }

        progressFrame = findViewById(R.id.activity_file_list_progressFrame);
        progressFrame.setVisibility(View.VISIBLE);
        newFileButton.setOnClickListener(this);
        selectedFiles = new HashSet<>();

        adapter = new FileListAdapter(this, directory);
        filesNamesList = adapter.getFileList();

        ListView filesListView = (ListView) findViewById(R.id.activity_file_list_listView);
        filesListView.setAdapter(adapter);

        filesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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


        filesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                toggledFileSelection(adapter.getItem(position));
                return true;
            }
        });

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.activity_file_list_swipe_refresh);
        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(this);

            swipeRefresh.setColorSchemeColors(
                    Color.parseColor("#FF00DDFF"),
                    Color.parseColor("#FF99CC00"),
                    Color.parseColor("#FFFFBB33"),
                    Color.parseColor("#FFFF4444")
            );
        }

        scheduler.postEveryMilliseconds(new AccountService.LoadFileListRequest(directory), 1000 * 30);
    }

    protected void attachLoadedFileList(ArrayList<String> loadedFileList){
        filesNamesList.clear();
        filesNamesList.addAll(loadedFileList);
        adapter.notifyDataSetChanged();
        progressFrame.setVisibility(View.GONE);
    }

    protected void deleteSelectedItems (HashSet <String> toggledItems){
        progressFrame.setVisibility(View.VISIBLE);
        bus.post(new AccountService.DeleteFileRequest(directory, toggledItems, null));
    }

    protected void deleteFiles(HashSet<String> fileNames){
        for (String fileName : fileNames){
            filesNamesList.remove(fileName);
            Toast.makeText(getApplicationContext(),
                    fileName + getString(R.string.toast_file_deleted), Toast.LENGTH_SHORT).show();
        }
        adapter.notifyDataSetChanged();
        progressFrame.setVisibility(View.GONE);
    }

    protected void toggledFileSelection(String selectedItem) {
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
        startActivity(new Intent(this, makeFileActivity));
    }

    protected class ItemsActionModeCallback implements ActionMode.Callback{

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
            BaseFilesListActivity.this.actionMode = null;
            selectedFiles.clear();
            adapter.notifyDataSetChanged();
        }
    }

    protected void showFile(String fileToShow) {
        Intent intent = new Intent(this, showFileActivity);
        intent.putExtra(EXTRA_FILE_NAME, fileToShow);
        startActivity(intent);
    }

    protected class FileListAdapter extends BaseFileListAdapter {

        public FileListAdapter(BaseAuthenticatedActivity activity, String directory) {
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

    @Override
    public void onRefresh(){
        swipeRefresh.setRefreshing(true);
        bus.post(new AccountService.LoadFileListRequest(directory));
    }
}
