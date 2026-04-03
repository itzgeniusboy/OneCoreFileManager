package com.onecore.filemanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    private List<FileItem> fileList;
    private OnFileActionListener listener;

    public interface OnFileActionListener {
        void onFileClick(FileItem item);
        void onFileLongClick(FileItem item);
    }

    public FileAdapter(List<FileItem> fileList, OnFileActionListener listener) {
        this.fileList = fileList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileItem item = fileList.get(position);
        holder.fileName.setText(item.getName());
        
        if (item.isDirectory()) {
            holder.fileIcon.setImageResource(R.drawable.ic_folder);
            holder.fileInfo.setText("Folder");
        } else {
            holder.fileIcon.setImageResource(R.drawable.ic_file);
            holder.fileInfo.setText(formatSize(item.getSize()) + " | " + formatDate(item.getLastModified()));
        }

        holder.itemView.setOnClickListener(v -> listener.onFileClick(item));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onFileLongClick(item);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    private String formatSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new java.text.DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    private String formatDate(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(time));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView fileIcon;
        TextView fileName;
        TextView fileInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileIcon = itemView.findViewById(R.id.fileIcon);
            fileName = itemView.findViewById(R.id.fileName);
            fileInfo = itemView.findViewById(R.id.fileInfo);
        }
    }
}
