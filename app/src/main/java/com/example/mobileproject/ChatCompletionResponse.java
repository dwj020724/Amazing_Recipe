package com.example.mobileproject;

import com.example.mobileproject.Models.Message;

public class ChatCompletionResponse {
    private Choice[] choices;

    public Choice[] getChoices() {
        return choices;
    }

    public static class Choice {
        private Message message;

        public Message getMessage() {
            return message;
        }
    }
}
