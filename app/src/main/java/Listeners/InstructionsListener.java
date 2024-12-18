package Listeners;

import com.example.mobileproject.Models.InstructionsResponse;
import java.util.List;

public interface InstructionsListener {
    void didFetch(List<InstructionsResponse> responses, String message);
    void didError(String message);
}