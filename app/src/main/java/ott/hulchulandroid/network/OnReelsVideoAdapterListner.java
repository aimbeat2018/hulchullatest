package ott.hulchulandroid.network;

import android.view.MotionEvent;

import ott.hulchulandroid.databinding.ItemReelsBinding;
import ott.hulchulandroid.models.ReelsModel;

public interface OnReelsVideoAdapterListner {

    void onItemClick(ItemReelsBinding reelsBinding, int pos, int type);

    void onOpenClick(ItemReelsBinding reelsBinding, int pos);

    void onShareClick(ItemReelsBinding reelsBinding, int pos);

    void onDoubleClick(ReelsModel model, MotionEvent event, ItemReelsBinding binding);

    void onClickLike(ItemReelsBinding reelsBinding, String likeStatus, int pos);

}
