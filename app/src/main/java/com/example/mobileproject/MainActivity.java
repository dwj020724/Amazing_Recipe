package com.example.mobileproject;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.Models.RandomRecipetApiResponse;

import java.util.ArrayList;
import java.util.List;

import Adapters.RandomRecipeAdapter;
import Listeners.RandomRecipeResponseListener;

public class MainActivity extends AppCompatActivity {
    ProgressDialog dialog;
    RequestManager manager;
    RandomRecipeAdapter randomRecipeAdapter;
    RecyclerView recyclerView;
    Spinner spinner;
    List<String> tags = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        dialog = new ProgressDialog(this);
//        dialog.setTitle("Loading... ");

        spinner = findViewById(R.id.spinner_tags);

// Use a default layout for spinner items
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.tags, // Your string-array in strings.xml
                R.layout.spinner_text // Default layout
        );
        arrayAdapter.setDropDownViewResource(R.layout.spinner_inner_text);

        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(spinnerSelectedListener);




        manager = new RequestManager(this);
//        manager.getRandomRecipes(randomRecipeResponseListener);
//        Log.d("SpinnerTest", "First item: " + firstItem);
//        dialog.show();
    }

    private final RandomRecipeResponseListener randomRecipeResponseListener = new RandomRecipeResponseListener() {
        @Override
        public void didFetch(RandomRecipetApiResponse response, String message) {
            recyclerView = findViewById(R.id.recycler_random);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 1));
            randomRecipeAdapter = new RandomRecipeAdapter(MainActivity.this, response.recipes);
            recyclerView.setAdapter(randomRecipeAdapter);
//            dialog.dismiss();
        }

        @Override
        public void didError(String message) {
//            dialog.dismiss();
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT);
        }
    };

    private final AdapterView.OnItemSelectedListener spinnerSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            tags.clear();
            tags.add(adapterView.getSelectedItem().toString());
            manager.getRandomRecipes(randomRecipeResponseListener, tags);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            // Handle cases when no item is selected if needed
        }
        };
}