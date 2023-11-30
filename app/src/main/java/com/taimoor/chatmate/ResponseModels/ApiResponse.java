package com.taimoor.chatmate.ResponseModels;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ApiResponse {


    @SerializedName("id")
    private String id;

    @SerializedName("generations")
    private List<Generation> generations;

    @SerializedName("prompt")
    private String prompt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Generation> getGenerations() {
        return generations;
    }

    public void setGenerations(List<Generation> generations) {
        this.generations = generations;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
