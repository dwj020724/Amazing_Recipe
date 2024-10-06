package Listeners;

import com.example.mobileproject.Models.RandomRecipetApiResponse;

public interface RandomRecipeResponseListener {
    void didFetch(RandomRecipetApiResponse response, String message);
    void didError(String message);
}
