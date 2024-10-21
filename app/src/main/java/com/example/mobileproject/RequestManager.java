package com.example.mobileproject;
import android.content.Context;

import com.example.mobileproject.Models.InstructionsResponse;
import com.example.mobileproject.Models.RandomRecipetApiResponse;
import com.example.mobileproject.Models.RecipeDetailsResponse;
import com.example.mobileproject.Models.SimilarRecipeResponse;

import java.util.List;

import Listeners.InstructionsListener;
import Listeners.RandomRecipeResponseListener;
import Listeners.RecipeDetailsListener;
import Listeners.SimilarRecipesListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;


public class RequestManager {
    Context context;
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://api.spoonacular.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public RequestManager(Context context) {
        this.context = context;
    }

    public void getRandomRecipes(RandomRecipeResponseListener listener, List<String> tags){
        CallRandomRecipes callRandomRecipes = retrofit.create(CallRandomRecipes.class);
        Call<RandomRecipetApiResponse> call = callRandomRecipes.callRandomRecipe(context.getString(R.string.api_key), "10", tags);
        call.enqueue(new Callback<RandomRecipetApiResponse>() {
            @Override
            public void onResponse(Call<RandomRecipetApiResponse> call, Response<RandomRecipetApiResponse> response) {
                if (!response.isSuccessful()){
                    listener.didError(response.message());
                    return;
                }
                listener.didFetch(response.body(), response.message());
            }

            @Override
            public void onFailure(Call<RandomRecipetApiResponse> call, Throwable throwable) {
                listener.didError(throwable.getMessage());
            }
        });
    }
    //pass listener
    public void getRecipeDetails(RecipeDetailsListener listener, int id){
        CallRecipeDetails callRecipeDetails = retrofit.create(CallRecipeDetails.class);
        Call<RecipeDetailsResponse> call = callRecipeDetails.callRecipeDetails(id, context.getString(R.string.api_key));
        call.enqueue(new Callback<RecipeDetailsResponse>() {
            @Override
            public void onResponse(Call<RecipeDetailsResponse> call, Response<RecipeDetailsResponse> response) {
                 if(!response.isSuccessful()){
                     listener.didError(response.message());
                     return;
                 }
                 listener.didFetch(response.body(), response.message());
            }

            @Override
            public void onFailure(Call<RecipeDetailsResponse> call, Throwable throwable) {
                listener.didError(throwable.getMessage());
            }
        });
    }

    public void getSimilarRecipes(SimilarRecipesListener listener, int id){
        CallSimilarRecipes callSimilarRecipes = retrofit.create(CallSimilarRecipes.class);
        Call<List<SimilarRecipeResponse>> call = callSimilarRecipes.callSimilarRecipe(id, "4", context.getString(R.string.api_key));
        call.enqueue(new Callback<List<SimilarRecipeResponse>>() {
            @Override
            public void onResponse(Call<List<SimilarRecipeResponse>> call, Response<List<SimilarRecipeResponse>> response) {
                if (!response.isSuccessful()) {
                    listener.didError(response.message());
                    return;
                }
                listener.didFetch(response.body(), response.message());
            }

            @Override
            public void onFailure(Call<List<SimilarRecipeResponse>> call, Throwable t) {
                listener.didError(t.getMessage());
            }
        });
    }

    public void getInstructions(InstructionsListener listener, int id) {
        CallInstructions callInstructions = retrofit.create(CallInstructions.class);
        Call<List<InstructionsResponse>> call = callInstructions.callInstructions(id, context.getString(R.string.api_key));
        call.enqueue(new Callback<List<InstructionsResponse>>() {
            @Override
            public void onResponse(Call<List<InstructionsResponse>> call, Response<List<InstructionsResponse>> response) {
                if (!response.isSuccessful()) {
                    listener.didError(response.message());
                    return;
                }
                listener.didFetch(response.body(), response.message());
            }

            @Override
            public void onFailure(Call<List<InstructionsResponse>> call, Throwable t) {
                listener.didError(t.getMessage());
            }
        });
    }

    private interface CallRandomRecipes{
        @GET("recipes/random")
        Call<RandomRecipetApiResponse> callRandomRecipe(
                    @Query("apiKey") String apiKey,
                    @Query("number") String number,
                    @Query("tags")List<String> tags

            );
    }
    private interface CallRecipeDetails{
        @GET("recipes/{id}/information")
        Call<RecipeDetailsResponse> callRecipeDetails(
                @Path("id") int id,
                @Query("apiKey") String apiKey
        );
    }

    private interface CallSimilarRecipes{
        @GET("recipes/{id}/similar")
        Call<List<SimilarRecipeResponse>> callSimilarRecipe(
                @Path("id") int id,
                @Query("number") String number,
                @Query("apiKey") String apiKey
        );
    }

    private interface CallInstructions{
        @GET("recipes/{id}/analyzedInstructions")
        Call<List<InstructionsResponse>> callInstructions(
                @Path("id") int id,
                @Query("apiKey") String apiKey
        );
    }
}
