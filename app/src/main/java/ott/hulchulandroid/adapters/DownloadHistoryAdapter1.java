package ott.hulchulandroid.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ott.hulchulandroid.DetailsActivity;
import ott.hulchulandroid.R;
import ott.hulchulandroid.models.VideoFile;
import ott.hulchulandroid.models.Work;
import ott.hulchulandroid.utils.Constants;
import ott.hulchulandroid.utils.Tools;

public class DownloadHistoryAdapter1 extends RecyclerView.Adapter<DownloadHistoryAdapter1.ViewHolder> {
    private OnDeleteDownloadFileListener listener;
    private Context context;
    private List<VideoFile> videoFiles;
    private List<Work> downloadArrayList;

    public DownloadHistoryAdapter1(Context context, List<VideoFile> videoFiles, List<Work> downloadArrayList) {
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
        /*final VideoFile videoFile = videoFiles.get(position);
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
                *//*Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoFile.getPath()));
                intent.setDataAndType(Uri.parse(videoFile.getPath()), "video/*");
                context.startActivity(intent);*//*

                Intent intent = new Intent(context, DetailsActivity.class);
                intent.putExtra(Constants.CONTENT_ID, "");
                intent.putExtra(Constants.CONTENT_TITLE, "");
                intent.putExtra(Constants.IMAGE_URL, "");
                intent.putExtra(Constants.STREAM_URL, videoFile.getPath());
                intent.putExtra(Constants.SERVER_TYPE, ".mp4");
                intent.putExtra(Constants.CATEGORY_TYPE, "");
                intent.putExtra(Constants.POSITION, "");
                intent.putExtra(Constants.IS_FROM_CONTINUE_WATCHING, true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            }
        });*/

        final Work videoFile = downloadArrayList.get(position);
        final VideoFile videoFile1 = videoFiles.get(position);
        holder.fileNameTv.setText(videoFile1.getFileName());
        holder.fileSizeTv.setText("Size: " + Tools.byteToMb(videoFile1.getTotalSpace()));
        holder.dateTv.setText(Tools.milliToDate(videoFile1.getLastModified()));
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
                Intent intent = new Intent(context, DetailsActivity.class);
                intent.putExtra(Constants.CONTENT_ID, videoFile.getDownloadId());
                intent.putExtra(Constants.CONTENT_TITLE, videoFile.getFileName());
                intent.putExtra(Constants.IMAGE_URL, videoFile.getDownloadId());
                intent.putExtra(Constants.STREAM_URL, videoFile1.getPath());
                intent.putExtra(Constants.SERVER_TYPE, ".mp4");
                intent.putExtra(Constants.CATEGORY_TYPE, "");
                intent.putExtra(Constants.POSITION, position);
                intent.putExtra(Constants.IS_FROM_CONTINUE_WATCHING, true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            }
        });
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
