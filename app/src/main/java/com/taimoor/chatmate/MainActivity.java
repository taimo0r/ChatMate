package com.taimoor.chatmate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.taimoor.chatmate.ResponseModels.ApiResponse;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    RecyclerView chatsRecycler;
    EditText userMsgEdt;
    FloatingActionButton sendMsgFAB;
    private final String BOT_KEY = "bot";
    private final String USER_KEY = "user";
    private ArrayList<ChatsModel> chatsModelArrayList;
    private ChatRecyclerAdapter chatRecyclerAdapter;
    private RetrofitApi apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatsRecycler = findViewById(R.id.idRvChats);
        userMsgEdt = findViewById(R.id.idEdtMsg);
        sendMsgFAB = findViewById(R.id.idFABSend);

        apiService = RetrofitClient.getRetrofitInstance().create(RetrofitApi.class);

        chatsModelArrayList = new ArrayList<>();
        chatRecyclerAdapter = new ChatRecyclerAdapter(this, chatsModelArrayList);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        chatsRecycler.setLayoutManager(manager);
        chatsRecycler.setAdapter(chatRecyclerAdapter);

        sendMsgFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userMsgEdt.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please Enter a message", Toast.LENGTH_SHORT).show();
                    return;
                }
                getResponse(userMsgEdt.getText().toString());
                userMsgEdt.setText("");
            }
        });
    }

    private void getResponse(String message) {

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"truncate\":\"END\",\"return_likelihoods\":\"NONE\",\"prompt\":\" " + message + " \"}");


        chatsModelArrayList.add(new ChatsModel(message, USER_KEY));
        chatRecyclerAdapter.notifyDataSetChanged();
//

        Call<ApiResponse> call = apiService.generate("generate", body);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {

                    ApiResponse model = response.body();
                    chatsModelArrayList.add(new ChatsModel(model.getGenerations().get(0).getText(), BOT_KEY));
                    chatsRecycler.scrollToPosition(chatsModelArrayList.size()-1);
                    chatRecyclerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                chatsModelArrayList.add(new ChatsModel("Please revert your question", BOT_KEY));
                chatRecyclerAdapter.notifyDataSetChanged();
            }
        });
    }

}