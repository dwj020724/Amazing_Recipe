package Listeners;

import com.example.mobileproject.Models.InstructionsResponse;
import java.util.List;

public interface InstructionsListener {
    void didFetch(InstructionsResponse responses, String message);
    void didError(String message);
}