package com.taimoor.chatmate.ResponseModels;

import com.google.gson.annotations.SerializedName;

public class Generation {


    @SerializedName("id")
    private String id;

    @SerializedName("text")
    private String text;

    @SerializedName("finish_reason")
    private String finishReason;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFinishReason() {
        return finishReason;
    }

    public void setFinishReason(String finishReason) {
        this.finishReason = finishReason;
    }
}


