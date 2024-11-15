package com.example.mobileproject;

import com.example.mobileproject.Models.Message;

public class ChatRequest {
    private String model;
    private Message[] messages;

    public ChatRequest(String model, Message[] messages) {
        this.model = model;
        this.messages = messages;
    }
}
