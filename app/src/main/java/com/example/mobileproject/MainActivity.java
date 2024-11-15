package com.example.mobileproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.Models.RandomRecipetApiResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import Adapters.RandomRecipeAdapter;
import Listeners.RandomRecipeResponseListener;
import Listeners.RecipeClickListener;

public class MainActivity extends AppCompatActivity {

    ProgressDialog dialog;
    RequestManager manager;
    RandomRecipeAdapter randomRecipeAdapter;
    RecyclerView recyclerView;
    Spinner spinner;
    List<String> tags = new ArrayList<>();
    SearchView searchView;

    FirebaseAuth auth;
    Button button;
    TextView textView;
    FirebaseUser user;

    private static final int REQUEST_CREATE_OWN = 1;
    private ArrayAdapter<CharSequence> arrayAdapter;
    private boolean isSpinnerProgrammatic = false; // Flag to control spinner selection

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.logout);
        textView = findViewById(R.id.user_details);
        user = auth.getCurrentUser();
        if (user == null){
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }
        else{
            textView.setText(user.getEmail());

        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        dialog = new ProgressDialog(this);
        dialog.setTitle("Loading... ");
        searchView = findViewById(R.id.searchView_home);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                tags.clear();
                tags.add(query);
                manager.getRandomRecipes(randomRecipeResponseListener, tags);
                dialog.show();
                // 隐藏键盘
                View view = MainActivity.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
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
            randomRecipeAdapter = new RandomRecipeAdapter(MainActivity.this, response.recipes,recipeClickListener);
            recyclerView.setAdapter(randomRecipeAdapter);
            dialog.dismiss();
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
//            tags.clear();
//            tags.add(adapterView.getSelectedItem().toString());
//            manager.getRandomRecipes(randomRecipeResponseListener, tags);
            String selectedTag = adapterView.getSelectedItem().toString();

            if (selectedTag.equals("create my own")) {
                // Launch CreateOwnRecipeActivity for custom ingredient entry
                Intent intent = new Intent(MainActivity.this, CreateOwnRecipeActivity.class);
                startActivityForResult(intent, REQUEST_CREATE_OWN);
            } else {
                // Clear tags and update with selected item, then fetch random recipes
                tags.clear();
                tags.add(selectedTag);
                manager.getRandomRecipes(randomRecipeResponseListener, tags);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            // Handle cases when no item is selected if needed
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CREATE_OWN && resultCode == RESULT_OK && data != null) {
            String selectedTag = data.getStringExtra("selected_tag");
            if (selectedTag != null) {
                tags.clear();
                tags.add(selectedTag);

                // Check if arrayAdapter is null
                if (arrayAdapter == null) {
                    arrayAdapter = ArrayAdapter.createFromResource(
                            this,
                            R.array.tags,
                            R.layout.spinner_text
                    );
                    arrayAdapter.setDropDownViewResource(R.layout.spinner_inner_text);
                    spinner.setAdapter(arrayAdapter);
                }

                // Set the spinner selection programmatically
                isSpinnerProgrammatic = true; // Set flag to true before changing selection
                int position = arrayAdapter.getPosition(selectedTag);
                Log.d("MainActivity", "Selected tag: " + selectedTag);
                Log.d("MainActivity", "Spinner position: " + position);

                if (position >= 0) {
                    spinner.setSelection(position);
                } else {
                    // Handle invalid position
                    Toast.makeText(this, "Selected tag not found in spinner options.", Toast.LENGTH_SHORT).show();
                    // Optionally set a default selection
                    spinner.setSelection(0);
                }
                isSpinnerProgrammatic = false; // Reset flag after changing selection

                manager.getRandomRecipes(randomRecipeResponseListener, tags);
                dialog.show();
            }
        }
    }
    private final RecipeClickListener recipeClickListener = new RecipeClickListener() {
        @Override
        //show the recipe id
        public void onRecipeClicked(String id) {
            startActivity(new Intent(MainActivity.this, RecipeDetailsActivity.class)
                    .putExtra("id",id));
        }
    };
}