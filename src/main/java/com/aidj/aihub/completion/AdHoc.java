package com.aidj.aihub.completion;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aidj.aihub.LlmFactory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;

public class AdHoc {
    public static String adHocComplete(List<String> systemPrompts, List<String> userPrompts) {
        ChatModel model = LlmFactory.createChatModel();
        List<ChatMessage> chatMessages = Stream.concat(
            systemPrompts.stream().map(p -> new SystemMessage(p)),
            userPrompts.stream().map(p -> new UserMessage(p))
        ).collect(Collectors.toList());

        ChatResponse response = model.chat(chatMessages);
        return response.aiMessage().text();
    }
}
