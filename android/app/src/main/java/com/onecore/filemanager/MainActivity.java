package com.onecore.filemanager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_MANAGE_STORAGE = 1001;
    private static final int REQUEST_CODE_SAF_DATA = 1002;
    private static final int REQUEST_CODE_SAF_OBB = 1003;
    private static final String TAG = "OneCoreFM";

    private RecyclerView recyclerView;
    private FileAdapter adapter;
    private List<FileItem> fileList = new ArrayList<>();
    private List<FileItem> filteredList = new ArrayList<>();
    private DocumentFile currentRoot;
    private TextView pathText;
    private EditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pathText = findViewById(R.id.pathText);
        searchInput = findViewById(R.id.searchInput);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new FileAdapter(filteredList, new FileAdapter.OnFileActionListener() {
            @Override
            public void onFileClick(FileItem item) {
                if (item.isDirectory()) {
                    loadFiles(DocumentFile.fromTreeUri(MainActivity.this, item.getUri()));
                } else {
                    showFileActions(item);
                }
            }

            @Override
            public void onFileLongClick(FileItem item) {
                showFileActions(item);
            }
        });
        recyclerView.setAdapter(adapter);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.btnData).setOnClickListener(v -> requestSAF("data"));
        findViewById(R.id.btnObb).setOnClickListener(v -> requestSAF("obb"));
        findViewById(R.id.btnTelegram).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/OneCoreStore"));
            startActivity(intent);
        });

        checkPermissions();
        checkForUpdates();
    }

    private void checkForUpdates() {
        new Thread(() -> {
            try {
                // Replace with your GitHub raw URL for version.json
                String urlStr = "https://raw.githubusercontent.com/itzraviking/OneCoreFileManager/main/version.json";
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                
                JSONObject json = new JSONObject(sb.toString());
                int latestVersionCode = json.getInt("versionCode");
                String downloadUrl = json.getString("downloadUrl");

                int currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;

                if (latestVersionCode > currentVersionCode) {
                    runOnUiThread(() -> showUpdateDialog(downloadUrl));
                }
            } catch (Exception e) {
                Log.e(TAG, "Update check failed: " + e.getMessage());
            }
        }).start();
    }

    private void showUpdateDialog(String downloadUrl) {
        new AlertDialog.Builder(this)
                .setTitle("New Update Available")
                .setMessage("A new version of OneCore File Manager is available. Would you like to download and install the latest update?")
                .setPositiveButton("Download Now", (dialog, which) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
                    startActivity(intent);
                })
                .setNegativeButton("Later", null)
                .setCancelable(false)
                .show();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                requestAllFilesAccess();
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
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

    private void requestSAF(String type) {
        String path = type.equals("data") ? "Android%2Fdata" : "Android%2Fobb";
        String uriStr = "content://com.android.externalstorage.documents/tree/primary%3A" + path;
        Uri uri = Uri.parse(uriStr);
        
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.putExtra("android.provider.extra.INITIAL_URI", uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        
        startActivityForResult(intent, type.equals("data") ? REQUEST_CODE_SAF_DATA : REQUEST_CODE_SAF_OBB);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri treeUri = data.getData();
            getContentResolver().takePersistableUriPermission(treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            
            loadFiles(DocumentFile.fromTreeUri(this, treeUri));
        }
    }

    private void loadFiles(DocumentFile root) {
        if (root == null) return;
        currentRoot = root;
        fileList.clear();
        pathText.setText(root.getName());

        DocumentFile[] files = root.listFiles();
        for (DocumentFile file : files) {
            fileList.add(new FileItem(file.getName(), file.isDirectory(), file.getUri(), file.length(), file.lastModified()));
        }
        
        filter(searchInput.getText().toString());
    }

    private void filter(String text) {
        filteredList.clear();
        for (FileItem item : fileList) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showFileActions(FileItem item) {
        String[] options = {"Rename", "Delete", "Copy Path"};
        new AlertDialog.Builder(this)
                .setTitle(item.getName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: showRenameDialog(item); break;
                        case 1: showDeleteDialog(item); break;
                        case 2: copyToClipboard(item.getUri().toString()); break;
                    }
                })
                .show();
    }

    private void showRenameDialog(FileItem item) {
        EditText input = new EditText(this);
        input.setText(item.getName());
        new AlertDialog.Builder(this)
                .setTitle("Rename")
                .setView(input)
                .setPositiveButton("OK", (dialog, which) -> {
                    DocumentFile file = DocumentFile.fromSingleUri(this, item.getUri());
                    if (file != null && file.renameTo(input.getText().toString())) {
                        loadFiles(currentRoot);
                        Toast.makeText(this, "Renamed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteDialog(FileItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete " + item.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    DocumentFile file = DocumentFile.fromSingleUri(this, item.getUri());
                    if (file != null && file.delete()) {
                        loadFiles(currentRoot);
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void copyToClipboard(String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Path", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Path copied to clipboard", Toast.LENGTH_SHORT).show();
    }
}
