package com.example.proverbs;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.telecom.Call;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtResponse = findViewById(R.id.textResponse);
        edtPrompt = findViewById(R.id.editTextPrompt);
        btnTranslate = findViewById(R.id.buttonTranslate);
    }




    public void generatePrompt(View view) {
        String prompt = "I need more time";

        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("model","gpt-3.5-turbo");
            jsonObject.put("prompt", prompt);
            jsonObject.put("max_tokens", 64);
            jsonObject.put("temperature", 0.7);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        RequestBody body = RequestBody.create(mediaType, jsonObject.toString());

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/completions")
                .addHeader("Authorization", "Bearer YOUR_API_KEY")
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
                        String englishTranslation = choice.getString("text");

                        // Process the English translation and provide a brief meaning
                        String meaning = getMeaning(englishTranslation);

                        // Update the UI with the translated text and its meaning
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Update UI elements with the translated text and its meaning
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    // Handle the response error
                }
            }
        });
    }

    private String getMeaning(String translation) {
        // Implement logic to retrieve the meaning of the proverb
        // You can use a dictionary or other sources to look up the meaning based on the translated proverb
        return "Brief meaning of the proverb";
    }


}