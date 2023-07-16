package com.example.proverbs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.telecom.Call;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    TextView txtResponse;
    EditText edtPrompt;
    Button btnTranslate;

    private ProgressBar progressBar;
    private ConstraintLayout rootLayout;


    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtResponse = findViewById(R.id.textResponse);
        edtPrompt = findViewById(R.id.editTextPrompt);
        btnTranslate = findViewById(R.id.buttonTranslate);
        progressBar = findViewById(R.id.progressBar);
        rootLayout = findViewById(R.id.rootLayout);


    }

    public void generatePrompt(View view) {

        btnTranslate.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        String sentence = edtPrompt.getText().toString();
        if (sentence.length() <= 0 ) {
            sentence = "I am blank";
        }

        String prompt = "From now on you are a native chinese speaker attempting to relay what I say into some proverb.  I only want the english translation in the response with a brief meaning after. I want the response to be formatted as such: \"English translation\" - \" Chinese proverb\" - (Chinese pronunciation) \n\ngit s Meaning: Explanation of proverb. To start I want you to turn this into a proverb: [" + sentence + "]";

        MediaType mediaType = MediaType.parse("application/json");

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("model", "gpt-3.5-turbo");
            JSONArray messagesArray = new JSONArray();
            JSONObject messageObject = new JSONObject();
            messageObject.put("role", "system");
            messageObject.put("content", prompt);
            messagesArray.put(messageObject);
            JSONObject userInputObject = new JSONObject();
            userInputObject.put("role", "user");
            userInputObject.put("content", sentence);
            messagesArray.put(userInputObject);
            jsonObject.put("messages", messagesArray);
            jsonObject.put("max_tokens", 128);
            jsonObject.put("temperature", 0.7);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        RequestBody body = RequestBody.create(mediaType, jsonObject.toString());

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + Keys.OPEN_AI)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseData);
                        JSONArray choices = json.getJSONArray("choices");
                        JSONObject choice = choices.getJSONObject(0);
                        String content = choice.getJSONObject("message").getString("content");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtResponse.setText(content);
                                progressBar.setVisibility(View.GONE);
                                btnTranslate.setEnabled(true);

                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    final String errorMessage = "Error: " + response.message();
                    System.out.println(response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txtResponse.setText(errorMessage);
                            progressBar.setVisibility(View.GONE);
                            btnTranslate.setEnabled(true);
                        }
                    });
                }
            }
        });
    }
}