package com.taimoor.chatmate;


import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.taimoor.chatmate.ResponseModels.ApiResponse;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    RecyclerView chatsRecycler;
    EditText userMsgEdt;
    FloatingActionButton sendMsgFAB;
    ImageButton cameraBtn;

    private static final int CAMERA_REQUEST = 2;
    private final String BOT_KEY = "bot";
    private ArrayList<ChatsModel> chatsModelArrayList;
    private ChatRecyclerAdapter chatRecyclerAdapter;
    private RetrofitApi apiService;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatsRecycler = findViewById(R.id.idRvChats);
        userMsgEdt = findViewById(R.id.idEdtMsg);
        sendMsgFAB = findViewById(R.id.idFABSend);
        cameraBtn = findViewById(R.id.cameraBtn);

        apiService = RetrofitClient.getRetrofitInstance().create(RetrofitApi.class);

        chatsModelArrayList = new ArrayList<>();
        chatRecyclerAdapter = new ChatRecyclerAdapter(this, chatsModelArrayList);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        chatsRecycler.setLayoutManager(manager);
        chatsRecycler.setAdapter(chatRecyclerAdapter);

        sendMsgFAB.setOnClickListener(view -> {
            if (userMsgEdt.getText().toString().isEmpty()) {

                Toast.makeText(MainActivity.this, "Please Enter a message", Toast.LENGTH_SHORT).show();
                return;

            }
            getResponse(userMsgEdt.getText().toString());
            userMsgEdt.setText("");
        });


        cameraBtn.setOnClickListener(view -> callCameraPermissionAndValidation());
    }

    private void getResponse(String message) {

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"truncate\":\"END\",\"return_likelihoods\":\"NONE\",\"prompt\":\" " + message + " \"}");


        String USER_KEY = "user";
        chatsModelArrayList.add(new ChatsModel(message, USER_KEY));
        chatRecyclerAdapter.notifyDataSetChanged();

        Call<ApiResponse> call = apiService.generate("generate", body);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {

                    ApiResponse model = response.body();
                    chatsModelArrayList.add(new ChatsModel(model.getGenerations().get(0).getText(), BOT_KEY));
                    chatsRecycler.scrollToPosition(chatsModelArrayList.size() - 1);
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


    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAMERA_REQUEST);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void callCameraPermissionAndValidation() {
        if (checkCallingOrSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            takePicture();
        } else {
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                Toast.makeText(MainActivity.this, "Camera permission is needed", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap capturedBitmap = (Bitmap) extras.get("data");

            // Process the captured image (e.g., display or send it to OCR)
            processTextRecognition(capturedBitmap);
        }
    }


    private void processTextRecognition(Bitmap bitmap) {
        TextRecognizer textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        textRecognizer.process(image)
                .addOnSuccessListener(text -> {
                    // Handle the recognized text
                    String recognizedText = text.getText();

                    // Display the recognized text to the user
                    showRecognizedTextDialog(recognizedText);
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                    Log.e("TextRecognition", "Error recognizing text", e);
                });
    }

    private void showRecognizedTextDialog(String recognizedText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recognized Text");

        // Create a TextView to display the recognized text
        TextView textView = new TextView(this);
        textView.setPadding(10, 0, 0, 0);
        textView.setText(recognizedText);
        textView.setTextIsSelectable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        builder.setView(textView);

        builder.setPositiveButton("Copy All to Clipboard", (dialog, which) -> {
            // Copy the entire recognized text to the clipboard
            copyToClipboard(recognizedText);
        });

        builder.setNegativeButton("Dismiss", (dialog, which) -> {
            // Dismiss the dialog
            dialog.dismiss();
        });

        builder.show();
    }


    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Recognized Text", text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CAMERA_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePicture();
            } else {
                String messagePermissions = "Permission was not granted";
                Toast.makeText(MainActivity.this, messagePermissions, Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}