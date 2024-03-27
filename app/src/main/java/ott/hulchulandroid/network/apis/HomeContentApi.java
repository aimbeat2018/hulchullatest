package ott.hulchulandroid.network.apis;

import ott.hulchulandroid.models.home_content.HomeContent;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface HomeContentApi {

    @GET("home_content_for_android")
    Call<HomeContent> getHomeContent(@Header("API-KEY") String apiKey,@Query("user_id") String userId);

    @GET("home_safe_content_for_android")
    Call<HomeContent> getFamilyContent(@Header("API-KEY") String apiKey,@Query("user_id") String userId);
}
