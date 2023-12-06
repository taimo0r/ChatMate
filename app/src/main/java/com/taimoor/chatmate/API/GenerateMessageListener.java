package com.taimoor.chatmate.API;

import com.taimoor.chatmate.ResponseModels.ApiResponse;

public interface GenerateMessageListener {

    void onFetch(ApiResponse response, String message);

    void onError(String message);

}
