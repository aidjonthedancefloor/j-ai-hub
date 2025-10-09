package com.aidj.aihub;

import dev.langchain4j.model.chat.ChatModel;

public class LlmHello {

    public static void main(String[] args) {
        ChatModel model = LlmFactory.createChatModel();
        String answer = model.chat("What is an Automatic Activation Device?");
        System.out.println(answer);
    }
}