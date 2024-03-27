package ott.hulchulandroid;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.viewpagerindicator.LinePageIndicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ott.hulchulandroid.adapters.CommonGoldAdapter;
import ott.hulchulandroid.adapters.CommonGridAdapter;
import ott.hulchulandroid.adapters.ContinueWatchingAdapter;
import ott.hulchulandroid.adapters.CountryAdapter;
import ott.hulchulandroid.adapters.GenreAdapter;
import ott.hulchulandroid.adapters.GenreHomeAdapter;
import ott.hulchulandroid.adapters.HomePageAdapter;
import ott.hulchulandroid.adapters.LiveTvHomeAdapter;
import ott.hulchulandroid.adapters.PopularStarAdapter;
import ott.hulchulandroid.adapters.SliderListAdapter;
import ott.hulchulandroid.database.DatabaseHelper;
import ott.hulchulandroid.database.continueWatching.ContinueWatchingModel;
import ott.hulchulandroid.database.continueWatching.ContinueWatchingViewModel;
import ott.hulchulandroid.database.homeContent.HomeContentViewModel;
import ott.hulchulandroid.models.CommonModels;
import ott.hulchulandroid.models.GenreModel;
import ott.hulchulandroid.models.home_content.AllCountry;
import ott.hulchulandroid.models.home_content.FeaturedTvChannel;
import ott.hulchulandroid.models.home_content.FeaturesGenreAndMovie;
import ott.hulchulandroid.models.home_content.GoldVideo;
import ott.hulchulandroid.models.home_content.HomeContent;
import ott.hulchulandroid.models.home_content.LatestTvseries;
import ott.hulchulandroid.models.home_content.PopularMovie;
import ott.hulchulandroid.models.home_content.PopularStars;
import ott.hulchulandroid.models.home_content.Slide;
import ott.hulchulandroid.models.home_content.Slider;
import ott.hulchulandroid.models.home_content.Video;
import ott.hulchulandroid.network.RetrofitClient;
import ott.hulchulandroid.network.apis.HomeContentApi;
import ott.hulchulandroid.network.apis.TvSeriesApi;
import ott.hulchulandroid.network.model.config.AdsConfig;
import ott.hulchulandroid.utils.ApiResources;
import ott.hulchulandroid.utils.Constants;
import ott.hulchulandroid.utils.NetworkInst;
import ott.hulchulandroid.utils.PreferenceUtils;
import ott.hulchulandroid.utils.SpacingItemDecoration;
import ott.hulchulandroid.utils.Tools;
import ott.hulchulandroid.utils.ads.BannerAds;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;


public class CpGoldFragment extends Fragment {

    private ShimmerFrameLayout shimmerFrameLayout;
    private RecyclerView recyclerView;
    private CommonGoldAdapter mAdapter;
    private List<CommonModels> list = new ArrayList<>();

    private ApiResources apiResources;

    private boolean isLoading = false;
    private ProgressBar progressBar;
    private int pageCount = 1;
    private SwipeRefreshLayout swipeRefreshLayout;

    private CoordinatorLayout coordinatorLayout;
    private TextView tvNoItem;
    private RelativeLayout adView;

    private LinearLayout searchRootLayout;

    private CardView searchBar;
    private ImageView menuIv, searchIv;
    private TextView pageTitle;

    private MainActivity activity;

    private static final int HIDE_THRESHOLD = 20;
    private int scrolledDistance = 0;
    private boolean controlsVisible = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        activity = (MainActivity) getActivity();

        return inflater.inflate(R.layout.fragment_cp_gold, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //getActivity().setTitle(getResources().getString(R.string.tv_series));

        initComponent(view);

        pageTitle.setText(getResources().getString(R.string.tv_series));

        if (activity.isDark) {
            pageTitle.setTextColor(activity.getResources().getColor(R.color.white));
            searchBar.setCardBackgroundColor(activity.getResources().getColor(R.color.black_window_light));
            menuIv.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_menu));
            searchIv.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_search_white));
        }


    }

    private void initComponent(View view) {

        apiResources = new ApiResources();

        adView = view.findViewById(R.id.adView);
        progressBar = view.findViewById(R.id.item_progress_bar);
        shimmerFrameLayout = view.findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout.startShimmer();
        swipeRefreshLayout = view.findViewById(R.id.swipe_layout);
        coordinatorLayout = view.findViewById(R.id.coordinator_lyt);
        tvNoItem = view.findViewById(R.id.tv_noitem);

        searchRootLayout = view.findViewById(R.id.search_root_layout);
        searchBar = view.findViewById(R.id.search_bar);
        menuIv = view.findViewById(R.id.bt_menu);
        pageTitle = view.findViewById(R.id.page_title_tv);
        searchIv = view.findViewById(R.id.search_iv);


        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
//        recyclerView.addItemDecoration(new SpacingItemDecoration(3, Tools.dpToPx(getActivity(), 0), true));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        mAdapter = new CommonGoldAdapter(getContext(), list);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1) && !isLoading) {

                    coordinatorLayout.setVisibility(View.GONE);

                    pageCount = pageCount + 1;
                    isLoading = true;

                    progressBar.setVisibility(View.VISIBLE);

                    getTvSeriesData(pageCount);
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
                    animateSearchBar(true);
                    controlsVisible = false;
                    scrolledDistance = 0;
                } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
                    animateSearchBar(false);
                    controlsVisible = true;
                    scrolledDistance = 0;
                }

                if ((controlsVisible && dy > 0) || (!controlsVisible && dy < 0)) {
                    scrolledDistance += dy;
                }


            }
        });

        if (new NetworkInst(getContext()).isNetworkAvailable()) {
            getTvSeriesData(pageCount);
        } else {
            tvNoItem.setText(getResources().getString(R.string.no_internet));
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            coordinatorLayout.setVisibility(View.VISIBLE);
        }


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                pageCount = 1;
                coordinatorLayout.setVisibility(View.GONE);
                list.clear();
                recyclerView.removeAllViews();
                mAdapter.notifyDataSetChanged();
                if (new NetworkInst(getContext()).isNetworkAvailable()) {
                    getTvSeriesData(pageCount);
                } else {
                    tvNoItem.setText(getResources().getString(R.string.no_internet));
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    coordinatorLayout.setVisibility(View.VISIBLE);
                }
            }
        });

//        loadAd();

    }


    @Override
    public void onStart() {
        super.onStart();

        menuIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                activity.openDrawer();
            }
        });
        searchIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.goToSearchActivity();
            }
        });

    }

    private void loadAd() {
        AdsConfig adsConfig = new DatabaseHelper(getContext()).getConfigurationData().getAdsConfig();
        if (adsConfig.getAdsEnable().equals("1")) {

            if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.ADMOB)) {
                BannerAds.ShowAdmobBannerAds(activity, adView);

            } else if (adsConfig.getMobileAdsNetwork().equals(Constants.START_APP)) {
                BannerAds.showStartAppBanner(activity, adView);

            } else if (adsConfig.getMobileAdsNetwork().equals(Constants.NETWORK_AUDIENCE)) {
                BannerAds.showFANBanner(getActivity(), adView);
            }
        }
    }

    private void getTvSeriesData(int pageNum) {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        TvSeriesApi api = retrofit.create(TvSeriesApi.class);
        Call<List<GoldVideo>> call = api.gold_tvseries(AppConfig.API_KEY, pageNum, PreferenceUtils.getUserId(activity));
        call.enqueue(new Callback<List<GoldVideo>>() {
            @Override
            public void onResponse(Call<List<GoldVideo>> call, retrofit2.Response<List<GoldVideo>> response) {
                if (response.code() == 200) {
                    isLoading = false;
                    progressBar.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    if (response.body().size() == 0 && pageCount == 1) {
                        coordinatorLayout.setVisibility(View.VISIBLE);
                    } else {
                        coordinatorLayout.setVisibility(View.GONE);
                    }

                    for (int i = 0; i < response.body().size(); i++) {
                        GoldVideo video = response.body().get(i);
                        CommonModels models = new CommonModels();
                        models.setImageUrl(video.getThumbnailUrl());
                        models.setTitle(video.getTitle());
                        models.setVideoType("tvseries");
                        models.setReleaseDate(video.getRelease());
                        models.setQuality(video.getVideoQuality());
                        models.setId(video.getVideosId());
                        models.setVideo_flag(video.getVideoFlag());
                        models.setVideo_plan(video.getVideoPlan());
                        models.setIs_gold_paid(video.getIsGoldPaid());
                        list.add(models);
                    }
                    mAdapter.notifyDataSetChanged();

                } else {
                    isLoading = false;
                    progressBar.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    if (pageCount == 1) {
                        coordinatorLayout.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<GoldVideo>> call, Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                if (pageCount == 1) {
                    coordinatorLayout.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    boolean isSearchBarHide = false;

    private void animateSearchBar(final boolean hide) {
        if (isSearchBarHide && hide || !isSearchBarHide && !hide) return;
        isSearchBarHide = hide;
        int moveY = hide ? -(2 * searchRootLayout.getHeight()) : 0;
        searchRootLayout.animate().translationY(moveY).setStartDelay(100).setDuration(300).start();
    }


}
