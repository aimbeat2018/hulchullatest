package ott.hulchulandroid.adapters;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ott.hulchulandroid.DetailsActivity;
//import ott.hulchulandroid.OfflinePlayerActivity;
import ott.hulchulandroid.R;
import ott.hulchulandroid.models.VideoFile;
import ott.hulchulandroid.models.Work;
import ott.hulchulandroid.utils.Constants;
import ott.hulchulandroid.utils.Tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class DownloadHistoryAdapter extends RecyclerView.Adapter<DownloadHistoryAdapter.ViewHolder> {
    private OnDeleteDownloadFileListener listener;
    private Context context;
    private List<VideoFile> videoFiles;
    private List<Work> downloadArrayList;

    public DownloadHistoryAdapter(Context context, List<VideoFile> videoFiles, List<Work> downloadArrayList) {
        this.context = context;
        this.videoFiles = videoFiles;
        this.downloadArrayList = downloadArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.layout_download_history, parent,
                false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final VideoFile videoFile = videoFiles.get(position);
        holder.fileNameTv.setText(videoFile.getFileName());
        holder.fileSizeTv.setText("Size: " + Tools.byteToMb(videoFile.getTotalSpace()));
        holder.dateTv.setText(Tools.milliToDate(videoFile.getLastModified()));
        holder.item_holder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.onDeleteDownloadFile(videoFile);
                }
                return false;
            }
        });

        holder.item_holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoFile.getPath()));
                intent.setDataAndType(Uri.parse(videoFile.getPath()), "video/*");
                context.startActivity(intent);*/

//                Intent intent = new Intent(context, OfflinePlayerActivity.class);
//                intent.putExtra(Constants.CONTENT_ID, "");
//                intent.putExtra(Constants.CONTENT_TITLE, "");
//                intent.putExtra(Constants.IMAGE_URL, "");
//                intent.putExtra(Constants.STREAM_URL, videoFile.getPath());
//                intent.putExtra(Constants.SERVER_TYPE, ".mp4");
//                intent.putExtra(Constants.CATEGORY_TYPE, "");
//                intent.putExtra(Constants.POSITION, "");
//                intent.putExtra(Constants.IS_FROM_CONTINUE_WATCHING, true);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                context.startActivity(intent);
            }
        });

        /*final Work videoFile = downloadArrayList.get(position);
        holder.fileNameTv.setText(videoFile.getFileName());
        holder.fileSizeTv.setText("Size: " + Tools.byteToMb(Long.parseLong(videoFile.getTotalSize())));
//        holder.dateTv.setText(Tools.milliToDate(videoFile.getLastModified()));
        holder.item_holder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null){
//                    listener.onDeleteDownloadFile(videoFile);
                }
                return false;
            }
        });

        holder.item_holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                *//*Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoFile.getPath()));
                intent.setDataAndType(Uri.parse(videoFile.getPath()), "video/*");
                context.startActivity(intent);*//*
            }
        });*/
    }
    @Override
    public int getItemCount() {
        return videoFiles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView fileNameTv, fileSizeTv, dateTv;
        RelativeLayout item_holder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            fileNameTv = itemView.findViewById(R.id.file_name_tv);
            fileSizeTv = itemView.findViewById(R.id.file_size_tv);
            dateTv = itemView.findViewById(R.id.date_tv);
            item_holder = itemView.findViewById(R.id.item_view);

        }
    }

    public interface OnDeleteDownloadFileListener {
        void onDeleteDownloadFile(VideoFile videoFile);
    }

    public void setListener(OnDeleteDownloadFileListener listener) {
        this.listener = listener;
    }
}
