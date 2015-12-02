package com.example.bogdan.dropboxphoto.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.example.bogdan.dropboxphoto.PreviewImageActivity;
import com.example.bogdan.dropboxphoto.R;
import com.example.bogdan.dropboxphoto.VideoPlayer;
import com.example.bogdan.dropboxphoto.views.MainNavDrawer;
import com.example.bogdan.dropboxphoto.views.MyAdapter;

import java.util.ArrayList;
import java.util.HashSet;

public class FileListActivity extends BaseAuthenticatedActivity {
    private static final int DELETE_ID =1;
    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCES_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    private ArrayList<String> fileUIArrayList;
    private FilesListAdapter adapter;

    private String itemForDelete;
    private String directory;
    private Handler handler;
    private String toggledFileName;

    private ActionMode actionMode;
    private HashSet<String> selectedFiles;
    private DropboxAPI<AndroidAuthSession> mDBApi;

    /*@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0,DELETE_ID,0,"Delete file");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        final int position = info.position;
        View view = info.targetView;
        if (item.getItemId() == DELETE_ID) {
            TextView v = (TextView)view.findViewById(R.id.textViewList);
            itemForDelete = v.getText().toString();
            fileUIArrayList.remove(position);
            adapter.notifyDataSetChanged();
            Toast.makeText(getApplicationContext(),
                    itemForDelete + " удален.", Toast.LENGTH_SHORT).show();

            Thread deleteThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        LoginClass.mDBApi.delete(directory + itemForDelete);

                    } catch (DropboxException e) {
                        e.printStackTrace();
                    }
                }
            });
            deleteThread.start();
            return true;
        } else return super.onContextItemSelected(item);
    }*/

    @Override
    protected void onDbxAppCreate (Bundle savedInstanceState) {
        setContentView(R.layout.activity_file_list);

        mDBApi = application.getAuth().getmDBApi();

        setNavdrawer(new MainNavDrawer(this));
        Intent intent = getIntent();
        directory = intent.getStringExtra("Type");

        getSupportActionBar().setTitle(directory);


        fileUIArrayList = new ArrayList<String>();
        adapter = new FilesListAdapter(this, fileUIArrayList, mDBApi, directory);
        ListView lv = (ListView) findViewById(R.id.activity_file_list_listView);
        lv.setAdapter(adapter);
        registerForContextMenu(lv);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = adapter.getItem(position);
                if (actionMode == null) {
                    showFile(selectedItem);
                } else {
                    toggledFileSelection(selectedItem);
                }
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                toggledFileSelection(adapter.getItem(position));
                return true;
            }
        });

        handler = new Handler(){
            public void handleMessage(Message msg){
                adapter.notifyDataSetChanged();
            }
        };

        Thread dataThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Entry entries = mDBApi.metadata(directory, 0, null, true, null);
                    for (Entry entry : entries.contents) {
                        fileUIArrayList.add(entry.fileName());
                    } handler.sendEmptyMessage(0);
                } catch (DropboxException e) {
                    e.printStackTrace();
                }
            }
        });
        dataThread.start();

        selectedFiles = new HashSet<>();

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

    private void deleteSelectedItems (Iterable <String> toggledItems){
        for (final String item : toggledItems){
            fileUIArrayList.remove(item);
            Toast.makeText(getApplicationContext(),
                    item + " удален.", Toast.LENGTH_SHORT).show();

            Thread deleteThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mDBApi.delete(directory + item);

                    } catch (DropboxException e) {
                        e.printStackTrace();
                    }
                }
            });
            deleteThread.start();

        }
        actionMode.finish();
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
            FileListActivity.this.actionMode = null;
            selectedFiles.clear();
            adapter.notifyDataSetChanged();
        }
    }

    private void showFile(String fileToShow) {
        Intent intent;
        if (directory.equals("/Photos/")) {
            intent = new Intent(getApplicationContext(), PreviewImageActivity.class);
        } else {
            intent = new Intent(getApplicationContext(), VideoPlayer.class);
        }

        intent.putExtra("filepath", fileToShow);
        startActivity(intent);
    }

    private class FilesListAdapter extends MyAdapter {

        public FilesListAdapter(Activity activity, ArrayList<String> names, DropboxAPI<AndroidAuthSession> mDBApi, String directory) {
            super(activity, names, mDBApi, directory);

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


}
