package com.taimoor.chatmate;

import com.taimoor.chatmate.ResponseModels.ApiResponse;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface RetrofitApi {

    @Headers({
            "accept: application/json",
            "content-type: application/json",
            "authorization: Bearer 76oMXcX9QQ8UoIFbtQX9fXASrf6HKLAnJLJEgzcv"
    })
    @POST
    Call<ApiResponse> generate(@Url String url, @Body RequestBody body);

}
