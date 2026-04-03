package com.encounter.mod.filetool;

import android.net.Uri;

public class FileItem {
    private String name;
    private boolean isDirectory;
    private Uri uri;

    public FileItem(String name, boolean isDirectory, Uri uri) {
        this.name = name;
        this.isDirectory = isDirectory;
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public Uri getUri() {
        return uri;
    }
}
