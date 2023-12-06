package com.taimoor.chatmate.API;

import android.content.Context;
import android.widget.Toast;

import com.taimoor.chatmate.ResponseModels.ApiResponse;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

public class RetrofitClient {

    private static final String BASE_URL = "https://api.cohere.ai/v1/";

    OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // Example: 30 seconds
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    Context context;

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build();

    public RetrofitClient(Context context) {
        this.context = context;
    }


    public interface generateApiService {

        @Headers({
                "accept: application/json",
                "content-type: application/json",
                "authorization: Bearer 76oMXcX9QQ8UoIFbtQX9fXASrf6HKLAnJLJEgzcv"
        })
        @POST
        Call<ApiResponse> generate(@Url String url, @Body RequestBody body);

    }


    public void getGenerateAIResponse(GenerateMessageListener listener, RequestBody body) {

        generateApiService apiService = retrofit.create(generateApiService.class);
        Call<ApiResponse> call = apiService.generate("generate", body);

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show();
                    return;
                }
                listener.onFetch(response.body(), response.message());
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                listener.onError(t.getMessage());
            }
        });

    }


}
