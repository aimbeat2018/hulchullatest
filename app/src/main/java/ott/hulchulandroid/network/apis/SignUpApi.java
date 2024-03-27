package ott.hulchulandroid.network.apis;


import ott.hulchulandroid.network.model.User;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;




public interface SignUpApi {
    @FormUrlEncoded
    @POST("signup")
    Call<User> signUp(@Header("API-KEY") String apiKey,
                      @Field("email") String email,
                      @Field("password") String password,
                      @Field("name") String name,
                      @Field("mobile_no") String mobile,
                      @Field("country_code") String countryCode,
                      @Field("device_no") String device_no,
                      @Field("age") String age,
                      @Field("device_token") String device_token,
                      @Field("user_age") String user_age,
                      @Field("pin") String pin);


}


