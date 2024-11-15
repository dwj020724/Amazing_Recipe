package com.example.mobileproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import okhttp3.*;

import com.example.mobileproject.Models.Message;
import com.google.gson.Gson;
import java.io.IOException;



public class CreateOwnRecipeActivity extends AppCompatActivity {



    private Spinner spinner;
    private boolean isSpinnerInitial = true; // Flag to track initial setup
    private EditText ingredientInput;
    private Button submitButton;
    private LinearLayout ingredientsLayout; // This is the LinearLayout with id display_ingredients
    private ArrayList<String> ingredientsList; // List to store ingredients
    private TextView generatedRecipeTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_own_recipe);

        ingredientInput = findViewById(R.id.ingredient_input);
        submitButton = findViewById(R.id.submit_button);
        ingredientsLayout = findViewById(R.id.display_ingredients);
        spinner = findViewById(R.id.spinner_tags);
        generatedRecipeTextView = findViewById(R.id.generatedRecipeTextView);

//        testAPIConnection();
        ingredientsList = new ArrayList<>();

        // Use the same custom layouts as in MainActivity
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.tags, // Ensure this array is defined in strings.xml
                R.layout.spinner_text // Custom layout for spinner items
        );
        arrayAdapter.setDropDownViewResource(R.layout.spinner_inner_text);

        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(spinnerSelectedListener);
        // Set the spinner to "create my own"
        String createMyOwnText = "create my own"; // Ensure this matches the case in your strings.xml
        int position = arrayAdapter.getPosition(createMyOwnText);
        if (position >= 0) {
            spinner.setSelection(position);
        }

        // Handle submit button click
        submitButton.setOnClickListener(view -> {
            String ingredient = ingredientInput.getText().toString().trim();

            if (!ingredient.isEmpty()) {
                // Add the ingredient to the list
                ingredientsList.add(ingredient);
                // Update the UI
                addIngredientView(ingredient);
                // Clear the input field
                ingredientInput.setText("");
            } else {
                Toast.makeText(this, "Please enter an ingredient", Toast.LENGTH_SHORT).show();
            }
        });

        Button generateRecipeButton = findViewById(R.id.generate_recipe_button);
        generateRecipeButton.setOnClickListener(v -> {
            if (!ingredientsList.isEmpty()) {
                callChatGPTAPI(); // Call the API when the button is clicked
            } else {
                Toast.makeText(this, "Please add some ingredients first", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void addIngredientView(String ingredient) {
        // Create a horizontal LinearLayout to hold the ingredient TextView and buttons
        LinearLayout ingredientLayout = new LinearLayout(this);
        ingredientLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        ingredientLayout.setOrientation(LinearLayout.HORIZONTAL);
        ingredientLayout.setPadding(0, 8, 0, 8);

        // Create TextView for the ingredient
        TextView ingredientTextView = new TextView(this);
        ingredientTextView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1)); // Weight of 1 to fill remaining space
        ingredientTextView.setText(ingredient);
        ingredientTextView.setTextSize(16);
        ingredientTextView.setTextColor(getResources().getColor(R.color.black));

        // Create Delete button
        Button deleteButton = new Button(this);
        deleteButton.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        deleteButton.setText("Delete");
        deleteButton.setTextColor(getResources().getColor(R.color.green));
        deleteButton.setBackgroundColor(getResources().getColor(R.color.white));

        // Add views to the ingredientLayout
        ingredientLayout.addView(ingredientTextView);
//        ingredientLayout.addView(editButton);
        ingredientLayout.addView(deleteButton);

        // Add the ingredientLayout to the ingredientsLayout
        ingredientsLayout.addView(ingredientLayout);


        // Handle Delete button click
        deleteButton.setOnClickListener(v -> {
            // Remove the ingredient from the list and UI
            ingredientsList.remove(ingredient);
            ingredientsLayout.removeView(ingredientLayout);
        });
    }
    private void callChatGPTAPI() {
        // Create OkHttpClient instance
        OkHttpClient client = new OkHttpClient();

        // Prepare the list of ingredients as a single string
        String ingredients = String.join(", ", ingredientsList);

        // Create the request payload
        ChatRequest chatRequest = new ChatRequest("gpt-3.5-turbo", new Message[]{new Message("user", "Create a recipe using these ingredients: " + ingredients)});
        String json = new Gson().toJson(chatRequest);

        // Create the request body
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        // Create the request object
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer APIKEY") // Replace with your actual API key
                .post(body)
                .build();

        // Make the API call
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                // Run UI updates on the main thread
                runOnUiThread(() -> Toast.makeText(CreateOwnRecipeActivity.this, "Failed to connect. Try again.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    ChatCompletionResponse completionResponse = new Gson().fromJson(responseBody, ChatCompletionResponse.class);
                    String generatedRecipe = completionResponse.getChoices()[0].getMessage().getContent();

                    runOnUiThread(() -> generatedRecipeTextView.setText(generatedRecipe));
                } else {
                    // Log the response details for troubleshooting
                    String errorBody = response.body() != null ? response.body().string() : "No response body";
                    System.out.println("Error Code: " + response.code());
                    System.out.println("Error Body: " + errorBody);

                    runOnUiThread(() -> Toast.makeText(CreateOwnRecipeActivity.this, "Failed to generate recipe. Status code: " + response.code() + ". Check logs for details.", Toast.LENGTH_LONG).show());
                }
            }
            });
    }

//private void testAPIConnection() {
//    OkHttpClient client = new OkHttpClient();
//
//    // Create a simple test message to verify connectivity
//    String testJson = new Gson().toJson(new ChatRequest("gpt-3.5-turbo", new Message[]{new Message("user", "Test connection")}));
//
//    RequestBody body = RequestBody.create(testJson, MediaType.parse("application/json"));
//    String apiKey = "Bearer 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";
//    // Replace securely in your environment
//
//    Request request = new Request.Builder()
//            .url("https://api.openai.com/v1/chat/completions")
//            .addHeader("Authorization", apiKey)
//            .post(body)
//            .build();
//
//    client.newCall(request).enqueue(new Callback() {
//        @Override
//        public void onFailure(Call call, IOException e) {
//            e.printStackTrace();
//            runOnUiThread(() -> Toast.makeText(CreateOwnRecipeActivity.this, "Failed to connect to API. Check your connection or API key.", Toast.LENGTH_SHORT).show());
//        }
//
//        @Override
//        public void onResponse(Call call, Response response) throws IOException {
//            if (response.isSuccessful()) {
//                String responseBody = response.body().string();
//                runOnUiThread(() -> Toast.makeText(CreateOwnRecipeActivity.this, "API Connection Successful!", Toast.LENGTH_SHORT).show());
//                // Optionally log or display the response for debugging
//                System.out.println("Response: " + responseBody);
//            } else {
//                runOnUiThread(() -> Toast.makeText(CreateOwnRecipeActivity.this, "API connection failed with status code: " + response.code(), Toast.LENGTH_SHORT).show());
//            }
//        }
//    });
//}



    private final AdapterView.OnItemSelectedListener spinnerSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (isSpinnerInitial) {
                isSpinnerInitial = false; // Skip the initial setup
                return;
            }

            String selectedTag = adapterView.getItemAtPosition(i).toString();


            if (selectedTag.equals("create my own")) {
                // Do nothing, stay in this activity
            } else {
                // Return to MainActivity and pass the selected tag
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selected_tag", selectedTag);
                setResult(RESULT_OK, resultIntent);
                finish(); // Close this activity and go back to MainActivity
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            // Handle cases when no item is selected if needed
        }
    };


}
