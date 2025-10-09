package com.aidj.aihub;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import io.github.cdimascio.dotenv.Dotenv;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

public class LlmHello {

    public static Dotenv dotenv = Dotenv.load();
    public static final String OPENAI_API_KEY = dotenv.get("OPENAI_API_KEY");

    public static void main(String[] args) {
        ChatModel model = OpenAiChatModel.builder()
                .apiKey(OPENAI_API_KEY)
                .modelName(GPT_4_O_MINI)
                .build();

        String answer = model.chat("What is an RSL?");

        System.out.println(answer);
    }
}