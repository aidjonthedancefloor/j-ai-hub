package com.aidj.aihub;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import io.github.cdimascio.dotenv.Dotenv;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

public class LlmFactory {

    public static Dotenv dotenv = Dotenv.load();
    public static final String OPENAI_API_KEY = dotenv.get("OPENAI_API_KEY");

    public static ChatModel createChatModel() {
        return OpenAiChatModel.builder()
                .apiKey(OPENAI_API_KEY)
                .modelName(GPT_4_O_MINI)
                .build();
    }

    public static StreamingChatModel createStreamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(OPENAI_API_KEY)
                .modelName(GPT_4_O_MINI)
                .build();
    }
}
