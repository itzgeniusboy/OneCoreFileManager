package com.encounter.mod.filetool;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    private List<FileItem> fileList;
    private OnFileClickListener listener;

    public interface OnFileClickListener {
        void onFileClick(FileItem item);
    }

    public FileAdapter(List<FileItem> fileList, OnFileClickListener listener) {
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
        holder.fileIcon.setImageResource(item.isDirectory() ? R.drawable.ic_folder : R.drawable.ic_file);
        holder.itemView.setOnClickListener(v -> listener.onFileClick(item));
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView fileIcon;
        TextView fileName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileIcon = itemView.findViewById(R.id.fileIcon);
            fileName = itemView.findViewById(R.id.fileName);
        }
    }
}
