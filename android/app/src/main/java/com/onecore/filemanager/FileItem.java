package com.onecore.filemanager;

import android.net.Uri;

public class FileItem {
    private String name;
    private boolean isDirectory;
    private Uri uri;
    private long size;
    private long lastModified;

    public FileItem(String name, boolean isDirectory, Uri uri, long size, long lastModified) {
        this.name = name;
        this.isDirectory = isDirectory;
        this.uri = uri;
        this.size = size;
        this.lastModified = lastModified;
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

    public long getSize() {
        return size;
    }

    public long getLastModified() {
        return lastModified;
    }
}
