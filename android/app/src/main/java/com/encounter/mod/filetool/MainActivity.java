package com.encounter.mod.filetool;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_MANAGE_STORAGE = 1001;
    private static final int REQUEST_CODE_SAF_DATA = 1002;
    private static final int REQUEST_CODE_SAF_OBB = 1003;
    private static final String TAG = "ScopedStorageFixer";

    private RecyclerView recyclerView;
    private FileAdapter adapter;
    private List<FileItem> fileList = new ArrayList<>();
    private DocumentFile currentRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FileAdapter(fileList, this::onFileClick);
        recyclerView.setAdapter(adapter);

        checkPermissions();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                requestAllFilesAccess();
            } else {
                requestSAFData();
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            } else {
                requestSAFData();
            }
        }
    }

    private void requestAllFilesAccess() {
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_MANAGE_STORAGE);
        } catch (Exception e) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivityForResult(intent, REQUEST_CODE_MANAGE_STORAGE);
        }
    }

    private void requestSAFData() {
        // Hardcoded Initial URI for Android/data
        String dataPath = "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata";
        Uri uri = Uri.parse(dataPath);
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.putExtra("android.provider.extra.INITIAL_URI", uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_CODE_SAF_DATA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_MANAGE_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    requestSAFData();
                } else {
                    Toast.makeText(this, "All Files Access Denied", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == REQUEST_CODE_SAF_DATA && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri treeUri = data.getData();
                getContentResolver().takePersistableUriPermission(treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                
                loadFiles(treeUri);
            }
        }
    }

    private void loadFiles(Uri treeUri) {
        currentRoot = DocumentFile.fromTreeUri(this, treeUri);
        fileList.clear();
        if (currentRoot != null && currentRoot.isDirectory()) {
            for (DocumentFile file : currentRoot.listFiles()) {
                fileList.add(new FileItem(file.getName(), file.isDirectory(), file.getUri()));
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void onFileClick(FileItem item) {
        if (item.isDirectory()) {
            loadFiles(item.getUri());
        } else {
            Toast.makeText(this, "File: " + item.getName(), Toast.LENGTH_SHORT).show();
            // Implement Copy, Rename, Delete logic here
        }
    }

    // Example: Delete Operation
    public void deleteFile(Uri fileUri) {
        DocumentFile file = DocumentFile.fromSingleUri(this, fileUri);
        if (file != null && file.delete()) {
            Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show();
            // Refresh list
        }
    }

    // Example: Rename Operation
    public void renameFile(Uri fileUri, String newName) {
        DocumentFile file = DocumentFile.fromSingleUri(this, fileUri);
        if (file != null && file.renameTo(newName)) {
            Toast.makeText(this, "Renamed successfully", Toast.LENGTH_SHORT).show();
            // Refresh list
        }
    }
}
