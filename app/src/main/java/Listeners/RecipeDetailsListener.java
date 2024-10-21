package Listeners;
import com.example.mobileproject.Models.RecipeDetailsResponse;
public interface RecipeDetailsListener {
    void didFetch(RecipeDetailsResponse response, String message);
    void didError(String message);
}
