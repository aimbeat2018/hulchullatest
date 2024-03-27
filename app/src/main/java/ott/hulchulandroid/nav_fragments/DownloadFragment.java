//package ott.hulchulandroid.nav_fragments;
//
//import android.app.DownloadManager;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.database.Cursor;
//import android.os.Bundle;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.widget.Toolbar;
//import androidx.coordinatorlayout.widget.CoordinatorLayout;
//import androidx.fragment.app.Fragment;
//import androidx.lifecycle.Observer;
//import androidx.lifecycle.ViewModelProviders;
//import androidx.recyclerview.widget.DividerItemDecoration;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import android.os.Handler;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.security.InvalidKeyException;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import javax.crypto.Cipher;
//import javax.crypto.CipherInputStream;
//import javax.crypto.NoSuchPaddingException;
//import javax.crypto.spec.SecretKeySpec;
//
//import ott.hulchulandroid.DownloadActivity;
//import ott.hulchulandroid.MainActivity;
//import ott.hulchulandroid.R;
//import ott.hulchulandroid.adapters.DownloadHistoryAdapter;
//import ott.hulchulandroid.adapters.FileDownloadingAdapter;
//import ott.hulchulandroid.database.DatabaseHelper;
//import ott.hulchulandroid.database.downlaod.DownloadViewModel;
//import ott.hulchulandroid.models.DownloadInfo;
//import ott.hulchulandroid.models.VideoFile;
//import ott.hulchulandroid.models.Work;
//import ott.hulchulandroid.utils.Constants;
//
//public class DownloadFragment extends Fragment implements DownloadHistoryAdapter.OnDeleteDownloadFileListener {
//    public static final String ACTION_PLAY_VIDEO = "play_video";
//    public static final String TAG = "DownloadActivity";
//    private RecyclerView downloadingRv, downloadedFileRv;
//    private ProgressBar progressBar;
//    private TextView amountTv;
//    private LinearLayout progressLayout;
//
//    Toolbar toolbar;
//    CoordinatorLayout coordinatorLayout;
//
//    private TextView downloadStatusTv;
//    private TextView downloadedFileTV, downloadingFileTv;
//    private Work work;
//    private ImageView startPauseIv, cancelIV;
//    private boolean isDownloading = true;
//    private int actionPosition;
//    private FileDownloadingAdapter downloadingAdapter;
//    private DownloadHistoryAdapter downloadHistoryAdapter;
//
//    private List<VideoFile> videoFiles = new ArrayList<>();
//    private boolean isDark;
//    private List<DownloadInfo> downloadingFileList = new ArrayList<>();
//    private MainActivity activity;
//    private TextView pageTitle;
//    DatabaseHelper databaseHelper;
//    List<Work> downloadArrayList = new ArrayList<>();
//    int dl_progress;
//    DownloadViewModel viewModel;
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        activity = (MainActivity) getActivity();
//        databaseHelper = new DatabaseHelper(activity);
//        viewModel = ViewModelProviders.of(this).get(DownloadViewModel.class);
//        return inflater.inflate(R.layout.fragment_download, null);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        activity.setTitle(getResources().getString(R.string.download));
//
//        downloadedFileTV = view.findViewById(R.id.downloaded_file_tv);
//        downloadingFileTv = view.findViewById(R.id.downloading_file_tv);
//        downloadedFileRv = view.findViewById(R.id.downloaded_file_rv);
////        toolbar = view.findViewById(R.id.appBar);
//        coordinatorLayout = view.findViewById(R.id.coordinator_lyt);
//        progressLayout = view.findViewById(R.id.progress_layout);
//
//        /*downloading recycler view*/
//        downloadingRv = view.findViewById(R.id.download_rv);
//        FileDownloadingAdapter downloadingAdapter = new FileDownloadingAdapter(activity, downloadingFileList, activity);
//        downloadingRv.setLayoutManager(new LinearLayoutManager(activity, RecyclerView.VERTICAL, false));
//        downloadingRv.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.HORIZONTAL));
//        downloadingRv.hasFixedSize();
//        downloadingRv.setAdapter(downloadingAdapter);
//
//        /*downloaded recycler view*/
//        LinearLayoutManager layoutManager = new LinearLayoutManager(activity, RecyclerView.VERTICAL, false);
//        downloadedFileRv.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.HORIZONTAL));
//        downloadedFileRv.setLayoutManager(layoutManager);
//        downloadHistoryAdapter = new DownloadHistoryAdapter(activity, videoFiles, downloadArrayList);
//        downloadedFileRv.setHasFixedSize(true);
//        downloadedFileRv.setAdapter(downloadHistoryAdapter);
//        downloadHistoryAdapter.setListener(this);
//
//        DownloadViewModel viewModel = ViewModelProviders.of(this).get(DownloadViewModel.class);
//        viewModel.getAllDownloads().observe(activity, list -> {
//            if (list != null && list.size() > 0) {
//                //update downloading adapter
//                downloadingFileList.clear();
//                for (int i = 0; i < list.size(); i++) {
//                    int status = getStatus(activity, list.get(i).getDownloadId());
//                    if (status == DownloadManager.STATUS_RUNNING) {
//                        downloadingFileList.add(list.get(i));
//                    }
//                }
//
//                if (downloadingFileList.size() > 0) {
//                    coordinatorLayout.setVisibility(View.GONE);
//                    downloadingRv.setVisibility(View.VISIBLE);
//                    downloadingFileTv.setVisibility(View.VISIBLE);
//                } else {
//                    downloadingRv.setVisibility(View.GONE);
//                    downloadingFileTv.setVisibility(View.GONE);
//                    coordinatorLayout.setVisibility(View.VISIBLE);
//                }
////                    downloadingFileList.addAll(list);
//                downloadingAdapter.notifyDataSetChanged();
//            } else {
//            }
////            getDownloadFiles();
//        });
//
////        getDownloadFromDb();
//    }
//
//    public void updateProgress() {
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                for (int i = 0; i < downloadingFileList.size(); i++) {
//                    int progress = updateProgress(activity, downloadingFileList.get(i).getDownloadId(), "");
//                    DownloadInfo info = new DownloadInfo(downloadingFileList.get(i).getDownloadId(),
//                            downloadingFileList.get(i).getFileName(),progress/*,
//                            downloadingFileList.get(i).getVideoId()*/);
//
////                    viewModel.update();
//                }
//
//            }
//        }, 5000);
//    }
//
////    public int getStatus(Context context, long downloadId) {
////        DownloadManager downloadManager =
////                (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
////        DownloadManager.Query query = new DownloadManager.Query();
////        query.setFilterById(downloadId);// filter your download bu download Id
////        Cursor c = downloadManager.query(query);
////        if (c.moveToFirst()) {
////            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
////            c.close();
////            Log.i("DOWNLOAD_STATUS", String.valueOf(status));
//////            Toast.makeText(context, String.valueOf(status), Toast.LENGTH_SHORT).show();
////            return status;
////        }
////        Log.i("AUTOMATION_DOWNLOAD", "DEFAULT");
////        return -1;
////    }
//
////    public int updateProgress(Context context, long downloadId, String videoId) {
////        DownloadManager downloadManager =
////                (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
////        DownloadManager.Query query = new DownloadManager.Query();
////        query.setFilterById(downloadId);// filter your download bu download Id
////        Cursor c = downloadManager.query(query);
////        if (c.moveToFirst()) {
////            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
////
////            if (status == DownloadManager.STATUS_RUNNING) {
////                try {
////                    int bytes_downloaded = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
////                    int bytes_total = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
////                    dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);
////
////
////                } catch (Exception e) {
//////                    Log.e(TAG, "run: Download cancel of id: " + id);
////                    //delete percentage
//////                            if (viewModel != null) {
//////                                viewModel.delete(download);
//////                            }
////
////                } finally {
////                    c.close();
////                }
////
////            }
////            Log.i("DOWNLOAD_STATUS", String.valueOf(status));
////        }
////        Log.i("AUTOMATION_DOWNLOAD", "DEFAULT");
////
////        return dl_progress;
////    }
//
//    private void getDownloadFromDb() {
//        downloadArrayList = new ArrayList<>();
//        downloadArrayList = databaseHelper.getDownload();
//
//        if (downloadArrayList.size() > 0) {
//            coordinatorLayout.setVisibility(View.GONE);
//            downloadingRv.setVisibility(View.VISIBLE);
//            downloadingFileTv.setVisibility(View.VISIBLE);
////            downloadHistoryAdapter.notifyDataSetChanged();
//
//            /*downloaded recycler view*/
//            LinearLayoutManager layoutManager = new LinearLayoutManager(activity, RecyclerView.VERTICAL, false);
//            downloadedFileRv.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.HORIZONTAL));
//            downloadedFileRv.setLayoutManager(layoutManager);
//            downloadHistoryAdapter = new DownloadHistoryAdapter(activity, videoFiles, downloadArrayList);
//            downloadedFileRv.setHasFixedSize(true);
//            downloadedFileRv.setAdapter(downloadHistoryAdapter);
//            downloadHistoryAdapter.setListener(this);
//        } else {
//            downloadingRv.setVisibility(View.GONE);
//            downloadingFileTv.setVisibility(View.GONE);
//            coordinatorLayout.setVisibility(View.VISIBLE);
//        }
//
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            activity.finish();
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    public void progressHideShowControl() {
//        if (progressLayout.getVisibility() == View.VISIBLE) {
//            progressLayout.setVisibility(View.GONE);
//        } else {
//            progressLayout.setVisibility(View.VISIBLE);
//        }
//    }
//
//
//
//
//    @Override
//    public void onResume() {
//        super.onResume();
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
////        unregisterReceiver(playVideoBroadcast);
//    }
//
//
//    @Override
//    public void onDeleteDownloadFile(VideoFile videoFile) {
//        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
//        dialog.setTitle("Attention");
//        dialog.setMessage("Do you want to delete this file?");
//        dialog.setIcon(R.drawable.ic_warning);
//        dialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                deleteFile(videoFile);
//            }
//        });
//        dialog.setNegativeButton("No", null);
//        dialog.show();
//
//    }
//
//    private void deleteFile(VideoFile videoFile) {
//        File file = new File(videoFile.getPath());
//        if (file.exists()) {
//            try {
//                boolean isDeleted = file.getCanonicalFile().delete();
//                if (isDeleted) {
//                    Toast.makeText(activity, "File deleted successfully.", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(activity, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            if (file.exists()) {
//                boolean isDeleted = activity.getApplicationContext().deleteFile(file.getName());
//
//                for (int i = 0; i < downloadingFileList.size(); i++) {
//                    if (file.getName().equals(downloadingFileList.get(i).getFileName())) {
//                        DownloadViewModel viewModel = ViewModelProviders.of(this).get(DownloadViewModel.class);
//                        viewModel.delete(downloadingFileList.get(i));
//                    }
//                }
//                if (isDeleted) {
//                    Toast.makeText(activity, "File deleted successfully.", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(activity, getString(R.string.something_went_text), Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
//
////        getDownloadFiles();
//    }
//}